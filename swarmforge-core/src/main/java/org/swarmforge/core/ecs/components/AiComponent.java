package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * Component defining which AI logic controls this entity.
 */
public class AiComponent extends Component {
    public enum AiType {
        SIMPLE_FORAGER, // The basic random walk + homing
        RL_AGENT,       // Reinforcement Learning
        FSM_WORKER,     // Classical FSM
        FUZZY_LOGIC,    // Fuzzy Logic blend
        MANUAL          // Controlled by user/god mode
    }
    
    public AiType type = AiType.SIMPLE_FORAGER;
    
    // Timer/Cooldown for decision making to avoid CPU overload
    public float decisionTimer = 0f;
    public float decisionInterval = 0.5f; // Seconds between major brain updates
    
    // Runtime storage for the actual AI object (RLArchitecture, etc.)
    // We use Object to avoid circular deps or complex generics for now.
    public Object brainInstance;
}
