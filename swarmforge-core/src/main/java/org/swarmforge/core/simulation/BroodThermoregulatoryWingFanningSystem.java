package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles fanning wings over brood comb cells during high ambient heat to cool larvae.
 */
public class BroodThermoregulatoryWingFanningSystem {

    public void processThermoregulatoryWingFanning(Individual worker, boolean highHeat) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canFanWingsForBroodThermoregulation()) return;

        if (highHeat && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.02f));
        }
    }
}
