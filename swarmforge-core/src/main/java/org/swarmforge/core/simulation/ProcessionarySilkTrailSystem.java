package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles single file procession guided by silk thread and abdominal trail pheromones in caterpillars.
 */
public class ProcessionarySilkTrailSystem {

    public void processProcessionaryTrail(Individual caterpillar, boolean inProcession) {
        if (caterpillar == null) return;
        Species species = caterpillar.getSpecies();
        if (species == null || !species.canFormProcessionarySilkTrail()) return;

        if (inProcession && caterpillar.isAlive()) {
            caterpillar.setEnergy(Math.max(0.0f, caterpillar.getEnergy() - 0.01f));
        }
    }
}
