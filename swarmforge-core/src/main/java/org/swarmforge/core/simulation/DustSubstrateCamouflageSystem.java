package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Manages fine soil dust coating for visual predator camouflage.
 */
public class DustSubstrateCamouflageSystem {

    public void processDustCoating(Individual agent) {
        if (agent == null) return;
        Species species = agent.getSpecies();
        if (species == null || !species.canApplyDustSubstrateCamouflage()) return;

        if (agent.isAlive()) {
            agent.setHealth(Math.min(agent.getMaxHealth(), agent.getHealth() + 1.0f));
        }
    }
}
