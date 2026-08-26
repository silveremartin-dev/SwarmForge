/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Arboreal Weaver Ant Leaf-Sewing & Silk Larval Weaving System.
 * Models physical chains of Oecophylla workers pulling leaf margins together while holding
 * silk-producing larvae like glue dispensers to construct aerial nests.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeaverSilkSystem {

    public static class LeafSeam {
        private float tensionProgress = 0.0f; // 0 to 100% leaf closure
        private float silkWeaveIntegrity = 0.0f; // 0 to 100% seam strength

        public void pullLeaves(int workerChainSize) {
            this.tensionProgress = Math.min(100.0f, tensionProgress + workerChainSize * 1.5f);
        }

        public void applyLarvalSilk(Individual weaverWorker, Individual larva) {
            if (weaverWorker == null || weaverWorker.getSpecies() == null) return;
            if (!weaverWorker.getSpecies().canSewLeavesWithLarvalSilk()) return;

            if (tensionProgress >= 80.0f) {
                this.silkWeaveIntegrity = Math.min(100.0f, silkWeaveIntegrity + 5.0f);
            }
        }

        public boolean isSeamComplete() {
            return tensionProgress >= 100.0f && silkWeaveIntegrity >= 100.0f;
        }

        public float getTensionProgress() { return tensionProgress; }
        public float getSilkWeaveIntegrity() { return silkWeaveIntegrity; }
    }
}
