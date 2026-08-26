/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Self-Assembled Waterproof Living Raft System.
 * Models fire ants (Solenopsis invicta) interlocking tarsal claws to construct floating hydrophobic rafts
 * that carry queen and brood safely across water surfaces during floods.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SelfAssembledRaftSystem {

    public static boolean formWaterproofRaft(Individual ant, boolean isOnWaterSurface) {
        if (ant == null || ant.getSpecies() == null) return false;
        if (!ant.getSpecies().canFormLivingRaft()) return false;

        if (isOnWaterSurface) {
            ant.setZ(0.0f); // Float on water surface layer
            return true;
        }
        return false;
    }
}
