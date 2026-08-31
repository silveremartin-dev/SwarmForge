package org.swarmforge.core.ecs.components;

import com.artemis.Component;

/**
 * ECS Component for Deep Reinforcement Learning (MARL) controlled entities.
 * Stores observation vectors (sensory inputs: pheromones, health, food distance)
 * and action vectors (motion dx, dy, dz, mandible bite, pheromone deposit).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class RlAgentComponent extends Component {

    // Sensory Observation Vector [16 floats]:
    // 0..2: Local Pheromone conc (Alarm, Trail, Recruitment)
    // 3..5: Relative Nearest Food vector (dx, dy, dz)
    // 6..8: Relative Nest Entrance vector (dx, dy, dz)
    // 9: Current Energy (0..1)
    // 10: Current Health (0..1)
    // 11: Mandible Wear (0..1)
    // 12..15: Nearby ant density (same colony, rival colony, predator, queen)
    public float[] observations = new float[16];

    // Action Vector [5 floats]:
    // 0..2: Desired velocity steering (dx, dy, dz)
    // 3: Pheromone deposit type & amount (0=none, >0=trail/alarm)
    // 4: Action trigger (0=idle, 1=harvest/bite, 2=trophallaxis)
    public float[] actions = new float[5];

    public String modelId = "default_marl_v1";
    public float cumulativeReward = 0.0f;
}
