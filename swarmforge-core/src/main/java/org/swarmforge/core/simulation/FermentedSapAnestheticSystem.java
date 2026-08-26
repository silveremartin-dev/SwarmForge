package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Manages fermented sap ingestion prior to major inter-colony battles.
 */
public class FermentedSapAnestheticSystem {

    public void processSapIngestion(Individual warrior, boolean warImminent) {
        if (warrior == null) return;
        Species species = warrior.getSpecies();
        if (species == null || !species.canConsumeFermentedSapAnesthetic()) return;

        if (warImminent && warrior.isAlive()) {
            warrior.setHealth(Math.min(100.0f, warrior.getHealth() + 10.0f));
        }
    }
}
