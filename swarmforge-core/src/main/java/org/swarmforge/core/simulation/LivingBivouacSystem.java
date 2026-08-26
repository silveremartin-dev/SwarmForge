/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

import java.util.List;

/**
 * Nomadic Living Bivouac System.
 * Models interlocking worker structures in army ants (Eciton burchellii) forming
 * a suspended living cluster to shelter the queen and brood during nomadic phases.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LivingBivouacSystem {

    public static class BivouacCluster {
        private int participatingWorkers = 0;
        private float internalTemperatureCelsius = 28.5f;

        public void addWorkerToBivouac(Individual worker) {
            if (worker == null || worker.getSpecies() == null) return;
            if (!worker.getSpecies().canFormLivingBivouac()) return;

            participatingWorkers++;
            // Internal cluster temperature stabilization
            internalTemperatureCelsius = Math.min(30.0f, 28.0f + (participatingWorkers * 0.001f));
        }

        public int getParticipatingWorkers() { return participatingWorkers; }
        public float getInternalTemperatureCelsius() { return internalTemperatureCelsius; }
    }
}
