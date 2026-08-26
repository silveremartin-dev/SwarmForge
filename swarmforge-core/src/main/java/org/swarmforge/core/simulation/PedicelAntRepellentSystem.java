package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles glandular organ secretion coating on nest pedicel stalk to block ant invasion in paper wasps.
 */
public class PedicelAntRepellentSystem {

    public void processPedicelCoating(Individual wasp, boolean antThreat) {
        if (wasp == null) return;
        Species species = wasp.getSpecies();
        if (species == null || !species.canApplyPedicelAntRepellent()) return;

        if (antThreat && wasp.isAlive()) {
            wasp.setEnergy(Math.max(0.0f, wasp.getEnergy() - 0.02f));
        }
    }
}
