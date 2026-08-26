package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles holding living larvae like silk glue-guns to stitch nest leaves together.
 */
public class LarvalSilkHarnessSystem {

    public void processLarvalSilkHarness(Individual weaver, boolean leafEdgesAligned) {
        if (weaver == null) return;
        Species species = weaver.getSpecies();
        if (species == null || !species.canHarnessLarvalSilkCocoon()) return;

        if (leafEdgesAligned && weaver.isAlive()) {
            weaver.setEnergy(Math.max(0.0f, weaver.getEnergy() - 0.025f));
        }
    }
}
