/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Self-Assembled Living Architectural Bridges.
 * Models Eciton army ants linking tarsi across spatial gaps to form living bridges that shorten foraging paths.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LivingBridgeSystem {

    public static boolean formLivingBridge(Individual ant, float gapWidthMeters) {
        if (ant == null || ant.getSpecies() == null) return false;
        if (!ant.getSpecies().canFormLivingBridges()) return false;

        if (gapWidthMeters <= 0.5f) {
            // Anchor body across spatial gap
            return true;
        }
        return false;
    }
}
