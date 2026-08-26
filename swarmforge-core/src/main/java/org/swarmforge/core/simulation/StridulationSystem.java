/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Emergency Rescue Stridulation System.
 * Models abdominal plectrum-file stridulation in leafcutter ants (Atta, Pogonomyrmex)
 * trapped during tunnel cave-ins, emitting substrate acoustic vibrations to guide excavating rescuers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class StridulationSystem {

    public static float emitRescueStridulation(Individual trappedAnt, boolean isTrappedUnderground) {
        if (trappedAnt == null || trappedAnt.getSpecies() == null) return 0.0f;
        if (!trappedAnt.getSpecies().canStridulateRescueCall()) return 0.0f;

        if (isTrappedUnderground) {
            return 85.0f; // 85 dB substrate acoustic distress signal broadcast
        }
        return 0.0f;
    }
}
