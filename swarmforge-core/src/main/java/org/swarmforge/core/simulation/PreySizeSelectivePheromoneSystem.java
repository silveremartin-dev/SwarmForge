package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles distinct chemical trails denoting prey mass for recruitment tuning.
 */
public class PreySizeSelectivePheromoneSystem {

    public void processTrailMarking(Individual scout, double preyMassGrams) {
        if (scout == null) return;
        Species species = scout.getSpecies();
        if (species == null || !species.hasPreySizeSelectivePheromones()) return;

        if (scout.isAlive() && preyMassGrams > 5.0) {
            scout.setEnergy(Math.max(0.0f, scout.getEnergy() - 0.01f));
        }
    }
}
