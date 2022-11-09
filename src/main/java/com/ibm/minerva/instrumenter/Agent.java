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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.instrument.Instrumentation;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.minerva.instrumenter.codegen.TraceGenerator;
import com.ibm.minerva.instrumenter.codegen.TraceGeneratorFactory;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessorFactory;

public final class Agent {
    
    private static final Logger logger = LoggingUtil.getLogger(Agent.class);
    
    private static final String DEFAULT_CONFIG_FILE_NAME = "instrumenter-config.json";
    
    private static final String FILTER_NAME = "filter";
    private static final String GENERATOR_NAME = "generator";
    private static final String LOGGING_NAME = "logging";
    private static final String TYPE_NAME = "type";
    private static final String VERSION_NAME = "version";
    private static final String CONFIG_NAME = "config";
    
    private static volatile File agentConfig;
    
    private Agent() {}

    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }
    
    public static void agentmain(String agentArgs, Instrumentation inst) {
        logger.info(() -> formatMessage("StartingAgent"));
        JsonObject config = null;
        if (agentArgs != null) {
            agentArgs = agentArgs.trim();
            File f = new File(agentArgs);
            if (f.isDirectory()) {
                f = new File(f, DEFAULT_CONFIG_FILE_NAME);
            }
            agentConfig = f;
            logger.info(() -> formatMessage("AgentConfigFile", agentConfig.getAbsolutePath()));
            if (agentConfig.exists()) {
                config = parseJsonDocument(agentConfig);
            }
            else {
                logger.severe(() -> formatMessage("FileDoesNotExist",
                        agentConfig.getAbsolutePath()));
            }
        }
        processLoggingConfiguration(config);
        final ApplicationProcessor ap = createApplicationProcessor(config);
        final TraceGenerator tg = createTraceGenerator(config);
        if (ap != null && tg != null) {
            inst.addTransformer(new TraceInjector(ap, tg));
        }
        else {
            if (ap == null) {
                logger.severe(() -> formatMessage("AgentNoFilter"));
            }
            if (tg == null) {
                logger.severe(() -> formatMessage("AgentNoGenerator"));
            }
        }
    }
    
    // Resolves a path against the agent config file.
    public static File resolvePath(String path) {
        final File file = new File(path);
        if (file.isAbsolute()) {
            try {
                return file.getCanonicalFile();
            } catch (IOException e) {
                return file;
            }
        }
        File parent = agentConfig;
        if (parent != null) {
            parent = parent.getParentFile();
            if (parent != null && parent.isDirectory()) {
                final File resolved = new File(parent, path);
                try {
                    return resolved.getCanonicalFile();
                } catch (IOException e) {
                    return resolved;
                }
            }
        }
        return file;
    }
    
    public static JsonObject parseJsonDocument(File jsonDocument) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(new FileInputStream(jsonDocument), "UTF-8");
            JsonElement element = JsonParser.parseReader(reader);
            if (element != null && element.isJsonObject()) {
                return element.getAsJsonObject();
            }
        }
        catch (Exception e) {   
            logger.severe(() -> formatMessage("JSONFileUnreadable",
                    jsonDocument.getAbsolutePath(), e.getMessage()));
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (Exception e) {}
            }
        }
        return null;
    }
    
    private static void processLoggingConfiguration(JsonObject o) {
        if (o != null) {
            JsonElement e = o.get(LOGGING_NAME);
            if (e != null && e.isJsonPrimitive()) {
                final String value = e.getAsString().toUpperCase(Locale.ENGLISH);
                try {
                    final Level level = Level.parse(value);
                    LoggingUtil.setLoggingLevel(level);
                }
                catch (Exception ex) {
                    logger.warning(() -> formatMessage("LoggingLevelNotSet", 
                            value, LoggingUtil.getLoggingLevel().getName()));
                }
            }
        }
    }
        
    private static ApplicationProcessor createApplicationProcessor(JsonObject o) {
        if (o != null) {
            JsonElement e = o.get(FILTER_NAME);
            if (e != null && e.isJsonObject()) {
                o = e.getAsJsonObject();
                final ApplicationProcessorFactory apf = createTypedFactory(o, ApplicationProcessorFactory.class);
                if (apf != null) {
                    return apf.createApplicationProcessor(o.get(CONFIG_NAME));
                }
            }
        }
        return null;
    }
    
    private static TraceGenerator createTraceGenerator(JsonObject o) {
        if (o != null) {
            JsonElement e = o.get(GENERATOR_NAME);
            if (e != null && e.isJsonObject()) {
                o = e.getAsJsonObject();
                final TraceGeneratorFactory tgf = createTypedFactory(o, TraceGeneratorFactory.class);
                if (tgf != null) {
                    return tgf.createTraceGenerator(o.get(CONFIG_NAME));
                }
            }
        }
        return null;
    }
    
    private static <T extends TypedFactory> T createTypedFactory(JsonObject o, Class<T> factoryType) {
        JsonElement e = o.get(TYPE_NAME);
        if (e != null && e.isJsonPrimitive()) {
            final String type = e.getAsString();
            e = o.get(VERSION_NAME);
            if (e != null && e.isJsonPrimitive()) {
                final String version = e.getAsString();
                final ServiceLoader<T> sl = ServiceLoader.load(factoryType, Agent.class.getClassLoader());
                for (T factory : sl) {
                    if (factory.matches(type, version)) {
                        return factory;
                    }
                }
                // service loader found no factory with the specified type and version.
                logger.severe(() -> formatMessage("AgentNoFactory", factoryType.getName(), type, version));
            }
        }
        return null;
    }
}
