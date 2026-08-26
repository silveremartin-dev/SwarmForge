/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Wasp & Hornet (Vespula / Vespa) Predatory Mastication & Larval Salivary Trophallaxis System.
 * Simulates adult wasps hunting insect prey, masticating flesh into protein meatballs for larvae.
 * In return, larvae secrete nutrient-rich salivary droplets containing essential amino acids,
 * serving as the primary carbohydrate & energy source for adult wasps.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LarvalSalivaryTrophallaxisSystem {

    public static class LarvalExchangeResult {
        public final float proteinGivenToLarva;
        public final float salivaRewardToAdult;

        public LarvalExchangeResult(float proteinGivenToLarva, float salivaRewardToAdult) {
            this.proteinGivenToLarva = proteinGivenToLarva;
            this.salivaRewardToAdult = salivaRewardToAdult;
        }
    }

    /**
     * Executes protein-saliva trophallactic exchange between adult wasp and larva.
     */
    public static LarvalExchangeResult feedLarva(Individual adultWasp, float masticatedMeatballMass) {
        if (adultWasp == null || !adultWasp.isAlive() || masticatedMeatballMass <= 0.0f) {
            return new LarvalExchangeResult(0f, 0f);
        }

        // Larva absorbs protein for growth and synthesizes 40% equivalent volume of rich salivary liquid reward
        float salivaReward = masticatedMeatballMass * 0.40f;
        adultWasp.setEnergy(Math.min(100f, adultWasp.getEnergy() + salivaReward * 15.0f));

        return new LarvalExchangeResult(masticatedMeatballMass, salivaReward);
    }
}
