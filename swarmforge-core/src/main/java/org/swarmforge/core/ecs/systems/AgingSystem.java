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
        
        // Increment age in seconds using simulation delta time
        float dt = world.getDelta();
        life.ageSeconds += dt;

        if (life.ageSeconds >= life.maxLifespanSeconds) {
            if (mMeta.has(entityId)) {
                MetabolismComponent meta = mMeta.get(entityId);
                if (meta.causeOfDeath == null || "Inconnue".equals(meta.causeOfDeath)) {
                    meta.causeOfDeath = "Old Age / Natural Senescence";
                }
                meta.alive = false;
            }
        }
    }
}
