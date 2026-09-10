package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.InventoryComponent;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.ecs.components.VelocityComponent;
import org.swarmforge.core.ecs.components.MetabolismComponent;
import org.swarmforge.core.ecs.components.ColonyComponent;

/**
 * Basic AI behavior system for foraging.
 * Replacement for the basic FSM in Individual.java
 */
public class ForagingSystem extends IteratingSystem {
    ComponentMapper<PositionComponent> mPos;
    ComponentMapper<VelocityComponent> mVel;
    ComponentMapper<InventoryComponent> mInv;
    ComponentMapper<MetabolismComponent> mMeta; // Only live ants forage
    ComponentMapper<ColonyComponent> mColony;

    public ForagingSystem() {
        super(Aspect.all(PositionComponent.class, VelocityComponent.class, InventoryComponent.class, ColonyComponent.class));
    }

    @Override
    protected void process(int entityId) {
        // If dead, do nothing (should probably filter out dead entities in Aspect or remove components)
        if (mMeta.has(entityId) && !mMeta.get(entityId).alive) {
            mVel.get(entityId).speed = 0;
            return;
        }

        InventoryComponent inv = mInv.get(entityId);
        VelocityComponent vel = mVel.get(entityId);
        PositionComponent pos = mPos.get(entityId);

        // Very simple logic:
        // If carrying food -> Go Home (0,0,0 for now)
        // If not carrying -> Wander randomly looking for food
        
        java.util.UUID colonyId = mColony.get(entityId).colonyId;
        org.swarmforge.core.domain.Colony colony = org.swarmforge.core.ecs.ColonyRegistry.getColony(colonyId);
        float nestX = colony != null ? colony.getNestX() : 50.0f;
        float nestY = colony != null ? colony.getNestY() : 50.0f;
        float nestZ = colony != null ? colony.getNestZ() : 0.0f;

        if (inv.carriedItem == InventoryComponent.ItemType.FOOD) {
            float dx = nestX - pos.x;
            float dy = nestY - pos.y;
            float dz = nestZ - pos.z;
            float distSq = dx * dx + dy * dy + dz * dz;

            if (distSq < 1.0f) {
                inv.carriedItem = InventoryComponent.ItemType.NONE;
                if (colony != null) {
                    colony.addResource(org.swarmforge.core.domain.ResourceType.SEED, 1.0f);
                }
                vel.dx = 0;
                vel.dy = 0;
                vel.dz = 0;
            } else {
                float invDist = (float) (1.0 / Math.sqrt(distSq));
                vel.dx = dx * invDist * vel.speed;
                vel.dy = dy * invDist * vel.speed;
                vel.dz = dz * invDist * vel.speed;
            }
        } else {
            // Wander Logic (Random Walk on X/Y plane)
            if (java.util.concurrent.ThreadLocalRandom.current().nextFloat() < 0.05f) {
                double angle = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
                vel.dx = (float) Math.cos(angle) * vel.speed;
                vel.dy = (float) Math.sin(angle) * vel.speed;
                vel.dz = 0;
            }
            
            // Boundary check (Bounce)
            if (pos.x < 0 || pos.x > 100) vel.dx *= -1;
            if (pos.y < 0 || pos.y > 100) vel.dy *= -1;
        }
    }
}
