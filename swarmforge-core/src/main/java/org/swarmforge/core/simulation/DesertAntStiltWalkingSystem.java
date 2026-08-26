package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles raising body high on long legs ("stilt walking") and pausing on dry grass stems to cool off above 50°C desert sand.
 */
public class DesertAntStiltWalkingSystem {

    public void processStiltWalkingCooling(Individual desertAnt, boolean extremeSandHeat) {
        if (desertAnt == null) return;
        Species species = desertAnt.getSpecies();
        if (species == null || !species.canStiltWalkThermalRegim()) return;

        if (extremeSandHeat && desertAnt.isAlive()) {
            desertAnt.setEnergy(Math.max(0.0f, desertAnt.getEnergy() - 0.01f));
        }
    }
}
