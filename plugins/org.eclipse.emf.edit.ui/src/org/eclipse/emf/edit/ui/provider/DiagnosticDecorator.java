/**
 * Copyright (c) 2012 Eclipse contributors and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.eclipse.emf.edit.ui.provider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.ui.ImageURIRegistry;
import org.eclipse.emf.common.ui.MarkerHelper;
import org.eclipse.emf.common.ui.viewer.ColumnViewerInformationControlToolTipSupport;
import org.eclipse.emf.common.ui.viewer.IStyledLabelDecorator;
import org.eclipse.emf.common.ui.viewer.IUndecoratingLabelProvider;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.emf.edit.provider.ComposedImage;
import org.eclipse.emf.edit.ui.EMFEditUIPlugin;
import org.eclipse.emf.edit.ui.action.ValidateAction;
import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;
import org.eclipse.emf.edit.ui.view.ExtendedPropertySheetPage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A {@link ILabelDecorator label decorator} for associating {@link Diagnostic diagnostic} decorations with a {@link StructuredViewer structured viewer}'s content labels.
 *
 * @since 2.9
 */
public class DiagnosticDecorator extends CellLabelProvider implements ILabelDecorator
{
  /**
   * Converts special characters to entities except those in {@link #enquote(String) enquoted ranges}.
   */
  public static String escapeContent(String content)
  {
    StringBuilder result = new StringBuilder();
    boolean escape = true;
    for (int i = 0, length = content.length(); i < length; ++i)
    {
      char character = content.charAt(i);
      if (escape)
      {
        switch (character)
        {
          case '\002':
          {
            escape = false;
            break;
          }
          case ' ':
          {
            result.append("&#160;");
            break;
          }
          case '&':
          {
            result.append("&amp;");
            break;
          }
          case '<':
          {
            result.append("&lt;");
            break;
          }
          default:
          {
            result.append(character);
            break;
          }
        }
      }
      else if (character == '\003')
      {
        escape = true;
      }
      else
      {
        result.append(character);
      }
    }
    return result.toString();
  }

  /**
   * Marks the string such that the content will not be subsequently {@link #escapeContent(String) escaped}.
   */
  public static String enquote(String content)
  {
    return '\002' + content + '\003';
  }

  private static final Pattern ENQUOTED_SEGMENT_PATTERN = Pattern.compile("\002<img src='[^']*'/> (?:<i>)?([^\003]*)(?:</i>)?\003");

  /**
   * Cleans up the escaping and HTML tags inserted by the {@link LiveValidator live validator}.
   * It can be safely called on text not produced by the live validator because it only transformed {@link #enquote(String) enquoted} content.
   */
  public static String strip(String content)
  {
    Matcher matcher = ENQUOTED_SEGMENT_PATTERN.matcher(content);
    if (matcher.find())
    {
      StringBuilder result = new StringBuilder();
      int start = 0;
      do
      {
        result.append(content.substring(start, matcher.start()));
        String label = matcher.group(1);
        result.append(label.replace("&lt;", "<").replace("&amp;", "&").replace("&#160;", " "));
        start = matcher.end();
      }
      while (matcher.find());
      result.append(content.substring(start));
      return result.toString();
    }
    else
    {
      return content;
    }
  }

  /**
   * A content adapter for monitoring a {@link ResourceSet resource set}'s resources.
   */
  public static abstract class DiagnosticAdapter extends EContentAdapter
  {
    /**
     * Inform all diagnostic adapters associated with the notifier of new diagnostic information.
     */
    public static void update(Notifier notifier, Diagnostic diagnostic)
    {
      for (Adapter adapter : notifier.eAdapters())
      {
        if (adapter instanceof DiagnosticAdapter)
        {
          ((DiagnosticAdapter)adapter).updateDiagnostic(diagnostic);
        }
      }
    }

    protected abstract void updateDiagnostic(Diagnostic diagnostic);

    /**
     * This will typically compute a diagnostic for the resource and call {@link #updateDiagnostic(Diagnostic)}.
     */
    protected abstract void handleResourceDiagnostics(List<Resource> resources);

    @Override
    public void notifyChanged(Notification notification)
    {
      Object notifier = notification.getNotifier();
      if (notifier instanceof Resource)
      {
        if (!notification.isTouch())
        {
          switch (notification.getFeatureID(Resource.class))
          {
            case Resource.RESOURCE__IS_LOADED:
            {
              Resource resource = (Resource)notifier;
              handleResourceDiagnostics(Collections.singletonList(resource));
              break;
            }
            case Resource.RESOURCE__ERRORS:
            case Resource.RESOURCE__WARNINGS:
            {
              // Avoid processing the resource diagnostics during loading and unloading.
              // I.e., during unloading, isLoaded will become false, though that notification is sent later during the processing,
              // then the contents, errors, and warnings are cleared, in that order, each producing a visible notification immediately.
              // Similarly, during loading, isLoaded will become true, though that notification will be sent later during the processing,
              // then contents, errors, and warnings can be populated in any order, each producing a visible notification immediately.
              // So ignoring these notifications when the resource isn't loaded or is in the process of loading,
              // ensures that we won't try to resolve marker proxies into the resource until it's fully unloaded,
              // i.e., when the final isLoaded notification is dispatched, 
              // at which time the resource diagnostics will be handled by the switch case above.
              Resource.Internal resource = (Resource.Internal)notifier;
              if (resource.isLoaded() && !resource.isLoading())
              {
                handleResourceDiagnostics(Collections.<Resource>singletonList(resource));
              }
              break;
            }
          }
        }
      }
      else
      {
        // Be careful because we're adding the adapter on a background thread.
        //
        synchronized (notifier)
        {
          super.notifyChanged(notification);
        }
      }
    }

    @Override
    protected void setTarget(ResourceSet target)
    {
      super.setTarget(target);

      handleResourceDiagnostics(new ArrayList<Resource>(target.getResources()));
    }

    @Override
    protected void setTarget(Resource target)
    {
      basicSetTarget(target);
    }

    @Override
    protected void unsetTarget(Resource target)
    {
      basicUnsetTarget(target);
    }
  }

  /**
   * A listener that interprets links as navigable references to objects in the resource set.
   */
  public static class EditingDomainLocationListener extends ColumnViewerInformationControlToolTipSupport.PathLocationListener
  {
    protected EditingDomain editingDomain;

    public EditingDomainLocationListener(EditingDomain editingDomain, StructuredViewer viewer)
    {
      super(viewer);
      this.editingDomain = editingDomain;
    }

    @Override
    public void changing(LocationEvent event)
    {
      EObject eObject = null;
      try
      {
        URI uri = URI.createURI(event.location);
        if (uri.hasFragment())
        {
          eObject = editingDomain.getResourceSet().getEObject(uri, false);
        }
      }
      catch (Throwable throwable)
      {
        // Ignore when we can't find the object.
      }
      if (eObject != null)
      {
        event.doit = false;
        if (editingDomain instanceof AdapterFactoryEditingDomain)
        {
          setSelection(((AdapterFactoryEditingDomain)editingDomain).getWrapper(eObject));
        }
        else
        {
          setSelection(eObject);
        }
      }
      else
      {
        super.changing(event);
      }
    }
  }

  public static class LiveValidator
  {
    public static class LiveValidationAction extends Action
    {
      public static final String LIVE_VALIDATOR_DIALOG_SETTINGS_KEY = "liveValidator";

      protected EditingDomain domain;
      protected IDialogSettings dialogSettings;

      public LiveValidationAction(EditingDomain domain, IDialogSettings dialogSettings)
      {
        this(dialogSettings);
        this.domain = domain;
        update();
      }

      public LiveValidationAction(IDialogSettings dialogSettings)
      {
        super(EMFEditUIPlugin.INSTANCE.getString("_UI_LiveValidation_menu_item"));
        setDescription(EMFEditUIPlugin.INSTANCE.getString("_UI_LiveValidation_simple_description"));
        this.dialogSettings = dialogSettings;
      }

      public EditingDomain getEditingDomain()
      {
        return domain;
      }

      public void setEditingDomain(EditingDomain domain)
      {
        this.domain = domain;
      }

      @Override
      public void run()
      {
        boolean checked = isChecked();
        if (dialogSettings != null)
        {
          dialogSettings.put(LIVE_VALIDATOR_DIALOG_SETTINGS_KEY, checked);
        }
        update();
        LiveValidator liveValidator = LIVE_VALIDATORS.get(domain);
        if (liveValidator != null)
        {
          ResourceSet resourceSet = domain.getResourceSet();
          EList<Resource> resources = resourceSet.getResources();
          if (checked)
          {
            liveValidator.scheduledResources.addAll(resources);
            liveValidator.scheduleValidation();
          }
          else
          {
            for (Adapter adapter : resourceSet.eAdapters())
            {
              if (adapter instanceof DiagnosticDecoratorAdapter)
              {
                DiagnosticDecoratorAdapter diagnosticDecoratorAdapter = (DiagnosticDecoratorAdapter)adapter;
                diagnosticDecoratorAdapter.refreshResourceDiagnostics(resources);
              }
            }
          }
        }
      }

      public void update()
      {
        setEnabled(domain != null);
        if (dialogSettings != null)
        {
          setChecked(dialogSettings.getBoolean(LIVE_VALIDATOR_DIALOG_SETTINGS_KEY));
        }
      }

      public void setActiveWorkbenchPart(IWorkbenchPart workbenchPart)
      {
        setEditingDomain(workbenchPart instanceof IEditingDomainProvider ? ((IEditingDomainProvider)workbenchPart).getEditingDomain() : null);
      }
    }

    private final List<DiagnosticDecorator> diagnosticDecorators = new ArrayList<DiagnosticDecorator>();

    protected EditingDomain editingDomain;
    protected IDialogSettings dialogSettings;
    protected AdapterFactory adapterFactory;
    protected ILabelProvider labelProvider;
    protected Job validationJob;
    protected List<Resource> scheduledResources = Collections.synchronizedList(new UniqueEList<Resource>());

    public LiveValidator(final EditingDomain editingDomain, IDialogSettings dialogSettings)
    {
      this.editingDomain = editingDomain;
      this.dialogSettings = dialogSettings;
      adapterFactory = editingDomain instanceof AdapterFactoryEditingDomain ? ((AdapterFactoryEditingDomain)editingDomain).getAdapterFactory() : null;
      labelProvider = adapterFactory == null ? null : new AdapterFactoryLabelProvider(adapterFactory);

      editingDomain.getCommandStack().addCommandStackListener
        (new CommandStackListener()
         {
           public void commandStackChanged(EventObject event)
           {
             Command mostRecentCommand = ((CommandStack)event.getSource()).getMostRecentCommand();
             if (!(mostRecentCommand instanceof AbstractCommand.NonDirtying))
             {
               scheduledResources.addAll(editingDomain.getResourceSet().getResources());
               scheduleValidation();
             }
           }
         });
    }

    public void scheduleValidation(Resource resource)
    {
      scheduledResources.add(resource);
      scheduleValidation();
    }

    public void scheduleValidation()
    {
      if (validationJob == null && (dialogSettings == null || dialogSettings.getBoolean(LiveValidationAction.LIVE_VALIDATOR_DIALOG_SETTINGS_KEY)))
      {
        validationJob =
          new Job("Validation Job")
          {
            @Override
            protected IStatus run(final IProgressMonitor monitor)
            {
              try
              {
                Diagnostician diagnostician =
                  new Diagnostician()
                  {
                    @Override
                    public String getObjectLabel(EObject eObject)
                    {
                      String text = labelProvider != null && eObject.eIsProxy() ? ((InternalEObject)eObject).eProxyURI().toString() : labelProvider.getText(eObject);
                      if (text == null || text.length() == 0)
                      {
                        text = "<i>null</i>";
                      }
                      else
                      {
                        text = escapeContent(text);
                      }
                      Image image = labelProvider != null ? labelProvider.getImage(eObject) : null;
                      if (image != null)
                      {
                        URI imageURI = ImageURIRegistry.INSTANCE.getImageURI(image);
                        return enquote("<img src='" + imageURI + "'/> " + text);
                      }
                      else
                      {
                        return text;
                      }
                    }

                    @Override
                    public boolean validate(EClass eClass, EObject eObject, DiagnosticChain diagnostics, Map<Object, Object> context)
                    {
                      if (monitor.isCanceled())
                      {
                        throw new RuntimeException();
                      }

                      monitor.worked(1);
                      return super.validate(eClass, eObject, diagnostics, context);
                    }
                  };

                final ResourceSet resourceSet = editingDomain.getResourceSet();

                List<Resource> resources = new UniqueEList<Resource>(Arrays.asList(scheduledResources.toArray(new Resource[0])));
                scheduledResources.removeAll(resources);

                // Count all the objects we need to validate and resolve proxies.
                //
                int count = 0;
                for (Resource resource : resources)
                {
                  synchronized (resource)
                  {
                    synchronized (resourceSet)
                    {
                      for (Iterator<EObject> i = resource.getAllContents(); i.hasNext(); )
                      {
                        ++count;
                        EObject eObject = i.next();
                        for (@SuppressWarnings("unused") EObject eCrossReference : eObject.eCrossReferences())
                        {
                          if (monitor.isCanceled())
                          {
                            throw new RuntimeException();
                          }
                        }
                      }
                    }
                  }
                }

                // Also count all the objects we need to validate from resources loaded because of proxy resolution.
                //
                for (List<Resource> moreResources = Arrays.asList(scheduledResources.toArray(new Resource[0]));
                     !moreResources.isEmpty();
                     moreResources = Arrays.asList(scheduledResources.toArray(new Resource[0])))
                {
                  resources.addAll(moreResources);
                  scheduledResources.removeAll(moreResources);
                  for (Resource resource : moreResources)
                  {
                    synchronized (resource)
                    {
                      synchronized (resourceSet)
                      {
                        for (Iterator<EObject> i = resource.getAllContents(); i.hasNext(); )
                        {
                          ++count;
                          EObject eObject = i.next();
                          for (@SuppressWarnings("unused") EObject eCrossReference : eObject.eCrossReferences())
                          {
                            if (monitor.isCanceled())
                            {
                              throw new RuntimeException();
                            }
                          }
                        }
                      }
                    }
                  }
                }

                monitor.beginTask("", count);
                final BasicDiagnostic diagnostic =
                  new BasicDiagnostic
                    (EObjectValidator.DIAGNOSTIC_SOURCE,
                     0,
                     EMFEditUIPlugin.INSTANCE.getString("_UI_DiagnosisOfNObjects_message", new String[] { "" + resources.size() }),
                     new Object [] { resourceSet } );
                Map<Object, Object> context = diagnostician.createDefaultContext();
                for (int i = 0; i < resources.size(); ++i)
                {
                  Resource resource = resources.get(i);
                  monitor.setTaskName(EMFEditUIPlugin.INSTANCE.getString("_UI_Validating_message", new Object [] { resource.getURI() }));

                  final BasicDiagnostic resourceDiagnostic =
                    new BasicDiagnostic
                      (EObjectValidator.DIAGNOSTIC_SOURCE,
                       0,
                       EMFEditUIPlugin.INSTANCE.getString("_UI_DiagnosisOfNObjects_message", new String[] { "1" }),
                       new Object [] { resource } );

                  for (EObject eObject : resource.getContents())
                  {
                    diagnostician.validate(eObject, resourceDiagnostic, context);
                    context.remove(EObjectValidator.ROOT_OBJECT);
                  }

                  for (Diagnostic instrinsicDiagnostic : new EditUIMarkerHelper().getIntrinsicDiagnostics(resource, false))
                  {
                    resourceDiagnostic.add(instrinsicDiagnostic);
                  }

                  diagnostic.add(resourceDiagnostic);

                  if (monitor.isCanceled())
                  {
                    throw new RuntimeException();
                  }
                  monitor.worked(1);
                }

                DiagnosticAdapter.update(resourceSet, diagnostic);

                Display.getDefault().asyncExec
                  (new Runnable()
                   {
                     public void run()
                     {
                       validationJob = null;
                       if (!monitor.isCanceled() && !scheduledResources.isEmpty())
                       {
                         LiveValidator.this.scheduleValidation();
                       }
                     }
                   });

                return Status.OK_STATUS;
              }
              catch (RuntimeException exception)
              {
                validationJob = null;
                return Status.CANCEL_STATUS;
              }
            }
          };
        validationJob.setPriority(Job.DECORATE);
        validationJob.schedule(500);
      }
    }

    /**
     * Cancels the validation job, if there is one.
     * It blocks until the job is terminated.
     *
     * @since 2.12
     */
    public void cancelValidation()
    {
      final Job validationJob = this.validationJob;
      if (validationJob != null)
      {
        validationJob.cancel();

        Display display = Display.getCurrent();
        BusyIndicator.showWhile(display, new Runnable()
          {
            public void run()
            {
              while (validationJob.getState() == Job.RUNNING)
              {
                try
                {
                  Thread.sleep(10);
                }
                catch (InterruptedException e)
                {
                  // Ignore.
                }
              }
            }
          });

        // If the job was only scheduled and not yet running, it will not get a chance to clear its field.
        // So best we do it here to be sure it's done and a new job can be created and scheduled.
        this.validationJob = null;
      }
    }

    public void register(DiagnosticDecorator diagnosticDecorator)
    {
      diagnosticDecorators.add(diagnosticDecorator);
    }

    public void deregister(DiagnosticDecorator diagnosticDecorator)
    {
      if (diagnosticDecorators.remove(diagnosticDecorator) && diagnosticDecorators.isEmpty())
      {
        dispose();
      }
    }

    public void dispose()
    {
      LIVE_VALIDATORS.remove(editingDomain);
      if (validationJob != null)
      {
        validationJob.cancel();
      }
    }
  }

  /**
   * An extended {@link DiagnosticDecorator} that handle style strings decoration.
   * @since 2.10
   */
  public static class Styled extends DiagnosticDecorator implements IStyledLabelDecorator
  {
    /**
     * Creates an instance that supports {@link LiveValidator live validation} and supports
     * {@link DiagnosticDecorator.LiveValidator.LiveValidationAction#LIVE_VALIDATOR_DIALOG_SETTINGS_KEY enablement}
     * via {@link IDialogSettings dialog setting}.
     */
    public Styled(EditingDomain editingDomain, ExtendedPropertySheetPage propertySheetPage, IDialogSettings dialogSettings)
    {
      super(editingDomain, propertySheetPage, dialogSettings);
    }

    /**
     * Creates an instance that supports {@link LiveValidator live validation}.
     */
    public Styled(EditingDomain editingDomain, ExtendedPropertySheetPage propertySheetPage)
    {
      super(editingDomain, propertySheetPage);
    }

    /**
     * Creates an instance that supports {@link LiveValidator live validation} and supports
     * {@link DiagnosticDecorator.LiveValidator.LiveValidationAction#LIVE_VALIDATOR_DIALOG_SETTINGS_KEY enablement}
     * via {@link IDialogSettings dialog setting}.
     */
    public Styled(EditingDomain editingDomain, StructuredViewer viewer, IDialogSettings dialogSettings)
    {
      super(editingDomain, viewer, dialogSettings);
    }

    /**
     * Creates an instance that supports {@link LiveValidator live validation}.
     */
    public Styled(EditingDomain editingDomain, StructuredViewer viewer)
    {
      super(editingDomain, viewer);
    }

    /**
     * Creates an instance that doesn't support {@link LiveValidator live validation}.
     * Only decorations explicitly produced from {@link ValidateAction} or those
     * {@link EditUIMarkerHelper#getMarkerDiagnostics(Object, org.eclipse.core.resources.IFile, boolean)
     * derived from markers} are displayed.
     */
    public Styled(ResourceSet resourceSet, ExtendedPropertySheetPage propertySheetPage)
    {
      super(resourceSet, propertySheetPage);
    }

    /**
     * Creates an instance that doesn't support {@link LiveValidator live validation}.
     * Only decorations explicitly produced from {@link ValidateAction} or those
     * {@link EditUIMarkerHelper#getMarkerDiagnostics(Object, org.eclipse.core.resources.IFile, boolean)
     * derived from markers} are displayed.
     */
    public Styled(ResourceSet resourceSet, StructuredViewer viewer)
    {
      super(resourceSet, viewer);
    }

    public StyledString decorateStyledText(StyledString styledString, Object object)
    {
      return styledString;
    }
  }

  /**
   * A styled diagnostic decorator that will decorate the given styled string by underlying it
     * with a {@link SWT#UNDERLINE_ERROR} underline style colored in {@link JFacePreferences#ERROR_COLOR}.
   * @author mbarbero
   *
   */
  public static class StyledError extends DiagnosticDecorator.Styled implements IStyledLabelDecorator
  {

    public StyledError(EditingDomain editingDomain,
        ExtendedPropertySheetPage propertySheetPage,
        IDialogSettings dialogSettings)
    {
      super(editingDomain, propertySheetPage, dialogSettings);
    }

    public StyledError(EditingDomain editingDomain,
        ExtendedPropertySheetPage propertySheetPage)
    {
      super(editingDomain, propertySheetPage);
    }

    public StyledError(EditingDomain editingDomain, StructuredViewer viewer,
        IDialogSettings dialogSettings)
    {
      super(editingDomain, viewer, dialogSettings);
    }

    public StyledError(EditingDomain editingDomain, StructuredViewer viewer)
    {
      super(editingDomain, viewer);
    }

    public StyledError(ResourceSet resourceSet,
        ExtendedPropertySheetPage propertySheetPage)
    {
      super(resourceSet, propertySheetPage);
    }

    public StyledError(ResourceSet resourceSet, StructuredViewer viewer)
    {
      super(resourceSet, viewer);
    }

    /**
     * Decorate the given {@code styledString} by underlying the whole given {@code styledString}
     * with a {@link SWT#UNDERLINE_ERROR} underline style colored in {@link JFacePreferences#ERROR_COLOR}.
     */
    @Override
    public StyledString decorateStyledText(StyledString styledString, Object object)
    {
      if (styledString == null || object == null)
      {
        throw new NullPointerException();
      }
      Diagnostic diagnostic = getDecorations().get(object);
      if (diagnostic != null && diagnostic.getSeverity() >= Diagnostic.WARNING)
      {
        StyledString result = new StyledString();
        StyleRange[] styleRanges = styledString.getStyleRanges();
        String string = styledString.getString();
        if (styleRanges.length == 0)
        {
          result.append(string, ErrorStyler.INSTANCE);
        }
        else
        {
          int start = 0;
          for (StyleRange range : styleRanges)
          {
            if (start < range.start)
            {
              result.append(string.substring(start, (range.start - start)), ErrorStyler.INSTANCE);
            }
            start = range.start + range.length;
            result.append(string.substring(range.start, start), new ComposedStyler(range, ErrorStyler.INSTANCE));
          }
          if (start < styledString.length())
          {
            result.append(string.substring(start, string.length()), ErrorStyler.INSTANCE);
          }
        }

        return result;
      }
      return styledString;
    }

    /**
     * A styler underlying the given text with a {@link SWT#UNDERLINE_ERROR} underline style colored
     * in {@link JFacePreferences#ERROR_COLOR}.
     * @since 2.10
     */
    public static final class ErrorStyler extends Styler
    {
      public static final Styler INSTANCE = new ErrorStyler();

      @Override
      public void applyStyles(TextStyle textStyle)
      {
        textStyle.underline = true;
        textStyle.underlineStyle = SWT.UNDERLINE_ERROR;
        textStyle.underlineColor = JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR);
      }
    }

    /**
     * An extended styler applying a wrapped base style first, and successively apply other
     * stylers to the given TextStyle, overriding any previous style.
     *
     * @since 2.10
     */
    public static class ComposedStyler extends Styler
    {
      protected TextStyle baseStyle;
      protected Styler[] stylers;

      public ComposedStyler(TextStyle baseStyle, Styler... stylers)
      {
        this.baseStyle = baseStyle;
        this.stylers = stylers;
      }

      @Override
      public void applyStyles(TextStyle textStyle)
      {
        textStyle.font = baseStyle.font;
        textStyle.metrics = baseStyle.metrics;
        textStyle.rise = baseStyle.rise;

        textStyle.background = baseStyle.background;
        textStyle.foreground = baseStyle.foreground;

        textStyle.borderColor = baseStyle.borderColor;
        textStyle.borderStyle = baseStyle.borderStyle;

        textStyle.strikeout = baseStyle.strikeout;
        textStyle.strikeoutColor = baseStyle.strikeoutColor;

        textStyle.underline = baseStyle.underline;
        textStyle.underlineStyle = baseStyle.underlineStyle;
        textStyle.underlineColor = baseStyle.underlineColor;

        textStyle.data = baseStyle.data;

        for (Styler styler : stylers)
        {
          styler.applyStyles(textStyle);
        }
      }

    }
  }

  public class DiagnosticDecoratorAdapter extends DiagnosticAdapter
  {
    protected class Dispatcher implements Runnable
    {
      protected Display display = Display.getDefault();

      protected List<Diagnostic> diagnostics;

      protected int expectedSize;

      private boolean isDispatching;

      public void dispatch(Diagnostic diagnostic)
      {
        List<Diagnostic> currentDiagnostics = null;
        synchronized (this)
        {
          if (diagnostics == null)
          {
            diagnostics = new ArrayList<Diagnostic>();
          }

          // Merge in the new diagnostics with the ones we have cached for dispatching.
          //
          updateDiagnotics(diagnostics, diagnostic);

          // If we didn't delay dispatching, then prepare to dispatch this to the decorator.
          //
          if (!dispatch())
          {
            currentDiagnostics = diagnostics;
            diagnostics = null;
          }
        }

        // If we have are dispatching the diagnostics immediately...
        //
        if (currentDiagnostics != null)
        {
          dispatch(currentDiagnostics);
        }
      }

      /**
       * Returns <code>true</code> when a delayed dispatch has been scheduled.
       */
      protected boolean dispatch()
      {
        // If we're not in the display thread...
        //
        if (Display.getCurrent() != display)
        {
          // Set up the expected size and do a delayed asynchronous call.
          //
          expectedSize = diagnostics.size();
          display.asyncExec
            (new Runnable()
             {
               public void run()
               {
                 display.timerExec(1000, Dispatcher.this);
               }
             });
          return true;
        }
        else
        {
          return false;
        }
      }

      /**
       * Dispatches the diagnostics to the decorator.
       */
      protected void dispatch(List<Diagnostic> diagnostics)
      {
        BasicDiagnostic resourceSetDiagnostic =
          new BasicDiagnostic
            (EObjectValidator.DIAGNOSTIC_SOURCE,
             0,
             EMFEditUIPlugin.INSTANCE.getString("_UI_DiagnosisOfNObjects_message", new String[] { "" + diagnostics.size() }),
             new Object [] { resourceSet } );
        for (Diagnostic diagnostic : diagnostics)
        {
          resourceSetDiagnostic.add(diagnostic);
        }

        if (!isDispatching)
        {
          try
          {
            isDispatching = true;
            handleDiagnostic(resourceSetDiagnostic);
          }
          finally
          {
            isDispatching = false;
          }
        }
      }

      public void run()
      {
        List<Diagnostic> currentDiagnostics;
        synchronized (this)
        {
          // If the diagnostics have already been dispatched, don't do anything else.
          //
          if (diagnostics == null)
          {
            return;
          }

          // If we've added more diagnostics since we started a delayed dispatch, delay a little longer.
          //
          if (diagnostics.size() != expectedSize)
          {
            expectedSize = diagnostics.size();
            display.asyncExec
              (new Runnable()
               {
                 public void run()
                 {
                   display.timerExec(1000, Dispatcher.this);
                 }
               });
            return;
          }
          currentDiagnostics = diagnostics;
          diagnostics = null;
        }

        // Dispatch the diagnostics immediately.
        //
        dispatch(currentDiagnostics);
      }
    }

    protected  Dispatcher dispatcher;

    protected Dispatcher getDispatcher()
    {
      if (dispatcher == null)
      {
        dispatcher = new Dispatcher();
      }
      return dispatcher;
    }

    @Override
    public void updateDiagnostic(Diagnostic diagnostic)
    {
      getDispatcher().dispatch(diagnostic);
    }

    @Override
    protected void handleResourceDiagnostics(List<Resource> resources)
    {
      final BasicDiagnostic diagnostic =
        new BasicDiagnostic
          (EObjectValidator.DIAGNOSTIC_SOURCE,
           0,
           EMFEditUIPlugin.INSTANCE.getString("_UI_DiagnosisOfNObjects_message", new String[] { "" + resources.size() }),
           new Object [] { resourceSet } );
      LiveValidator liveValidator = getLiveValidator();
      if (liveValidator != null)
      {
        for (Resource resource : resources)
        {
          diagnostic.add(markerHelper.getMarkerDiagnostics(resource, null, false));
          liveValidator.scheduleValidation(resource);
        }
      }
      updateDiagnostic(diagnostic);
    }

    protected void refreshResourceDiagnostics(List<Resource> resources)
    {
      final BasicDiagnostic diagnostic =
        new BasicDiagnostic
          (EObjectValidator.DIAGNOSTIC_SOURCE,
           0,
           EMFEditUIPlugin.INSTANCE.getString("_UI_DiagnosisOfNObjects_message", new String[] { "" + resources.size() }),
           new Object [] { resourceSet } );
      for (Resource resource : resources)
      {
        diagnostic.add(markerHelper.getMarkerDiagnostics(resource, null, false));
        liveValidator.scheduleValidation(resource);
      }
      diagnostics.clear();
      updateDiagnostic(diagnostic);
    }
  }

  private static final Map<EditingDomain, LiveValidator> LIVE_VALIDATORS = new HashMap<EditingDomain, LiveValidator>();

  private int limit = getLimit();
  protected DiagnosticAdapter diagnosticAdapter;
  protected EditingDomain editingDomain;
  protected LiveValidator liveValidator;
  protected ResourceSet resourceSet;
  protected StructuredViewer viewer;
  protected ExtendedPropertySheetPage propertySheetPage;
  protected IDialogSettings dialogSettings;
  protected Map<Object, BasicDiagnostic> decorations = new HashMap<Object, BasicDiagnostic>();
  protected MarkerHelper markerHelper = new EditUIMarkerHelper();
  protected Object input;
  protected IContentProvider contentProvider;
  protected List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();

  /**
   * Creates an instance that doesn't support {@link LiveValidator live validation}.
   * Only decorations explicitly produced from {@link ValidateAction} or those {@link EditUIMarkerHelper#getMarkerDiagnostics(Object, org.eclipse.core.resources.IFile, boolean) derived from markers} are displayed.
   */
  public DiagnosticDecorator(ResourceSet resourceSet, StructuredViewer viewer)
  {
    this.viewer = viewer;
    this.resourceSet = resourceSet;

    diagnosticAdapter = new DiagnosticDecoratorAdapter();
    resourceSet.eAdapters().add(diagnosticAdapter);

    input = viewer.getInput();
    contentProvider = viewer.getContentProvider();
  }

  /**
   * Creates an instance that supports {@link LiveValidator live validation}.
   */
  public DiagnosticDecorator(EditingDomain editingDomain, StructuredViewer viewer)
  {
    this(editingDomain, viewer, null);
  }

  /**
   * Creates an instance that supports {@link LiveValidator live validation} and supports {@link DiagnosticDecorator.LiveValidator.LiveValidationAction#LIVE_VALIDATOR_DIALOG_SETTINGS_KEY enablement} via {@link IDialogSettings dialog setting}.
   */
  public DiagnosticDecorator(EditingDomain editingDomain, StructuredViewer viewer, IDialogSettings dialogSettings)
  {
    this.editingDomain = editingDomain;
    this.resourceSet = editingDomain.getResourceSet();
    this.viewer = viewer;
    this.dialogSettings = dialogSettings;

    diagnosticAdapter = new DiagnosticDecoratorAdapter();
    resourceSet.eAdapters().add(diagnosticAdapter);

    input = viewer.getInput();
    contentProvider = viewer.getContentProvider();
  }

  /**
   * Creates an instance that doesn't support {@link LiveValidator live validation}.
   * Only decorations explicitly produced from {@link ValidateAction} or those {@link EditUIMarkerHelper#getMarkerDiagnostics(Object, org.eclipse.core.resources.IFile, boolean) derived from markers} are displayed.
   */
  public DiagnosticDecorator(ResourceSet resourceSet, ExtendedPropertySheetPage propertySheetPage)
  {
    this.resourceSet = resourceSet;
    this.propertySheetPage = propertySheetPage;

    diagnosticAdapter = new DiagnosticDecoratorAdapter();
    resourceSet.eAdapters().add(diagnosticAdapter);

    input = propertySheetPage.getInput();
  }

  /**
   * Creates an instance that supports {@link LiveValidator live validation}.
   */
  public DiagnosticDecorator(EditingDomain editingDomain, ExtendedPropertySheetPage propertySheetPage)
  {
    this(editingDomain, propertySheetPage, null);
  }

  /**
   * Creates an instance that supports {@link LiveValidator live validation} and supports {@link DiagnosticDecorator.LiveValidator.LiveValidationAction#LIVE_VALIDATOR_DIALOG_SETTINGS_KEY enablement} via {@link IDialogSettings dialog setting}.
   */
  public DiagnosticDecorator(EditingDomain editingDomain, ExtendedPropertySheetPage propertySheetPage, IDialogSettings dialogSettings)
  {
    this.editingDomain = editingDomain;
    this.resourceSet = editingDomain.getResourceSet();
    this.propertySheetPage = propertySheetPage;
    this.dialogSettings = dialogSettings;

    diagnosticAdapter = new DiagnosticDecoratorAdapter();
    resourceSet.eAdapters().add(diagnosticAdapter);

    input = propertySheetPage.getInput();
  }

  /**
   * {@link LiveValidator#cancelValidation() Cancel}s the live validator's job, if there is one.
   *
   * @since 2.12
   */
  public static void cancel(EditingDomain editingDomain)
  {
    LiveValidator liveValidator = LIVE_VALIDATORS.get(editingDomain);
    if (liveValidator != null)
    {
      liveValidator.cancelValidation();
    }
  }

  protected LiveValidator createLiveValidator(EditingDomain editingDomain, IDialogSettings dialogSettings)
  {
    return new LiveValidator(editingDomain, dialogSettings);
  }

  protected LiveValidator getLiveValidator()
  {
    if (liveValidator == null && editingDomain != null)
    {
      liveValidator = LIVE_VALIDATORS.get(editingDomain);
      if (liveValidator == null)
      {
        liveValidator = createLiveValidator(editingDomain, dialogSettings);
        LIVE_VALIDATORS.put(editingDomain, liveValidator);
      }
      liveValidator.register(this);
    }
    return liveValidator;
  }

  public String decorateText(String text, Object object)
  {
    return text;
  }

  public Image decorateImage(Image image, Object object)
  {
    Diagnostic diagnostic = getDecorations().get(object);
    if (diagnostic != null && diagnostic.getSeverity() >= Diagnostic.WARNING)
    {
      return decorate(image, diagnostic);
    }
    else
    {
      return image;
    }
  }

  public Image decorate(Image image, Diagnostic diagnostic)
  {
    if (image == null)
    {
      return ExtendedImageRegistry.INSTANCE.getImage(EMFEditUIPlugin.INSTANCE.getImage(diagnostic.getSeverity() == Diagnostic.WARNING ? "full/ovr16/warning_ovr.gif" : "full/ovr16/error_ovr.gif"));
    }
    else
    {
      List<Object> images = new ArrayList<Object>(2);
      images.add(image);
      images.add(EMFEditUIPlugin.INSTANCE.getImage(diagnostic.getSeverity() == Diagnostic.WARNING ? "full/ovr16/warning_ovr.gif" : "full/ovr16/error_ovr.gif"));
      ComposedImage composedImage = new DecoratedComposedImage(images);
      return ExtendedImageRegistry.INSTANCE.getImage(composedImage);
    }
  }

  private static final class DecoratedComposedImage extends ComposedImage
  {
    private DecoratedComposedImage(Collection<?> images)
    {
      super(images);
    }

    @Override
    public List<Point> getDrawPoints(Size size)
    {
      List<Point> result = new ArrayList<Point>();
      result.add(new Point());
      Point overlay = new Point();
      overlay.y = 7;
      result.add(overlay);
      return result;
    }
  }

  public Map<Object, ? extends Diagnostic> getDecorations()
  {
    // Check that the current decorations are up-to-date with respect to the viewer input and content provider.
    //
    if (propertySheetPage != null)
    {
      List<?> input = propertySheetPage.getInput();
      if (!input.equals(this.input))
      {
        redecorate();
      }
      this.input = input;
    }
    else
    {
      Object input = viewer.getInput();
      IContentProvider contentProvider = viewer.getContentProvider();
      if (input != null && !input.equals(this.input) || contentProvider != null && !contentProvider.equals(this.contentProvider))
      {
        redecorate();
      }
      this.input = input;
      this.contentProvider = contentProvider;
    }

    return decorations;
  }

  protected void updateDiagnotics(List<Diagnostic> diagnostics, Diagnostic diagnostic)
  {
    String markerSource = markerHelper.getDiagnosticSource();
    String source = diagnostic.getSource();
    if (markerSource.equals(source))
    {
      // Diagnostics produced from markers are expected to have a root diagnostic with the resource as the data,
      // so we clean up old diagnostics that have the same source and for that resource.
      //
      for (Iterator<Diagnostic> i = diagnostics.iterator(); i.hasNext(); )
      {
        Diagnostic oldDiagnostic = i.next();
        if (markerSource.equals(oldDiagnostic.getSource()) && diagnostic.getData().equals(oldDiagnostic.getData()))
        {
          i.remove();
        }
      }
      diagnostics.add(diagnostic);
    }
    else
    {
      // If the root diagnostic is for a resource set, we expect it to hold per-resource children.
      //
      Object object = diagnostic.getData().get(0);
      if (object instanceof ResourceSet)
      {
        for (Diagnostic child : diagnostic.getChildren())
        {
          updateDiagnotics(diagnostics, child);
        }
      }
      else
      {
        // Generally these will be per resource diagnostics.
        // We clean up old diagnostics with a marker source or the same source and with the same data.
        //
        for (Iterator<Diagnostic> i = diagnostics.iterator(); i.hasNext(); )
        {
          Diagnostic oldDiagnostic = i.next();
          String oldSource = oldDiagnostic.getSource();
          if ((source.equals(oldSource) || markerSource.equals(oldSource)) && diagnostic.getData().equals(oldDiagnostic.getData()))
          {
            i.remove();
          }
        }
        diagnostics.add(diagnostic);
      }
    }
  }

  protected void handleDiagnostic(Diagnostic rootDiagnostic)
  {
    // Update the decorative diagnostics.
    //
    updateDiagnotics(diagnostics, rootDiagnostic);

    // Redecorate with the current diagnostics.
    //
    redecorate();
  }

  protected void redecorate()
  {
    // If the viewer has input and is ready to receive decorations...
    //
    if (propertySheetPage != null ?  propertySheetPage.getInput() != null : viewer.getInput() != null && viewer.getContentProvider() != null)
    {
      // Build up the map for root decorations.
      //
      Map<Object, BasicDiagnostic> objects = new HashMap<Object, BasicDiagnostic>();
      for (Diagnostic diagnostic : diagnostics)
      {
        for (Diagnostic child : diagnostic.getChildren())
        {
          List<?> data = child.getData();
          if (!data.isEmpty())
          {
            decorate(objects, data.get(0), child, null);
          }
        }
      }

      // Keep track of the old decorations and start building fresh new ones.
      //
      Map<Object, BasicDiagnostic> oldDecorations = decorations;
      decorations = new HashMap<Object, BasicDiagnostic>();

      // Decorate based on the root decorations.
      //
      decorate(objects);

      if (propertySheetPage != null)
      {
        // If this is for the property sheet and there are decorations that need cleaning or there are new decorations to show, refresh the view.
        //
        if (!decorations.isEmpty() || !oldDecorations.isEmpty())
        {
          final Control control = propertySheetPage.getControl();
          if (control != null && !control.isDisposed())
          {
            control.getDisplay().asyncExec
              (new Runnable()
               {
                 public void run()
                 {
                   if (!control.isDisposed())
                   {
                     propertySheetPage.refreshLabels();
                   }
                 }
               });
          }
        }
      }
      else
      {
        // Compute the objects that need to be refreshed.
        //
        final Set<Object> objectsToRefresh = new HashSet<Object>();
        for (Map.Entry<Object, BasicDiagnostic> entry : decorations.entrySet())
        {
          Object decoratedObject = entry.getKey();
          BasicDiagnostic oldDiagnostic = oldDecorations.get(decoratedObject);
          if (oldDiagnostic == null || entry.getValue().getSeverity() != oldDiagnostic.getSeverity())
          {
            // Each object that's newly decorated or has a different decoration needs to be refreshed.
            //
            objectsToRefresh.add(decoratedObject);
          }

          // Forget about this old decoration; either the object is already added for refreshing or it doesn't need refreshing because the severity is the same.
          //
          if (oldDiagnostic != null)
          {
            oldDecorations.remove(decoratedObject);
          }
        }

        // Also refresh all the object with old decorations that no longer have decorations.
        //
        objectsToRefresh.addAll(oldDecorations.keySet());

        if (!objectsToRefresh.isEmpty())
        {
          // Be careful to refresh asynchronously if the viewer is busy.
          //
          if (!(viewer instanceof ColumnViewer) || !((ColumnViewer)viewer).isBusy())
          {
            viewer.update(objectsToRefresh.toArray(), null);
          }
          else
          {
            final Control control = viewer.getControl();
            if (!control.isDisposed())
            {
              control.getDisplay().asyncExec
                (new Runnable()
                 {
                   public void run()
                   {
                     if (!control.isDisposed())
                     {
                       viewer.update(objectsToRefresh.toArray(), null);
                     }
                   }
                 });
            }
          }
        }
      }
    }
  }

  protected BasicDiagnostic decorate(Map<Object, BasicDiagnostic> decorations, Object object, Diagnostic diagnostic, List<Integer> path)
  {
    BasicDiagnostic oldDiagnostic = decorations.get(object);
    if (diagnostic != null)
    {
      if (oldDiagnostic == null)
      {
        oldDiagnostic = new BasicDiagnostic(null, 0, null, path == null ? new Object [] { object } : new Object [] { object, path.toArray(new Integer[path.size()]) });
        decorations.put(object, oldDiagnostic);
      }

      String message = diagnostic.getMessage();
      if (message != null)
      {
        List<?> data = diagnostic.getData();
        for (Diagnostic childDiagnostic : oldDiagnostic.getChildren())
        {
          if (childDiagnostic.getMessage().equals(message) && data.equals(childDiagnostic.getData()))
          {
            return oldDiagnostic;
          }
        }
      }

      oldDiagnostic.add(diagnostic);
    }
    return oldDiagnostic;
  }

  protected void decorate(Map<Object, BasicDiagnostic> objects)
  {
    if (propertySheetPage != null)
    {
      for (Object object : propertySheetPage.getInput())
      {
        decorate(decorations, object, objects.get(object), null);
        if (object instanceof EObject)
        {
          EObject eObject = (EObject)object;
          for (EObject child : eObject.eContents())
          {
            decorate(decorations, child, objects.get(child), null);
          }
          for (EObject child : eObject.eCrossReferences())
          {
            decorate(decorations, child, objects.get(child), null);
          }
        }
      }
    }
    else
    {
      Object input = viewer.getInput();
      IContentProvider contentProvider = viewer.getContentProvider();
      if (contentProvider instanceof IStructuredContentProvider)
      {
        ITreeContentProvider treeContentProvider = contentProvider instanceof ITreeContentProvider ? (ITreeContentProvider)contentProvider : null;

        int index = 0;
        List<Integer> path = new ArrayList<Integer>();
        path.add(-1);
        for (Object object : ((IStructuredContentProvider)contentProvider).getElements(input))
        {
          path.set(0, index++);
          BasicDiagnostic childDiagnostic = getDiagnostic(objects, object, path);
          Diagnostic objectDiagnostic = decorate(decorations, object, childDiagnostic, path);
          if (treeContentProvider != null)
          {
            Set<Object> visited = new HashSet<Object>();
            objectDiagnostic = decorate(objects, treeContentProvider, visited, object, path);
          }
          decorate(decorations, input, objectDiagnostic, null);
        }
      }
    }
  }

  /**
   * @since 2.10
   */
  protected BasicDiagnostic getDiagnostic(Map<Object, BasicDiagnostic> objects, Object object, List<Integer> path)
  {
    BasicDiagnostic childDiagnostic = objects.get(object);
    if (childDiagnostic == null)
    {
      // Check if the unwrapped child is different and has diagnostics...
      //
      Object unwrappedChild = AdapterFactoryEditingDomain.unwrap(object);
      if (object != unwrappedChild)
      {
        childDiagnostic = objects.get(unwrappedChild);
        if (childDiagnostic != null)
        {
          // Create a new diagnostic wrapper for it.
          //
          childDiagnostic = 
            new BasicDiagnostic
              (childDiagnostic.getSource(), 
               childDiagnostic.getCode(), 
               childDiagnostic.getChildren(), 
               childDiagnostic.getMessage(), 
               new Object[] { object, path.toArray(new Integer[path.size()]) });
          objects.put(object, childDiagnostic);
        }
      }
    }
    return childDiagnostic;
  }

  protected BasicDiagnostic decorate(Map<Object, BasicDiagnostic> objects, ITreeContentProvider treeContentProvider, Set<Object> visited, Object object, List<Integer> path)
  {
    BasicDiagnostic result = decorations.get(object);
    if (visited.add(object))
    {
      int index = 0;
      int last = path.size();
      for (Object child : treeContentProvider.getChildren(object))
      {
        path.add(index++);
        BasicDiagnostic childDiagnostic = getDiagnostic(objects, child, path);
        BasicDiagnostic childResult = decorate(decorations, child, childDiagnostic, path);
        childResult = decorate(objects, treeContentProvider, visited, child, path);
        path.remove(last);
        result = decorate(decorations, object, childResult, path);
      }
    }
    return result;
  }

  @Override
  public String getToolTipText(Object object)
  {
    BasicDiagnostic diagnostic = decorations.get(object);
    if (diagnostic != null)
    {
      ILabelProvider labelProvider = (ILabelProvider)viewer.getLabelProvider();
      if (labelProvider instanceof IUndecoratingLabelProvider)
      {
        final IUndecoratingLabelProvider undecoratingLabelProvider = (IUndecoratingLabelProvider)labelProvider;
        labelProvider = 
          new ILabelProvider()
          {
            public void removeListener(ILabelProviderListener listener)
            {
              undecoratingLabelProvider.removeListener(listener);
            }

            public boolean isLabelProperty(Object element, String property)
            {
              return undecoratingLabelProvider.isLabelProperty(element, property);
            }

            public void dispose()
            {
              undecoratingLabelProvider.dispose();
            }

            public void addListener(ILabelProviderListener listener)
            {
              undecoratingLabelProvider.addListener(listener);
            }

            public String getText(Object element)
            {
              return undecoratingLabelProvider.getUndecoratedText(element);
            }

            public Image getImage(Object element)
            {
              return undecoratingLabelProvider.getUndecoratedImage(element);
            }
          };
      }
      StringBuilder result = new StringBuilder();
      buildToolTipText(result, labelProvider, diagnostic, object);
      return result.length() == 0 ? null : result.toString();
    }
    else
    {
      return null;
    }
  }

  protected void buildToolTipText(StringBuilder result, ILabelProvider labelProvider, Diagnostic diagnostic, Object object)
  {
    List<Diagnostic> children = diagnostic.getChildren();
    Diagnostic child = children.get(0);
    int index = 0;
    if (child.getData().contains(object))
    {
      ++index;
      for (Diagnostic grandChild : child.getChildren())
      {
        buildToolTipMessage(result, labelProvider, object, grandChild, 0);
      }
    }

    StringBuilder moreResults = new StringBuilder();
    for (int size = children.size(); index < size; ++index)
    {
      child = children.get(index);
      buildMoreToolTipText(moreResults, labelProvider, child);
      if (moreResults.length() > limit)
      {
        break;
      }
    }
    if (moreResults.length() != 0)
    {
      result.append("<h1>Problems on Children</h1>\n");
      result.append(moreResults);
    }
  }

  protected void buildToolTipMessage(StringBuilder result, ILabelProvider labelProvider, Object object, Diagnostic diagnostic, int indentation)
  {
    String message = diagnostic.getMessage();
    ImageDescriptor imageDescriptor =
      ExtendedImageRegistry.INSTANCE.getImageDescriptor
        (EMFEditUIPlugin.INSTANCE.getImage(diagnostic.getSeverity() == Diagnostic.WARNING ? "full/ovr16/warning_ovr.gif" : "full/ovr16/error_ovr.gif"));
    URI severityURI = ImageURIRegistry.INSTANCE.getImageURI(imageDescriptor);
    result.append("<div style='white-space: nowrap;'>");
    for (int i = 0; i < indentation; ++i)
    {
      result.append("&#160;&#160;&#160;");
    }
    result.append("<img src='");
    result.append(severityURI);
    result.append("'/> ");
    result.append(escapeContent(message));
    result.append("</div>\n");
    List<?> excludedObjects = object instanceof EObject ? ((EObject)object).eClass().getEAllStructuralFeatures() : Collections.emptyList();
    for (Object data : diagnostic.getData())
    {
      if (data != object && !excludedObjects.contains(data) && data instanceof EObject)
      {
        EObject eObject = (EObject)data;
        if (eObject.eResource() != null && eObject.eResource().getResourceSet() == resourceSet)
        {
          result.append("<div style='white-space: nowrap;'>");
          for (int i = 0; i <= indentation; ++i)
          {
            result.append("&#160;&#160;&#160;");
          }
          String text = escapeContent(labelProvider.getText(data));
          Image image = labelProvider.getImage(data);
          result.append("<img src='");
          result.append(ImageURIRegistry.INSTANCE.getImageURI(image));
          result.append("'/> <a href=\"");
          result.append(EcoreUtil.getURI((EObject)data));
          result.append("\">");
          result.append(text);
          result.append("</a></div>\n");
        }
      }
    }

    for (Diagnostic child : diagnostic.getChildren())
    {
      buildToolTipMessage(result, labelProvider, child.getData().get(0), child, indentation + 1);
    }
  }

  protected void buildMoreToolTipText(StringBuilder result, ILabelProvider labelProvider, Diagnostic diagnostic)
  {
    List<?> data = diagnostic.getData();
    Object object = data.get(0);

    List<Diagnostic> children = diagnostic.getChildren();
    Diagnostic child = children.get(0);
    int index = 0;
    if (child.getData().contains(object))
    {
      ++index;
      if (data.size() > 1)
      {
        result.append("<div style='white-space: nowrap;'>");
        result.append("<div>");
        Image image = labelProvider.getImage(object);
        if (image != null)
        {
          URI imageURI = ImageURIRegistry.INSTANCE.getImageURI(image);
          result.append("<img src='");
          result.append(imageURI);
          result.append("'/> ");
        }
        result.append("<a href='");
        result.append("path:");
        Integer[] path = (Integer[])data.get(1);
        for (Integer segment : path)
        {
          result.append('/');
          result.append(segment);
        }
        String text = escapeContent(labelProvider.getText(object));
        if (text == null || text.length() == 0)
        {
          text = "<i>null</i>";
        }
        result.append("'>");
        result.append(text);
        result.append("</a></div>\n");
        for (Diagnostic grandChild : child.getChildren())
        {
          buildToolTipMessage(result, labelProvider, object, grandChild, 1);
        }
      }
    }

    for (int size = children.size(); index < size; ++index)
    {
      child = children.get(index);
      buildMoreToolTipText(result, labelProvider, child);
      if (result.length() > limit)
      {
        break;
      }
    }
  }

  @Override
  public void update(ViewerCell cell)
  {
    // Do nothing.
  }

  @Override
  public void dispose()
  {
    if (liveValidator != null)
    {
      liveValidator.deregister(this);
    }

    if (diagnosticAdapter != null)
    {
      resourceSet.eAdapters().remove(diagnosticAdapter);
    }

    super.dispose();
  }

  private static int getLimit()
  {
    String property = System.getProperty("org.eclipse.emf.common.util.Diagnostic.limit");
    if (property != null)
    {
      try
      {
        // Allow for 100 lines of text per diagnostic.
        int result = Integer.parseInt(property) + 100;
        if (result > 0)
        {
          return result;
        }
      }
      catch (RuntimeException exception)
      {
        //$FALL-THROUGH$
      }
    }
    return 10000;
  }
}
