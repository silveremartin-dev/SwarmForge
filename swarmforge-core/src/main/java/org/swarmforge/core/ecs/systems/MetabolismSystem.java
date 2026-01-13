package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.MetabolismComponent;

/**
 * System that handles energy consumption, hunger, and death checks.
 */
public class MetabolismSystem extends IteratingSystem {
    ComponentMapper<MetabolismComponent> mMetabolism;

    public MetabolismSystem() {
        super(Aspect.all(MetabolismComponent.class));
    }

    @Override
    protected void process(int entityId) {
        MetabolismComponent meta = mMetabolism.get(entityId);
        
        if (!meta.alive) return;

        // Basic daily consumption
        float delta = world.getDelta();
        // Assume delta is seconds or ticks, adjust rates accordingly.
        // Copying rates from Individual.java: energy -= 0.1f * metabolism
        
        float rate = meta.metabolicRate * delta;
        
        meta.energy -= 0.1f * rate;
        meta.hunger += 0.05f * rate;
        meta.thirst += 0.03f * rate;

        // Check survival conditions
        if (meta.energy <= 0 || meta.hunger >= 100 || meta.thirst >= 100) {
            meta.alive = false;
            // Ideally we would emit a generic "DeathEvent" or handle cleanup
            // For now, we just mark flag.
        }
    }
}
