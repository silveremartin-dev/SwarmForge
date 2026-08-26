/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Morning Solar Brood Basking System.
 * Models workers transporting larvae to sunlit mound surfaces on cool spring mornings to absorb radiant solar warmth before carrying them back indoors.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SolarBroodBaskingSystem {

    public static boolean transportBroodToSun(Individual nurse, float ambientTempCelsius) {
        if (nurse == null || nurse.getSpecies() == null) return false;
        if (!nurse.getSpecies().canPerformSolarBroodBasking()) return false;

        if (ambientTempCelsius >= 15.0f && ambientTempCelsius <= 22.0f) {
            // Move larvae to solar mound surface
            return true;
        }
        return false;
    }
}
