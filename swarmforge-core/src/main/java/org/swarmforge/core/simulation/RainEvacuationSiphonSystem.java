package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles hydraulic curved siphon conduits for automatic storm water evacuation.
 */
public class RainEvacuationSiphonSystem {

    public void processRainSiphon(Individual engineer, boolean floodRisk) {
        if (engineer == null) return;
        Species species = engineer.getSpecies();
        if (species == null || !species.canConstructRainEvacuationSiphons()) return;

        if (floodRisk && engineer.isAlive()) {
            engineer.setEnergy(Math.max(0.0f, engineer.getEnergy() - 0.04f));
        }
    }
}
