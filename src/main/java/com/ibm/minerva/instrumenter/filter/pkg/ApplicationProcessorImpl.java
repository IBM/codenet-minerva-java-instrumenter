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

import java.util.Set;

import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;

import javassist.CtClass;

public final class ApplicationProcessorImpl implements ApplicationProcessor {
    
    private final Set<String> packages;
    
    public ApplicationProcessorImpl(Set<String> packages) {
        this.packages = packages;
    }

    @Override
    public boolean acceptClass(String className) {
        return packages.stream().anyMatch(x -> className.startsWith(x));
    }

    @Override
    public ClassProcessor acceptClass(CtClass ctClass) {
        return new ClassProcessorImpl(this, ctClass);
    }
}
