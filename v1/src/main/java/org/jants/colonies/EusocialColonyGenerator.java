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

package org.jants.colonies;

import org.jants.AbstractEusocialColony;
import org.jants.species.AbstractEusocialInsectSpecies;

// a colony may actually contain more than one nest (may be separated by up to several hundred metres)
// ants navigate from nest to nest following queen scent and colony needs
public class EusocialColonyGenerator {

    // https://en.wikipedia.org/wiki/Ant_colony
    public final static int colonyMonogyny = 1; // Establishment of an ant colony under a single egg-laying queen.
    public final static int colonyPolygyny = 2; // Establishment of an ant colony under multiple egg-laying queens.
    public final static int colonyOligogyny = 3; //Establishment of a polygynous colony where the multiple egg-laying queens remain far apart from one another in the nest.
    public final static int colonyHaplometrosis = 4; // Establishment of a colony by a single queen.
    public final static int colonyPleometrosis = 5; // Establishment of a colony by multiple queens.
    public final static int colonyMonodomy = 6; // Establishment of a colony at a single nest site.
    public final static int colonyPolydomy = 7; // Establishment of a colony across multiple nest sites.

    // https://en.wikipedia.org/wiki/Ant_supercolony
    public final static int colonyTypeUnicoloniality = 1;
    public final static int colonyTypeSupercoloniality = 2;
    public final static int colonyTypeMulticoloniality = 3;

    public AbstractEusocialColony generateColony(AbstractEusocialInsectSpecies abstractEusocialInsectSpecies, int colonytype, int colonialityType) {
        throw new RuntimeException("Not yes implemented");
    }

}
