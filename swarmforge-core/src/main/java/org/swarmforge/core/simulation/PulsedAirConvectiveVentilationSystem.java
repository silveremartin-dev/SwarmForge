package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles synchronized abdominal pumping creating forced air circulation in deep galleries.
 */
public class PulsedAirConvectiveVentilationSystem {

    public void processPulsedVentilation(Individual worker, boolean highCo2Level) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canPerformPulsedAirConvectiveVentilation()) return;

        if (highCo2Level && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.02f));
        }
    }
}
