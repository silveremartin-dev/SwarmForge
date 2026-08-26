package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles water-resistant salivary-clay mortar sealing gallery breaches against desiccation in subterranean termites.
 */
public class TermiteSalivaryCementMoistureSealSystem {

    public void processSalivaryCementSeal(Individual worker, boolean wallBreachDetected) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canApplySalivaryCementMoistureSeal()) return;

        if (wallBreachDetected && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.02f));
        }
    }
}
