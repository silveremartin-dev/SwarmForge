package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles traplining spatial memory trajectories for bumblebee foraging route optimization.
 */
public class TrapliningFlightRouteSystem {

    public void processTrapliningRoute(Individual bumblebee, boolean multiPatchVisited) {
        if (bumblebee == null) return;
        Species species = bumblebee.getSpecies();
        if (species == null || !species.canLearnTrapliningFlightRoutes()) return;

        if (multiPatchVisited && bumblebee.isAlive()) {
            bumblebee.setEnergy(Math.min(100.0f, bumblebee.getEnergy() + 0.02f));
        }
    }
}
