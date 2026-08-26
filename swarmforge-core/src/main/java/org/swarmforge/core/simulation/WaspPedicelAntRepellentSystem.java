package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles applying abdominal gland secretions onto nest attachment pedicels to deter climbing ants.
 */
public class WaspPedicelAntRepellentSystem {

    public void processPedicelRepellentCoating(Individual wasp, boolean antScoutThreat) {
        if (wasp == null) return;
        Species species = wasp.getSpecies();
        if (species == null || !species.canCoatWaspPedicelAntRepellent()) return;

        if (antScoutThreat && wasp.isAlive()) {
            wasp.setEnergy(Math.max(0.0f, wasp.getEnergy() - 0.015f));
        }
    }
}
