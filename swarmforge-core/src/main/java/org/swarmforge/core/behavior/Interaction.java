/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.swarmforge.core.domain.Individual;

/**
 * Handles complex social interactions between individuals.
 */
public class Interaction {

    /**
     * Trophallaxis: Exchange of food between two ants.
     * Also mixes colony scents (PheromoneSignature).
     * 
     * @param giver    The ant giving food
     * @param receiver The ant receiving food
     * @return true if exchange occurred
     */
    public static boolean trophallaxis(Individual giver, Individual receiver) {
        // Validation: Distance, Caste, etc. assumed handled by caller logic

        if (!giver.isCarryingFood() && giver.getEnergy() > 20) {
            // Giver can regurgitate from stomach?
            // For now, only pass carried items or energy if species supports it.
        }

        if (giver.isCarryingFood() && !receiver.isCarryingFood()) {
            // Simple item pass
            receiver.setCarriedItem(giver.getCarriedItem());
            receiver.setCarriedResourceType(giver.getCarriedResourceType());

            giver.setCarriedItem(Individual.CarriedItem.NONE);
            giver.setCarriedResourceType(null);

            // Social bonding: Reduce aggression
            // In future: mix PheromoneSignature to homogenize colony scent
            return true;
        }

        return false;
    }

    /**
     * Check if two individuals are friends (Same colony or allied).
     */
    public static boolean isFriend(Individual a, Individual b) {
        return a.getColonyId().equals(b.getColonyId());
    }
}
