package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles firing high-speed water jets creating cavitation bubbles against sponge intruders.
 */
public class ShrimpAcousticCannonSystem {

    public void processAcousticCannon(Individual shrimp, boolean intruderDetected) {
        if (shrimp == null) return;
        Species species = shrimp.getSpecies();
        if (species == null || !species.canFireShrimpAcousticCannon()) return;

        if (intruderDetected && shrimp.isAlive()) {
            shrimp.setEnergy(Math.max(0.0f, shrimp.getEnergy() - 0.035f));
        }
    }
}
