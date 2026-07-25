/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Endocrine & Hormonal Development System.
 * Tracks Juvenile Hormone (JH) titers, Ecdysone, and nutrition intake in larvae
 * to determine caste differentiation (Queen vs Worker vs Major/Soldier).
 * Simulates Queen Primer Pheromone ovarian suppression on workers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EndocrineSystem {

    /**
     * Determines the adult caste of a larva at the critical developmental threshold
     * based on cumulative nutrition and Juvenile Hormone (JH) titer.
     *
     * @param totalProteinConsumed Protein ingested during larval instar (mg)
     * @param jhTiter Juvenile Hormone level (0.0 to 1.0)
     * @param hasQueenPrimerPheromone true if queen primer pheromone is present in nest
     * @return Resulting adult caste
     */
    public static Individual.Caste determineCasteDifferentiation(float totalProteinConsumed, float jhTiter, boolean hasQueenPrimerPheromone) {
        // High protein + high JH titer without queen suppression = Gyne / Queen
        if (totalProteinConsumed > 2.5f && jhTiter > 0.8f && !hasQueenPrimerPheromone) {
            return Individual.Caste.QUEEN;
        }

        // Elevated protein + moderate JH titer = Soldier / Major caste
        if (totalProteinConsumed > 1.2f && jhTiter > 0.5f) {
            return Individual.Caste.SOLDIER;
        }

        // Standard nutrition = Worker caste
        return Individual.Caste.WORKER;
    }

    /**
     * Evaluates worker ovarian activation in queenless colonies (Gamergate / laying workers).
     * Returns true if worker ovaries activate due to absence of Queen Primer Pheromone.
     */
    public static boolean checkWorkerOvarianActivation(float queenPheromoneLevel, int daysQueenless) {
        if (queenPheromoneLevel < 0.05f && daysQueenless >= 3) {
            return Math.random() < 0.25; // 25% chance per tick to become laying worker
        }
        return false;
    }
}
