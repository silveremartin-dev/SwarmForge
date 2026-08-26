package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles pedestrian column migration with complete brood and resource transfer.
 */
public class PedestrianSwarmBuddingSystem {

    public void processSwarmBudding(Individual migrant, boolean nestCapacityReached) {
        if (migrant == null) return;
        Species species = migrant.getSpecies();
        if (species == null || !species.canPerformPedestrianSwarmBudding()) return;

        if (nestCapacityReached && migrant.isAlive()) {
            migrant.setEnergy(Math.max(0.0f, migrant.getEnergy() - 0.03f));
        }
    }
}
