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
 * Royal Pheromone & Ovary Inhibition System.
 * Models diffusion of queen mandibular pheromones (e.g. 9-ODA in honeybees) suppressing worker ovary development.
 * Upon queen death, pheromone concentration decays, triggering emergency queen cell construction or gamergate development.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class RoyalPheromoneSystem {

    private static final float DECAY_RATE_PER_TICK = 0.01f;

    public static class ColonyPheromoneState {
        private float royalPheromoneLevel = 1.0f; // 1.0 = Full queen presence, 0.0 = Queenless

        public void tick(boolean queenPresent) {
            if (queenPresent) {
                royalPheromoneLevel = Math.min(1.0f, royalPheromoneLevel + 0.05f);
            } else {
                royalPheromoneLevel = Math.max(0.0f, royalPheromoneLevel - DECAY_RATE_PER_TICK);
            }
        }

        public float getRoyalPheromoneLevel() {
            return royalPheromoneLevel;
        }

        public boolean isEmergencyQueenStateTriggered() {
            return royalPheromoneLevel < 0.2f;
        }
    }

    public static void updateColonyRoyalPheromones(Colony colony, ColonyPheromoneState state) {
        if (colony == null || state == null || colony.getSpecies() == null) return;
        if (!colony.getSpecies().hasRoyalPheromoneInhibition()) return;

        boolean queenAlive = colony.hasQueen();
        state.tick(queenAlive);

        if (state.isEmergencyQueenStateTriggered()) {
            // Trigger worker ovary activation or emergency queen cell construction
        }
    }
}
