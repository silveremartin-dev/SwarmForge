/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Granary Seed De-Germination System.
 * Models harvester ants (Messor barbarus) destroying the embryo and wings of collected seeds
 * to prevent subterranean germination in damp granary chambers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SeedStorageSystem {

    public static boolean deGermSeed(Individual harvester, boolean isSeedSprouting) {
        if (harvester == null || harvester.getSpecies() == null) return false;
        if (!harvester.getSpecies().canDeGermStoredSeeds()) return false;

        if (isSeedSprouting) {
            // Bite off radicle/embryo to preserve seed as dry flour reserve
            return true;
        }
        return false;
    }
}
