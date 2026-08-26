package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles sternal acoustic pulse signaling guard shift transitions at nest entrances.
 */
public class GuardShiftVibrationalWhisperSystem {

    public void processGuardRelief(Individual outgoingGuard, Individual incomingGuard) {
        if (outgoingGuard == null || incomingGuard == null) return;
        Species species = outgoingGuard.getSpecies();
        if (species == null || !species.canPerformGuardShiftVibrationalWhisper()) return;

        if (outgoingGuard.getEnergy() < 0.2f && incomingGuard.getEnergy() > 0.8f) {
            outgoingGuard.setEnergy(outgoingGuard.getEnergy() + 0.1f);
        }
    }
}
