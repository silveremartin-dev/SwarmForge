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
 * Robber Bee Hive Raid System.
 * Models kleptoparasitic raids by robber bees (Lestrimelitta) attacking weak host nests
 * to plunder honey reserves and propolis.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class RobberBeeSystem {

    public static boolean raidHostHiveHoney(Individual robber, Colony hostColony) {
        if (robber == null || hostColony == null || robber.getSpecies() == null) return false;
        if (!robber.getSpecies().isRobberBeeSpecies()) return false;

        // Plunder honey (carbohydrates) from host colony
        if (hostColony.getCarbohydrateStored() > 2.0f) {
            hostColony.setCarbohydrateStored(hostColony.getCarbohydrateStored() - 2.0f);
            robber.setEnergy(Math.min(100.0f, robber.getEnergyLevel() + 25.0f));
            return true;
        }
        return false;
    }
}
