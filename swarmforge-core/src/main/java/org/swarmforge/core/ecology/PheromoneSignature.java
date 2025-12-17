/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.ecology;

import java.util.UUID;

/**
 * Represents a unique chemical signature for pheromones or individuals.
 * Used for colony identification (Friend vs Foe) and trail decay.
 */
public record PheromoneSignature(UUID colonyId, float integrity, CompoundType type) {

    public enum CompoundType {
        TRAIL,
        ALARM,
        RECRUITMENT,
        COLONY_SCENT
    }

    /**
     * Creates a decayed version of this signature.
     */
    public PheromoneSignature decay(float amount) {
        return new PheromoneSignature(colonyId, Math.max(0, integrity - amount), type);
    }

    public boolean isSameColony(UUID otherColonyId) {
        return this.colonyId.equals(otherColonyId);
    }
}
