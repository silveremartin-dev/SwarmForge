package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles forming outer mantle layers of cool bees around the warm core during swarm cluster flight.
 */
public class HoneybeeSwarmCoreHeatShieldSystem {

    public void processSwarmHeatShielding(Individual bee, boolean swarmFlight) {
        if (bee == null) return;
        Species species = bee.getSpecies();
        if (species == null || !species.canShieldSwarmCoreHeat()) return;

        if (swarmFlight && bee.isAlive()) {
            bee.setEnergy(Math.max(0.0f, bee.getEnergy() - 0.02f));
        }
    }
}
