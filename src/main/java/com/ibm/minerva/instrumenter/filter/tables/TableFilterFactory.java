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

import static com.ibm.minerva.instrumenter.MessageFormatter.formatMessage;

import java.io.File;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.ibm.minerva.instrumenter.Agent;
import com.ibm.minerva.instrumenter.LoggingUtil;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessorFactory;

public final class TableFilterFactory implements ApplicationProcessorFactory {
    
    private static final Logger logger = LoggingUtil.getLogger(TableFilterFactory.class);
    
    public TableFilterFactory() {}

    @Override
    public String getType() {
        return "sym-ref-tables";
    }

    @Override
    public ApplicationProcessor createApplicationProcessor(JsonElement config) {
        if (config != null && config.isJsonPrimitive()) {
            final File tableDir = Agent.resolvePath(config.getAsString());
            logger.config(() -> formatMessage("AgentTableDirectory", tableDir.getAbsolutePath()));
            if (tableDir.exists() && tableDir.isDirectory()) {
                return createApplicationProcessor(tableDir);
            }
            else {
                logger.severe(() -> formatMessage("NoAgentTableDirectory", tableDir.getAbsolutePath()));
            }
        }
        else {
            logger.config(() -> formatMessage("AgentTableDirectory", "[undefined]"));
        }
        return null;
    }
    
    private ApplicationProcessor createApplicationProcessor(File collectorDir) {
        return new ApplicationProcessorImpl(collectorDir);
    }
}
