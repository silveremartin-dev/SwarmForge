/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Water Trophallaxis & Hive Humidity Regulation System.
 * Models water foragers transferring liquid water droplets to nurse bees to maintain 90% RH microclimate in brood chambers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WaterTrophallaxisSystem {

    public static boolean transferWater(Individual donor, Individual receiver) {
        if (donor == null || receiver == null || donor.getSpecies() == null) return false;
        if (!donor.getSpecies().canPerformWaterTrophallaxis()) return false;

        if (donor.getThirst() < 10.0f && receiver.getThirst() > 40.0f) {
            receiver.setThirst(Math.max(0.0f, receiver.getThirst() - 30.0f));
            return true;
        }
        return false;
    }
}
