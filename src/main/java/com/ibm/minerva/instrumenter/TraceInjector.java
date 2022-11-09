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

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.logging.Logger;

import com.ibm.minerva.instrumenter.codegen.TraceGenerator;
import com.ibm.minerva.instrumenter.codegen.TraceInjectionContext;
import com.ibm.minerva.instrumenter.codegen.TraceInjectionLocation;
import com.ibm.minerva.instrumenter.filter.ApplicationProcessor;
import com.ibm.minerva.instrumenter.filter.ClassProcessor;
import com.ibm.minerva.instrumenter.filter.MethodProcessor;

import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.runtime.Desc;
import javassist.scopedpool.ScopedClassPoolFactory;
import javassist.scopedpool.ScopedClassPoolFactoryImpl;
import javassist.scopedpool.ScopedClassPoolRepositoryImpl;

public final class TraceInjector implements ClassFileTransformer {
    
    private static final Logger logger = LoggingUtil.getLogger(TraceInjector.class);
    
    private final ApplicationProcessor appProcessor;
    private final TraceGenerator traceGenerator;
    private final ClassPool defaultClassPool;
    private final ScopedClassPoolFactory scopedClassPoolFactory;
    
    static {
        Desc.useContextClassLoader = true;
    }
    
    public TraceInjector(ApplicationProcessor appProcessor, TraceGenerator traceGenerator) {
        this.appProcessor = appProcessor;
        this.traceGenerator = traceGenerator;
        this.defaultClassPool = ClassPool.getDefault();
        this.scopedClassPoolFactory = new ScopedClassPoolFactoryImpl();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        // Transform class on initial load if it is accepted by the filter.
        if (classBeingRedefined == null && appProcessor.acceptClass(className)) {
            logger.fine(() -> formatMessage("InjectingEntryExitTraceClass", className.replace('/', '.')));
            try {
                final ClassPool classPool = scopedClassPoolFactory.create(loader, defaultClassPool,
                        ScopedClassPoolRepositoryImpl.getInstance());
                final CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                // Inject trace into this class if it is accepted by the filter.
                final ClassProcessor classProcessor = appProcessor.acceptClass(ctClass);
                if (classProcessor != null) {
                    // Inject entry/exit trace into each constructor.
                    injectEntryExitTrace(classProcessor, ctClass.getDeclaredConstructors());

                    // Inject entry/exit trace into each method.
                    injectEntryExitTrace(classProcessor, ctClass.getDeclaredMethods());
                }
                final byte[] transformedClass = ctClass.toBytecode();
                ctClass.detach();
                return transformedClass;
            }
            catch (Throwable t) {
                logger.severe(() -> formatMessage("ErrorInjectingTraceClass", className, t.getMessage()));
            }
        }
        return null;
    }
    
    // Inject entry/exit trace into methods that have a body (non-abstract and non-native).
    private void injectEntryExitTrace(ClassProcessor classProcessor, CtBehavior[] ctBehaviors) {
        if (ctBehaviors != null) {
            Arrays.stream(ctBehaviors).filter(x -> !MethodProcessor.isAbstractOrNative(x)).forEach(x -> injectEntryExitTrace(classProcessor, x));
        }
    }
    
    private void injectEntryExitTrace(ClassProcessor classProcessor, CtBehavior ctBehavior) {
        try {
            // Inject trace into this method if it is accepted by the filter.
            final MethodProcessor methodProcessor = classProcessor.acceptMethod(ctBehavior);
            if (methodProcessor != null) {
                logger.finer(() -> formatMessage("InjectingEntryExitTraceMethod", 
                        classProcessor.getCtClass().getName(), methodProcessor.getMethodSignature()));
                ctBehavior.insertBefore(getEntryTrace(classProcessor, methodProcessor));
                ctBehavior.insertAfter(getExitTrace(classProcessor, methodProcessor), true);
            }
        }
        catch (Throwable t) {
            logger.severe(() -> formatMessage("ErrorInjectingTraceMethod", 
                    classProcessor.getCtClass().getName(), 
                    ctBehavior.getName(), t.getMessage()));
        }
    }
    
    private String getEntryTrace(ClassProcessor classProcessor, MethodProcessor methodProcessor) {
        return getTrace(classProcessor, methodProcessor, TraceInjectionLocation.ENTRY);
    }
    
    private String getExitTrace(ClassProcessor classProcessor, MethodProcessor methodProcessor) {
        return getTrace(classProcessor, methodProcessor, TraceInjectionLocation.EXIT);
    }
    
    private String getTrace(ClassProcessor classProcessor, MethodProcessor methodProcessor, TraceInjectionLocation til) {
        final String snippet = traceGenerator.generateSourceSnippet(new TraceInjectionContext() {
            @Override
            public ApplicationProcessor getApplicationProcessor() {
                return appProcessor;
            }
            @Override
            public ClassProcessor getClassProcessor() {
                return classProcessor;
            }
            @Override
            public MethodProcessor getMethodProcessor() {
                return methodProcessor;
            }
            @Override
            public TraceInjectionLocation getTraceInjectionLocation() {
                return til;
            }
        });
        logger.finest(() -> formatMessage("InjectingTraceCode", 
                        classProcessor.getCtClass().getName(), methodProcessor.getMethodSignature(), til.toString(), snippet));
        return snippet;
    }
}
