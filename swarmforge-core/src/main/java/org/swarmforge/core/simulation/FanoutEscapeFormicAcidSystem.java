package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles 360-degree fan-out escape response upon detecting enemy formic acid emissions.
 */
public class FanoutEscapeFormicAcidSystem {

    public void processFanoutEscape(Individual worker, boolean formicAcidDetected) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canPerformFanoutEscapeFormicAcid()) return;

        if (formicAcidDetected && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.03f));
        }
    }
}
