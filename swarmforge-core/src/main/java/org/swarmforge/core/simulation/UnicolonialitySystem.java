/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Unicoloniality & Supercolony Intrusive Acceptance System (Linepithema humile).
 * Simulates bottleneck-induced Cuticular Hydrocarbon (CHC) profile homogeneity.
 * Disables inter-nest aggression between geographically distant nests belonging to the same supercolony,
 * enabling fluid worker and queen exchanges across polycalic networks.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class UnicolonialitySystem {

    /**
     * Evaluates if two individuals belong to an invasive unicolonial supercolony network.
     */
    public static boolean isUnicolonialAcceptance(Individual a, Individual b, boolean isUnicolonialSpecies) {
        if (!isUnicolonialSpecies) return false;
        if (a == null || b == null) return false;
        // In unicolonial species (e.g. Argentine Ant), nestmate recognition threshold is relaxed to 0.25 similarity
        float odorSim = CuticularHydrocarbonSystem.computeOdorSimilarity(a.getChcProfile(), b.getChcProfile());
        return odorSim >= 0.25f;
    }
}
