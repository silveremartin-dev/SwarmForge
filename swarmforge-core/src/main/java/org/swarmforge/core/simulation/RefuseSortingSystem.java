/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * External Refuse Pit Sorting & Quarantine System.
 * Models undertaker workers sorting waste, exuviae, and moldy debris in segregated external refuse dumps to prevent nest contamination.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class RefuseSortingSystem {

    public static boolean sortRefuse(Individual undertaker, float refuseDistanceToNestMeters) {
        if (undertaker == null || undertaker.getSpecies() == null) return false;
        if (!undertaker.getSpecies().canSortExternalRefusePits()) return false;

        if (refuseDistanceToNestMeters >= 5.0f) {
            // Deposit refuse in external dump zone
            return true;
        }
        return false;
    }
}
