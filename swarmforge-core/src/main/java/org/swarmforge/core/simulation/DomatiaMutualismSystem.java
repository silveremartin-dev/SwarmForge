/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Domatia Plant Mutualism & Foliage Pruning System.
 * Models Pseudomyrmex ants nesting inside hollow swollen thorn domatia (Acacia),
 * harvesting pearl bodies / beltian bodies and aggressively pruning surrounding vegetation to prevent vine encroachment.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class DomatiaMutualismSystem {

    public static boolean pruneEncroachingFoliage(Individual ant, float distanceToDomatiaPlantMeters) {
        if (ant == null || ant.getSpecies() == null) return false;
        if (!ant.getSpecies().canInhabitDomatia()) return false;

        if (distanceToDomatiaPlantMeters <= 2.0f) {
            // Cut invading plant tendrils to protect host Acacia plant
            ant.setEnergy(Math.min(100.0f, ant.getEnergyLevel() + 5.0f)); // Sustained by Beltian bodies
            return true;
        }
        return false;
    }
}
