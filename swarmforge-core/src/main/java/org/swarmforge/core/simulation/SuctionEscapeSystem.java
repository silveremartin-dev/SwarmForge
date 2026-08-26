/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Substrate Clamping Suction Escape Posture System.
 * Models specialized arboreal ants flattening their bodies and clamping legs firmly to bark surfaces to withstand bird/lizard strikes or wind gusts.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SuctionEscapeSystem {

    public static boolean clampToSubstrate(Individual ant, boolean isPredatorStrikeDetected) {
        if (ant == null || ant.getSpecies() == null) return false;
        if (!ant.getSpecies().canPerformSuctionEscapePosture()) return false;

        if (isPredatorStrikeDetected) {
            // Clamp body to bark surface
            return true;
        }
        return false;
    }
}
