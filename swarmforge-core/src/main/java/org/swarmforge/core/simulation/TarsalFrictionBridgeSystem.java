package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles forming multi-individual tensile bridges pulling heavy tree branches together using tarsal adhesive pads.
 */
public class TarsalFrictionBridgeSystem {

    public void processTarsalFrictionBridge(Individual weaverAnt, boolean branchPullingActive) {
        if (weaverAnt == null) return;
        Species species = weaverAnt.getSpecies();
        if (species == null || !species.canFormTarsalFrictionBridge()) return;

        if (branchPullingActive && weaverAnt.isAlive()) {
            weaverAnt.setEnergy(Math.max(0.0f, weaverAnt.getEnergy() - 0.03f));
        }
    }
}
