/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Queen Recognition Stridulation System.
 * Models low-frequency acoustic stridulations emitted by the queen mother to suppress worker aggression during royal visits.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class QueenRecognitionStridulationSystem {

    public static float emitQueenRecognitionSignal(Individual queen) {
        if (queen == null || queen.getSpecies() == null) return 0.0f;
        if (!queen.getSpecies().canStridulateQueenRecognition()) return 0.0f;

        return 120.0f; // 120 Hz acoustic recognition tone
    }
}
