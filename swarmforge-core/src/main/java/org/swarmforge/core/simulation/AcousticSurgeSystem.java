/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Acoustic Stridulation Prey Surge System.
 * Models predatory ants emitting high-frequency cuticular stridulations to coordinate simultaneous attacks on large prey.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AcousticSurgeSystem {

    public static float emitPreySurgeSignal(Individual hunter) {
        if (hunter == null || hunter.getSpecies() == null) return 0.0f;
        if (!hunter.getSpecies().canEmitAcousticPreySurge()) return 0.0f;

        return 75.0f; // 75 dB acoustic surge pulse
    }
}
