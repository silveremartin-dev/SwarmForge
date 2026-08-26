package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles emergency digging of vertical drainage shafts during water table breaches or nest flooding.
 */
public class VerticalDrainageShaftSystem {

    public void processVerticalDrainage(Individual worker, boolean floodingThreat) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canExcavateVerticalDrainageShafts()) return;

        if (floodingThreat && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.04f));
        }
    }
}
