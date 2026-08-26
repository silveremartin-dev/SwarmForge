package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles synchronous vibrato call triggering opening of upper vent chimneys upon mound overheating.
 */
public class MoundOverheatVibratoSystem {

    public void processOverheatVibrato(Individual worker, double moundTemperatureC) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canEmitMoundOverheatVibrato()) return;

        if (moundTemperatureC > 38.0 && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.02f));
        }
    }
}
