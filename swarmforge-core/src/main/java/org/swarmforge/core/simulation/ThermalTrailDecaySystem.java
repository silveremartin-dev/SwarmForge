/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.species.Species;

/**
 * Temperature-Dependent Q10 Pheromone Trail Decay System.
 * Models chemical trail degradation rate as an Arrhenius exponential function of soil temperature.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ThermalTrailDecaySystem {

    public static float calculateDecayedIntensity(float currentIntensity, float soilTempCelsius, Species species) {
        if (species != null && !species.hasThermalTrailDecay()) {
            return currentIntensity * 0.99f; // Standard static decay
        }

        // Q10 thermal degradation factor (Decay doubles every 10°C increase)
        float tempDiff = soilTempCelsius - 20.0f;
        float q10Factor = (float) Math.pow(2.0, tempDiff / 10.0);
        float decayRate = 0.01f * q10Factor;

        return Math.max(0.0f, currentIntensity * (1.0f - decayRate));
    }
}
