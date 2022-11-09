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

package com.ibm.minerva.instrumenter.codegen.logging;

import com.ibm.minerva.instrumenter.codegen.TraceGenerator;
import com.ibm.minerva.instrumenter.codegen.TraceInjectionContext;
import com.ibm.minerva.instrumenter.codegen.TraceInjectionLocation;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;
import com.ibm.minerva.instrumenter.filter.MethodProcessor;

public final class TraceGeneratorImpl implements TraceGenerator {
    
    public TraceGeneratorImpl() {}

    @Override
    public String generateSourceSnippet(TraceInjectionContext context) {
        final ClassProcessor classProcessor = context.getClassProcessor();
        final MethodProcessor methodProcessor = context.getMethodProcessor();
        final TraceInjectionLocation til = context.getTraceInjectionLocation();
        // Logger.getLogger().entering(className,methodName,threadId) / Logger.getLogger().exiting(className,methodName,threadId)
        return "java.util.logging.Logger.getLogger(\"" + classProcessor.getClassName() + "\")." + til.getLoggingName() + "(\"" + classProcessor.getClassName() + "\",\"" + methodProcessor.getMethodName() + "\",java.lang.String.valueOf(java.lang.Thread.currentThread().getId()));";
    }
}
