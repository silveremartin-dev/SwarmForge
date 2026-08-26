package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles termite cultivation of Termitomyces fungal gardens on chewed fecal pellets to digest lignin.
 */
public class TermiteFungalCombSystem {

    public void processFungalCombInoculation(Individual termiteWorker, boolean combCellPrepared) {
        if (termiteWorker == null) return;
        Species species = termiteWorker.getSpecies();
        if (species == null || !species.canInoculateFungalCombTermite()) return;

        if (combCellPrepared && termiteWorker.isAlive()) {
            termiteWorker.setEnergy(Math.max(0.0f, termiteWorker.getEnergy() - 0.03f));
        }
    }
}
