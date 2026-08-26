package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles adult-pupal vibrational acoustic duetting coordinating gallery movements.
 */
public class PassalidSubstrateDuetSystem {

    public void processSubstrateDuet(Individual adult, Individual pupa) {
        if (adult == null || pupa == null) return;
        Species species = adult.getSpecies();
        if (species == null || !species.canDuetPassalidSubstrateVibration()) return;

        if (adult.isAlive() && pupa.isAlive()) {
            adult.setEnergy(Math.max(0.0f, adult.getEnergy() - 0.01f));
        }
    }
}
