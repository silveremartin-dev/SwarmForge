package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles sterile soldier aphid horn stabbing and hemolymph gluing of ladybug larvae predators.
 */
public class AphidSoldierHornStabbingSystem {

    public void processHornStab(Individual soldierAphid, boolean predatorThreat) {
        if (soldierAphid == null) return;
        Species species = soldierAphid.getSpecies();
        if (species == null || !species.canStabFrontalHornsAphid()) return;

        if (predatorThreat && soldierAphid.isAlive()) {
            soldierAphid.setEnergy(Math.max(0.0f, soldierAphid.getEnergy() - 0.05f));
        }
    }
}
