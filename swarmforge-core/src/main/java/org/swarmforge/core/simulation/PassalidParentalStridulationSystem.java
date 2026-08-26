package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles passalid beetle adult/larval stridulatory acoustic communication during masticated wood frass feeding.
 */
public class PassalidParentalStridulationSystem {

    public void processPassalidStridulation(Individual beetle, boolean larvaFeeding) {
        if (beetle == null) return;
        Species species = beetle.getSpecies();
        if (species == null || !species.canStridulatePassalidParentalCare()) return;

        if (larvaFeeding && beetle.isAlive()) {
            beetle.setEnergy(Math.max(0.0f, beetle.getEnergy() - 0.01f));
        }
    }
}
