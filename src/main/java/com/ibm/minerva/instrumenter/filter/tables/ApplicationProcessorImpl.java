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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibm.minerva.instrumenter.Agent;
import com.ibm.minerva.instrumenter.LoggingUtil;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;

import javassist.CtClass;

public class ApplicationProcessorImpl implements ApplicationProcessor {
    
    private static final Logger logger = LoggingUtil.getLogger(ApplicationProcessorImpl.class);
    
    private static final String SYM_TABLE_FILE_NAME = "symTable.json";
    private static final String REF_TABLE_FILE_NAME = "refTable.json";
    
    private static final String REF_TABLE_FQCN_NAME = "FQCN";
    private static final String REF_TABLE_VERSION_NAME = "Version";
    
    private final JsonObject symTable;
    private final JsonObject refTable;
    
    public ApplicationProcessorImpl(File tableDir) {
        final File symTableFile = new File(tableDir, SYM_TABLE_FILE_NAME);
        JsonObject _symTable = null;
        if (symTableFile.exists()) {
            _symTable = Agent.parseJsonDocument(symTableFile);
        }
        else {
            logger.severe(() -> formatMessage("FileDoesNotExist",
                    symTableFile.getAbsolutePath()));
        }
        final File refTableFile = new File(tableDir, REF_TABLE_FILE_NAME);
        JsonObject _refTable = null;
        if (refTableFile.exists()) {
            _refTable = Agent.parseJsonDocument(refTableFile);
        }
        else {
            logger.severe(() -> formatMessage("FileDoesNotExist",
                    refTableFile.getAbsolutePath()));
        }
        symTable = _symTable;
        refTable = _refTable;
    }

    @Override
    public boolean acceptClass(String className) {
        if (symTable == null || refTable == null) {
            return false;
        }
        // If the FQCN for this class exists in the refTable then accept it.
        final JsonElement e = refTable.get(REF_TABLE_FQCN_NAME);
        if (e != null && e.isJsonObject()) {
            final JsonObject fqcns = e.getAsJsonObject();
            return fqcns.has(toFQCN(className));
        }
        return false;
    }

    @Override
    public ClassProcessor acceptClass(CtClass ctClass) {
        if (symTable == null || refTable == null) {
            return null;
        }
        // Map the FQCN (for this class) from the refTable to a class object in the symTable.
        JsonElement e = refTable.get(REF_TABLE_FQCN_NAME);
        if (e != null && e.isJsonObject()) {
            final JsonObject fqcns = e.getAsJsonObject();
            e = fqcns.get(toFQCN(ctClass));
            if (e != null && e.isJsonArray()) {
                final JsonArray symbolTableKeys = e.getAsJsonArray();
                if (symbolTableKeys.size() > 0) {
                    e = symbolTableKeys.get(0);
                    if (e != null && e.isJsonPrimitive()) {
                        String symbolTableKey = e.getAsString();
                        e = symTable.get(symbolTableKey);
                        if (e != null && e.isJsonObject()) {
                            return new ClassProcessorImpl(this, ctClass, symbolTableKey, e.getAsJsonObject());
                        }
                    }
                }
            }
        }
        return null;
    }
    
    @Override
    public String getInstrumentationVersion() {
        if (refTable != null) {
            JsonElement e = refTable.get(REF_TABLE_VERSION_NAME);
            if (e != null && e.isJsonPrimitive()) {
                return e.getAsString();
            }
        }
        return ApplicationProcessor.super.getInstrumentationVersion();
    }
    
    private static String toFQCN(String className) {
        return className.replace('/', '.').replace("$", ".$");
    }
    
    private static String toFQCN(CtClass ctClass) {
        return ctClass.getName().replace("$", ".$");
    }
}
