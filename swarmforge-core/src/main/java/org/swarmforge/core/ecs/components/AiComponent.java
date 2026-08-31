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
    // Seconds between major brain updates, randomized to reduce spikes
    /**
     * Seconds between AI brain updates — staggered per-entity to flatten per-tick CPU spikes.
     * At 2-3 s interval: each agent gets a decision ~0.5×/s which is ethologically realistic.
     * At 50,000 agents & 60 Hz: ~1,666 AI decisions/tick → viable on quad-core CPU.
     */
    public float decisionInterval = 2.0f + java.util.concurrent.ThreadLocalRandom.current().nextFloat() * 1.0f;
    
    // Runtime storage for the actual AI object (RLArchitecture, etc.)
    // We use Object to avoid circular deps or complex generics for now.
    public Object brainInstance;
}
