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
    boolean isCarryingFood();
    boolean isAtNest();
    UUID getColonyId();
    
    // Actions output
    // (Note: In pure ECS, output is side-effect on components, but for this bridge we keep the structure)
}
