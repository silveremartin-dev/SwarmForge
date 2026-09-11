/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
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

    public enum DiplomaticAction {
        PROPOSE_ALLIANCE,
        ACCEPT_ALLIANCE,
        REJECT_ALLIANCE,
        BREAK_ALLIANCE,
        DECLARE_WAR,
        OFFER_TRIBUTE,
        PROPOSE_TRADE,
        CANCEL_TRADE
    }

    private final Map<UUID, DiplomaticAction> pendingProposals = new ConcurrentHashMap<>();

    public void proposeAlliance(UUID targetColonyId) {
        if (!colonyId.equals(targetColonyId)) {
            pendingProposals.put(targetColonyId, DiplomaticAction.PROPOSE_ALLIANCE);
        }
    }

    public boolean acceptAlliance(UUID targetColonyId) {
        if (pendingProposals.remove(targetColonyId) == DiplomaticAction.PROPOSE_ALLIANCE) {
            setStatus(targetColonyId, RelationshipStatus.ALLY);
            return true;
        }
        return false;
    }

    public void declareWar(UUID targetColonyId) {
        setStatus(targetColonyId, RelationshipStatus.ENEMY);
        pendingProposals.remove(targetColonyId);
    }

    public void breakAlliance(UUID targetColonyId) {
        setStatus(targetColonyId, RelationshipStatus.NEUTRAL);
    }

    public boolean transferTribute(org.swarmforge.core.domain.Colony source, org.swarmforge.core.domain.Colony target, float amount) {
        if (source == null || target == null || amount <= 0.0f) return false;
        if (source.getFoodStored() < amount) return false;
        source.setFoodStored(source.getFoodStored() - amount);
        target.setFoodStored(target.getFoodStored() + amount);
        if (getStatus(target.getId()) == RelationshipStatus.ENEMY) {
            setStatus(target.getId(), RelationshipStatus.NEUTRAL); // Appease enemy with tribute
        }
        return true;
    }

    public boolean isAlly(UUID otherColonyId) {
        return getStatus(otherColonyId) == RelationshipStatus.ALLY;
    }

    public boolean isEnemy(UUID otherColonyId) {
        return getStatus(otherColonyId) == RelationshipStatus.ENEMY;
    }

    public Map<UUID, DiplomaticAction> getPendingProposals() {
        return java.util.Collections.unmodifiableMap(pendingProposals);
    }
}
