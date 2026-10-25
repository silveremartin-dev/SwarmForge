/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain.traits;

import java.io.Serializable;
import java.util.UUID;
import org.swarmforge.core.domain.Individual;

/**
 * Tandem Running Trait for recruitment dynamics in Formicidae (e.g. Temnothorax albipennis).
 * Models bidirectional tactile feedback loops between leaders and followers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TandemRunningTrait implements Serializable {
    private static final long serialVersionUID = 1L;

    private Individual.TandemRole role = Individual.TandemRole.NONE;
    private UUID partnerId = null;
    private long lastContactTick = 0;

    public Individual.TandemRole getRole() {
        return role;
    }

    public void setRole(Individual.TandemRole role) {
        this.role = (role != null) ? role : Individual.TandemRole.NONE;
    }

    public UUID getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(UUID partnerId) {
        this.partnerId = partnerId;
    }

    public long getLastContactTick() {
        return lastContactTick;
    }

    public void setLastContactTick(long tick) {
        this.lastContactTick = tick;
    }

    public boolean isLeader() {
        return role == Individual.TandemRole.LEADER;
    }

    public boolean isFollower() {
        return role == Individual.TandemRole.FOLLOWER;
    }

    public void pair(UUID selfId, Individual partner, boolean asLeader) {
        if (partner == null) return;
        this.role = asLeader ? Individual.TandemRole.LEADER : Individual.TandemRole.FOLLOWER;
        this.partnerId = partner.getId();
        this.lastContactTick = 0;
    }

    public void reset() {
        this.role = Individual.TandemRole.NONE;
        this.partnerId = null;
        this.lastContactTick = 0;
    }
}
