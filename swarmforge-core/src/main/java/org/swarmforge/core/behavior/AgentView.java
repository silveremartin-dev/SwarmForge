package org.swarmforge.core.behavior;

import java.util.UUID;

/**
 * Interface representing the view of an agent for the AI.
 * Allows decoupling ReasoningArchitecture from the heavy Individual class,
 * enabling ECS integration via Adapter pattern.
 */
public interface AgentView {
    float getX();
    float getY();
    float getZ();
    float getHeading();
    float getHomeX();
    float getHomeY();
    default float getHomeZ() { return 0.0f; }
    boolean isCarryingFood();
    boolean isAtNest();
    float getEnergyLevel();
    float getHunger();
    UUID getColonyId();
    boolean isSoldier();
    default boolean isQueen() { return false; }
    default boolean isNurse() { return false; }
    default boolean isDrone() { return false; }
    default boolean isMajor() { return isSoldier(); }
    default org.swarmforge.core.species.Species getSpecies() { return null; }
    default java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() { return java.util.Collections.emptySet(); }
    default String getAgentId() { return ""; }
    default boolean canFly() { return false; }
    default boolean isFlying() { return false; }
    
    // Actions output
    // (Note: In pure ECS, output is side-effect on components, but for this bridge we keep the structure)
}
