package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles specialized nasus nozzle squirt of sticky terpenoid defense glue by nasute termite soldiers.
 */
public class NasuteChemicalSquirtSystem {

    public void processNasuteSquirt(Individual soldier, boolean enemyDetected) {
        if (soldier == null) return;
        Species species = soldier.getSpecies();
        if (species == null || !species.canSquirtNasuteChemical()) return;

        if (enemyDetected && soldier.isAlive()) {
            soldier.setEnergy(Math.max(0.0f, soldier.getEnergy() - 0.04f));
        }
    }
}
