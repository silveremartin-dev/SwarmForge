package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles using dorsal rim area ommatidia to decode UV atmospheric polarization patterns for path integration.
 */
public class PolarizedLightCompassNavigationSystem {

    public void processPolarizedLightNavigation(Individual ant, boolean skyVisible) {
        if (ant == null) return;
        Species species = ant.getSpecies();
        if (species == null || !species.canNavigatePolarizedLightCompass()) return;

        if (skyVisible && ant.isAlive()) {
            ant.setEnergy(Math.max(0.0f, ant.getEnergy() - 0.005f));
        }
    }
}
