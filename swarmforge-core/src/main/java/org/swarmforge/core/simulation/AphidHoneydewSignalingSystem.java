package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles abdominal flicks ejecting honeydew droplets to recruit mutualist ants in eusocial aphids.
 */
public class AphidHoneydewSignalingSystem {

    public void processHoneydewSignal(Individual aphid, boolean antNearby) {
        if (aphid == null) return;
        Species species = aphid.getSpecies();
        if (species == null || !species.canEjectHoneydewSignalingDroplets()) return;

        if (antNearby && aphid.isAlive()) {
            aphid.setEnergy(Math.max(0.0f, aphid.getEnergy() - 0.01f));
        }
    }
}
