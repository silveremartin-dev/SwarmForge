/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Thoracic Shivering Incubation System.
 * Models flight muscle shivering in Formica queens and honeybees to press a warm thorax (up to 40°C)
 * against brood cells, accelerating larval/pupal maturation rates by +30%.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ThoracicIncubationSystem {

    public static float incubateBrood(Individual worker, float currentBroodTemp) {
        if (worker == null || worker.getSpecies() == null) return currentBroodTemp;
        if (!worker.getSpecies().canPerformThoracicIncubation()) return currentBroodTemp;

        // Warm thorax shivering heat transfer
        return Math.min(35.5f, currentBroodTemp + 2.5f);
    }
}
