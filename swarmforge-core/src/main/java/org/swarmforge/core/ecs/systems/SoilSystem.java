package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.SoilComponent;

/**
 * Manages ecological soil properties.
 */
public class SoilSystem extends IteratingSystem {
    ComponentMapper<SoilComponent> mSoil;

    public SoilSystem() {
        super(Aspect.all(SoilComponent.class));
    }

    @Override
    protected void process(int entityId) {
        SoilComponent soil = mSoil.get(entityId);
        float delta = world.getDelta();

        // Evaporation: moisture decreases slowly
        soil.moisture = Math.max(0, soil.moisture - 0.001f * delta);
        
        // Natural nutrient recovery (very slow)
        soil.nutrientLevel = Math.min(1.0f, soil.nutrientLevel + 0.0001f * delta);
    }
}
