package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles depositing rejection pheromones on sporulating infected pupal corpses.
 */
public class ParasitizedCadaverRepellentSystem {

    public void processCadaverRepellentMarking(Individual undertaker, boolean infectedCadaverPresent) {
        if (undertaker == null) return;
        Species species = undertaker.getSpecies();
        if (species == null || !species.canMarkParasitizedCadaverRepellent()) return;

        if (infectedCadaverPresent && undertaker.isAlive()) {
            undertaker.setEnergy(Math.max(0.0f, undertaker.getEnergy() - 0.015f));
        }
    }
}
