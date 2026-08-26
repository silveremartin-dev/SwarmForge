package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles micro-masticating leaf margins into fine pulp and applying fecal fluid digestive enzymes before comb insertion.
 */
public class LeafPulpEnzymeInoculationSystem {

    public void processLeafPulpInoculation(Individual worker, boolean leafDiscHarvested) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canInoculateLeafPulpEnzymes()) return;

        if (leafDiscHarvested && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.02f));
        }
    }
}
