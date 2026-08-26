/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.structure.Chamber;

/**
 * Subterranean Load-Bearing Spiral Clay Pillar System.
 * Models termite architects constructing vertical spiral clay pillars to support large subterranean chambers
 * under high overburden soil pressure.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ClayPillarSystem {

    public static boolean constructSupportPillar(Individual builder, Chamber chamber) {
        if (builder == null || chamber == null || builder.getSpecies() == null) return false;
        if (!builder.getSpecies().canConstructClayPillars()) return false;

        // Reinforce chamber structural stability
        chamber.setStabilityFactor(chamber.getStabilityFactor() + 0.40f);
        return true;
    }
}
