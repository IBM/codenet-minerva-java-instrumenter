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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.runtime.Desc;
import javassist.scopedpool.ScopedClassPoolFactoryImpl;
import javassist.scopedpool.ScopedClassPoolRepositoryImpl;

public final class ResourceHelper {
    
    private ResourceHelper() {}
    
    private static final File baseResourcePath = Paths.get("src", "test", "resources").toFile();
    
    public static String resolveResource(String resource) {
        return resolveResourceToFile(resource).getAbsolutePath();
    }
    
    public static File resolveResourceToFile(String resource) {
        return new File(baseResourcePath, resource);
    }
    
    public static byte[] resourceToByteArray(String resource) throws IOException {
        File f = resolveResourceToFile(resource);
        InputStream is = new FileInputStream(f);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        int bytesRead;
        byte[] buffer = new byte[1024];
        
        while ((bytesRead = is.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        is.close();
        return baos.toByteArray();
    }
    
    public static CtClass resourceToCtClass(String resource) throws IOException {
        byte[] bytes = resourceToByteArray(resource);
        Desc.useContextClassLoader = true;
        final ClassPool classPool = new ScopedClassPoolFactoryImpl().create(ResourceHelper.class.getClassLoader(),
                ClassPool.getDefault(), ScopedClassPoolRepositoryImpl.getInstance());
        return classPool.makeClass(new ByteArrayInputStream(bytes));
    }
}
