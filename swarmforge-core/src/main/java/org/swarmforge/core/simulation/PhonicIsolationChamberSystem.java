package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles phonic isolation clay chamber construction around queen royal cells.
 */
public class PhonicIsolationChamberSystem {

    public void processPhonicIsolation(Individual builder, boolean noiseThreat) {
        if (builder == null) return;
        Species species = builder.getSpecies();
        if (species == null || !species.canConstructPhonicIsolationChambers()) return;

        if (noiseThreat && builder.isAlive()) {
            builder.setEnergy(Math.max(0.0f, builder.getEnergy() - 0.02f));
        }
    }
}
