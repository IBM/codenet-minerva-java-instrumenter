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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.ibm.minerva.instrumenter.TypedFactory;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessorFactory;

@DisplayName("PackageFilterFactory Test")
public class PackageFilterFactoryTest {
    
    @Test
    public void testType() {
        final TypedFactory tf = new PackageFilterFactory();
        assertEquals("package", tf.getType());
    }
    
    @Test
    public void testVersion() {
        final TypedFactory tf = new PackageFilterFactory();
        assertEquals("1.0", tf.getVersion());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"com.ibm.minerva.instrumenter", "java.util:org.w3c", "null"})
    public void testCreateApplicationProcessor(String packages) {
        final ApplicationProcessorFactory apf = new PackageFilterFactory();
        final JsonElement config;
        if ("null".equals(packages)) {
            config = null;
        }
        else if (packages.indexOf(':') == -1) {
            config = new JsonPrimitive(packages);
        }
        else {
            final JsonArray array = new JsonArray();
            for (String value : packages.split(":")) {
                array.add(value);
            }
            config = array;
        }
        final ApplicationProcessor ap = apf.createApplicationProcessor(config);
        if (config != null) {
            assertInstanceOf(ApplicationProcessorImpl.class, ap);
        }
        else {
            assertNull(ap);
        }
    }
}
