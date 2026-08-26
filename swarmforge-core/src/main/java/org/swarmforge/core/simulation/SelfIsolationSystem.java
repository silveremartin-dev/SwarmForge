/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Necrotropic Self-Isolation System.
 * Models terminally ill or fungal spore infected workers (Formica) voluntarily exiting the nest
 * to die in isolation outdoors, protecting the colony from pathogen outbreaks.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SelfIsolationSystem {

    public static boolean triggerSelfIsolation(Individual worker, float fungalSporeInfectionLevel) {
        if (worker == null || worker.getSpecies() == null) return false;
        if (!worker.getSpecies().canSelfIsolateWhenInfected()) return false;

        if (fungalSporeInfectionLevel >= 0.7f) {
            // Leave nest galleries and stay outdoors until death
            return true;
        }
        return false;
    }
}
