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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.ibm.minerva.instrumenter.TypedFactory;
import com.ibm.minerva.instrumenter.codegen.TraceGeneratorFactory;

@DisplayName("PrintTraceFactory Test")
public class PrintTraceFactoryTest {
    
    @Test
    public void testType() {
        final TypedFactory tf = new PrintTraceFactory();
        assertEquals("println", tf.getType());
    }
    
    @Test
    public void testVersion() {
        final TypedFactory tf = new PrintTraceFactory();
        assertEquals("1.0", tf.getVersion());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"out", "err", "null"})
    public void testCreateTraceGenerator(String streamType) {
        final TraceGeneratorFactory tgf = new PrintTraceFactory();
        final JsonElement config;
        if ("null".equals(streamType)) {
            config = null;
        }
        else {
            config = new JsonPrimitive(streamType);
        }
        assertInstanceOf(TraceGeneratorImpl.class, tgf.createTraceGenerator(config));
    }
}
