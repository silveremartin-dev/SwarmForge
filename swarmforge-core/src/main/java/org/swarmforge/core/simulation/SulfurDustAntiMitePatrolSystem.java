package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles natural mineral sulfur dusting of brood chambers for parasite control.
 */
public class SulfurDustAntiMitePatrolSystem {

    public void processSulfurDustPatrol(Individual nurse, boolean parasiteThreat) {
        if (nurse == null) return;
        Species species = nurse.getSpecies();
        if (species == null || !species.canDepositSulfurDustAntiMitePatrol()) return;

        if (parasiteThreat && nurse.isAlive()) {
            nurse.setEnergy(Math.max(0.0f, nurse.getEnergy() - 0.02f));
        }
    }
}
