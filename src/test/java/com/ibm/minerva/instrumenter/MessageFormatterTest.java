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

import static com.ibm.minerva.instrumenter.MessageFormatter.formatMessage;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MessageFormatter Test")
public class MessageFormatterTest {
    
    @Test
    public void testFormatMessage() {
        final String configFileName = "/usr/myconfig.json";
        final String msg = formatMessage("AgentConfigFile", configFileName);
        assertTrue(msg.contains(configFileName));
    }
    
    @Test
    public void testBadMessageKey() {
        final String badMessageKey = "ThisKeyDoesNotExistAndIsBad";
        final String msg = formatMessage(badMessageKey, "badValue1", "badValue2");
        assertTrue(msg.contains(badMessageKey));
    }
}
