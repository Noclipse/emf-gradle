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
package org.eclipse.emf.mapping.xsd2ecore.impl;


import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EPackageImpl;
import org.eclipse.emf.mapping.MappingPackage;
import org.eclipse.emf.mapping.xsd2ecore.XSD2EcoreFactory;
import org.eclipse.emf.mapping.xsd2ecore.XSD2EcoreMappingRoot;
import org.eclipse.emf.mapping.xsd2ecore.XSD2EcorePackage;


/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class XSD2EcorePackageImpl extends EPackageImpl implements XSD2EcorePackage
{
  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private EClass xsd2EcoreMappingRootEClass = null;

  /**
   * Creates an instance of the model <b>Package</b>, registered with
   * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
   * package URI value.
   * <p>Note: the correct way to create the package is via the static
   * factory method {@link #init init()}, which also performs
   * initialization of the package, or returns the registered package,
   * if one already exists.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see org.eclipse.emf.ecore.EPackage.Registry
   * @see org.eclipse.emf.mapping.xsd2ecore.XSD2EcorePackage#eNS_URI
   * @see #init()
   * @generated
   */
  private XSD2EcorePackageImpl()
  {
    super(eNS_URI, XSD2EcoreFactory.eINSTANCE);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private static boolean isInited = false;

  /**
   * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
   * 
   * <p>This method is used to initialize {@link XSD2EcorePackage#eINSTANCE} when that field is accessed.
   * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #eNS_URI
   * @see #createPackageContents()
   * @see #initializePackageContents()
   * @generated
   */
  public static XSD2EcorePackage init()
  {
    if (isInited) return (XSD2EcorePackage)EPackage.Registry.INSTANCE.getEPackage(XSD2EcorePackage.eNS_URI);

    // Obtain or create and register package
    XSD2EcorePackageImpl theXSD2EcorePackage = (XSD2EcorePackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof XSD2EcorePackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new XSD2EcorePackageImpl());

    isInited = true;

    // Initialize simple dependencies
    MappingPackage.eINSTANCE.eClass();

    // Create package meta-data objects
    theXSD2EcorePackage.createPackageContents();

    // Initialize created meta-data
    theXSD2EcorePackage.initializePackageContents();

    // Mark meta-data to indicate it can't be changed
    theXSD2EcorePackage.freeze();

  
    // Update the registry and return the package
    EPackage.Registry.INSTANCE.put(XSD2EcorePackage.eNS_URI, theXSD2EcorePackage);
    return theXSD2EcorePackage;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public EClass getXSD2EcoreMappingRoot()
  {
    return xsd2EcoreMappingRootEClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public XSD2EcoreFactory getXSD2EcoreFactory()
  {
    return (XSD2EcoreFactory)getEFactoryInstance();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private boolean isCreated = false;

  /**
   * Creates the meta-model objects for the package.  This method is
   * guarded to have no affect on any invocation but its first.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void createPackageContents()
  {
    if (isCreated) return;
    isCreated = true;

    // Create classes and their features
    xsd2EcoreMappingRootEClass = createEClass(XSD2_ECORE_MAPPING_ROOT);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  private boolean isInitialized = false;

  /**
   * Complete the initialization of the package and its meta-model.  This
   * method is guarded to have no affect on any invocation but its first.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void initializePackageContents()
  {
    if (isInitialized) return;
    isInitialized = true;

    // Initialize package
    setName(eNAME);
    setNsPrefix(eNS_PREFIX);
    setNsURI(eNS_URI);

    // Obtain other dependent packages
    MappingPackage theMappingPackage = (MappingPackage)EPackage.Registry.INSTANCE.getEPackage(MappingPackage.eNS_URI);

    // Create type parameters

    // Set bounds for type parameters

    // Add supertypes to classes
    xsd2EcoreMappingRootEClass.getESuperTypes().add(theMappingPackage.getMappingRoot());

    // Initialize classes and features; add operations and parameters
    initEClass(xsd2EcoreMappingRootEClass, XSD2EcoreMappingRoot.class, "XSD2EcoreMappingRoot", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

    // Create resource
    createResource(eNS_URI);
  }

} //XSD2EcorePackageImpl
