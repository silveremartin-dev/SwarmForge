package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles abdominal alarm pheromone burst signaling nymphs to cluster tightly under maternal shield in parent bugs.
 */
public class ParentBugAlarmGatheringSystem {

    public void processAlarmGathering(Individual motherBug, boolean predatorApproaching) {
        if (motherBug == null) return;
        Species species = motherBug.getSpecies();
        if (species == null || !species.canEmitParentBugAlarmGathering()) return;

        if (predatorApproaching && motherBug.isAlive()) {
            motherBug.setEnergy(Math.max(0.0f, motherBug.getEnergy() - 0.02f));
        }
    }
}
