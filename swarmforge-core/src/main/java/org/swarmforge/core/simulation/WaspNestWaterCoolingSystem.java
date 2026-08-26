package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles water droplet regurgitation on cell caps followed by wing fanning for evaporative cooling in wasps.
 */
public class WaspNestWaterCoolingSystem {

    public void processWaterCooling(Individual hornet, boolean overheatingAlert) {
        if (hornet == null) return;
        Species species = hornet.getSpecies();
        if (species == null || !species.canCoolNestWaterRegurgitation()) return;

        if (overheatingAlert && hornet.isAlive()) {
            hornet.setEnergy(Math.max(0.0f, hornet.getEnergy() - 0.02f));
        }
    }
}
