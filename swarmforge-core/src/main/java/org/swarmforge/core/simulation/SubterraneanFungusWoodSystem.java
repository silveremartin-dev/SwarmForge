/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Subterranean Wood-Fungus Garden Cultivation System.
 * Models termites cultivating Termitomyces fungal combs on digested wood substrate inside underground chambers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SubterraneanFungusWoodSystem {

    public static boolean cultivateFungusComb(Individual termite, float woodSubstrateKg) {
        if (termite == null || termite.getSpecies() == null) return false;
        if (!termite.getSpecies().canCultivateWoodFungus()) return false;

        if (woodSubstrateKg > 0.1f) {
            // Transform wood substrate into fungal comb
            return true;
        }
        return false;
    }
}
