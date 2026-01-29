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
    public ComponentMapper<MetabolismComponent> mMeta;
    public ComponentMapper<ColonyComponent> mColony;
    
    // Configuration / Constants
    private final float homeX = 50f; // Mock
    private final float homeY = 0f;
    private final float homeZ = 50f;

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
    public float getEnergyLevel() {
        if (mMeta != null && mMeta.has(entityId)) {
            return mMeta.get(entityId).energy / 100f; // Normalized 0-1
        }
        return 1.0f;
    }

    @Override
    public boolean isSoldier() {
        return false; // Mock for now
    }

    @Override
    public java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        return java.util.Set.of(org.swarmforge.core.domain.ResourceType.SEED); // Mock
    }

    @Override
    public String getAgentId() {
        return String.valueOf(entityId);
    }

    @Override
    public UUID getColonyId() { 
        if (mColony.has(entityId)) {
            return mColony.get(entityId).colonyId;
        }
        return null;
    }
}
