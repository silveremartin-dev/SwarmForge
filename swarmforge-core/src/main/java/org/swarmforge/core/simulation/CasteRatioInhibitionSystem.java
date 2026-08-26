/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;

/**
 * Pheromonal Caste Ratio Feedback Regulation System.
 * Models soldier caste pheromones inhibiting further soldier differentiation when soldier population ratio exceeds 15%.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class CasteRatioInhibitionSystem {

    public static boolean isSoldierDifferentiationSuppressed(Colony colony, float currentSoldierRatio) {
        if (colony == null || colony.getSpecies() == null) return false;
        if (!colony.getSpecies().hasCasteRatioPheromoneInhibition()) return false;

        return currentSoldierRatio >= 0.15f; // Suppress soldier differentiation if >= 15%
    }
}
