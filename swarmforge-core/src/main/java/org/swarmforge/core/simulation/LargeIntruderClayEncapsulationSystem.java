package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles clay-saliva propolis mummification of large intruder carcasses.
 */
public class LargeIntruderClayEncapsulationSystem {

    public void processCarcassMummification(Individual worker, boolean largeCarcassInNest) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canEncapsulateLargeIntrudersClay()) return;

        if (largeCarcassInNest && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.03f));
        }
    }
}
