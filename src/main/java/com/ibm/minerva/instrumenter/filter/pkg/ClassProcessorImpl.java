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

import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;
import com.ibm.minerva.instrumenter.filter.MethodProcessor;

import javassist.CtBehavior;
import javassist.CtClass;

public final class ClassProcessorImpl implements ClassProcessor {
    
    private final ApplicationProcessor appProcessor;
    private final CtClass ctClass;
    
    public ClassProcessorImpl(ApplicationProcessor appProcessor, CtClass ctClass) {
        this.appProcessor = appProcessor;
        this.ctClass = ctClass;
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
        return new MethodProcessorImpl(this, ctBehavior);
    }
}
