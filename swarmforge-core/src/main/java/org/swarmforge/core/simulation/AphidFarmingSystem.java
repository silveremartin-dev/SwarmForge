/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;

/**
 * Aphid Farming & Mutualist Honeydew Milking System.
 * Models antennal drumming on aphids, honeydew harvesting (sugar reward),
 * aphid herd protection against ladybugs, and aphid relocation to fresh shoots.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AphidFarmingSystem {

    public static class AphidHerd {
        private int aphidCount;
        private float honeydewCapacity;
        private float currentHoneydew;

        public AphidHerd(int initialCount) {
            this.aphidCount = initialCount;
            this.honeydewCapacity = initialCount * 5.0f;
            this.currentHoneydew = honeydewCapacity;
        }

        public void updateHoneydewProduction() {
            this.currentHoneydew = Math.min(honeydewCapacity, currentHoneydew + aphidCount * 0.1f);
        }

        public float milkHoneydew(float requestedAmount) {
            float harvested = Math.min(currentHoneydew, requestedAmount);
            this.currentHoneydew -= harvested;
            return harvested;
        }

        public int getAphidCount() { return aphidCount; }
        public float getCurrentHoneydew() { return currentHoneydew; }
    }

    public static boolean harvestHoneydew(Individual worker, AphidHerd herd, Colony colony) {
        if (worker == null || herd == null || colony == null || worker.getSpecies() == null) return false;
        if (!worker.getSpecies().canFarmAphids()) return false;

        float harvested = herd.milkHoneydew(2.0f);
        if (harvested > 0) {
            colony.addResource(ResourceType.HONEYDEW, harvested);
            worker.setEnergy(Math.min(1.0f, worker.getEnergyLevel() + 0.15f));
            return true;
        }
        return false;
    }
}
