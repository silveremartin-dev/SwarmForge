package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles age-dependent abdominal autothysis rupture releasing toxic blue copper protein crystals in termites.
 */
public class FontanelleAutothysisSystem {

    public void processAutothysisSacrifice(Individual worker, boolean overwhelmedByEnemies) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canPerformFontanelleAutothysis()) return;

        if (overwhelmedByEnemies && worker.isAlive()) {
            worker.setEnergy(0.0f); // Self-sacrificial rupture
        }
    }
}
