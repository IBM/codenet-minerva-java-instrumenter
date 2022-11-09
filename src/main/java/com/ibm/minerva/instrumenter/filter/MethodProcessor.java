/******************************************************************************* 
 * Copyright (c) contributors to the Minerva for Modernization project.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/

package com.ibm.minerva.instrumenter.filter;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.SignatureAttribute.MethodSignature;

public interface MethodProcessor {
    
    public ClassProcessor getClassProcessor();
    public CtBehavior getCtBehavior();
    
    public default String getMethodName() {
        final CtBehavior ctBehavior = getCtBehavior();
        final String methodName = getCtBehavior().getName();
        // Handle the special case of a constructor of an inner class.
        // Return the inner most class name.
        if (ctBehavior.getMethodInfo().isConstructor()) {
            final ClassProcessor classProcessor = getClassProcessor();
            if (classProcessor.isNestedClass()) {
                final int idx = methodName.lastIndexOf('$');
                if (idx >= 0) {
                    return methodName.substring(idx + 1);
                }
            }
        }
        return methodName;
    }
    
    public default CtClass[] getParameterTypes() throws NotFoundException {
        final CtBehavior ctBehavior = getCtBehavior();
        final CtClass[] params = ctBehavior.getParameterTypes();
        // Handle the special case of a constructor of a non-static inner class.
        // Ignore the implicit outer class parameter.
        if (ctBehavior.getMethodInfo().isConstructor()) {
            final ClassProcessor classProcessor = getClassProcessor();
            if (classProcessor.isNestedClass() && !classProcessor.isStaticClass()) {
                if (params != null && params.length > 0) {
                    if (params.length > 1) {
                        final CtClass[] modifiedParams = new CtClass[params.length - 1];
                        System.arraycopy(params, 1, modifiedParams, 0, modifiedParams.length);
                        return modifiedParams;
                    }
                    return null;
                }
            }
        }
        return params;
    }
    
    public default SignatureAttribute.Type[] getGenericParameterTypes() {
        final CtBehavior ctBehavior = getCtBehavior();
        final String genericSig = ctBehavior.getGenericSignature();
        if (genericSig != null) {
            try {
                final MethodSignature ms = SignatureAttribute.toMethodSignature(genericSig);
                final SignatureAttribute.Type[] paramTypes = ms.getParameterTypes();
                final int paramTypesLen = paramTypes != null ? paramTypes.length : 0;
                final CtClass[] params = ctBehavior.getParameterTypes();
                final int paramsLen = params != null ? params.length : 0;
                // This should always be true, but guards against a possible defect in Javassist.
                if (paramsLen == paramTypesLen) {
                    // Handle the special case of a constructor of a non-static inner class.
                    // Ignore the implicit outer class parameter.
                    if (ctBehavior.getMethodInfo().isConstructor()) {
                        final ClassProcessor classProcessor = getClassProcessor();
                        if (classProcessor.isNestedClass() && !classProcessor.isStaticClass()) {
                            if (paramTypesLen > 0) {
                                if (paramTypesLen > 1) {
                                    final SignatureAttribute.Type[] modParamTypes = new SignatureAttribute.Type[paramTypesLen - 1];
                                    System.arraycopy(paramTypes, 1, modParamTypes, 0, modParamTypes.length);
                                    return modParamTypes;
                                }
                                return null;
                            }
                        }
                    }
                    return paramTypes;
                }
            }
            catch (Exception e) {
                // TODO: Add logging message.
            }
        }
        return null;
    }
    
    public static boolean isAbstractOrNative(CtBehavior ctBehavior) {
        final int modifiers = ctBehavior.getModifiers();
        return Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers);
    }
    
    public default String getMethodSignature() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getMethodName());
        sb.append('(');
        try {
            final CtClass[] params = getParameterTypes();
            boolean paramProcessed = false;
            if (params != null && params.length > 0) {
                for (CtClass param : params) {
                    if (paramProcessed) {
                        sb.append(", ");
                    }
                    sb.append(param.getSimpleName().replace('$', '.'));
                    paramProcessed = true;
                }
            }
        } catch (NotFoundException e) {}
        sb.append(')');
        return sb.toString();
    }
}
