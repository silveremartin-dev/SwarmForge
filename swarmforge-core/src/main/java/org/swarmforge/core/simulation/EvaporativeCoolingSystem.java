/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;

/**
 * Water Foraging & Evaporative Hive Cooling System.
 * Models water droplet deposition on brood combs combined with wing fanning by honeybees (Apis mellifera)
 * to maintain brood temperature below 35°C during heat waves.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EvaporativeCoolingSystem {

    public static float applyEvaporativeCooling(Colony colony, Individual worker, float ambientTemp) {
        if (colony == null || worker == null || worker.getSpecies() == null) return ambientTemp;
        if (!worker.getSpecies().canPerformEvaporativeCooling()) return ambientTemp;

        if (ambientTemp > 35.0f) {
            // Drop temperature by up to 4°C via water evaporation + wing fanning
            return Math.max(34.5f, ambientTemp - 3.5f);
        }
        return ambientTemp;
    }
}
