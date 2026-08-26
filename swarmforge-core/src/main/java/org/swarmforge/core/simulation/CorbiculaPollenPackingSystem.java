package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles moistening pollen with nectar and combing it into hind tibia corbiculae.
 */
public class CorbiculaPollenPackingSystem {

    public void processCorbiculaPollenPacking(Individual bee, boolean pollenGathered) {
        if (bee == null) return;
        Species species = bee.getSpecies();
        if (species == null || !species.canPackCorbiculaPollenBaskets()) return;

        if (pollenGathered && bee.isAlive()) {
            bee.setEnergy(Math.max(0.0f, bee.getEnergy() - 0.015f));
        }
    }
}
