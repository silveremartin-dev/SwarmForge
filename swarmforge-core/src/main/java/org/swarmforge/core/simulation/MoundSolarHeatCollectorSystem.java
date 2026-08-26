package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles workers basking in morning sunlight on mound surfaces and carrying absorbed body heat back down into deep subterranean brood chambers.
 */
public class MoundSolarHeatCollectorSystem {

    public void processSolarHeatCollection(Individual woodAnt, boolean morningSunlight) {
        if (woodAnt == null) return;
        Species species = woodAnt.getSpecies();
        if (species == null || !species.canClusterSolarHeatCollector()) return;

        if (morningSunlight && woodAnt.isAlive()) {
            woodAnt.setEnergy(Math.max(0.0f, woodAnt.getEnergy() - 0.015f));
        }
    }
}
