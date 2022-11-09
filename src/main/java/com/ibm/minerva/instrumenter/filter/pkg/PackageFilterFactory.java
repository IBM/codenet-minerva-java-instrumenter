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

import static com.ibm.minerva.instrumenter.MessageFormatter.formatMessage;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.ibm.minerva.instrumenter.LoggingUtil;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessorFactory;

public final class PackageFilterFactory implements ApplicationProcessorFactory {
    
    private static final Logger logger = LoggingUtil.getLogger(PackageFilterFactory.class);
    
    public PackageFilterFactory() {}
    
    @Override
    public String getType() {
        return "package";
    }

    @Override
    public ApplicationProcessor createApplicationProcessor(JsonElement config) {
        Set<String> packages = Collections.emptySet();
        if (config != null) {
            // Array
            if (config.isJsonArray()) {
                final JsonArray packageArray = config.getAsJsonArray();
                final Set<String> pkgs = new TreeSet<>();
                packageArray.forEach(x -> {
                    if (x != null && x.isJsonPrimitive()) {
                        // Convert package names from x.y.z to x/y/z/ for use with ClassFileTransformer
                        final String pkg = toJVMPackageName(x);
                        if (pkg != null) {
                            pkgs.add(pkg);
                        }
                    }
                });
                packages = pkgs;
            }
            // Primitive
            else if (config.isJsonPrimitive()) {
                // Convert package name from x.y.z to x/y/z/ for use with ClassFileTransformer
                final String pkg = toJVMPackageName(config);
                if (pkg != null) {
                    packages = Collections.singleton(pkg);
                }
            }
        }
        final Set<String> _packages = packages;
        logger.config(() -> formatMessage("AgentPackageFilter", _packages));
        if (!packages.isEmpty()) {
            return createApplicationProcessor(packages);
        }
        return null;
    }
    
    private ApplicationProcessor createApplicationProcessor(Set<String> packages) {
        return new ApplicationProcessorImpl(packages);
    }
    
    private String toJVMPackageName(JsonElement e) {
        final String pkg = e.getAsString().trim();
        if (pkg.length() > 0) {
            return pkg.replace('.', '/') + '/';
        }
        return null;
    }
}
