/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.diplomacy;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages diplomatic relations for a single colony.
 */
public class DiplomacyManager implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID colonyId;
    private final Map<UUID, RelationshipStatus> relations = new ConcurrentHashMap<>();

    public DiplomacyManager(UUID colonyId) {
        this.colonyId = colonyId;
    }

    public void setStatus(UUID otherColonyId, RelationshipStatus status) {
        if (colonyId.equals(otherColonyId))
            return;
        relations.put(otherColonyId, status);
    }

    public RelationshipStatus getStatus(UUID otherColonyId) {
        if (colonyId.equals(otherColonyId))
            return RelationshipStatus.ALLY; // Self is ally
        return relations.getOrDefault(otherColonyId, RelationshipStatus.NEUTRAL);
    }

    public boolean isAlly(UUID otherColonyId) {
        return getStatus(otherColonyId) == RelationshipStatus.ALLY;
    }

    public boolean isEnemy(UUID otherColonyId) {
        return getStatus(otherColonyId) == RelationshipStatus.ENEMY;
    }
}
