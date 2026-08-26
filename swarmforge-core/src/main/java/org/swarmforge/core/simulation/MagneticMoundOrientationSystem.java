package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;

/**
 * Handles building wedge-shaped mounds strictly aligned N-S along geomagnetic lines for solar thermoregulation.
 */
public class MagneticMoundOrientationSystem {

    public void processMagneticAlignment(Individual builder, boolean geomagnetismSensed) {
        if (builder == null) return;
        Species species = builder.getSpecies();
        if (species == null || !species.canOrientMagneticMound()) return;

        if (geomagnetismSensed && builder.isAlive()) {
            builder.setEnergy(Math.max(0.0f, builder.getEnergy() - 0.02f));
        }
    }
}
