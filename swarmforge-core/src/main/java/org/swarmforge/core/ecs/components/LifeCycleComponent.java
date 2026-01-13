package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * Component for tracking age, caste, and life stage.
 */
public class LifeCycleComponent extends Component {
    public int ageTicks = 0;
    public int maxLifespan = 5000;
    
    public enum LifeStage { EGG, LARVA, PUPA, ADULT }
    public LifeStage stage = LifeStage.ADULT;
    
    public String casteName = "WORKER";
}
