package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles non-lethal ritual wrestling matches establishing reproductive dominance.
 */
public class RitualMandibularWrestlingSystem {

    public void processRitualWrestling(Individual participantA, Individual participantB) {
        if (participantA == null || participantB == null) return;
        Species species = participantA.getSpecies();
        if (species == null || !species.canPerformRitualMandibularWrestling()) return;

        if (participantA.isAlive() && participantB.isAlive()) {
            participantA.setEnergy(Math.max(0.0f, participantA.getEnergy() - 0.01f));
        }
    }
}
