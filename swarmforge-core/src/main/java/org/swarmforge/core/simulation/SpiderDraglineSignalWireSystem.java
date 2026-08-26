package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles sensing tension changes along communal draglines to coordinate simultaneous multi-spider strikes.
 */
public class SpiderDraglineSignalWireSystem {

    public void processDraglineSignalTrip(Individual spider, boolean preyTrippedWire) {
        if (spider == null) return;
        Species species = spider.getSpecies();
        if (species == null || !species.canSensePreySignalWireTripping()) return;

        if (preyTrippedWire && spider.isAlive()) {
            spider.setEnergy(Math.max(0.0f, spider.getEnergy() - 0.01f));
        }
    }
}
