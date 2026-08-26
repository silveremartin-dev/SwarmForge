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
 * Storm Wall Clay Breach Repair System.
 * Models rapid deployment of builder workers carrying clay mortar to patch breached nest mound walls following heavy rainfall.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ClayBreachRepairSystem {

    public static boolean patchBreach(Individual builder, Chamber chamber) {
        if (builder == null || chamber == null || builder.getSpecies() == null) return false;
        if (!builder.getSpecies().canRepairBreachesClay()) return false;

        chamber.setStabilityFactor(Math.min(1.0f, chamber.getStabilityFactor() + 0.25f));
        return true;
    }
}
