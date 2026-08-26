package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles elastic energy release in mandibles for catapult defense jumps.
 */
public class TrapMandibleCatapultSystem {

    public void processCatapultJump(Individual warrior, boolean acutePredatorThreat) {
        if (warrior == null) return;
        Species species = warrior.getSpecies();
        if (species == null || !species.canSnapTrapMandiblesCatapult()) return;

        if (acutePredatorThreat && warrior.isAlive()) {
            warrior.setEnergy(Math.max(0.0f, warrior.getEnergy() - 0.05f));
        }
    }
}
