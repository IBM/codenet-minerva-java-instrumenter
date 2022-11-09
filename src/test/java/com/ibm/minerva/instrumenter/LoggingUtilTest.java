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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LoggingUtil Test")
public class LoggingUtilTest {
    
    @Test
    public void testLoggingConfiguration() {
        final Logger logger = LoggingUtil.getLogger(LoggingUtilTest.class);
        assertNotNull(logger, "Logger must not be null.");
        assertEquals(Level.INFO, LoggingUtil.getLoggingLevel());
        assertEquals(Level.INFO, logger.getLevel());
        LoggingUtil.setLoggingLevel(Level.FINEST);
        assertEquals(Level.FINEST, LoggingUtil.getLoggingLevel());
        assertEquals(Level.FINEST, logger.getLevel());
        LoggingUtil.setLoggingLevel(Level.INFO);
        assertEquals(Level.INFO, LoggingUtil.getLoggingLevel());
        assertEquals(Level.INFO, logger.getLevel());
    }
}
