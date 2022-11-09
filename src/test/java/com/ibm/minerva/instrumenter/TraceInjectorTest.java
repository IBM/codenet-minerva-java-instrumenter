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

package com.ibm.minerva.instrumenter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ibm.minerva.instrumenter.codegen.TraceGenerator;
import com.ibm.minerva.instrumenter.codegen.TraceInjectionContext;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;
import com.ibm.minerva.instrumenter.filter.MethodProcessor;

import javassist.CtBehavior;
import javassist.CtClass;

@DisplayName("TraceInjector Test")
public class TraceInjectorTest {
    
    private static final String EXAMPLE_CLASS = "example/Example";
    private static final String EXAMPLE$1_CLASS = "example/Example$1";
    private static final String EXAMPLE$2_CLASS = "example/Example$2";
    private static final String EXAMPLE$ANOTHER_CLASS = "example/Example$Another";
    private static final String GENERIC_CLASS = "example/Generic";
    private static final String GENERIC$1POINT_CLASS = "example/Generic$1Point";
    private static final String GENERIC$2POINT_CLASS = "example/Generic$2Point";
    private static final String OTHER_CLASS = "example/Other";
    
    private final Mockery context = new JUnit5Mockery();
    
    @Test
    public void testTransformRejectClass() throws IllegalClassFormatException {
        final ApplicationProcessor ap = context.mock(ApplicationProcessor.class);
        final TraceGenerator tg = context.mock(TraceGenerator.class);
        context.checking(new Expectations() {
            {
                oneOf(ap).acceptClass(EXAMPLE_CLASS); will(returnValue(false));
            }
        });
        final TraceInjector ti = new TraceInjector(ap, tg);
        final byte[] b = ti.transform(TraceInjectorTest.class.getClassLoader(), EXAMPLE_CLASS, null, null, new byte[] {});
        context.assertIsSatisfied();
        assertNull(b);    
    }
    
    @ParameterizedTest
    @ValueSource(strings = {EXAMPLE_CLASS, EXAMPLE$1_CLASS, EXAMPLE$2_CLASS, 
            EXAMPLE$ANOTHER_CLASS, GENERIC_CLASS, GENERIC$1POINT_CLASS, 
            GENERIC$2POINT_CLASS, OTHER_CLASS})
    public void testTransformRejectCtClass(String className) throws IllegalClassFormatException, IOException {
        final byte[] exampleClass = ResourceHelper.resourceToByteArray(className + ".class");
        assertNotNull(exampleClass);
        final ApplicationProcessor ap = context.mock(ApplicationProcessor.class);
        final TraceGenerator tg = context.mock(TraceGenerator.class);
        context.checking(new Expectations() {
            {
                oneOf(ap).acceptClass(className); will(returnValue(true));
                oneOf(ap).acceptClass(with(any(CtClass.class))); will(returnValue(null));
            }
        });
        final TraceInjector ti = new TraceInjector(ap, tg);
        final byte[] b = ti.transform(TraceInjectorTest.class.getClassLoader(), className, null, null, exampleClass);
        context.assertIsSatisfied();
        assertNotNull(b);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {EXAMPLE_CLASS, EXAMPLE$1_CLASS, EXAMPLE$2_CLASS, 
            EXAMPLE$ANOTHER_CLASS, GENERIC_CLASS, GENERIC$1POINT_CLASS, 
            GENERIC$2POINT_CLASS, OTHER_CLASS})
    public void testTransformAcceptCtClassRejectCtBehavior(String className) throws IllegalClassFormatException, IOException {
        final byte[] exampleClass = ResourceHelper.resourceToByteArray(className + ".class");
        assertNotNull(exampleClass);
        final ApplicationProcessor ap = context.mock(ApplicationProcessor.class);
        final ClassProcessor cp = context.mock(ClassProcessor.class);
        final TraceGenerator tg = context.mock(TraceGenerator.class);
        context.checking(new Expectations() {
            {
                oneOf(ap).acceptClass(className); will(returnValue(true));
                oneOf(ap).acceptClass(with(any(CtClass.class))); will(returnValue(cp));
                allowing(cp).acceptMethod(with(any(CtBehavior.class))); will(returnValue(null));
            }
        });
        final TraceInjector ti = new TraceInjector(ap, tg);
        final byte[] b = ti.transform(TraceInjectorTest.class.getClassLoader(), className, null, null, exampleClass);
        context.assertIsSatisfied();
        assertNotNull(b);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {EXAMPLE_CLASS, EXAMPLE$1_CLASS, EXAMPLE$2_CLASS, 
            EXAMPLE$ANOTHER_CLASS, GENERIC_CLASS, GENERIC$1POINT_CLASS, 
            GENERIC$2POINT_CLASS, OTHER_CLASS})
    public void testTransformAcceptCtClassAcceptCtBehavior(String className) throws IllegalClassFormatException, IOException {
        final byte[] exampleClass = ResourceHelper.resourceToByteArray(className + ".class");
        assertNotNull(exampleClass);
        final ApplicationProcessor ap = context.mock(ApplicationProcessor.class);
        final ClassProcessor cp = context.mock(ClassProcessor.class);
        final MethodProcessor mp = context.mock(MethodProcessor.class);
        final TraceGenerator tg = context.mock(TraceGenerator.class);
        context.checking(new Expectations() {
            {
                oneOf(ap).acceptClass(className); will(returnValue(true));
                oneOf(ap).acceptClass(with(any(CtClass.class))); will(returnValue(cp));
                allowing(cp).acceptMethod(with(any(CtBehavior.class))); will(returnValue(mp));
                allowing(tg).generateSourceSnippet(with(any(TraceInjectionContext.class))); will(returnValue("java.lang.System.out.println(\"test\");"));
            }
        });
        final TraceInjector ti = new TraceInjector(ap, tg);
        final byte[] b = ti.transform(TraceInjectorTest.class.getClassLoader(), className, null, null, exampleClass);
        context.assertIsSatisfied();
        assertNotNull(b);
    }
}
