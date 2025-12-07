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

package org.jants.species;

import org.jants.AbstractEusocialndividual;
import org.jants.Species;

//https://en.wikipedia.org/wiki/Meat_ant
//singleton class
public final class MeatAntSpecies extends AbstractEusocialInsectSpecies {

    private static class MeatAntSpeciesHolder {
        private final static MeatAntSpecies instance = new MeatAntSpecies();

    }

    public static MeatAntSpecies getInstance() {
        return MeatAntSpeciesHolder.instance;
    }

    private MeatAntSpecies() {
        super();
    }

    @Override
    public final String getScientificName() {
        return "Iridomyrmex purpureus";
    }

    @Override
    public final String getName() {
        return "Meat ant";
    }

    @Override
    //avoid calling this method, prefer createIndividual(int sex, int stage, int scentId, int x, int y, int z)
    public MeatAntIndividual createIndividual(int x, int y, int z) {
        return new MeatAntIndividual(this, 0, 0, 0);
    }

    public MeatAntIndividual createIndividual(int sex, int stage, int scentId, int x, int y, int z) {
        return new MeatAntIndividual(this, sex, stage, scentId, x, y, z);
    }

    private class MeatAntIndividual extends AbstractEusocialndividual {

        private MeatAntIndividual(Species species, int x, int y, int z) {
            super(AbstractEusocialndividual.sexWorker, AbstractEusocialndividual.stageEgg, AbstractEusocialndividual.scentUndefined, x, y, z);
            this.species = species;
        }

        private MeatAntIndividual(Species species, int sex, int stage, int scentId, int x, int y, int z) {
            super(sex, stage, scentId, x, y, z);
            this.species = species;
        }

        @Override
        public int behave() {
            return super.behave();
        }

    }

}
