package org.swarmforge.core.ecs.systems;

import org.swarmforge.core.behavior.AgentView;
import org.swarmforge.core.ecs.components.*;
import java.util.UUID;
import com.artemis.ComponentMapper;

/**
 * Flyweight Adapter that makes an ECS Entity look like an AgentView.
 * This object is reused (setEntityId is called for each entity).
 */
public class EcsAgentAdapter implements AgentView {
    
    private int entityId;
    
    // Mappers (Injected by System)
    public ComponentMapper<PositionComponent> mPos;
    public ComponentMapper<VelocityComponent> mVel;
    public ComponentMapper<InventoryComponent> mInv;
    
    // Configuration / Constants
    private final float homeX = 50f; // Mock
    private final float homeY = 0f;
    private final float homeZ = 50f;
    private final UUID colonyId = UUID.randomUUID(); // Mock

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public float getX() { return mPos.get(entityId).x; }

    @Override
    public float getY() { return mPos.get(entityId).y; }

    @Override
    public float getZ() { return mPos.get(entityId).z; }

    @Override
    public float getHeading() { return mPos.get(entityId).heading; }

    @Override
    public float getHomeX() { return homeX; } // Should come from a ColonyComponent

    @Override
    public float getHomeY() { return homeY; }
    
    @Override
    public boolean isCarryingFood() {
        if (mInv.has(entityId)) {
            return mInv.get(entityId).carriedItem == InventoryComponent.ItemType.FOOD;
        }
        return false;
    }
    
    @Override
    public boolean isAtNest() {
        float dx = getX() - homeX;
        float dz = getZ() - homeZ;
        return (dx*dx + dz*dz) < 4.0f; 
    }

    @Override
    public UUID getColonyId() { return colonyId; }
}
