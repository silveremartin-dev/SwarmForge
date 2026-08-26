package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles building trumpet-shaped mud and resin entrance tubes guarded by specialized stingless bee soldiers.
 */
public class MudResinEntranceFunnelSystem {

    public void processMudResinEntranceBuilding(Individual bee, boolean nestEntranceBuilding) {
        if (bee == null) return;
        Species species = bee.getSpecies();
        if (species == null || !species.canConstructMudResinEntranceFunnel()) return;

        if (nestEntranceBuilding && bee.isAlive()) {
            bee.setEnergy(Math.max(0.0f, bee.getEnergy() - 0.025f));
        }
    }
}
