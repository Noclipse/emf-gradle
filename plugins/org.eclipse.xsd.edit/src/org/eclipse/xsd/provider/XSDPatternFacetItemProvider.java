/**
 * Copyright (c) 2002-2006 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: 
 *   IBM - Initial API and implementation
 */
package org.eclipse.xsd.provider;


import java.util.List;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;

import org.eclipse.xsd.XSDPatternFacet;


/**
 * This is the item provider adpater for a {@link org.eclipse.xsd.XSDPatternFacet} object.
 */
public class XSDPatternFacetItemProvider
  extends XSDRepeatableFacetItemProvider
  implements 
    IEditingDomainItemProvider,
    IStructuredItemContentProvider, 
    ITreeItemContentProvider, 
    IItemLabelProvider, 
    IItemPropertySource
{
  /**
   * This constructs an instance from a factory and a notifier.
   */
  public XSDPatternFacetItemProvider(AdapterFactory adapterFactory)
  {
    super(adapterFactory);
  }

  /**
   * This returns the property descriptors for the adapted class.
   */
  @Override
  public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object)
  {
    if (itemPropertyDescriptors == null)
    {
      super.getPropertyDescriptors(object);

      // This is for the value feature.
      //
      itemPropertyDescriptors.add
        (new ItemPropertyDescriptor
          (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
           XSDEditPlugin.INSTANCE.getString("_UI_Value_label"),
           XSDEditPlugin.INSTANCE.getString("_UI_ValuePatternFacet_description"),
           xsdPackage.getXSDPatternFacet_Value(),
           false,
           ItemPropertyDescriptor.TEXT_VALUE_IMAGE));

    }
    return itemPropertyDescriptors;
  }

  /**
   * This returns XSDPatternFacet.gif.
   */
  @Override
  public Object getImage(Object object)
  {
    return XSDEditPlugin.INSTANCE.getImage("full/obj16/XSDPatternFacet");
  }

  @Override
  public String getText(Object object)
  {
    XSDPatternFacet xsdPatternFacet = ((XSDPatternFacet)object);
    String result = xsdPatternFacet.getLexicalValue();
    return result == null ? "" : result;
  }

  /**
   * This handles notification by calling {@link #fireNotifyChanged fireNotifyChanged}.
   */
  @Override
  public void notifyChanged(Notification msg) 
  {
    if (msg.getFeature() == xsdPackage.getXSDFacet_LexicalValue())
    {
      fireNotifyChanged(msg);
      return;
    }
    super.notifyChanged(msg);
  }

}
