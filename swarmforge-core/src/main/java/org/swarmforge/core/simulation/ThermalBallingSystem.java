/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Predator;

/**
 * Thermal Balling Defensive Oven System.
 * Models Asian honeybees (Apis cerana) forming a dense heat ball of hundreds of bees around an invading giant hornet,
 * vibrating flight muscles to elevate temperature to 47°C, lethal to hornets but non-lethal to bees.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ThermalBallingSystem {

    public static boolean executeThermalBalling(Individual bee, Predator hornet, int attackingBeesCount) {
        if (bee == null || hornet == null || bee.getSpecies() == null) return false;
        if (!bee.getSpecies().canPerformThermalBalling()) return false;

        if (attackingBeesCount >= 150) {
            // Core temperature reaches 47°C inside bee ball
            hornet.setHealth(hornet.getHealth() - 15.0f);
            return true;
        }
        return false;
    }
}
