package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles substrate vibration warnings to foraging columns about fallen obstacles or cave-ins.
 */
public class SubstrateObstacleVibratoSystem {

    public void processObstacleVibrato(Individual scout, boolean trailBlocked) {
        if (scout == null) return;
        Species species = scout.getSpecies();
        if (species == null || !species.canEmitSubstrateObstacleVibrato()) return;

        if (trailBlocked && scout.isAlive()) {
            scout.setEnergy(Math.max(0.0f, scout.getEnergy() - 0.015f));
        }
    }
}
