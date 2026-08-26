package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles assembling living sphagnum moss onto nest mounds for atmospheric humidity capture.
 */
public class SphagnumMoistureDomeSystem {

    public void processSphagnumDomeBuilding(Individual ant, boolean dryAirCondition) {
        if (ant == null) return;
        Species species = ant.getSpecies();
        if (species == null || !species.canConstructSphagnumMoistureDomes()) return;

        if (dryAirCondition && ant.isAlive()) {
            ant.setEnergy(Math.max(0.0f, ant.getEnergy() - 0.02f));
        }
    }
}
