/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.ResourceType;

import java.util.List;

/**
 * Polycalic Nest Network Routing & Resource Balancing System.
 * Manages inter-mound foraging highways and nutrient flow redistribution between daughter nests
 * based on local brood starvation levels and nest capacity.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PolycalicNetworkSystem {

    public static void balanceResourcesBetweenNests(Colony sourceNest, Colony targetNest, ResourceType type) {
        if (sourceNest == null || targetNest == null || type == null) return;
        if (sourceNest.getSpecies() == null || !sourceNest.getSpecies().isPolycalic()) return;

        float sourceAmount = sourceNest.getResourceAmount(type);
        float targetAmount = targetNest.getResourceAmount(type);

        if (sourceAmount > targetAmount + 20.0f) {
            float transferAmount = (sourceAmount - targetAmount) * 0.1f; // Transfer 10% of gradient difference
            if (sourceNest.sendResource(targetNest, type, transferAmount)) {
                // Network transfer successful
            }
        }
    }
}
