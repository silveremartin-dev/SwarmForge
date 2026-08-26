package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles tarsal brush grooming of antennal sensilla to maintain chemical acuity.
 */
public class AntennalDustGroomingSystem {

    public void processAntennalGrooming(Individual worker, boolean dustAccumulated) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canPerformAntennalDustGrooming()) return;

        if (dustAccumulated && worker.isAlive()) {
            worker.setHealth(Math.min(100.0f, worker.getHealth() + 0.01f));
        }
    }
}
