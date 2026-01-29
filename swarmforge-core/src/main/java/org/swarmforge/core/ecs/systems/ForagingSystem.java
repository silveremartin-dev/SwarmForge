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
        
        if (inv.carriedItem == InventoryComponent.ItemType.FOOD) {
            // Return Home Logic
            float targetX = 50f; // Mock Nest X
            float targetZ = 50f; // Mock Nest Z
            
            float dx = targetX - pos.x;
            float dz = targetZ - pos.z;
            float dist = (float) Math.sqrt(dx*dx + dz*dz);
            
            if (dist < 1.0f) {
                // Arrived at nest -> Drop food
                inv.carriedItem = InventoryComponent.ItemType.NONE;
                // Add to Colony resource stock
                java.util.UUID colonyId = mColony.get(entityId).colonyId;
                org.swarmforge.core.domain.Colony colony = org.swarmforge.core.ecs.ColonyRegistry.getColony(colonyId);
                if (colony != null) {
                    colony.addResource(org.swarmforge.core.domain.ResourceType.SEED, 1.0f);
                }
            } else {
                // Move towards nest
                vel.dx = (dx / dist) * vel.speed;
                vel.dz = (dz / dist) * vel.speed;
            }
        } else {
            // Wander Logic (Random Walk)
            // In a real system, would check for pheromones here
            if (java.util.concurrent.ThreadLocalRandom.current().nextFloat() < 0.05f) {
                // Change direction occasionally
                double angle = java.util.concurrent.ThreadLocalRandom.current().nextDouble() * Math.PI * 2;
                vel.dx = (float) Math.cos(angle) * vel.speed;
                vel.dz = (float) Math.sin(angle) * vel.speed;
            }
            
            // Boundary check (Bounce)
            if (pos.x < 0 || pos.x > 100) vel.dx *= -1;
            if (pos.z < 0 || pos.z > 100) vel.dz *= -1;
        }
    }
}
