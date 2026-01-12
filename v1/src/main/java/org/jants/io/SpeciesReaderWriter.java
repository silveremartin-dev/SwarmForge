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

import org.jants.Species;

import java.io.*;

// may be switch to implements java.io.Externalizable
// as the aim here is not to store a specific individual but the common parameters of the species it belongs to
// that said, it won't be possible using Externalizable at we may need extra parameters for specific species therefore needing as many subclasses as species
// eventually also, we would like to read and save in XML (through XSD schema) with all parameters related to a species to design a colony (in case of an ant species)
// but it is not really possible to code behave() in XML
public class SpeciesReaderWriter {

    public Species readSpecies(String path) throws IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(path));
        Species species = (Species)in.readObject();
        in.close();
        return species;
    }

    public void writeSpecies(String path, Species species) throws IOException {
        FileOutputStream fout = new FileOutputStream(path);
        ObjectOutputStream out = new ObjectOutputStream(fout);
        out.writeObject(species);
        out.flush();
        out.close();
    }

}
