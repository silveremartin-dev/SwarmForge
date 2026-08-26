package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles coating egg clutches in protective antimicrobial mucilage to block parasitoid wasps.
 */
public class EggMassMucilageEnvelopeSystem {

    public void processEggMucilageCoating(Individual motherBeetle, boolean eggLaid) {
        if (motherBeetle == null) return;
        Species species = motherBeetle.getSpecies();
        if (species == null || !species.canApplyEggMassMucilageEnvelope()) return;

        if (eggLaid && motherBeetle.isAlive()) {
            motherBeetle.setEnergy(Math.max(0.0f, motherBeetle.getEnergy() - 0.015f));
        }
    }
}
