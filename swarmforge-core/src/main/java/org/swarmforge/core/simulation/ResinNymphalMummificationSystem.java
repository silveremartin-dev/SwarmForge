package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles antiseptic resin sealing of queen pupal cocoons during metamorphosis.
 */
public class ResinNymphalMummificationSystem {

    public void processResinMummification(Individual nurse, boolean queenPupaPresent) {
        if (nurse == null) return;
        Species species = nurse.getSpecies();
        if (species == null || !species.canResinMummifyNymphalChambers()) return;

        if (queenPupaPresent && nurse.isAlive()) {
            nurse.setEnergy(Math.max(0.0f, nurse.getEnergy() - 0.03f));
        }
    }
}
