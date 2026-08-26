/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Acoustic Queen Suppression Piping System.
 * Models newly emerged virgin honeybee queens emitting high-frequency acoustic "piping" vocalizations
 * (tooting and quacking at 400-500 Hz) to challenge and locate unhatched rival queen cells.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class QueenPipingSystem {

    public static float emitQueenPiping(Individual queen, boolean isVirginQueen) {
        if (queen == null || queen.getSpecies() == null) return 0.0f;
        if (!queen.getSpecies().canPerformQueenPiping()) return 0.0f;

        if (isVirginQueen) {
            return 450.0f; // 450 Hz high-frequency acoustic piping call
        }
        return 0.0f;
    }
}
