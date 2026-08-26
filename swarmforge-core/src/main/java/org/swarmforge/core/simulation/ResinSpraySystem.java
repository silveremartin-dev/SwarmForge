/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Social Immunity Antimicrobial Resin Spray System.
 * Models wood ants (Formica rufa) spraying formic acid mixed with gathered conifer tree resin
 * over brood chambers as a broad-spectrum social disinfectant.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ResinSpraySystem {

    public static float sprayAntimicrobialResin(Individual worker, float chamberBacterialLoad) {
        if (worker == null || worker.getSpecies() == null) return chamberBacterialLoad;
        if (!worker.getSpecies().canSprayFormicResinDisinfectant()) return chamberBacterialLoad;

        // Reduces bacterial load in chamber by 80%
        return Math.max(0.0f, chamberBacterialLoad - 0.80f);
    }
}
