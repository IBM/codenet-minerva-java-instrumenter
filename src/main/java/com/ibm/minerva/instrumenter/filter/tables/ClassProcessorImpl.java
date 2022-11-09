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

package com.ibm.minerva.instrumenter.filter.tables;

import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;
import com.ibm.minerva.instrumenter.filter.MethodProcessor;
import com.ibm.minerva.instrumenter.filter.pkg.MethodProcessorImpl;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.SignatureAttribute;

public class ClassProcessorImpl implements ClassProcessor {
    
    private static final String SYM_TABLE_FILE_NAME = "file";
    private static final String SYM_TABLE_FUNCSIG_NAME = "funcSig";
    private static final String SYM_TABLE_FUNCTION_LIST = "funcL";
    private static final String SYM_TABLE_FUNCTION_ARGS = "Args";
    private static final String SYM_TABLE_FUNCTION_ARG_TYPE = "Type";
    private static final String SYM_TABLE_FUNCTION_OBJ_SIGNATURE = "signature";
    
    private final ApplicationProcessor appProcessor;
    private final CtClass ctClass;
    private final String symTableKey;
    private final JsonObject symTableClassObject;
    
    public ClassProcessorImpl(ApplicationProcessor appProcessor, CtClass ctClass, String symTableKey, JsonObject symTableClassObject) {
        this.appProcessor = appProcessor;
        this.ctClass = ctClass;
        this.symTableKey = symTableKey;
        this.symTableClassObject = symTableClassObject;
    }
    
    @Override
    public ApplicationProcessor getApplicationProcessor() {
        return appProcessor;
    }

    @Override
    public CtClass getCtClass() {
        return ctClass;
    }

    @Override
    public MethodProcessor acceptMethod(CtBehavior ctBehavior) {
        // Accept this method if its method signature exists in the 
        // set of function signatures within the symbol table class object.
        final JsonElement e = symTableClassObject.get(SYM_TABLE_FUNCSIG_NAME);
        final MethodProcessor methodProcessor = new MethodProcessorImpl(this, ctBehavior);
        if (e != null && e.isJsonObject()) {
            final JsonObject funcSigs = e.getAsJsonObject();
            if (funcSigs.has(methodProcessor.getMethodSignature())) {
                return methodProcessor;
            }
        }
        // The function signature table either doesn't exist or the direct
        // lookup found nothing. Try to find a match by walking over the
        // function list and inspecting each candidate function's list of
        // arguments.
        final String signature = getMethodSignature(methodProcessor);
        if (signature != null) {
            // This overrides the signature that would have 
            // been computed directly from the CtBehavior.
            return new MethodProcessorImpl(this, ctBehavior, signature);
        }
        return null;
    }
    
    @Override
    public String getClassName() {
        // Return the fully qualified class name if the function signature 
        // table exists, otherwise return the symbol table key as the class
        // name. The function signature table is only available in a symbol
        // table that has been augmented by the JavaParser.
        final JsonElement e = symTableClassObject.get(SYM_TABLE_FUNCSIG_NAME);
        if (e != null) {
            return ClassProcessor.super.getClassName();
        }
        return symTableKey;
    }
    
    @Override
    public String getSourcePath() {
        JsonElement e = symTableClassObject.get(SYM_TABLE_FILE_NAME);
        if (e != null && e.isJsonPrimitive()) {
            final String fileName = e.getAsString();
            e = symTableClassObject.get(SYM_TABLE_FUNCSIG_NAME);
            if (e != null) {
                return fileName;
            }
            // Add ':class' to the source path value if the symbol
            // table has not been augmented by the JavaParser.
            return fileName + ":class";
        }
        return ClassProcessor.super.getSourcePath();
    }
    
    private String getMethodSignature(MethodProcessor methodProcessor) {
        // Walk through the function list to obtain a
        // method signature that matches the given method.
        final JsonElement e = symTableClassObject.get(SYM_TABLE_FUNCTION_LIST);
        if (e != null && e.isJsonObject()) {
            final JsonObject funcL = e.getAsJsonObject();
            final String methodName = methodProcessor.getMethodName();
            final JsonElement methodElement = funcL.get(methodName);
            if (methodElement != null && methodElement.isJsonObject()) {
                final String signature = getMethodSignature(methodProcessor, methodName, methodElement.getAsJsonObject());
                if (signature != null) {
                    return signature;
                }
            }
            // If there are multiple overloaded methods the key will have this pattern.
            final String overloadedMethodPrefix = methodName + " [overloaded_";
            for (Map.Entry<String,JsonElement> entry : funcL.entrySet()) {
                final String key = entry.getKey();
                if (key != null && key.startsWith(overloadedMethodPrefix)) {
                    final JsonElement value = entry.getValue();
                    if (value != null && value.isJsonObject()) {
                        final String signature = getMethodSignature(methodProcessor, key, value.getAsJsonObject());
                        if (signature != null) {
                            return signature;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private String getMethodSignature(MethodProcessor methodProcessor, String methodKey, JsonObject methodObj) {
        final JsonElement e = methodObj.get(SYM_TABLE_FUNCTION_ARGS);
        if (e != null && e.isJsonObject()) {
            try {
                final JsonObject argsObj = e.getAsJsonObject();
                final Set<Map.Entry<String,JsonElement>> argSet = argsObj.entrySet();
                final CtClass[] params = methodProcessor.getParameterTypes();
                final int paramsLength = params != null ? params.length : 0;
                // Check that the candidate method has the correct number of arguments.
                if (argSet.size() != paramsLength) {
                    return null;
                }
                // Check that each parameter has the correct type.
                int i = 0;
                boolean gotParamTypes = false;
                SignatureAttribute.Type[] paramTypes = null;
                for (Map.Entry<String,JsonElement> entry : argSet) {
                    final JsonElement argElement = entry.getValue();
                    if (argElement == null || !argElement.isJsonObject()) {
                        return null;
                    }
                    final JsonObject argObj = argElement.getAsJsonObject();
                    final JsonElement argType = argObj.get(SYM_TABLE_FUNCTION_ARG_TYPE);
                    if (argType == null || !argType.isJsonPrimitive()) {
                        return null;
                    }
                    final String type = toRawType(argType.getAsString());
                    final CtClass param = params[i++];
                    // Check that either the simple name, the fully qualified
                    // type name or the suffix of the type name matches.
                    if (!type.equals(toClassName(param.getSimpleName())) && 
                            !type.equals(toClassName(param.getName())) && 
                            !toClassName(param.getName()).endsWith("." + type)) {
                        // Only fetch the parameter types once in this loop.
                        if (!gotParamTypes) {
                            paramTypes = methodProcessor.getGenericParameterTypes();
                            gotParamTypes = true;
                        }
                        // If this method has generic parameters types (e.g. getMap(K k, V v)), 
                        // check whether the type name matches the parameter type name.
                        if (paramTypes == null || !type.equals(paramTypes[i-1].toString())) {
                            return null;
                        }
                    }
                }
                // Return the function signature if the function signature table exists
                // otherwise return the key into the function list as the method signature.
                final JsonElement funcSig = symTableClassObject.get(SYM_TABLE_FUNCSIG_NAME);
                if (funcSig != null) {
                    final JsonElement sigElem = methodObj.get(SYM_TABLE_FUNCTION_OBJ_SIGNATURE);
                    if (sigElem != null && sigElem.isJsonPrimitive()) {
                        return sigElem.getAsString();
                    }
                }
                else {
                    return methodKey;
                } 
            }
            catch (NotFoundException nfe) {}
        }
        return null;
    }
    
    private static String toClassName(String type) {
        return type.replace('$', '.');
    }
    
    private static String toRawType(String type) {
        type = type.replaceAll("\\s", ""); // Remove extra spaces (e.g. int [] -> int[])
        final int length = type.length();
        if (length > 0) {
            // Remove generic type parameters (e.g. Map<K,V> -> Map)
            final int start = type.indexOf('<');
            if (start != -1) {
                final int end = type.lastIndexOf('>');
                if (end != -1 && end > start) {
                    return type.substring(0, start) + type.substring(end + 1, length);
                }
            }
        }
        return type;
    }
}
