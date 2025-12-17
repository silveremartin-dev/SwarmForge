/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.ResourceType;

/**
 * Represents a fungus garden within a colony (specifically for Atta species).
 * Converts Mulch into Fungus over time.
 */
public class FungusGarden implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private final Colony colony;
    private float health = 1.0f; // 0.0 to 1.0

    public FungusGarden(Colony colony) {
        this.colony = colony;
    }

    public void tick() {
        // 0. Convert Leaves to Mulch (Ants processing leaves)
        float leaves = colony.getResourceAmount(ResourceType.LEAF);
        if (leaves > 0) {
            // Processing rate depends on population? For now simple rate
            float processed = Math.min(leaves, 0.5f); // 0.5 leaves per tick
            colony.consumeResource(ResourceType.LEAF, processed);
            colony.addResource(ResourceType.MULCH, processed);
        }

        // 1. Consume Mulch
        float mulchAvailable = colony.getResourceAmount(ResourceType.MULCH);

        // Conversion Logic: 1 Mulch -> 0.8 Fungus (plus some loss)
        // Need critical mass of Mulch to grow

        if (mulchAvailable > 0) {
            float mulchConsumed = colony.consumeResource(ResourceType.MULCH, Math.min(mulchAvailable, 0.1f)); // Consume
                                                                                                              // slowly

            // Produce Fungus
            float fungusProduced = mulchConsumed * 2.0f * health; // Magic multiplier
            colony.addResource(ResourceType.FUNGUS, fungusProduced);

            // Health increases if fed
            health = Math.min(1.0f, health + 0.001f);
        } else {
            // Decay if starving
            health = Math.max(0.0f, health - 0.0005f);

            // Fungus dies off if garden is dying
            if (health < 0.5f) {
                float fungus = colony.getResourceAmount(ResourceType.FUNGUS);
                if (fungus > 0) {
                    colony.consumeResource(ResourceType.FUNGUS, fungus * 0.01f); // Rotting
                }
            }
        }
    }

    public float getHealth() {
        return health;
    }
}
