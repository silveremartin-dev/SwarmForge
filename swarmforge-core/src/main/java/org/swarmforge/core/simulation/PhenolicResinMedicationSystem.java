package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles selective ingestion of phenolic plant resins to boost immune defenses during fungal outbreaks.
 */
public class PhenolicResinMedicationSystem {

    public void processResinMedication(Individual ant, boolean fungalOutbreak) {
        if (ant == null) return;
        Species species = ant.getSpecies();
        if (species == null || !species.canIngestPhenolicResinMedication()) return;

        if (fungalOutbreak && ant.isAlive()) {
            ant.setEnergy(Math.max(0.0f, ant.getEnergy() - 0.01f));
        }
    }
}
