package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * Component for entity velocity.
 */
public class VelocityComponent extends Component {
    public float dx, dy, dz;
    public float speed = 1.0f;
    
    public VelocityComponent() {}
}
