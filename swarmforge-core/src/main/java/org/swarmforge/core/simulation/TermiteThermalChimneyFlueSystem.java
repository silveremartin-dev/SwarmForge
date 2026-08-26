package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles vertical clay chimney flues drawing cool ambient air into subterranean fungus chambers.
 */
public class TermiteThermalChimneyFlueSystem {

    public void processChimneyFlueVentilation(Individual termite, boolean hotNest) {
        if (termite == null) return;
        Species species = termite.getSpecies();
        if (species == null || !species.canConstructThermalChimneyFlues()) return;

        if (hotNest && termite.isAlive()) {
            termite.setEnergy(Math.max(0.0f, termite.getEnergy() - 0.025f));
        }
    }
}
