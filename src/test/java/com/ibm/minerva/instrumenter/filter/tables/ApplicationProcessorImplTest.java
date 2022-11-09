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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.ibm.minerva.instrumenter.ResourceHelper;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;
import com.ibm.minerva.instrumenter.filter.MethodProcessor;

import javassist.CtBehavior;
import javassist.CtClass;

@DisplayName("ApplicationProcessorImpl(tables) Test")
public class ApplicationProcessorImplTest {
    
    private static final String EXAMPLE_CLASS = "example/Example";
    private static final String EXAMPLE$1_CLASS = "example/Example$1";
    private static final String EXAMPLE$2_CLASS = "example/Example$2";
    private static final String EXAMPLE$ANOTHER_CLASS = "example/Example$Another";
    private static final String GENERIC_CLASS = "example/Generic";
    private static final String GENERIC$1POINT_CLASS = "example/Generic$1Point";
    private static final String GENERIC$2POINT_CLASS = "example/Generic$2Point";
    private static final String OTHER_CLASS = "example/Other";
    
    private static final String TABLES_JPARSER = "tables/jparser";
    private static final String TABLES_NOJPARSER = "tables/nojparser";
    private static final String TABLES_INVALID = "tables/invalid";
    
    @ParameterizedTest
    @ValueSource(strings = {TABLES_JPARSER, TABLES_NOJPARSER, TABLES_INVALID})
    public void testInstrumentationVersion(String dir) {
        final ApplicationProcessor ap = new ApplicationProcessorImpl(ResourceHelper.resolveResourceToFile(dir));
        assertNotNull(ap.getInstrumentationVersion());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {EXAMPLE_CLASS, EXAMPLE$1_CLASS, EXAMPLE$2_CLASS, 
            EXAMPLE$ANOTHER_CLASS, GENERIC_CLASS, GENERIC$1POINT_CLASS, 
            GENERIC$2POINT_CLASS, OTHER_CLASS})
    public void testAcceptClassWithJParser(String className) throws IOException {
        testAcceptClass(className, TABLES_JPARSER);
    }
    
    @ParameterizedTest
    @ValueSource(strings = {EXAMPLE_CLASS, EXAMPLE$1_CLASS, EXAMPLE$2_CLASS, 
            EXAMPLE$ANOTHER_CLASS, GENERIC_CLASS, GENERIC$1POINT_CLASS, 
            GENERIC$2POINT_CLASS, OTHER_CLASS})
    public void testAcceptClassWithNoJParser(String className) throws IOException {
        testAcceptClass(className, TABLES_NOJPARSER);
    }
    
    private void testAcceptClass(String className, String dir) throws IOException {
        final ApplicationProcessor ap = new ApplicationProcessorImpl(ResourceHelper.resolveResourceToFile(dir));
        if (ap.acceptClass(className)) {
            final CtClass ctClass = ResourceHelper.resourceToCtClass(className + ".class");
            final ClassProcessor cp = ap.acceptClass(ctClass);
            if (cp != null) {
                assertSame(ap, cp.getApplicationProcessor());
                assertSame(ctClass, cp.getCtClass());
                assertNotNull(cp.getClassName());
                assertNotNull(cp.getSourcePath());
                testAcceptCtBehavior(cp, ctClass.getDeclaredConstructors());
                testAcceptCtBehavior(cp, ctClass.getDeclaredMethods());
            }
        }  
    }
    
    private void testAcceptCtBehavior(ClassProcessor cp, CtBehavior[] ctBehavior) {
        if (ctBehavior != null) {
            Arrays.stream(ctBehavior).forEach(x -> testAcceptCtBehavior(cp, x));
        }
    }
    
    private void testAcceptCtBehavior(ClassProcessor cp, CtBehavior ctBehavior) {
        final MethodProcessor mp = cp.acceptMethod(ctBehavior);
        if (mp != null) {
            assertSame(cp, mp.getClassProcessor());
            assertSame(ctBehavior, mp.getCtBehavior());
            assertNotNull(mp.getMethodSignature());
        }
    }
}
