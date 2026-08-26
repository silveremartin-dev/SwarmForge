package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles hornet venom-spraying alarm recruitment triggering mass coordinated slaughter raids.
 */
public class HornetGroupAlarmPheromoneSystem {

    public void processHornetAlarmRaid(Individual hornetScout, boolean targetHiveLocated) {
        if (hornetScout == null) return;
        Species species = hornetScout.getSpecies();
        if (species == null || !species.canEmitHornetGroupAlarmPheromone()) return;

        if (targetHiveLocated && hornetScout.isAlive()) {
            hornetScout.setEnergy(Math.max(0.0f, hornetScout.getEnergy() - 0.03f));
        }
    }
}
