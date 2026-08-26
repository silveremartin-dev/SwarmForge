/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;

/**
 * Tremble Dance Recruitment System.
 * Models honeybee (Apis mellifera) tremble dances executed when returning foragers
 * experience search delays for food receiver bees, recruiting idle workers to become receivers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TrembleDanceSystem {

    public static boolean triggerTrembleDance(Individual forager, Colony colony, float receiverSearchDelaySeconds) {
        if (forager == null || colony == null || forager.getSpecies() == null) return false;
        if (!forager.getSpecies().canPerformTrembleDance()) return false;

        if (receiverSearchDelaySeconds > 40.0f) {
            // Trigger tremble dance: recruit idle in-hive workers into nectar processor role
            return true;
        }
        return false;
    }
}
