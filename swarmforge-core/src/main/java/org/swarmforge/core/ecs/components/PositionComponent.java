package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * Component for entity position and heading.
 * Data-oriented design: purely data, no logic.
 */
public class PositionComponent extends Component {
    public float x, y, z;
    public float heading; // Radians
    
    public PositionComponent() {}
    
    public PositionComponent(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
