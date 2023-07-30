/**
 * Copyright (c) 2002-2007 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *   IBM - Initial API and implementation
 */
package org.eclipse.emf.ecore.presentation;


import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import org.eclipse.swt.SWT;

import org.eclipse.swt.custom.CTabFolder;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;

import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

import org.eclipse.ui.dialogs.SaveAsDialog;

import org.eclipse.ui.ide.IGotoMarker;

import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;

import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheet;
import org.eclipse.ui.views.properties.PropertySheetPage;

import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.ui.viewer.ColumnViewerInformationControlToolTipSupport;
import org.eclipse.emf.common.command.AbstractCommand;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.command.CommandStackListener;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.ui.MarkerHelper;

import org.eclipse.emf.common.ui.editor.ProblemEditorPart;

import org.eclipse.emf.common.ui.viewer.IViewerProvider;

import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.UniqueEList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EGenericType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.ETypeParameter;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.ecore.util.EcoreUtil;

import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.GenericXMLResourceFactoryImpl;

import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;

import org.eclipse.emf.edit.provider.AdapterFactoryItemDelegator;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;

import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;

import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;

import org.eclipse.emf.edit.ui.celleditor.AdapterFactoryTreeEditor;

import org.eclipse.emf.edit.ui.dnd.EditingDomainViewerDropAdapter;
import org.eclipse.emf.edit.ui.dnd.LocalTransfer;
import org.eclipse.emf.edit.ui.dnd.ViewerDragAdapter;

import org.eclipse.emf.edit.ui.provider.AdapterFactoryContentProvider;
import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
import org.eclipse.emf.edit.ui.provider.DecoratingColumLabelProvider;
import org.eclipse.emf.edit.ui.provider.DiagnosticDecorator;

import org.eclipse.emf.edit.ui.provider.UnwrappingSelectionProvider;
import org.eclipse.emf.edit.ui.util.EditUIMarkerHelper;

import org.eclipse.emf.edit.ui.util.EditUIUtil;
import org.eclipse.emf.edit.ui.util.FindAndReplaceTarget;
import org.eclipse.emf.edit.ui.util.IRevertablePart;
import org.eclipse.emf.edit.ui.view.ExtendedPropertySheetPage;

import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.emf.ecore.provider.EcoreItemProviderAdapterFactory;

import org.eclipse.ui.actions.WorkspaceModifyOperation;


/**
 * This is an example of a Ecore model editor.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class EcoreEditor
  extends MultiPageEditorPart
  implements IEditingDomainProvider, ISelectionProvider, IMenuListener, IViewerProvider, IGotoMarker, IRevertablePart
{
  public static class XML extends EcoreEditor
  {
    public XML()
    {
      try
      {
        editingDomain.getResourceSet().getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new GenericXMLResourceFactoryImpl());

        Class<?> theItemProviderClass = CommonPlugin.loadClass("org.eclipse.xsd.edit", "org.eclipse.xsd.provider.XSDItemProviderAdapterFactory");
        AdapterFactory xsdItemProviderAdapterFactory = (AdapterFactory) theItemProviderClass.getDeclaredConstructor().newInstance();
        adapterFactory.insertAdapterFactory(xsdItemProviderAdapterFactory);
      }
      catch (Exception exception)
      {
        EcoreEditorPlugin.INSTANCE.log(exception);
      }
    }

    @Override
    public void createModel()
    {
      super.createModel();

      // Load the schema and packages that were used to load the instance into this resource set.
      //
      ResourceSet resourceSet = editingDomain.getResourceSet();
      if (!resourceSet.getResources().isEmpty())
      {
        Resource resource = resourceSet.getResources().get(0);
        if (!resource.getContents().isEmpty())
        {
          EObject rootObject = resource.getContents().get(0);
          Resource metaDataResource =  rootObject.eClass().eResource();
          if (metaDataResource != null && metaDataResource.getResourceSet() != null)
          {
            resourceSet.getResources().addAll(metaDataResource.getResourceSet().getResources());
          }
        }
      }
    }
  }

  /**
   * This keeps track of the editing domain that is used to track all changes to the model.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected AdapterFactoryEditingDomain editingDomain;

  /**
   * This is the one adapter factory used for providing views of the model.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ComposedAdapterFactory adapterFactory;

  /**
   * @since 2.14
   */
  protected EcoreItemProviderAdapterFactory ecoreItemProviderAdapterFactory;

  /**
   * This is the content outline page.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected IContentOutlinePage contentOutlinePage;

  /**
   * This is a kludge...
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected IStatusLineManager contentOutlineStatusLineManager;

  /**
   * This is the content outline page's viewer.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected TreeViewer contentOutlineViewer;

  /**
   * This is the property sheet page.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected List<PropertySheetPage> propertySheetPages = new ArrayList<PropertySheetPage>();

  /**
   * This is the viewer that shadows the selection in the content outline.
   * The parent relation must be correctly defined for this to work.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected TreeViewer selectionViewer;

  /**
   * This keeps track of the active content viewer, which may be either one of the viewers in the pages or the content outline viewer.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected Viewer currentViewer;

  /**
   * This listens to which ever viewer is active.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ISelectionChangedListener selectionChangedListener;

  /**
   * This keeps track of all the {@link org.eclipse.jface.viewers.ISelectionChangedListener}s that are listening to this editor.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();

  /**
   * This keeps track of the selection of the editor as a whole.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected ISelection editorSelection = StructuredSelection.EMPTY;

  /**
   * The MarkerHelper is responsible for creating workspace resource markers presented
   * in Eclipse's Problems View.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected MarkerHelper markerHelper = new EditUIMarkerHelper();

  /**
   * This listens for when the outline becomes active
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected IPartListener partListener =
    new IPartListener()
    {
      public void partActivated(IWorkbenchPart p)
      {
        if (p instanceof ContentOutline)
        {
          if (((ContentOutline)p).getCurrentPage() == contentOutlinePage)
          {
            getActionBarContributor().setActiveEditor(EcoreEditor.this);

            setCurrentViewer(contentOutlineViewer);
          }
        }
        else if (p instanceof PropertySheet)
        {
          if (propertySheetPages.contains(((PropertySheet)p).getCurrentPage()))
          {
            getActionBarContributor().setActiveEditor(EcoreEditor.this);
            handleActivate();
          }
        }
        else if (p == EcoreEditor.this)
        {
          handleActivate();
        }
      }
      public void partBroughtToTop(IWorkbenchPart p)
      {
        // Ignore.
      }
      public void partClosed(IWorkbenchPart p)
      {
        // Ignore.
      }
      public void partDeactivated(IWorkbenchPart p)
      {
        // Ignore.
      }
      public void partOpened(IWorkbenchPart p)
      {
        // Ignore.
      }
    };

  /**
   * Resources that have been removed since last activation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected Collection<Resource> removedResources = new ArrayList<Resource>();

  /**
   * Resources that have been changed since last activation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
  protected Collection<Resource> changedResources = new UniqueEList<Resource>();

  /**
   * Resources that have been saved.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected Collection<Resource> savedResources = new ArrayList<Resource>();

  /**
   * Map to store the diagnostic associated with a resource.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected Map<Resource, Diagnostic> resourceToDiagnosticMap = new LinkedHashMap<Resource, Diagnostic>();

  /**
   * Controls whether the problem indication should be updated.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected boolean updateProblemIndication = true;

  /**
   * Adapter used to update the problem indication when resources are demanded loaded.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected EContentAdapter problemIndicationAdapter = 
    new EContentAdapter()
    {
      protected boolean dispatching;

      @Override
      public void notifyChanged(Notification notification)
      {
        if (notification.getNotifier() instanceof Resource)
        {
          switch (notification.getFeatureID(Resource.class))
          {
            case Resource.RESOURCE__IS_LOADED:
            case Resource.RESOURCE__ERRORS:
            case Resource.RESOURCE__WARNINGS:
            {
              Resource resource = (Resource)notification.getNotifier();
              Diagnostic diagnostic = analyzeResourceProblems(resource, null);
              if (diagnostic.getSeverity() != Diagnostic.OK)
              {
                resourceToDiagnosticMap.put(resource, diagnostic);
              }
              else
              {
                resourceToDiagnosticMap.remove(resource);
              }
              dispatchUpdateProblemIndication();
              break;
            }
          }
        }
        else
        {
          super.notifyChanged(notification);
        }
      }

      protected void dispatchUpdateProblemIndication()
      {
        if (updateProblemIndication && !dispatching)
        {
          dispatching = true;
          getSite().getShell().getDisplay().asyncExec
            (new Runnable()
             {
               public void run()
               {
                 dispatching = false;
                 updateProblemIndication();
               }
             });
        }
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
        resourceToDiagnosticMap.remove(target);
        dispatchUpdateProblemIndication();
      }
    };

  /**
   * This listens for workspace changes.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected IResourceChangeListener resourceChangeListener =
    new IResourceChangeListener()
    {
      public void resourceChanged(IResourceChangeEvent event)
      {
        IResourceDelta delta = event.getDelta();
        try
        {
          class ResourceDeltaVisitor implements IResourceDeltaVisitor
          {
            protected ResourceSet resourceSet = editingDomain.getResourceSet();
            protected Collection<Resource> changedResources = new ArrayList<Resource>();
            protected Collection<Resource> removedResources = new ArrayList<Resource>();

            public boolean visit(final IResourceDelta delta)
            {
              if (delta.getResource().getType() == IResource.FILE)
              {
                if (delta.getKind() == IResourceDelta.REMOVED ||
                    delta.getKind() == IResourceDelta.CHANGED)
                {
                  final Resource resource = resourceSet.getResource(URI.createPlatformResourceURI(delta.getFullPath().toString(), true), false);
                  if (resource != null)
                  {
                    if (delta.getKind() == IResourceDelta.REMOVED)
                    {
                      removedResources.add(resource);
                    }
                    else
                    {
                      if ((delta.getFlags() & IResourceDelta.MARKERS) != 0)
                      {
                        DiagnosticDecorator.DiagnosticAdapter.update(resource, markerHelper.getMarkerDiagnostics(resource, (IFile)delta.getResource(), false));
                      }
                      if ((delta.getFlags() & IResourceDelta.CONTENT) != 0)
                      {
                        if (!savedResources.remove(resource))
                        {
                          changedResources.add(resource);
                        }
                      }
                    }
                  }
                }
                return false;
              }

              return true;
            }

            public Collection<Resource> getChangedResources()
            {
              return changedResources;
            }

            public Collection<Resource> getRemovedResources()
            {
              return removedResources;
            }
          }

          final ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
          delta.accept(visitor);

          if (!visitor.getRemovedResources().isEmpty())
          {
            getSite().getShell().getDisplay().asyncExec
              (new Runnable()
               {
                 public void run()
                 {
                   removedResources.addAll(visitor.getRemovedResources());
                   if (!isDirty())
                   {
                     getSite().getPage().closeEditor(EcoreEditor.this, false);
                   }
                 }
               });
          }

          if (!visitor.getChangedResources().isEmpty())
          {
            getSite().getShell().getDisplay().asyncExec
              (new Runnable()
               {
                 public void run()
                 {
                   changedResources.addAll(visitor.getChangedResources());
                   if (getSite().getPage().getActiveEditor() == EcoreEditor.this)
                   {
                     handleActivate();
                   }
                 }
               });
          }
        }
        catch (CoreException exception)
        {
          EcoreEditorPlugin.INSTANCE.log(exception);
        }
      }
    };

  /**
   * Handles activation of the editor or it's associated views.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected void handleActivateGen()
  {
    // Recompute the read only state.
    //
    if (editingDomain.getResourceToReadOnlyMap() != null)
    {
      editingDomain.getResourceToReadOnlyMap().clear();

      // Refresh any actions that may become enabled or disabled.
      //
      setSelection(getSelection());
    }

    if (!removedResources.isEmpty())
    {
      if (handleDirtyConflict())
      {
        getSite().getPage().closeEditor(EcoreEditor.this, false);
      }
      else
      {
        removedResources.clear();
        changedResources.clear();
        savedResources.clear();
      }
    }
    else if (!changedResources.isEmpty())
    {
      changedResources.removeAll(savedResources);
      handleChangedResources();
      changedResources.clear();
      savedResources.clear();
    }
  }

  protected static final List<String> NON_DYNAMIC_EXTENSIONS = Arrays.asList(new String [] { "xcore", "oclinecore", "xcoreiq", "emof", "ecore", "genmodel" });

  protected void handleActivate()
  {
    if (removedResources.isEmpty() && !changedResources.isEmpty())
    {
      for (Resource resource : editingDomain.getResourceSet().getResources())
      {
        if (!changedResources.contains(resource))
        {
          URI uri = resource.getURI();
          if (!"java".equals(uri.scheme()) && !NON_DYNAMIC_EXTENSIONS.contains(uri.fileExtension()))
          {
            for (Iterator<EObject> i = resource.getAllContents(); i.hasNext(); )
            {
              EObject eObject = i.next();
              if (changedResources.contains(eObject.eClass().eResource()))
              {
                changedResources.add(resource);
                break;
              }
            }
          }
        }
      }
    }
    handleActivateGen();
  }

  /**
   * Handles what to do with changed resources on activation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
  protected void handleChangedResources()
  {
    if (!changedResources.isEmpty() && (!isDirty() || handleDirtyConflict()))
    {
      if (isDirty())
      {
        changedResources.addAll(editingDomain.getResourceSet().getResources());
      }
      editingDomain.getCommandStack().flush();

      updateProblemIndication = false;
      List<Resource> unloadedResources = new ArrayList<Resource>();
      for (Resource resource : changedResources)
      {
        if (resource.isLoaded())
        {
          resource.unload();
          unloadedResources.add(resource);
        }
      }

      for (Resource resource : unloadedResources)
      {
        try
        {
          resource.load(Collections.EMPTY_MAP);
        }
        catch (IOException exception)
        {
          if (!resourceToDiagnosticMap.containsKey(resource))
          {
            resourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
          }
        }
      }

      if (AdapterFactoryEditingDomain.isStale(editorSelection))
      {
        setSelection(StructuredSelection.EMPTY);
      }

      updateProblemIndication = true;
      updateProblemIndication();
    }
  }

  /**
   * Updates the problems indication with the information described in the specified diagnostic.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected void updateProblemIndication()
  {
    if (updateProblemIndication)
    {
      BasicDiagnostic diagnostic =
        new BasicDiagnostic
          (Diagnostic.OK,
           "org.eclipse.emf.ecore.editor",
           0,
           null,
           new Object [] { editingDomain.getResourceSet() });
      for (Diagnostic childDiagnostic : resourceToDiagnosticMap.values())
      {
        if (childDiagnostic.getSeverity() != Diagnostic.OK)
        {
          diagnostic.add(childDiagnostic);
        }
      }

      int lastEditorPage = getPageCount() - 1;
      if (lastEditorPage >= 0 && getEditor(lastEditorPage) instanceof ProblemEditorPart)
      {
        ((ProblemEditorPart)getEditor(lastEditorPage)).setDiagnostic(diagnostic);
        if (diagnostic.getSeverity() != Diagnostic.OK)
        {
          setActivePage(lastEditorPage);
        }
      }
      else if (diagnostic.getSeverity() != Diagnostic.OK)
      {
        ProblemEditorPart problemEditorPart = new ProblemEditorPart();
        problemEditorPart.setDiagnostic(diagnostic);
        problemEditorPart.setMarkerHelper(markerHelper);
        try
        {
          addPage(++lastEditorPage, problemEditorPart, getEditorInput());
          setPageText(lastEditorPage, problemEditorPart.getPartName());
          setActivePage(lastEditorPage);
          showTabs();
        }
        catch (PartInitException exception)
        {
          EcoreEditorPlugin.INSTANCE.log(exception);
        }
      }

      if (markerHelper.hasMarkers(editingDomain.getResourceSet()))
      {
        try
        {
          markerHelper.updateMarkers(diagnostic);
        }
        catch (CoreException exception)
        {
          EcoreEditorPlugin.INSTANCE.log(exception);
        }
      }
    }
  }

  /**
   * Shows a dialog that asks if conflicting changes should be discarded.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected boolean handleDirtyConflict()
  {
    return
      MessageDialog.openQuestion
        (getSite().getShell(),
         getString("_UI_FileConflict_label"),
         getString("_WARN_FileConflict"));
  }

  /**
   * This creates a model editor.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EcoreEditor()
  {
    super();
    initializeEditingDomain();
  }

  /**
   * This sets up the editing domain for the model editor.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
  protected void initializeEditingDomain()
  {
    // Create an adapter factory that yields item providers.
    //
    adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);

    adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
    ecoreItemProviderAdapterFactory = new EcoreItemProviderAdapterFactory();
    adapterFactory.addAdapterFactory(ecoreItemProviderAdapterFactory);
    adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());

    // Create the command stack that will notify this editor as commands are executed.
    //
    BasicCommandStack commandStack = new BasicCommandStack()
      {
        @Override
        public void execute(Command command)
        {
          if (!(command instanceof AbstractCommand.NonDirtying))
          {
            DiagnosticDecorator.cancel(editingDomain);
          }
          super.execute(command);
        }
      };

    // Add a listener to set the most recent command's affected objects to be the selection of the viewer with focus.
    //
    commandStack.addCommandStackListener
      (new CommandStackListener()
       {
         public void commandStackChanged(final EventObject event)
         {
           getContainer().getDisplay().asyncExec
             (new Runnable()
              {
                public void run()
                {
                  firePropertyChange(IEditorPart.PROP_DIRTY);

                  // Try to select the affected objects.
                  //
                  Command mostRecentCommand = ((CommandStack)event.getSource()).getMostRecentCommand();
                  if (mostRecentCommand != null)
                  {
                    setSelectionToViewer(mostRecentCommand.getAffectedObjects());
                  }
                  for (Iterator<PropertySheetPage> i = propertySheetPages.iterator(); i.hasNext(); )
                  {
                    PropertySheetPage propertySheetPage = i.next();
                    if (propertySheetPage.getControl() == null || propertySheetPage.getControl().isDisposed())
                    {
                      i.remove();
                    }
                    else
                    {
                      propertySheetPage.refresh();
                    }
                  }
                }
              });
         }
       });

    // Create the editing domain with a special command stack.
    //
    editingDomain =
      new AdapterFactoryEditingDomain(adapterFactory, commandStack)
      {
        {
          resourceToReadOnlyMap = new HashMap<Resource, Boolean>();
        }

        @Override
        public boolean isReadOnly(Resource resource)
        {
          if (super.isReadOnly(resource) || resource == null)
          {
            return true;
          }
          else
          {
            URI uri = resource.getURI();
            boolean result =
                "java".equals(uri.scheme()) ||
                 "xcore".equals(uri.fileExtension()) ||
                 "xcoreiq".equals(uri.fileExtension()) ||
                 "oclinecore".equals(uri.fileExtension()) ||
                 "genmodel".equals(uri.fileExtension()) ||
                 uri.isPlatformResource() && !resourceSet.getURIConverter().normalize(uri).isPlatformResource() ||
                 uri.isPlatformPlugin();
            if (!result)
            {
              for (Object value : resourceSet.getPackageRegistry().values())
              {
                if (value instanceof EObject && ((EObject)value).eResource() == resource)
                {
                  result = true;
                  break;
                }
              }
            }
            if (resourceToReadOnlyMap != null)
            {
              resourceToReadOnlyMap.put(resource, result);
            }
            return result;
          }
        }
      };
  }

  /**
   * This is here for the listener to be able to call it.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected void firePropertyChange(int action)
  {
    super.firePropertyChange(action);
  }

  /**
   * This sets the selection into whichever viewer is active.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSelectionToViewer(Collection<?> collection)
  {
    final Collection<?> theSelection = collection;
    // Make sure it's okay.
    //
    if (theSelection != null && !theSelection.isEmpty())
    {
      Runnable runnable =
        new Runnable()
        {
          public void run()
          {
            // Try to select the items in the current content viewer of the editor.
            //
            if (currentViewer != null)
            {
              currentViewer.setSelection(new StructuredSelection(theSelection.toArray()), true);
            }
          }
        };
      getSite().getShell().getDisplay().asyncExec(runnable);
    }
  }

  /**
   * This returns the editing domain as required by the {@link IEditingDomainProvider} interface.
   * This is important for implementing the static methods of {@link AdapterFactoryEditingDomain}
   * and for supporting {@link org.eclipse.emf.edit.ui.action.CommandAction}.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EditingDomain getEditingDomain()
  {
    return editingDomain;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public class ReverseAdapterFactoryContentProvider extends AdapterFactoryContentProvider
  {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public ReverseAdapterFactoryContentProvider(AdapterFactory adapterFactory)
    {
      super(adapterFactory);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object [] getElements(Object object)
    {
      Object parent = super.getParent(object);
      return (parent == null ? Collections.EMPTY_SET : Collections.singleton(parent)).toArray();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object [] getChildren(Object object)
    {
      Object parent = super.getParent(object);
      return (parent == null ? Collections.EMPTY_SET : Collections.singleton(parent)).toArray();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public boolean hasChildren(Object object)
    {
      Object parent = super.getParent(object);
      return parent != null;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object getParent(Object object)
    {
      return null;
    }
  }

  /**
   * This makes sure that one content viewer, either for the current page or the outline view, if it has focus,
   * is the current one.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setCurrentViewer(Viewer viewer)
  {
    // If it is changing...
    //
    if (currentViewer != viewer)
    {
      if (selectionChangedListener == null)
      {
        // Create the listener on demand.
        //
        selectionChangedListener =
          new ISelectionChangedListener()
          {
            // This just notifies those things that are affected by the section.
            //
            public void selectionChanged(SelectionChangedEvent selectionChangedEvent)
            {
              setSelection(selectionChangedEvent.getSelection());
            }
          };
      }

      // Stop listening to the old one.
      //
      if (currentViewer != null)
      {
        currentViewer.removeSelectionChangedListener(selectionChangedListener);
      }

      // Start listening to the new one.
      //
      if (viewer != null)
      {
        viewer.addSelectionChangedListener(selectionChangedListener);
      }

      // Remember it.
      //
      currentViewer = viewer;

      // Set the editors selection based on the current viewer's selection.
      //
      setSelection(currentViewer == null ? StructuredSelection.EMPTY : currentViewer.getSelection());
    }
  }

  /**
   * This returns the viewer as required by the {@link IViewerProvider} interface.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Viewer getViewer()
  {
    return currentViewer;
  }

  /**
   * This creates a context menu for the viewer and adds a listener as well registering the menu for extension.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected void createContextMenuForGen(StructuredViewer viewer)
  {
    MenuManager contextMenu = new MenuManager("#PopUp");
    contextMenu.add(new Separator("additions"));
    contextMenu.setRemoveAllWhenShown(true);
    contextMenu.addMenuListener(this);
    Menu menu= contextMenu.createContextMenu(viewer.getControl());
    viewer.getControl().setMenu(menu);
    getSite().registerContextMenu(contextMenu, new UnwrappingSelectionProvider(viewer));

    int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
    Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance(), LocalSelectionTransfer.getTransfer(), FileTransfer.getInstance() };
    viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(viewer));
    viewer.addDropSupport(dndOperations, transfers, new EditingDomainViewerDropAdapter(editingDomain, viewer));
  }

  protected void createContextMenuFor(StructuredViewer viewer)
  {
    createContextMenuForGen(viewer);

    viewer.getControl().addMouseListener
     (new MouseAdapter()
      {
        @Override
        public void mouseDoubleClick(MouseEvent event)
        {
          if (event.button == 1)
          {
            try
            {
              getEditorSite().getPage().showView("org.eclipse.ui.views.PropertySheet");
            }
            catch (PartInitException exception)
            {
              EcoreEditorPlugin.INSTANCE.log(exception);
            }
          }
        }
      });
   }

  /**
   * This is the method called to load a resource into the editing domain's resource set based on the editor's input.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void createModelGen()
  {
    URI resourceURI = EditUIUtil.getURI(getEditorInput(), editingDomain.getResourceSet().getURIConverter());
    Exception exception = null;
    Resource resource = null;
    try
    {
      // Load the resource through the editing domain.
      //
      resource = editingDomain.getResourceSet().getResource(resourceURI, true);
    }
    catch (Exception e)
    {
      exception = e;
      resource = editingDomain.getResourceSet().getResource(resourceURI, false);
    }

    Diagnostic diagnostic = analyzeResourceProblems(resource, exception);
    if (diagnostic.getSeverity() != Diagnostic.OK)
    {
      resourceToDiagnosticMap.put(resource,  analyzeResourceProblems(resource, exception));
    }
    editingDomain.getResourceSet().eAdapters().add(problemIndicationAdapter);
  }

  public void createModel()
  {
    boolean isReflective = getActionBarContributor() instanceof EcoreActionBarContributor.Reflective;
    
    final ResourceSet resourceSet = editingDomain.getResourceSet();
    resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap(true));

    resourceSet.getLoadOptions().put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);

    if (isReflective)
    { 
      final EPackage.Registry packageRegistry = new DynamicLoadingEPackageRegistryImpl(resourceSet);
      resourceSet.setPackageRegistry(packageRegistry);
 
      // If we're in the reflective editor, set up an option to handle missing packages.
      //
      final EPackage genModelEPackage = packageRegistry.getEPackage("http://www.eclipse.org/emf/2002/GenModel");
      if (genModelEPackage != null)
      {
        resourceSet.getLoadOptions().put
          (XMLResource.OPTION_MISSING_PACKAGE_HANDLER,
           new XMLResource.MissingPackageHandler()
           {
             protected EClass genModelEClass;
             protected EStructuralFeature genPackagesFeature;
             protected EClass genPackageEClass;
             protected EStructuralFeature ecorePackageFeature;
             protected Map<String, URI> ePackageNsURIToGenModelLocationMap;

             public EPackage getPackage(String nsURI)
             {
               // Initialize the metadata for accessing the GenModel reflective the first time.
               //
               if (genModelEClass == null)
               {
                 genModelEClass = (EClass)genModelEPackage.getEClassifier("GenModel");
                 genPackagesFeature = genModelEClass.getEStructuralFeature("genPackages");
                 genPackageEClass = (EClass)genModelEPackage.getEClassifier("GenPackage");
                 ecorePackageFeature = genPackageEClass.getEStructuralFeature("ecorePackage");
               }

               // Initialize the map from registered package namespaces to their GenModel locations the first time.
               //
               if (ePackageNsURIToGenModelLocationMap == null)
               {
                 ePackageNsURIToGenModelLocationMap = EcorePlugin.getEPackageNsURIToGenModelLocationMap(true);
               }

               // Look up the namespace URI in the map.
               //
               EPackage ePackage = null;
               URI uri = ePackageNsURIToGenModelLocationMap.get(nsURI);
               if (uri != null)
               {
                 // If we find it, demand load the model.
                 //
                 Resource resource = resourceSet.getResource(uri, true);

                 // Locate the GenModel and fetech it's genPackages.
                 //
                 EObject genModel = (EObject)EcoreUtil.getObjectByType(resource.getContents(), genModelEClass);
                 @SuppressWarnings("unchecked")
                 List<EObject> genPackages = (List<EObject>)genModel.eGet(genPackagesFeature);
                 for (EObject genPackage : genPackages)
                 {
                   // Check if that package's Ecore Package has them matching namespace URI.
                   //
                   EPackage dynamicEPackage = (EPackage)genPackage.eGet(ecorePackageFeature);
                   if (nsURI.equals(dynamicEPackage.getNsURI()))
                   {
                     // If so, that's the package we want to return, and we add it to the registry so it's easy to find from now on.
                     //
                     ePackage = dynamicEPackage;
                     packageRegistry.put(nsURI, ePackage);
                     break;
                   }
                 }
               }
               return ePackage;
             }
           });
       }
    }

    createModelGen();

    if (!resourceSet.getResources().isEmpty())
    {
      Resource resource = resourceSet.getResources().get(0);
      if (isReflective && resource instanceof XMLResource  && EcoreUtil.getObjectByType(resource.getContents(), EcorePackage.Literals.EPACKAGE) == null)
      {
        ((XMLResource)resource).getDefaultSaveOptions().put(XMLResource.OPTION_LINE_WIDTH, 10);
      }
      for (Iterator<EObject> i = resource.getAllContents(); i.hasNext(); )
      {
        EObject eObject = i.next();
        if (eObject instanceof ETypeParameter || eObject instanceof EGenericType && !((EGenericType)eObject).getETypeArguments().isEmpty())
        {
          ((EcoreActionBarContributor)getActionBarContributor()).showGenerics(true);
          break;
        }
      }
    }
  }

  /**
   * Returns a diagnostic describing the errors and warnings listed in the resource
   * and the specified exception (if any).
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public Diagnostic analyzeResourceProblems(Resource resource, Exception exception) 
  {
    boolean hasErrors = !resource.getErrors().isEmpty();
    if (hasErrors || !resource.getWarnings().isEmpty())
    {
      BasicDiagnostic basicDiagnostic =
        new BasicDiagnostic
          (hasErrors ? Diagnostic.ERROR : Diagnostic.WARNING,
           "org.eclipse.emf.ecore.editor",
           0,
           getString("_UI_CreateModelError_message", resource.getURI()),
           new Object [] { exception == null ? (Object)resource : exception });
      basicDiagnostic.merge(EcoreUtil.computeDiagnostic(resource, true));
      return basicDiagnostic;
    }
    else if (exception != null)
    {
      return
        new BasicDiagnostic
          (Diagnostic.ERROR,
           "org.eclipse.emf.ecore.editor",
           0,
           getString("_UI_CreateModelError_message", resource.getURI()),
           new Object[] { exception });
    }
    else
    {
      return Diagnostic.OK_INSTANCE;
    }
  }

  /**
   * This is the method used by the framework to install your own controls.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void createPages()
  {
    // Creates the model from the editor input
    //
    createModel();

    // Only creates the other pages if there is something that can be edited
    //
    if (!getEditingDomain().getResourceSet().getResources().isEmpty())
    {
      // Create a page for the selection tree view.
      //
      Tree tree = new Tree(getContainer(), SWT.MULTI);
      selectionViewer = new TreeViewer(tree);
      setCurrentViewer(selectionViewer);

      selectionViewer.setUseHashlookup(true);
      selectionViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
      selectionViewer.setLabelProvider(new DecoratingColumLabelProvider(new AdapterFactoryLabelProvider(adapterFactory), new DiagnosticDecorator(editingDomain, selectionViewer, EcoreEditorPlugin.getPlugin().getDialogSettings())));
      selectionViewer.setInput(editingDomain.getResourceSet());
      selectionViewer.setSelection(new StructuredSelection(editingDomain.getResourceSet().getResources().get(0)), true);

      new AdapterFactoryTreeEditor(selectionViewer.getTree(), adapterFactory);
      new ColumnViewerInformationControlToolTipSupport(selectionViewer, new DiagnosticDecorator.EditingDomainLocationListener(editingDomain, selectionViewer));

      createContextMenuFor(selectionViewer);
      int pageIndex = addPage(tree);
      setPageText(pageIndex, getString("_UI_SelectionPage_label"));

      getSite().getShell().getDisplay().asyncExec
        (new Runnable()
         {
           public void run()
           {
             if (!getContainer().isDisposed())
             {
               setActivePage(0);
             }
           }
         });
    }

    // Ensures that this editor will only display the page's tab
    // area if there are more than one page
    //
    getContainer().addControlListener
      (new ControlAdapter()
       {
        boolean guard = false;
        @Override
        public void controlResized(ControlEvent event)
        {
          if (!guard)
          {
            guard = true;
            hideTabs();
            guard = false;
          }
        }
       });

    getSite().getShell().getDisplay().asyncExec
      (new Runnable()
       {
         public void run()
         {
           updateProblemIndication();
         }
       });
  }

  /**
   * If there is just one page in the multi-page editor part,
   * this hides the single tab at the bottom.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected void hideTabs()
  {
    if (getPageCount() <= 1)
    {
      setPageText(0, "");
      if (getContainer() instanceof CTabFolder)
      {
        Point point = getContainer().getSize();
        Rectangle clientArea = getContainer().getClientArea();
        getContainer().setSize(point.x,  2 * point.y - clientArea.height - clientArea.y);
      }
    }
  }

  /**
   * If there is more than one page in the multi-page editor part,
   * this shows the tabs at the bottom.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected void showTabs()
  {
    if (getPageCount() > 1)
    {
      setPageText(0, getString("_UI_SelectionPage_label"));
      if (getContainer() instanceof CTabFolder)
      {
        Point point = getContainer().getSize();
        Rectangle clientArea = getContainer().getClientArea();
        getContainer().setSize(point.x, clientArea.height + clientArea.y);
      }
    }
  }

  /**
   * This is used to track the active viewer.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected void pageChange(int pageIndex)
  {
    super.pageChange(pageIndex);

    if (contentOutlinePage != null)
    {
      handleContentOutlineSelection(contentOutlinePage.getSelection());
    }
  }

  /**
   * This is how the framework determines which interfaces we implement.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public Object getAdapter(Class key)
  {
    if (key.equals(IContentOutlinePage.class))
    {
      return showOutlineView() ? getContentOutlinePage() : null;
    }
    else if (key.equals(IPropertySheetPage.class))
    {
      return getPropertySheetPage();
    }
    else if (key.equals(IGotoMarker.class))
    {
      return this;
    }
    else if (key.equals(IFindReplaceTarget.class))
    {
      return FindAndReplaceTarget.getAdapter(key, this, EcoreEditorPlugin.getPlugin());
    }
    else if ("org.eclipse.ui.texteditor.ITextEditor".equals(key.getName()))
    {
      // WTP registers a property tester that tries to get this adapter even when closing the workbench 
      // at which point the multi-page editor is already disposed and throws an exception.
      // Of course the Ecore editor can never be adapted to a text editor, so we can just always return null.
      //
      return null;
    }
    else
    {
      return super.getAdapter(key);
    }
  }

  /**
   * This accesses a cached version of the content outliner.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IContentOutlinePage getContentOutlinePage()
  {
    if (contentOutlinePage == null)
    {
      // The content outline is just a tree.
      //
      class MyContentOutlinePage extends ContentOutlinePage
      {
        @Override
        public void createControl(Composite parent)
        {
          super.createControl(parent);
          contentOutlineViewer = getTreeViewer();
          contentOutlineViewer.addSelectionChangedListener(this);

          // Set up the tree viewer.
          //
          contentOutlineViewer.setUseHashlookup(true);
          contentOutlineViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
          contentOutlineViewer.setLabelProvider(new DecoratingColumLabelProvider(new AdapterFactoryLabelProvider(adapterFactory), new DiagnosticDecorator(editingDomain, contentOutlineViewer, EcoreEditorPlugin.getPlugin().getDialogSettings())));
          contentOutlineViewer.setInput(editingDomain.getResourceSet());

          new ColumnViewerInformationControlToolTipSupport(contentOutlineViewer, new DiagnosticDecorator.EditingDomainLocationListener(editingDomain, contentOutlineViewer));

          // Make sure our popups work.
          //
          createContextMenuFor(contentOutlineViewer);

          if (!editingDomain.getResourceSet().getResources().isEmpty())
          {
            // Select the root object in the view.
            //
            contentOutlineViewer.setSelection(new StructuredSelection(editingDomain.getResourceSet().getResources().get(0)), true);
          }
        }

        @Override
        public void makeContributions(IMenuManager menuManager, IToolBarManager toolBarManager, IStatusLineManager statusLineManager)
        {
          super.makeContributions(menuManager, toolBarManager, statusLineManager);
          contentOutlineStatusLineManager = statusLineManager;
        }

        @Override
        public void setActionBars(IActionBars actionBars)
        {
          super.setActionBars(actionBars);
          getActionBarContributor().shareGlobalActions(this, actionBars);
        }
      }

      contentOutlinePage = new MyContentOutlinePage();

      // Listen to selection so that we can handle it is a special way.
      //
      contentOutlinePage.addSelectionChangedListener
        (new ISelectionChangedListener()
         {
           // This ensures that we handle selections correctly.
           //
           public void selectionChanged(SelectionChangedEvent event)
           {
             handleContentOutlineSelection(event.getSelection());
           }
         });
    }

    return contentOutlinePage;
  }

  /**
   * This accesses a cached version of the property sheet.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IPropertySheetPage getPropertySheetPage()
  {
    PropertySheetPage propertySheetPage =
      new ExtendedPropertySheetPage(editingDomain, ExtendedPropertySheetPage.Decoration.LIVE, EcoreEditorPlugin.getPlugin().getDialogSettings(), 10, true)
      {
        @Override
        public void setSelectionToViewer(List<?> selection)
        {
          EcoreEditor.this.setSelectionToViewer(selection);
          EcoreEditor.this.setFocus();
        }

        @Override
        public void setActionBars(IActionBars actionBars)
        {
          super.setActionBars(actionBars);
          getActionBarContributor().shareGlobalActions(this, actionBars);
        }
      };
    propertySheetPage.setPropertySourceProvider(new AdapterFactoryContentProvider(adapterFactory));
    propertySheetPages.add(propertySheetPage);

    return propertySheetPage;
  }

  /**
   * This deals with how we want selection in the outliner to affect the other views.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void handleContentOutlineSelection(ISelection selection)
  {
    if (selectionViewer != null && !selection.isEmpty() && selection instanceof IStructuredSelection)
    {
      Iterator<?> selectedElements = ((IStructuredSelection)selection).iterator();
      if (selectedElements.hasNext())
      {
        // Get the first selected element.
        //
        Object selectedElement = selectedElements.next();

        ArrayList<Object> selectionList = new ArrayList<Object>();
        selectionList.add(selectedElement);
        while (selectedElements.hasNext())
        {
          selectionList.add(selectedElements.next());
        }

        // Set the selection to the widget.
        //
        selectionViewer.setSelection(new StructuredSelection(selectionList));
      }
    }
  }

  /**
   * This is for implementing {@link IEditorPart} and simply tests the command stack.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean isDirty()
  {
    return ((BasicCommandStack)editingDomain.getCommandStack()).isSaveNeeded();
  }

  /**
   * This is for implementing {@link IRevertablePart}.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void doRevert()
  {
    DiagnosticDecorator.cancel(editingDomain);
    
    ResourceSet resourceSet = editingDomain.getResourceSet();
    List<Resource> resources = resourceSet.getResources();
    List<Resource> unloadedResources = new ArrayList<Resource>();
    updateProblemIndication = false;
    for (int i = 0; i < resources.size(); ++i)
    {
      Resource resource = resources.get(i);
      if (resource.isLoaded())
      {
        resource.unload();
        unloadedResources.add(resource);
      }
    }

    resourceToDiagnosticMap.clear();
    for (Resource resource : unloadedResources)
    {
      try
      {
        resource.load(resourceSet.getLoadOptions());
      }
      catch (IOException exception)
      {
        if (!resourceToDiagnosticMap.containsKey(resource))
        {
          resourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
        }
      }
    }

    editingDomain.getCommandStack().flush();

    if (AdapterFactoryEditingDomain.isStale(editorSelection))
    {
      setSelection(StructuredSelection.EMPTY);
    }

    updateProblemIndication = true;
    updateProblemIndication();
  }

  /**
   * This is for implementing {@link IEditorPart} and simply saves the model file.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void doSave(IProgressMonitor progressMonitor)
  {
    // Save only resources that have actually changed.
    //
    final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
    saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
    saveOptions.put(Resource.OPTION_LINE_DELIMITER, Resource.OPTION_LINE_DELIMITER_UNSPECIFIED);

    // Do the work within an operation because this is a long running activity that modifies the workbench.
    //
    WorkspaceModifyOperation operation =
      new WorkspaceModifyOperation()
      {
        // This is the method that gets invoked when the operation runs.
        //
        @Override
        public void execute(IProgressMonitor monitor)
        {
          // Save the resources to the file system.
          //
          boolean first = true;
          List<Resource> resources = editingDomain.getResourceSet().getResources();
          for (int i = 0; i < resources.size(); ++i)
          {
            Resource resource = resources.get(i);
            if ((first || !resource.getContents().isEmpty() || isPersisted(resource)) && !editingDomain.isReadOnly(resource))
            {
              try
              {
                long timeStamp = resource.getTimeStamp();
                resource.save(saveOptions);
                if (resource.getTimeStamp() != timeStamp)
                {
                  savedResources.add(resource);
                }
              }
              catch (Exception exception)
              {
                resourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
              }
              first = false;
            }
          }
        }
      };

    updateProblemIndication = false;
    try
    {
      // This runs the options, and shows progress.
      //
      new ProgressMonitorDialog(getSite().getShell()).run(true, false, operation);

      // Refresh the necessary state.
      //
      ((BasicCommandStack)editingDomain.getCommandStack()).saveIsDone();
      firePropertyChange(IEditorPart.PROP_DIRTY);
    }
    catch (Exception exception)
    {
      // Something went wrong that shouldn't.
      //
      EcoreEditorPlugin.INSTANCE.log(exception);
    }
    updateProblemIndication = true;
    updateProblemIndication();
  }

  /**
   * This returns whether something has been persisted to the URI of the specified resource.
   * The implementation uses the URI converter from the editor's resource set to try to open an input stream.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected boolean isPersisted(Resource resource)
  {
    boolean result = false;
    try
    {
      InputStream stream = editingDomain.getResourceSet().getURIConverter().createInputStream(resource.getURI());
      if (stream != null)
      {
        result = true;
        stream.close();
      }
    }
    catch (IOException e)
    {
      // Ignore
    }
    return result;
  }

  /**
   * This always returns true because it is not currently supported.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean isSaveAsAllowed()
  {
    return true;
  }

  public static final String ECORE_FILE_EXTENSION = "ecore";
  public static final String EMOF_FILE_EXTENSION = "emof";

  /**
   * This also changes the editor's input.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
  @Override
  public void doSaveAs()
  {
    SaveAsDialog saveAsDialog= new SaveAsDialog(getSite().getShell());
    saveAsDialog.create();
    saveAsDialog.setMessage(EcoreEditorPlugin.INSTANCE.getString("_UI_SaveAs_message"));
    saveAsDialog.open();
    IPath path= saveAsDialog.getResult();
    if (path != null)
    {
      IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
      if (file != null)
      {
        ResourceSet resourceSet = editingDomain.getResourceSet();
        Resource currentResource = resourceSet.getResources().get(0);
        String currentExtension = currentResource.getURI().fileExtension();

        URI newURI = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
        String newExtension = newURI.fileExtension();

        if (currentExtension.equals(newExtension))
        {
          currentResource.setURI(newURI);
        }
        else
        {
          // Ensure that all proxies are resolved before changing the containing resource implementation.
          EcoreUtil.resolveAll(currentResource);
          Resource newResource = resourceSet.createResource(newURI);
          newResource.getContents().addAll(currentResource.getContents());
          resourceSet.getResources().remove(0);
          resourceSet.getResources().move(0, newResource);
        }

        IFileEditorInput modelFile = new FileEditorInput(file);
        setInputWithNotify(modelFile);
        setPartName(file.getName());
        doSave(getActionBars().getStatusLineManager().getProgressMonitor());
      }
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected void doSaveAs(URI uri, IEditorInput editorInput)
  {
    (editingDomain.getResourceSet().getResources().get(0)).setURI(uri);
    setInputWithNotify(editorInput);
    setPartName(editorInput.getName());
    IProgressMonitor progressMonitor =
      getActionBars().getStatusLineManager() != null ?
        getActionBars().getStatusLineManager().getProgressMonitor() :
        new NullProgressMonitor();
    doSave(progressMonitor);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void gotoMarker(IMarker marker)
  {
    List<?> targetObjects = markerHelper.getTargetObjects(editingDomain, marker);
    if (!targetObjects.isEmpty())
    {
      setSelectionToViewer(targetObjects);
    }
  }

  /**
   * This is called during startup.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void init(IEditorSite site, IEditorInput editorInput)
  {
    setSite(site);
    setInputWithNotify(editorInput);
    setPartName(editorInput.getName());
    site.setSelectionProvider(this);
    site.getPage().addPartListener(partListener);
    ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void setFocus()
  {
    getControl(getActivePage()).setFocus();
  }

  /**
   * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void addSelectionChangedListener(ISelectionChangedListener listener)
  {
    selectionChangedListeners.add(listener);
  }

  /**
   * This implements {@link org.eclipse.jface.viewers.ISelectionProvider}.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void removeSelectionChangedListener(ISelectionChangedListener listener)
  {
    selectionChangedListeners.remove(listener);
  }

  /**
   * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to return this editor's overall selection.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public ISelection getSelection()
  {
    return editorSelection;
  }

  /**
   * This implements {@link org.eclipse.jface.viewers.ISelectionProvider} to set this editor's overall selection.
   * Calling this result will notify the listeners.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated NOT
   */
  public void setSelection(ISelection selection)
  {
    editorSelection = selection;

    for (ISelectionChangedListener listener : new ArrayList<ISelectionChangedListener>(selectionChangedListeners))
    {
      listener.selectionChanged(new SelectionChangedEvent(this, selection));
    }
    setStatusLineManager(selection);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setStatusLineManager(ISelection selection)
  {
    IStatusLineManager statusLineManager = currentViewer != null && currentViewer == contentOutlineViewer ?
      contentOutlineStatusLineManager : getActionBars().getStatusLineManager();

    if (statusLineManager != null)
    {
      if (selection instanceof IStructuredSelection)
      {
        Collection<?> collection = ((IStructuredSelection)selection).toList();
        switch (collection.size())
        {
          case 0:
          {
            statusLineManager.setMessage(getString("_UI_NoObjectSelected"));
            break;
          }
          case 1:
          {
            String text = new AdapterFactoryItemDelegator(adapterFactory).getText(collection.iterator().next());
            statusLineManager.setMessage(getString("_UI_SingleObjectSelected", text));
            break;
          }
          default:
          {
            statusLineManager.setMessage(getString("_UI_MultiObjectSelected", Integer.toString(collection.size())));
            break;
          }
        }
      }
      else
      {
        statusLineManager.setMessage("");
      }
    }
  }

  /**
   * This looks up a string in the plugin's plugin.properties file.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private static String getString(String key)
  {
    return EcoreEditorPlugin.INSTANCE.getString(key);
  }

  /**
   * This looks up a string in plugin.properties, making a substitution.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private static String getString(String key, Object s1)
  {
    return EcoreEditorPlugin.INSTANCE.getString(key, new Object [] { s1 });
  }

  /**
   * This implements {@link org.eclipse.jface.action.IMenuListener} to help fill the context menus with contributions from the Edit menu.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void menuAboutToShow(IMenuManager menuManager)
  {
    ((IMenuListener)getEditorSite().getActionBarContributor()).menuAboutToShow(menuManager);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EditingDomainActionBarContributor getActionBarContributor()
  {
    return (EditingDomainActionBarContributor)getEditorSite().getActionBarContributor();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public IActionBars getActionBars()
  {
    return getActionBarContributor().getActionBars();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public AdapterFactory getAdapterFactory()
  {
    return adapterFactory;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void dispose()
  {
    updateProblemIndication = false;

    ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);

    getSite().getPage().removePartListener(partListener);

    adapterFactory.dispose();

    if (getActionBarContributor().getActiveEditor() == this)
    {
      getActionBarContributor().setActiveEditor(null);
    }

    for (PropertySheetPage propertySheetPage : propertySheetPages)
    {
      propertySheetPage.dispose();
    }

    if (contentOutlinePage != null)
    {
      contentOutlinePage.dispose();
    }

    super.dispose();
  }

  /**
   * Returns whether the outline view should be presented to the user.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected boolean showOutlineView()
  {
    return false;
  }

  /**
   * A package registry implementation that locates and loads registered models based on references via the model's nsURI.
   */
  private static class DynamicLoadingEPackageRegistryImpl extends EPackageRegistryImpl
  {
    private static final long serialVersionUID = 1L;

    private final Map<String, EPackage> ePackageNsURItoEPackageMap = new HashMap<String, EPackage>();

    private final EList<Resource> editorResources;

    private final ResourceSet modelResourceSet;

    private Map<String, URI> ePackageNsURItoModelLocationMap;

    public DynamicLoadingEPackageRegistryImpl(ResourceSet editorResourceSet)
    {
      super(EPackage.Registry.INSTANCE);
      editorResources = editorResourceSet.getResources();
      modelResourceSet = new ResourceSetImpl();
      modelResourceSet.getURIConverter().getURIMap().putAll(editorResourceSet.getURIConverter().getURIMap());
    }

    @Override
    public EPackage getEPackage(String nsURI)
    {
      EPackage ePackage = super.getEPackage(nsURI);
      if (ePackage == null)
      {
        ePackage = ePackageNsURItoEPackageMap.get(nsURI);
      }

      if (ePackage == null)
      {
        if (ePackageNsURItoModelLocationMap == null)
        {
          ePackageNsURItoModelLocationMap = new HashMap<String, URI>(EcorePlugin.getEPackageNsURIToGenModelLocationMap(true));
          ePackageNsURItoModelLocationMap.putAll(EcorePlugin.getEPackageNsURIToDynamicModelLocationMap(true));
        }

        URI location = ePackageNsURItoModelLocationMap.get(nsURI);
        if (location != null)
        {
          EList<Resource> modelResources = modelResourceSet.getResources();
          if (modelResources.isEmpty())
          {
            // To support Xcore resources, we need a resource with a URI that helps determine the containing project.
            //
            modelResourceSet.createResource(editorResources.get(0).getURI());
          }

          try
          {
            // Load the model location in this separate model resource set and resolve all proxies to ensure that any referenced Ecore models are loaded.
            Resource modelResource = modelResourceSet.getResource(location, true);
            EcoreUtil.resolveAll(modelResource);

            // Look for an EPackage in all the resources.
            //
            EPackage.Registry packageRegistry = modelResourceSet.getPackageRegistry();
            for (Resource resource : modelResources)
            {
              for (TreeIterator<?> i = new EcoreUtil.ContentTreeIterator<Object>(resource.getContents())
                {
                  private static final long serialVersionUID = 1L;

                  @Override
                  protected Iterator<? extends EObject> getEObjectChildren(EObject eObject)
                  {
                    return eObject instanceof EPackage ? ((EPackage)eObject).getESubpackages().iterator() : Collections.<EObject> emptyList().iterator();
                  }
                };i.hasNext();)
              {
                Object content = i.next();
                if (content instanceof EPackage)
                {
                  // Register the nsUR of each EPackage in the model resource set's package registry, and in our ePackageNsURItoEPackageMap,
                  // but only if it's not already registered there.
                  //
                  EPackage candidate = (EPackage)content;
                  String candidateNsURI = candidate.getNsURI();
                  if (super.getEPackage(candidateNsURI) == null)
                  {
                    put(candidateNsURI, candidate);
                    packageRegistry.put(candidateNsURI, candidate);
                    ePackageNsURItoEPackageMap.put(candidateNsURI, candidate);

                    // Also register the resource's actual URI in the package registry and change the resource's URI to the nsURI.
                    // This ensures that the resource can still be found via the original URI and even if the resource is moved to the editor's resource set.
                    //
                    Resource candidateResource = candidate.eResource();
                    if (candidateResource != null)
                    {
                      String uri = candidateResource.getURI().toString();
                      if (!packageRegistry.containsKey(uri))
                      {
                        packageRegistry.put(uri, candidate);
                        candidateResource.setURI(URI.createURI(candidateNsURI));
                      }
                    }
                  }
                }
              }
            }

            ePackage = ePackageNsURItoEPackageMap.get(nsURI);
            if (ePackage != null)
            {
              Resource ePackageResource = ePackage.eResource();
              if (ePackageResource != null && !editorResources.contains(ePackageResource))
              {
                // Move the resource to the editor's resource set so that it's visible.
                //
                editorResources.add(ePackageResource);
              }
            }
          }
          catch (RuntimeException exception)
          {
            EcoreEditorPlugin.INSTANCE.log(exception);
          }
        }
      }

      return ePackage;
    }
  }
}
