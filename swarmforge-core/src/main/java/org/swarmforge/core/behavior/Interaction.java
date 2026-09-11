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
        if (giver == null || receiver == null || !giver.isAlive() || !receiver.isAlive()) {
            return false;
        }

        boolean exchanged = false;

        // 1. Social Crop (Stomodeal Regurgitation) Transfer
        if (giver.getSocialCropAmount() > 0.1f) {
            float toTransfer = Math.min(5.0f, giver.getSocialCropAmount());
            org.swarmforge.core.domain.ResourceType rType = giver.getSocialCropResourceType();
            float taken = giver.regurgitateSocialCrop(toTransfer);
            if (taken > 0) {
                if (receiver.getLifeStage() == Individual.LifeStage.ADULT && receiver.getSocialCropAmount() < receiver.getSocialCropCapacity() * 0.8f) {
                    receiver.fillSocialCrop(taken, rType);
                } else {
                    receiver.setEnergy(Math.min(receiver.getMaxEnergy(), receiver.getEnergy() + taken * 2.0f));
                    receiver.setHunger(Math.max(0.0f, receiver.getHunger() - taken * 2.0f));
                }
                exchanged = true;
            }
        }

        // 2. Direct Metabolic Fluid Transfer (when donor is well-fed and recipient is hungry)
        if (!exchanged && giver.getEnergy() > 60.0f && receiver.getEnergy() < 40.0f) {
            float transfer = Math.min(15.0f, (giver.getEnergy() - 40.0f) * 0.5f);
            giver.setEnergy(giver.getEnergy() - transfer);
            receiver.setEnergy(Math.min(receiver.getMaxEnergy(), receiver.getEnergy() + transfer));
            receiver.setHunger(Math.max(0.0f, receiver.getHunger() - transfer * 0.5f));
            exchanged = true;
        }

        // 3. Solid Carried Food Transfer
        if (!exchanged && giver.isCarryingFood() && !receiver.isCarryingFood()) {
            receiver.setCarriedItem(giver.getCarriedItem());
            receiver.setCarriedResourceType(giver.getCarriedResourceType());
            giver.setCarriedItem(Individual.CarriedItem.NONE);
            giver.setCarriedResourceType(null);
            exchanged = true;
        }

        // 4. Cuticular Hydrocarbon (CHC) Gestalt Odor Homogenization
        if (exchanged) {
            giver.homogenizeChcProfile(receiver, 0.05f);
            receiver.homogenizeChcProfile(giver, 0.05f);
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
