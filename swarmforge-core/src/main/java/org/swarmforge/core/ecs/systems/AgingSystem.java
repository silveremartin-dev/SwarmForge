package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.LifeCycleComponent;
import org.swarmforge.core.ecs.components.MetabolismComponent;

/**
 * System that handles aging and life stage transitions.
 */
public class AgingSystem extends IteratingSystem {
    ComponentMapper<LifeCycleComponent> mLife;
    ComponentMapper<MetabolismComponent> mMeta; // Optional, to kill if old

    public AgingSystem() {
        super(Aspect.all(LifeCycleComponent.class));
    }

    @Override
    protected void process(int entityId) {
        LifeCycleComponent life = mLife.get(entityId);
        
        // Increment age (assuming 1 tick per call for now, but should use delta)
        life.ageTicks++;

        if (life.ageTicks > life.maxLifespan) {
            if (mMeta.has(entityId)) {
                MetabolismComponent meta = mMeta.get(entityId);
                meta.alive = false;
            } else {
                // If it has no metabolism component, we might want to delete it directly
                // world.delete(entityId);
            }
        }
    }
}
