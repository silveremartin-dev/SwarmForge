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

        position.x += velocity.dx * world.getDelta();
        position.y += velocity.dy * world.getDelta();
        position.z += velocity.dz * world.getDelta();
    }
}
