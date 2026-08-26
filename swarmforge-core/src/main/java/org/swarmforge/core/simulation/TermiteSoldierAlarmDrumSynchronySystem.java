package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles phase-locking head drumming across hundreds of soldiers to amplify substrate vibration.
 */
public class TermiteSoldierAlarmDrumSynchronySystem {

    public void processSoldierDrumSynchrony(Individual soldier, boolean alarmTriggered) {
        if (soldier == null) return;
        Species species = soldier.getSpecies();
        if (species == null || !species.canSynchronizeSoldierAlarmDrumming()) return;

        if (alarmTriggered && soldier.isAlive()) {
            soldier.setEnergy(Math.max(0.0f, soldier.getEnergy() - 0.015f));
        }
    }
}
