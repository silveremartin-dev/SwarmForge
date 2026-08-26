package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles using flattened sclerotized abdominal tips to plug narrow gall entrance tubes.
 */
public class ThripsChitinousTubePlugSystem {

    public void processGallTubePlug(Individual thrips, boolean enemyApproaching) {
        if (thrips == null) return;
        Species species = thrips.getSpecies();
        if (species == null || !species.canPlugGallWithChitinousTube()) return;

        if (enemyApproaching && thrips.isAlive()) {
            thrips.setEnergy(Math.max(0.0f, thrips.getEnergy() - 0.015f));
        }
    }
}
