package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles cultivation of Streptomyces cuticular bacteria producing antifungals.
 */
public class StreptomycesAntibioticsSystem {

    public void processAntibioticsCultivation(Individual worker, boolean pathogenExposed) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canCultivateStreptomycesAntibiotics()) return;

        if (pathogenExposed && worker.isAlive()) {
            worker.setHealth(Math.min(100.0f, worker.getHealth() + 0.03f));
        }
    }
}
