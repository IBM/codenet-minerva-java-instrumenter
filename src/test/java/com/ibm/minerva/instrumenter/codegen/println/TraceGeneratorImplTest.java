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

package com.ibm.minerva.instrumenter.codegen.println;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.ibm.minerva.instrumenter.codegen.TraceGenerator;
import com.ibm.minerva.instrumenter.codegen.TraceInjectionContext;
import com.ibm.minerva.instrumenter.codegen.TraceInjectionLocation;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;
import com.ibm.minerva.instrumenter.filter.MethodProcessor;

@DisplayName("TraceGeneratorImpl(println) Test")
public class TraceGeneratorImplTest {
    
    private static final String VERSION = "v9.0.0r77";
    private static final String SOURCE_PATH = "project/src/main/java/java/lang/Object.java";
    private static final String CLASS_NAME = "java.lang.Object";
    private static final String METHOD_SIGNATURE = "notify()";
    
    private final Mockery context = new JUnit5Mockery();
    
    @ParameterizedTest
    @MethodSource("locationAndStreamProvider")
    public void generateSourceSnippet(TraceInjectionLocation location, SystemPrintStream sps) {
        final TraceGenerator tg = new TraceGeneratorImpl(sps);
        final TraceInjectionContext tic = context.mock(TraceInjectionContext.class);
        final ApplicationProcessor ap = context.mock(ApplicationProcessor.class);
        final ClassProcessor cp = context.mock(ClassProcessor.class);
        final MethodProcessor mp = context.mock(MethodProcessor.class);
        context.checking(new Expectations() {
            {
                atLeast(1).of(tic).getApplicationProcessor(); will(returnValue(ap));
                atLeast(1).of(tic).getClassProcessor(); will(returnValue(cp));
                atLeast(1).of(tic).getMethodProcessor(); will(returnValue(mp));
                atLeast(1).of(tic).getTraceInjectionLocation(); will(returnValue(location));
                atLeast(1).of(ap).getInstrumentationVersion(); will(returnValue(VERSION));
                atLeast(1).of(cp).getSourcePath(); will(returnValue(SOURCE_PATH));
                atLeast(1).of(cp).getClassName(); will(returnValue(CLASS_NAME));
                atLeast(1).of(mp).getMethodSignature(); will(returnValue(METHOD_SIGNATURE));
            }
        });
        final String snippet = tg.generateSourceSnippet(tic);
        assertTrue(snippet.startsWith("java.lang.System." + sps.getName() + ".println(")); // System.out/System.err
        assertTrue(snippet.contains("|" + VERSION + "|"));
        assertTrue(snippet.contains(location.getPrintName() + " ")); // Entering/Exiting
        assertTrue(snippet.contains(SOURCE_PATH + "::"));
        assertTrue(snippet.contains("::" + CLASS_NAME + "::"));
        assertTrue(snippet.contains("::" + METHOD_SIGNATURE));
        context.assertIsSatisfied();
    }
    
    public static Stream<Arguments> locationAndStreamProvider() {
        return Stream.of(
            arguments(TraceInjectionLocation.ENTRY, SystemPrintStream.OUT),
            arguments(TraceInjectionLocation.EXIT, SystemPrintStream.OUT),
            arguments(TraceInjectionLocation.ENTRY, SystemPrintStream.ERR),
            arguments(TraceInjectionLocation.EXIT, SystemPrintStream.ERR)
        );
    }
}
