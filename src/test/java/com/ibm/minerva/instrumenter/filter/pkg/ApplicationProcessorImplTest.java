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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ibm.minerva.instrumenter.ResourceHelper;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;
import com.ibm.minerva.instrumenter.filter.MethodProcessor;

import javassist.CtBehavior;
import javassist.CtClass;

@DisplayName("ApplicationProcessorImpl(pkg) Test")
public class ApplicationProcessorImplTest {
    
    private static final String EXAMPLE_CLASS = "example/Example";
    private static final String EXAMPLE$1_CLASS = "example/Example$1";
    private static final String EXAMPLE$2_CLASS = "example/Example$2";
    private static final String EXAMPLE$ANOTHER_CLASS = "example/Example$Another";
    private static final String GENERIC_CLASS = "example/Generic";
    private static final String GENERIC$1POINT_CLASS = "example/Generic$1Point";
    private static final String GENERIC$2POINT_CLASS = "example/Generic$2Point";
    private static final String OTHER_CLASS = "example/Other";
    
    private static final String ACCEPT_PACKAGE = "example";
    private static final String REJECT_PACKAGE = "java/lang";
    
    @Test
    public void testInstrumentationVersion() {
        final ApplicationProcessor ap = new ApplicationProcessorImpl(Collections.singleton(ACCEPT_PACKAGE));
        assertNotNull(ap.getInstrumentationVersion());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {EXAMPLE_CLASS, EXAMPLE$1_CLASS, EXAMPLE$2_CLASS, 
            EXAMPLE$ANOTHER_CLASS, GENERIC_CLASS, GENERIC$1POINT_CLASS, 
            GENERIC$2POINT_CLASS, OTHER_CLASS})
    public void testAcceptClass(String className) throws IOException {
        final ApplicationProcessor ap = new ApplicationProcessorImpl(Collections.singleton(ACCEPT_PACKAGE));
        assertTrue(ap.acceptClass(className), "Expected " + className + " to be accepted.");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {EXAMPLE_CLASS, EXAMPLE$1_CLASS, EXAMPLE$2_CLASS, 
            EXAMPLE$ANOTHER_CLASS, GENERIC_CLASS, GENERIC$1POINT_CLASS, 
            GENERIC$2POINT_CLASS, OTHER_CLASS})
    public void testRejectClass(String className) throws IOException {
        final ApplicationProcessor ap = new ApplicationProcessorImpl(Collections.singleton(REJECT_PACKAGE));
        assertFalse(ap.acceptClass(className), "Expected " + className + " to be rejected.");
    }
    
    @ParameterizedTest
    @ValueSource(strings = {EXAMPLE_CLASS, EXAMPLE$1_CLASS, EXAMPLE$2_CLASS, 
            EXAMPLE$ANOTHER_CLASS, GENERIC_CLASS, GENERIC$1POINT_CLASS, 
            GENERIC$2POINT_CLASS, OTHER_CLASS})
    public void testAcceptCtClass(String className) throws IOException {
        final ApplicationProcessor ap = new ApplicationProcessorImpl(Collections.singleton(ACCEPT_PACKAGE));
        final CtClass ctClass = ResourceHelper.resourceToCtClass(className + ".class");
        final ClassProcessor cp = ap.acceptClass(ctClass);
        assertNotNull(cp);
        assertSame(ap, cp.getApplicationProcessor());
        assertSame(ctClass, cp.getCtClass());
        assertNotNull(cp.getClassName());
        assertTrue(cp.getClassName().contains(ACCEPT_PACKAGE));
        assertNotNull(cp.getSourcePath());
        assertTrue(cp.getSourcePath().contains(ACCEPT_PACKAGE)); 
        testAcceptCtBehavior(cp, ctClass.getDeclaredConstructors());
        testAcceptCtBehavior(cp, ctClass.getDeclaredMethods());
    }
    
    private void testAcceptCtBehavior(ClassProcessor cp, CtBehavior[] ctBehavior) {
        if (ctBehavior != null) {
            Arrays.stream(ctBehavior).forEach(x -> testAcceptCtBehavior(cp, x));
        }
    }
    
    private void testAcceptCtBehavior(ClassProcessor cp, CtBehavior ctBehavior) {
        final MethodProcessor mp = cp.acceptMethod(ctBehavior); // all methods should be accepted.
        assertNotNull(mp);
        assertSame(cp, mp.getClassProcessor());
        assertSame(ctBehavior, mp.getCtBehavior());
        final String signature = mp.getMethodSignature();
        assertNotNull(signature);
        assertTrue(signature.startsWith(mp.getMethodName()));
    }
}
