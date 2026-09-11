package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.ecs.components.VelocityComponent;

public class MovementSystem extends IteratingSystem {
    ComponentMapper<PositionComponent> mPosition;
    ComponentMapper<VelocityComponent> mVelocity;

    public MovementSystem() {
        super(Aspect.all(PositionComponent.class, VelocityComponent.class));
    }

    @Override
    protected void process(int entityId) {
        PositionComponent position = mPosition.get(entityId);
        VelocityComponent velocity = mVelocity.get(entityId);

        float dt = world.getDelta();
        position.x += velocity.dx * dt;
        position.y += velocity.dy * dt;
        position.z += velocity.dz * dt;

        if (Math.abs(velocity.dx) > 0.001f || Math.abs(velocity.dy) > 0.001f) {
            position.heading = (float) Math.atan2(velocity.dy, velocity.dx);
        }
    }
}
