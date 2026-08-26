package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles collective silk tethering and immobilization of parasitic beetles inside the nest.
 */
public class ParasiteSilkBindingSystem {

    public void processParasiteSilkBinding(Individual worker, boolean myrmecophileDetected) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canBindParasitesWithSilk()) return;

        if (myrmecophileDetected && worker.isAlive()) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.03f));
        }
    }
}
