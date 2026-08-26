package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles mandible snapping against wooden gallery walls creating acoustic alarms in termites.
 */
public class TermiteMandibleSnapAlarmSystem {

    public void processMandibleSnapAlarm(Individual soldier, boolean intrusionDetected) {
        if (soldier == null) return;
        Species species = soldier.getSpecies();
        if (species == null || !species.canSnapMandibleAcousticAlarm()) return;

        if (intrusionDetected && soldier.isAlive()) {
            soldier.setEnergy(Math.max(0.0f, soldier.getEnergy() - 0.02f));
        }
    }
}
