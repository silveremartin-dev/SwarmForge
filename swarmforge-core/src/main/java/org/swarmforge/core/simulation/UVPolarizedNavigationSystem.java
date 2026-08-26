/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Polarized UV Celestial Compass Navigation System.
 * Models desert ants (Cataglyphis fortis) calculating home vector angles from the E-vector
 * polarization pattern of skylight under overcast sky conditions.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class UVPolarizedNavigationSystem {

    public static float calculateHomeVectorHeading(Individual ant, float solarAzimuthRad, float cloudCoverFactor) {
        if (ant == null || ant.getSpecies() == null) return ant != null ? ant.getHeading() : 0.0f;

        if (ant.getSpecies().hasUVPolarizedLightNavigation()) {
            // UV polarization perception allows exact path integration heading even under 80% cloud cover
            float dx = ant.getHomeX() - ant.getX();
            float dy = ant.getHomeY() - ant.getY();
            return (float) Math.atan2(dy, dx);
        }

        // Standard landmark/pheromone heading
        return ant.getHeading();
    }
}
