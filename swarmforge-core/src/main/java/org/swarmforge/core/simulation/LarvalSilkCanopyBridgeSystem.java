package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles inter-canopy silk bridge weaving using larval silk threads.
 */
public class LarvalSilkCanopyBridgeSystem {

    public void processSilkCanopyBridge(Individual weaver, boolean canopyGapDetected) {
        if (weaver == null) return;
        Species species = weaver.getSpecies();
        if (species == null || !species.canWeaveLarvalSilkCanopyBridges()) return;

        if (canopyGapDetected && weaver.isAlive()) {
            weaver.setEnergy(Math.max(0.0f, weaver.getEnergy() - 0.03f));
        }
    }
}
