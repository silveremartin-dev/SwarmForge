package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles weaving silk pavilions over honeydew aphid herds to shield them from rain and predators.
 */
public class WeaverSilkPavilionAphidShelterSystem {

    public void processSilkPavilionWeaving(Individual weaver, boolean aphidHerdExposed) {
        if (weaver == null) return;
        Species species = weaver.getSpecies();
        if (species == null || !species.canWeaveSilkPavilionAphidShelter()) return;

        if (aphidHerdExposed && weaver.isAlive()) {
            weaver.setEnergy(Math.max(0.0f, weaver.getEnergy() - 0.03f));
        }
    }
}
