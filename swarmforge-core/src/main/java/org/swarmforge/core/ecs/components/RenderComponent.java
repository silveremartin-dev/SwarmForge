package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * Component for holding rendering metadata.
 * The Client uses this to decide what mesh/material to draw.
 */
public class RenderComponent extends Component {
    public String meshId = "default_ant";
    public int color = 0xFF0000; // RGB or ARGB
    public float scale = 1.0f;
    
    public RenderComponent() {}
}
