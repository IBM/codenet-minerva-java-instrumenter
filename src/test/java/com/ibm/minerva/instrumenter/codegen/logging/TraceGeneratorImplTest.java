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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.ibm.minerva.instrumenter.codegen.TraceGenerator;
import com.ibm.minerva.instrumenter.codegen.TraceInjectionContext;
import com.ibm.minerva.instrumenter.codegen.TraceInjectionLocation;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;
import com.ibm.minerva.instrumenter.filter.MethodProcessor;

@DisplayName("TraceGeneratorImpl(logging) Test")
public class TraceGeneratorImplTest {
    
    private static final String CLASS_NAME = "java.lang.Object";
    private static final String METHOD_NAME = "notify";
    
    private final Mockery context = new JUnit5Mockery();
    
    @ParameterizedTest
    @EnumSource(TraceInjectionLocation.class)
    public void generateSourceSnippet(TraceInjectionLocation location) {
        final TraceGenerator tg = new TraceGeneratorImpl();
        final TraceInjectionContext tic = context.mock(TraceInjectionContext.class);
        final ClassProcessor cp = context.mock(ClassProcessor.class);
        final MethodProcessor mp = context.mock(MethodProcessor.class);
        context.checking(new Expectations() {
            {
                atLeast(1).of(tic).getClassProcessor(); will(returnValue(cp));
                atLeast(1).of(tic).getMethodProcessor(); will(returnValue(mp));
                atLeast(1).of(tic).getTraceInjectionLocation(); will(returnValue(location));
                atLeast(1).of(cp).getClassName(); will(returnValue(CLASS_NAME));
                atLeast(1).of(mp).getMethodName(); will(returnValue(METHOD_NAME));
            }
        });
        final String snippet = tg.generateSourceSnippet(tic);
        context.assertIsSatisfied();
        assertTrue(snippet.startsWith("java.util.logging.Logger.getLogger("));
        assertTrue(snippet.contains(CLASS_NAME));
        assertTrue(snippet.contains(METHOD_NAME));
        assertTrue(snippet.contains("." + location.getLoggingName() + "("));
    }
}
