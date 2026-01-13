package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * Stores soil parameters for a location or entity at a location.
 */
public class SoilComponent extends Component {
    public float moisture; // 0.0 to 1.0
    public float nutrientLevel; // 0.0 to 1.0
    public float compaction; // 0.0 to 1.0 (affects digging speed)
    
    public SoilComponent() {
        this.moisture = 0.5f;
        this.nutrientLevel = 0.5f;
        this.compaction = 0.2f;
    }
}
