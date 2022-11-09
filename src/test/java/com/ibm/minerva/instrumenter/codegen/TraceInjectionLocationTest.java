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

package com.ibm.minerva.instrumenter.codegen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("TraceInjectionLocation Test")
public class TraceInjectionLocationTest {
    
    @ParameterizedTest
    @EnumSource(TraceInjectionLocation.class)
    public void namesNotNull(TraceInjectionLocation location) {
        assertNotNull(location.getLoggingName(), "Logging name must not be null."); 
        assertNotNull(location.getPrintName(), "Print name must not be null."); 
    }
    
    @Test
    public void entryValueTest() {
        valueTestCommon(TraceInjectionLocation.ENTRY, "Entering", "entering");
    }
    
    @Test
    public void exitValueTest() {
        valueTestCommon(TraceInjectionLocation.EXIT, "Exiting", "exiting");
    }
    
    private void valueTestCommon(TraceInjectionLocation location, String printName, String loggingName) {
        assertEquals(printName, location.getPrintName());
        assertEquals(loggingName, location.getLoggingName());
    }
}
