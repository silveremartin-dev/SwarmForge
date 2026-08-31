package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * Component for biological needs and energy.
 */
public class MetabolismComponent extends Component {
    public float energy = 100f;
    public float maxEnergy = 100f;
    public float health = 100f;
    public float maxHealth = 100f;
    public float hunger = 0f;
    public float thirst = 0f;
    public boolean alive = true;
    
    // Metabolic rate multiplier (e.g. 1.0 = normal, 2.0 = fast burn)
    public float metabolicRate = 1.0f;
}
