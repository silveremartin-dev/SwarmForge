package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.ecs.components.PathogenComponent;
import org.swarmforge.core.ecs.components.MetabolismComponent;
import java.util.List;

/**
 * ECS System handling epidemic transmission and social immunity (allogrooming).
 * Uses O(1) SpatialPartitioningSystem lookups.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ParasiteSystem extends IteratingSystem {

    private ComponentMapper<PositionComponent> mPosition;
    private ComponentMapper<PathogenComponent> mPathogen;
    private ComponentMapper<MetabolismComponent> mMetabolism;

    private SpatialPartitioningSystem spatialSystem;

    public ParasiteSystem() {
        super(Aspect.all(PositionComponent.class, PathogenComponent.class, MetabolismComponent.class));
    }

    @Override
    protected void process(int entityId) {
        PathogenComponent pathogen = mPathogen.get(entityId);
        MetabolismComponent meta = mMetabolism.get(entityId);

        if (!meta.alive || pathogen.activePathogens == PathogenComponent.TYPE_NONE) return;

        float delta = world.getDelta();
        pathogen.incubationTimer += delta;

        // Pathogen damage progression
        if (pathogen.incubationTimer > 5.0f) { // After 5 seconds incubation
            pathogen.viralLoad = Math.min(100.0f, pathogen.viralLoad + 2.0f * delta);
            meta.energy -= 0.5f * delta * (pathogen.viralLoad / 50.0f);
            if (meta.energy <= 0.0f) {
                meta.alive = false;
                return;
            }
        }

        // Spatial transmission to nearby nestmates
        if (spatialSystem == null) {
            spatialSystem = world.getSystem(SpatialPartitioningSystem.class);
        }
        if (spatialSystem == null) return;

        PositionComponent pos = mPosition.get(entityId);
        List<Integer> nearby = spatialSystem.getNearbyEntities(pos.x, pos.y, pos.z);

        for (int neighborId : nearby) {
            if (neighborId == entityId) continue;

            PathogenComponent neighborPath = mPathogen.get(neighborId);
            MetabolismComponent neighborMeta = mMetabolism.get(neighborId);

            if (neighborMeta != null && neighborMeta.alive) {
                if (neighborPath != null) {
                    // Contagion
                    if (neighborPath.activePathogens == PathogenComponent.TYPE_NONE && pathogen.viralLoad > 20.0f) {
                        neighborPath.activePathogens = pathogen.activePathogens;
                        neighborPath.viralLoad = 5.0f;
                        neighborPath.incubationTimer = 0.0f;
                    }
                    // Social Immunity (Allogrooming: healthy nestmate cleans infected one)
                    else if (neighborPath.activePathogens == PathogenComponent.TYPE_NONE) {
                        pathogen.viralLoad = Math.max(0.0f, pathogen.viralLoad - 3.0f * delta);
                        if (pathogen.viralLoad <= 0.0f) {
                            pathogen.activePathogens = PathogenComponent.TYPE_NONE;
                        }
                    }
                }
            }
        }
    }
}
