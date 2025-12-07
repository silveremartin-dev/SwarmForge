/*
 *  Copyright 2022 Silvere Martin-Michiellot
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.jants.io;

import org.jants.AbstractSimulation;

import java.io.*;

//may actually allow to start a simulation with an established colony, not just stating from a queen that has to build it's nest
//stores in particular the heightmap
public class SimulationReaderWriter {

    public AbstractSimulation readSimulation(String path) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
        AbstractSimulation abstractSimulation = (AbstractSimulation)in.readObject();
        in.close();
        return abstractSimulation;
    }

    public void writeSimulation(String path, AbstractSimulation abstractSimulation) throws IOException {
        FileOutputStream fout = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeObject(abstractSimulation);
        out.flush();
        out.close();
    }

}
