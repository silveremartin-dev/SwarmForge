package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles harvesting micro-droplets condensed on abdominal cuticular setae during morning fogs.
 */
public class CuticularWaterCondensationSystem {

    public void processWaterCondensationHarvesting(Individual ant, boolean fogPresent) {
        if (ant == null) return;
        Species species = ant.getSpecies();
        if (species == null || !species.canHarvestCuticularWaterCondensation()) return;

        if (fogPresent && ant.isAlive()) {
            ant.setEnergy(Math.min(100.0f, ant.getEnergy() + 0.02f));
        }
    }
}
