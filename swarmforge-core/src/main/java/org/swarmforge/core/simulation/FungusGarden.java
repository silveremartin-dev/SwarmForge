/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
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
            float processed = Math.min(leaves, 0.5f); // 0.5 leaves per tick
            colony.consumeResource(ResourceType.LEAF, processed);
            colony.addResource(ResourceType.MULCH, processed);
        }

        // 1. Nest Microclimate Check (Mycology parameters: 22-28°C, >75% RH)
        float nestTemp = colony.getSpecies() != null ? colony.getSpecies().getOptimalTempCelsius() : 24.0f;
        float rh = 85.0f; // Baseline subterranean relative humidity
        boolean idealClimate = (nestTemp >= 20.0f && nestTemp <= 29.0f && rh >= 70.0f);

        // 2. Consume Mulch
        float mulchAvailable = colony.getResourceAmount(ResourceType.MULCH);

        if (mulchAvailable > 0) {
            float mulchConsumed = colony.consumeResource(ResourceType.MULCH, Math.min(mulchAvailable, 0.1f));

            // Produce Fungus modulated by climate efficiency
            float climateMult = idealClimate ? 1.5f : 0.6f;
            float fungusProduced = mulchConsumed * 2.0f * health * climateMult;
            colony.addResource(ResourceType.FUNGUS, fungusProduced);

            // Health increases if fed under good microclimate
            if (idealClimate) {
                health = Math.min(1.0f, health + 0.001f);
            } else {
                health = Math.max(0.0f, health - 0.002f); // Sub-optimal climate stress
            }
        } else {
            // Decay if starving or dry
            health = Math.max(0.0f, health - 0.001f);

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

    private float contaminationLevel = 0.0f; // Escovopsis mold contamination
    public float getContaminationLevel() { return contaminationLevel; }
    public void setContaminationLevel(float level) { this.contaminationLevel = Math.max(0f, level); }
}
