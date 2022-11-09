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

package com.ibm.minerva.instrumenter.filter.pkg;

import com.ibm.minerva.instrumenter.filter.ClassProcessor;
import com.ibm.minerva.instrumenter.filter.MethodProcessor;

import javassist.CtBehavior;

public final class MethodProcessorImpl implements MethodProcessor {
    
    private final ClassProcessor classProcessor;
    private final CtBehavior ctBehavior;
    private String signature;
    
    public MethodProcessorImpl(ClassProcessor classProcessor, CtBehavior ctBehavior) {
        this(classProcessor, ctBehavior, null);
    }
    
    public MethodProcessorImpl(ClassProcessor classProcessor, CtBehavior ctBehavior, String signature) {
        this.classProcessor = classProcessor;
        this.ctBehavior = ctBehavior;
        this.signature = signature;
    }
    
    @Override
    public ClassProcessor getClassProcessor() {
        return classProcessor;
    }

    @Override
    public CtBehavior getCtBehavior() {
        return ctBehavior;
    }
    
    @Override
    public String getMethodSignature() {
        if (signature == null) {
            signature = MethodProcessor.super.getMethodSignature();
        }
        return signature;
    }
}
