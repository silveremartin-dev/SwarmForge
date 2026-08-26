package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles eusocial shrimp giant chela cavitation snap producing acoustic shockwaves in sponge canals.
 */
public class EusocialShrimpClawShockwaveSystem {

    public void processClawShockwave(Individual shrimp, boolean canalIntruder) {
        if (shrimp == null) return;
        Species species = shrimp.getSpecies();
        if (species == null || !species.canSnapClawAcousticShockwave()) return;

        if (canalIntruder && shrimp.isAlive()) {
            shrimp.setEnergy(Math.max(0.0f, shrimp.getEnergy() - 0.05f));
        }
    }
}
