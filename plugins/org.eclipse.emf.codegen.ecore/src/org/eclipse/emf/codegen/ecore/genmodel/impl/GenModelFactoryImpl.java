/**
 * Copyright (c) 2002-2010 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * 
 * Contributors: 
 *   IBM - Initial API and implementation
 */
package org.eclipse.emf.codegen.ecore.genmodel.impl;


import org.eclipse.emf.codegen.ecore.genmodel.*;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;


import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GenModelFactoryImpl extends EFactoryImpl implements GenModelFactory
{
  /**
   * Creates the default factory implementation.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public static GenModelFactory init()
  {
    try
    {
      GenModelFactory theGenModelFactory = (GenModelFactory)EPackage.Registry.INSTANCE.getEFactory(GenModelPackage.eNS_URI);
      if (theGenModelFactory != null)
      {
        return theGenModelFactory;
      }
    }
    catch (Exception exception)
    {
      EcorePlugin.INSTANCE.log(exception);
    }
    return new GenModelFactoryImpl();
  }

  /**
   * Creates an instance of the factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenModelFactoryImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public EObject create(EClass eClass)
  {
    switch (eClass.getClassifierID())
    {
      case GenModelPackage.GEN_MODEL: return createGenModel();
      case GenModelPackage.GEN_PACKAGE: return createGenPackage();
      case GenModelPackage.GEN_CLASS: return createGenClass();
      case GenModelPackage.GEN_FEATURE: return createGenFeature();
      case GenModelPackage.GEN_ENUM: return createGenEnum();
      case GenModelPackage.GEN_ENUM_LITERAL: return createGenEnumLiteral();
      case GenModelPackage.GEN_DATA_TYPE: return createGenDataType();
      case GenModelPackage.GEN_OPERATION: return createGenOperation();
      case GenModelPackage.GEN_PARAMETER: return createGenParameter();
      case GenModelPackage.GEN_ANNOTATION: return createGenAnnotation();
      case GenModelPackage.GEN_TYPE_PARAMETER: return createGenTypeParameter();
      default:
        throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object createFromString(EDataType eDataType, String initialValue)
  {
    switch (eDataType.getClassifierID())
    {
      case GenModelPackage.GEN_PROVIDER_KIND:
        return createGenProviderKindFromString(eDataType, initialValue);
      case GenModelPackage.GEN_PROPERTY_KIND:
        return createGenPropertyKindFromString(eDataType, initialValue);
      case GenModelPackage.GEN_RESOURCE_KIND:
        return createGenResourceKindFromString(eDataType, initialValue);
      case GenModelPackage.GEN_DELEGATION_KIND:
        return createGenDelegationKindFromString(eDataType, initialValue);
      case GenModelPackage.GEN_JDK_LEVEL:
        return createGenJDKLevelFromString(eDataType, initialValue);
      case GenModelPackage.GEN_RUNTIME_VERSION:
        return createGenRuntimeVersionFromString(eDataType, initialValue);
      case GenModelPackage.GEN_RUNTIME_PLATFORM:
        return createGenRuntimePlatformFromString(eDataType, initialValue);
      case GenModelPackage.GEN_DECORATION:
        return createGenDecorationFromString(eDataType, initialValue);
      case GenModelPackage.GEN_ECLIPSE_PLATFORM_VERSION:
        return createGenEclipsePlatformVersionFromString(eDataType, initialValue);
      case GenModelPackage.GEN_CODE_STYLE:
        return createGenCodeStyleFromString(eDataType, initialValue);
      case GenModelPackage.GEN_OS_GI_STYLE:
        return createGenOSGiStyleFromString(eDataType, initialValue);
      case GenModelPackage.PATH:
        return createPathFromString(eDataType, initialValue);
      case GenModelPackage.PROPERTY_EDITOR_FACTORY:
        return createPropertyEditorFactoryFromString(eDataType, initialValue);
      default:
        throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String convertToString(EDataType eDataType, Object instanceValue)
  {
    switch (eDataType.getClassifierID())
    {
      case GenModelPackage.GEN_PROVIDER_KIND:
        return convertGenProviderKindToString(eDataType, instanceValue);
      case GenModelPackage.GEN_PROPERTY_KIND:
        return convertGenPropertyKindToString(eDataType, instanceValue);
      case GenModelPackage.GEN_RESOURCE_KIND:
        return convertGenResourceKindToString(eDataType, instanceValue);
      case GenModelPackage.GEN_DELEGATION_KIND:
        return convertGenDelegationKindToString(eDataType, instanceValue);
      case GenModelPackage.GEN_JDK_LEVEL:
        return convertGenJDKLevelToString(eDataType, instanceValue);
      case GenModelPackage.GEN_RUNTIME_VERSION:
        return convertGenRuntimeVersionToString(eDataType, instanceValue);
      case GenModelPackage.GEN_RUNTIME_PLATFORM:
        return convertGenRuntimePlatformToString(eDataType, instanceValue);
      case GenModelPackage.GEN_DECORATION:
        return convertGenDecorationToString(eDataType, instanceValue);
      case GenModelPackage.GEN_ECLIPSE_PLATFORM_VERSION:
        return convertGenEclipsePlatformVersionToString(eDataType, instanceValue);
      case GenModelPackage.GEN_CODE_STYLE:
        return convertGenCodeStyleToString(eDataType, instanceValue);
      case GenModelPackage.GEN_OS_GI_STYLE:
        return convertGenOSGiStyleToString(eDataType, instanceValue);
      case GenModelPackage.PATH:
        return convertPathToString(eDataType, instanceValue);
      case GenModelPackage.PROPERTY_EDITOR_FACTORY:
        return convertPropertyEditorFactoryToString(eDataType, instanceValue);
      default:
        throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
    }
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenModel createGenModel()
  {
    GenModelImpl genModel = new GenModelImpl();
    return genModel;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenPackage createGenPackage()
  {
    GenPackageImpl genPackage = new GenPackageImpl();
    return genPackage;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenClass createGenClass()
  {
    GenClassImpl genClass = new GenClassImpl();
    return genClass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenFeature createGenFeature()
  {
    GenFeatureImpl genFeature = new GenFeatureImpl();
    return genFeature;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenEnum createGenEnum()
  {
    GenEnumImpl genEnum = new GenEnumImpl();
    return genEnum;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenEnumLiteral createGenEnumLiteral()
  {
    GenEnumLiteralImpl genEnumLiteral = new GenEnumLiteralImpl();
    return genEnumLiteral;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenDataType createGenDataType()
  {
    GenDataTypeImpl genDataType = new GenDataTypeImpl();
    return genDataType;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenOperation createGenOperation()
  {
    GenOperationImpl genOperation = new GenOperationImpl();
    return genOperation;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenParameter createGenParameter()
  {
    GenParameterImpl genParameter = new GenParameterImpl();
    return genParameter;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenAnnotation createGenAnnotation()
  {
    GenAnnotationImpl genAnnotation = new GenAnnotationImpl();
    return genAnnotation;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenTypeParameter createGenTypeParameter()
  {
    GenTypeParameterImpl genTypeParameter = new GenTypeParameterImpl();
    return genTypeParameter;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenProviderKind createGenProviderKindFromString(EDataType eDataType, String initialValue)
  {
    GenProviderKind result = GenProviderKind.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertGenProviderKindToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenPropertyKind createGenPropertyKindFromString(EDataType eDataType, String initialValue)
  {
    GenPropertyKind result = GenPropertyKind.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertGenPropertyKindToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenResourceKind createGenResourceKindFromString(EDataType eDataType, String initialValue)
  {
    GenResourceKind result = GenResourceKind.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertGenResourceKindToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenDelegationKind createGenDelegationKindFromString(EDataType eDataType, String initialValue)
  {
    GenDelegationKind result = GenDelegationKind.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertGenDelegationKindToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenJDKLevel createGenJDKLevelFromString(EDataType eDataType, String initialValue)
  {
    GenJDKLevel result = GenJDKLevel.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertGenJDKLevelToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenRuntimeVersion createGenRuntimeVersionFromString(EDataType eDataType, String initialValue)
  {
    GenRuntimeVersion result = GenRuntimeVersion.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertGenRuntimeVersionToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenRuntimePlatform createGenRuntimePlatformFromString(EDataType eDataType, String initialValue)
  {
    GenRuntimePlatform result = GenRuntimePlatform.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertGenRuntimePlatformToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenDecoration createGenDecorationFromString(EDataType eDataType, String initialValue)
  {
    GenDecoration result = GenDecoration.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String convertGenDecorationToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @since 2.14
   * @generated
   */
  public GenEclipsePlatformVersion createGenEclipsePlatformVersionFromString(EDataType eDataType, String initialValue)
  {
    GenEclipsePlatformVersion result = GenEclipsePlatformVersion.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @since 2.14
   * @generated
   */
  public String convertGenEclipsePlatformVersionToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @since 2.19
   * @generated
   */
  public GenCodeStyle createGenCodeStyleFromString(EDataType eDataType, String initialValue)
  {
    GenCodeStyle result = GenCodeStyle.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @since 2.19
   * @generated
   */
  public String convertGenCodeStyleToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @since 2.33
   * @generated
   */
  public GenOSGiStyle createGenOSGiStyleFromString(EDataType eDataType, String initialValue)
  {
    GenOSGiStyle result = GenOSGiStyle.get(initialValue);
    if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
    return result;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @since 2.33
   * @generated
   */
  public String convertGenOSGiStyleToString(EDataType eDataType, Object instanceValue)
  {
    return instanceValue == null ? null : instanceValue.toString();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @since 2.14
   * @generated NOT
   */
  public String createPathFromString(EDataType eDataType, String initialValue)
  {
    return initialValue;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @since 2.14
   * @generated NOT
   */
  public String convertPathToString(EDataType eDataType, Object instanceValue)
  {
    return (String)instanceValue;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @since 2.14
   * @generated
   */
  public String createPropertyEditorFactoryFromString(EDataType eDataType, String initialValue)
  {
    return (String)super.createFromString(eDataType, initialValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @since 2.14
   * @generated
   */
  public String convertPropertyEditorFactoryToString(EDataType eDataType, Object instanceValue)
  {
    return super.convertToString(eDataType, instanceValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public GenModelPackage getGenModelPackage()
  {
    return (GenModelPackage)getEPackage();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @deprecated
   * @generated
   */
  @Deprecated
  public static GenModelPackage getPackage()
  {
    return GenModelPackage.eINSTANCE;
  }

} //GenModelFactoryImpl
