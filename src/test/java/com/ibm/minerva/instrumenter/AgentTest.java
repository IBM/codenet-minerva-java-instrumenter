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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.instrument.Instrumentation;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.junit5.JUnit5Mockery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonObject;

@DisplayName("Agent Test")
public class AgentTest {
    
    private static final String DEFAULTED_CONFIG_FILE = "";
    private static final String PACKAGE_CONFIG_FILE = "package-config.json";
    private static final String TABLE_CONFIG_FILE = "table-config.json";
    private static final String LOGGING_CONFIG_FILE = "logging-config.json";
    private static final String NO_FILTER_FILE = "no-filter-config.json";
    private static final String NO_GENERATOR_FILE = "no-generator-config.json";
    private static final String UNKNOWN_FILTER_FILE = "unknown-filter-config.json";
    private static final String UNKNOWN_GENERATOR_FILE = "unknown-generator-config.json";
    private static final String MALFORMED_FILE = "package-config-malformed.json";
    
    private final Mockery context = new JUnit5Mockery();
    
    @ParameterizedTest
    @ValueSource(strings = {DEFAULTED_CONFIG_FILE, PACKAGE_CONFIG_FILE, TABLE_CONFIG_FILE, LOGGING_CONFIG_FILE})
    public void testAgentmain(String fileName) {
        final Instrumentation inst = context.mock(Instrumentation.class);
        context.checking(new Expectations() {
            {
                oneOf(inst).addTransformer(with(any((TraceInjector.class))));
            }
        });
        Agent.agentmain(ResourceHelper.resolveResource(fileName), inst);
        context.assertIsSatisfied();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {NO_FILTER_FILE, NO_GENERATOR_FILE, UNKNOWN_FILTER_FILE,
            UNKNOWN_GENERATOR_FILE, MALFORMED_FILE})
    public void testAgentmainBadConfig(String fileName) {
        final Instrumentation inst = context.mock(Instrumentation.class);
        Agent.agentmain(ResourceHelper.resolveResource(fileName), inst);
        context.assertIsSatisfied();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {DEFAULTED_CONFIG_FILE, PACKAGE_CONFIG_FILE, TABLE_CONFIG_FILE, LOGGING_CONFIG_FILE})
    public void testPremain(String fileName) {
        final Instrumentation inst = context.mock(Instrumentation.class);
        context.checking(new Expectations() {
            {
                oneOf(inst).addTransformer(with(any((TraceInjector.class))));
            }
        });
        Agent.premain(ResourceHelper.resolveResource(fileName), inst);
        context.assertIsSatisfied();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {NO_FILTER_FILE, NO_GENERATOR_FILE, UNKNOWN_FILTER_FILE,
            UNKNOWN_GENERATOR_FILE, MALFORMED_FILE})
    public void testPremainBadConfig(String fileName) {
        final Instrumentation inst = context.mock(Instrumentation.class);
        Agent.premain(ResourceHelper.resolveResource(fileName), inst);
        context.assertIsSatisfied();
    }
    
    @Test
    public void testResolvePath() {
        final File rf = Agent.resolvePath(PACKAGE_CONFIG_FILE);
        assertNotNull(rf);
        final String absolutePath = rf.getAbsolutePath();
        assertTrue(absolutePath.endsWith(PACKAGE_CONFIG_FILE));
        final File af = Agent.resolvePath(absolutePath);
        assertNotNull(af);
        assertEquals(absolutePath, af.getAbsolutePath());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {PACKAGE_CONFIG_FILE, TABLE_CONFIG_FILE, LOGGING_CONFIG_FILE,
            UNKNOWN_FILTER_FILE, UNKNOWN_GENERATOR_FILE})
    public void testParseJSONDocument(String fileName) {
        final JsonObject o = Agent.parseJsonDocument(ResourceHelper.resolveResourceToFile(fileName));
        assertNotNull(o);
        assertNotNull(o.get("filter"));
        assertNotNull(o.get("generator"));
    }
    
    @Test
    public void testParseJSONDocumentMalformed() {
        final JsonObject o = Agent.parseJsonDocument(ResourceHelper.resolveResourceToFile(MALFORMED_FILE));
        assertNull(o);
    }
}
