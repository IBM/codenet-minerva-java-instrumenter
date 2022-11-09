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

import java.io.PrintStream;

public enum SystemPrintStream {
    
    OUT("out", System.out), 
    ERR("err", System.err);
    
    String name;
    PrintStream stream;
    
    SystemPrintStream(String name, PrintStream stream) {
        this.name = name;
        this.stream = stream;
    }
    
    public String getName() {
        return name;
    }
    
    public PrintStream getStream() {
        return stream;
    }
}
