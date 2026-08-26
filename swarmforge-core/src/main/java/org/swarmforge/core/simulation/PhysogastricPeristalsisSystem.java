package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles termite queen massive abdominal muscular peristalsis supporting high daily egg laying (30,000 eggs/day).
 */
public class PhysogastricPeristalsisSystem {

    public void processPhysogastricPeristalsis(Individual queen, boolean eggLayingActive) {
        if (queen == null) return;
        Species species = queen.getSpecies();
        if (species == null || !species.canPerformPhysogastricPeristalsis()) return;

        if (eggLayingActive && queen.isAlive()) {
            queen.setEnergy(Math.max(0.0f, queen.getEnergy() - 0.02f));
        }
    }
}
