/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;

/**
 * Solar Mound Orientation & Thermal Capture System.
 * Models asymmetric south-sloping pine needle mounds (Formica rufa group)
 * capturing early spring solar radiation to accelerate brood development.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SolarMoundSystem {

    public static float calculateMoundSolarGain(Colony colony, float sunAngleDegrees, float ambientTemp) {
        if (colony == null || colony.getSpecies() == null) return ambientTemp;
        if (!colony.getSpecies().hasSolarOrientedMound()) return ambientTemp;

        // South orientation (180 deg) solar gain boost
        float solarIncidence = (float) Math.cos(Math.toRadians(sunAngleDegrees - 180.0f));
        if (solarIncidence > 0) {
            return ambientTemp + solarIncidence * 6.5f; // Up to +6.5°C thermal gain inside south face
        }
        return ambientTemp;
    }
}
