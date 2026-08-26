/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Aphid Herd Sanitary Cordon System.
 * Models ant herders identifying and culling pathogen-infected aphids to prevent epizootic outbreaks in managed aphid herds.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class AphidSanitaryCordonSystem {

    public static boolean cullInfectedAphid(Individual herder, boolean isAphidInfected) {
        if (herder == null || herder.getSpecies() == null) return false;
        if (!herder.getSpecies().canEnforceAphidSanitaryCordon()) return false;

        if (isAphidInfected) {
            // Remove infected aphid from plant stem
            return true;
        }
        return false;
    }
}
