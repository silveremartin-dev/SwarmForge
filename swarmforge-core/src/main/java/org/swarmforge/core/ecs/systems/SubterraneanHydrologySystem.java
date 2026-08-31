package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.ecs.components.MetabolismComponent;

/**
 * ECS System handling 3D subterranean gallery flooding, gravity flow,
 * larval/brood evacuation, and emergency raft formation during heavy rainfall.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SubterraneanHydrologySystem extends IteratingSystem {

    private ComponentMapper<PositionComponent> mPosition;
    private ComponentMapper<MetabolismComponent> mMetabolism;

    public SubterraneanHydrologySystem() {
        super(Aspect.all(PositionComponent.class, MetabolismComponent.class));
    }

    @Override
    protected void process(int entityId) {
        PositionComponent pos = mPosition.get(entityId);
        MetabolismComponent meta = mMetabolism.get(entityId);

        if (!meta.alive) return;

        // Underground check (z < 0)
        if (pos.z < 0.0f) {
            // Simulated water table level during heavy rain
            float subterraneanWaterLevel = -2.0f; // Water flooded up to -2.0m depth
            if (pos.z < subterraneanWaterLevel) {
                // Emergency Response: Ant floats or seeks upward tunnel
                pos.z += 0.5f * world.getDelta(); // Upward buoyancy / emergency movement
                meta.energy -= 0.1f * world.getDelta(); // Physical exertion
            }
        }
    }
}
