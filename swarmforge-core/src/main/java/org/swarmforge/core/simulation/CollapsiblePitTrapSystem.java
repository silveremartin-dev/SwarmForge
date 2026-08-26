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
 * Fragile Collapsible Subterranean Pit Trap System.
 * Models termite architects excavating fragile, thin-roofed pitfall traps around nest perimeters to collapse beneath heavy raiding anteaters or rival colonies.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class CollapsiblePitTrapSystem {

    public static boolean constructPitTrap(Individual builder, Chamber chamber) {
        if (builder == null || chamber == null || builder.getSpecies() == null) return false;
        if (!builder.getSpecies().canConstructCollapsiblePitTraps()) return false;

        chamber.setStabilityFactor(0.10f); // Fragile roof collapses under heavy load
        return true;
    }
}
