package org.swarmforge.client.ecs;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.client.view.SwarmViewerApp;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.ecs.components.RenderComponent;
import org.swarmforge.core.ecs.components.VelocityComponent;

/**
 * System running on the Client ECS World.
 * Reads ECS Position and updates the JMonkeyEngine Visuals.
 */
public class VisualSyncSystem extends IteratingSystem {
    ComponentMapper<PositionComponent> mPos;
    ComponentMapper<RenderComponent> mRender;
    
    // Direct reference to the JME App (Bridge)
    private final SwarmViewerApp jmeApp;

    public VisualSyncSystem(SwarmViewerApp jmeApp) {
        super(Aspect.all(PositionComponent.class, RenderComponent.class));
        this.jmeApp = jmeApp;
    }

    @Override
    protected void process(int entityId) {
        PositionComponent pos = mPos.get(entityId);
        // We use string ID for now to map to JME geometry map, 
        // but for InstancedNode we might need index management.
        // For this phase, we use the String ID based updateEntity method we refactored.
        
        jmeApp.updateEntity("ant_" + entityId, pos.x, pos.y, pos.z);
    }
    
    @Override
    protected void removed(int entityId) {
        jmeApp.removeEntity("ant_" + entityId);
    }
}
