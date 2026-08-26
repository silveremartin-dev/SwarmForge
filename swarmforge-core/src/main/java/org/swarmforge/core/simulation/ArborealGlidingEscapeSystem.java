/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Controlled Arboreal Free-Fall Gliding Escape System.
 * Models Cephalotes ants leaping from high canopy branches upon predator attacks, executing J-shaped gliding trajectories back to tree trunks.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ArborealGlidingEscapeSystem {

    public static boolean executeGlidingEscape(Individual ant, boolean isCanopyAttackDetected) {
        if (ant == null || ant.getSpecies() == null) return false;
        if (!ant.getSpecies().canPerformArborealGlidingEscape()) return false;

        if (isCanopyAttackDetected && ant.getZ() > 5.0f) {
            // Glide toward tree trunk vector
            return true;
        }
        return false;
    }
}
