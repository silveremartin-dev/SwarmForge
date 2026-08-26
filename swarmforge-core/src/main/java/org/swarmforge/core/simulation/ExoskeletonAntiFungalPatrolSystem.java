package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Manages cuticular acid washing patrols protecting callow worker exoskeletons.
 */
public class ExoskeletonAntiFungalPatrolSystem {

    public void processCallowSanitization(Individual nurse, Individual callow) {
        if (nurse == null || callow == null) return;
        Species species = nurse.getSpecies();
        if (species == null || !species.canPerformExoskeletonAntiFungalPatrol()) return;

        if (callow.getAge() < 1.0f) {
            callow.setHealth(Math.min(callow.getMaxHealth(), callow.getHealth() + 5.0f));
        }
    }
}
