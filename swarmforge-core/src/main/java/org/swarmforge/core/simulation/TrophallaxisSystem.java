/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Stochastic Trophallaxis & Queen Primer Pheromone (QPP) Dilution System.
 * Simulates mouth-to-mouth liquid food sharing (carbohydrates & proteins) between nestmates.
 * Concurrently transfers queen primer pheromones (QPP) that suppress worker fertility.
 * When QPP level drops below threshold due to colony spatial extent or queen absence,
 * workers activate ovaries and lay unfertilized male-destined or trophic eggs.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TrophallaxisSystem {

    public static class TrophallaxisResult {
        public final float foodTransferred;
        public final float qppTransferred;
        public final boolean occurred;

        public TrophallaxisResult(float foodTransferred, float qppTransferred, boolean occurred) {
            this.foodTransferred = foodTransferred;
            this.qppTransferred = qppTransferred;
            this.occurred = occurred;
        }
    }

    /**
     * Executes stochastic trophallaxis between donor and recipient individuals.
     */
    public static TrophallaxisResult performTrophallaxis(Individual donor, Individual recipient,
                                                        float donorQppLevel, float recipientQppLevel) {
        if (donor == null || recipient == null || !donor.isAlive() || !recipient.isAlive()) {
            return new TrophallaxisResult(0f, 0f, false);
        }

        // Trophallaxis occurs if donor has significantly higher energy/crop content than recipient
        float donorEnergy = donor.getEnergy();
        float recipientEnergy = recipient.getEnergy();
        if (donorEnergy - recipientEnergy < 15.0f) {
            return new TrophallaxisResult(0f, 0f, false);
        }

        // 30% of energy gradient transferred
        float foodTransfer = (donorEnergy - recipientEnergy) * 0.30f;
        donor.setEnergy(donorEnergy - foodTransfer);
        recipient.setEnergy(recipientEnergy + foodTransfer);

        // Queen Primer Pheromone equalization during fluid exchange
        float qppTransfer = (donorQppLevel - recipientQppLevel) * 0.25f;

        return new TrophallaxisResult(foodTransfer, qppTransfer, true);
    }

    /**
     * Evaluates worker fertility activation based on QPP concentration floor (0.15 threshold).
     */
    public static boolean isWorkerFertile(float qppConcentration) {
        return qppConcentration < 0.15f;
    }
}
