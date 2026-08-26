package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles squirting sticky terpenoid threads from frontal nozzle to entangle enemy ants.
 */
public class NasuteViscousResinSquirtSystem {

    public void processNasuteResinSquirt(Individual nasute, boolean antHostileTarget) {
        if (nasute == null) return;
        Species species = nasute.getSpecies();
        if (species == null || !species.canSquirtNasuteViscousResin()) return;

        if (antHostileTarget && nasute.isAlive()) {
            nasute.setEnergy(Math.max(0.0f, nasute.getEnergy() - 0.03f));
        }
    }
}
