/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Tandem Running Recruitment System.
 * Models step-by-step leader-follower recruitment in Temnothorax ants where a leader guides a single follower,
 * advancing only when receiving tactile antennal taps on its hind legs.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TandemRunningSystem {

    public static boolean executeTandemStep(Individual leader, Individual follower, float distanceUnits) {
        if (leader == null || follower == null || leader.getSpecies() == null) return false;
        if (!leader.getSpecies().canPerformTandemRunning()) return false;

        // If follower is within antennal contact range (<= 1.5 units)
        if (distanceUnits <= 1.5f) {
            follower.setHeading(leader.getHeading());
            return true; // Leader advances one step
        }
        return false; // Leader waits for follower to catch up
    }
}
