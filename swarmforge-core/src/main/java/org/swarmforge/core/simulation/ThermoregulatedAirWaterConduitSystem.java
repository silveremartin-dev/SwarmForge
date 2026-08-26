package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Controls subterranean dual air-water ventilation conduit network construction.
 */
public class ThermoregulatedAirWaterConduitSystem {

    public void processConduitRegulation(Individual worker, double galleryTempC, double targetTempC) {
        if (worker == null) return;
        Species species = worker.getSpecies();
        if (species == null || !species.canConstructThermoregulatedConduits()) return;

        if (Math.abs(galleryTempC - targetTempC) > 5.0) {
            worker.setEnergy(Math.max(0.0f, worker.getEnergy() - 0.05f));
        }
    }
}
