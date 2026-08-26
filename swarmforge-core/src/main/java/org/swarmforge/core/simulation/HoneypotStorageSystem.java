/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Honeypot Replete Storage System.
 * Models specialized honeypot repletes (Myrmecocystus) hanging from chamber ceilings
 * with distended gaster storing liquid sugars to feed the colony during droughts.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class HoneypotStorageSystem {

    public static class HoneypotReplete {
        private float storedNectarVolumeMl = 0.0f;
        private static final float MAX_CAPACITY_ML = 0.5f;

        public boolean fillStorage(Individual ant, float amountMl) {
            if (ant == null || ant.getSpecies() == null) return false;
            if (!ant.getSpecies().isHoneypotStorageCaste()) return false;

            this.storedNectarVolumeMl = Math.min(MAX_CAPACITY_ML, this.storedNectarVolumeMl + amountMl);
            return true;
        }

        public float dispenseNectar(float amountMl) {
            float dispensed = Math.min(storedNectarVolumeMl, amountMl);
            this.storedNectarVolumeMl -= dispensed;
            return dispensed;
        }

        public float getStoredNectarVolumeMl() { return storedNectarVolumeMl; }
        public boolean isFull() { return storedNectarVolumeMl >= MAX_CAPACITY_ML; }
    }
}
