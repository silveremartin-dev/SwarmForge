package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.ecs.components.VelocityComponent;
import org.swarmforge.core.ecs.components.MetabolismComponent;
import org.swarmforge.core.ecs.components.RlAgentComponent;

/**
 * ECS System bridging SwarmForge entity sensory states with external RL (PyTorch/TensorFlow) models.
 * Encapsulates observation tensor extraction and action tensor execution.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class RlBridgeSystem extends IteratingSystem {

    private ComponentMapper<PositionComponent> mPosition;
    private ComponentMapper<VelocityComponent> mVelocity;
    private ComponentMapper<MetabolismComponent> mMetabolism;
    private ComponentMapper<RlAgentComponent> mRlAgent;

    public RlBridgeSystem() {
        super(Aspect.all(PositionComponent.class, MetabolismComponent.class, RlAgentComponent.class));
    }

    @Override
    protected void process(int entityId) {
        PositionComponent pos = mPosition.get(entityId);
        MetabolismComponent meta = mMetabolism.get(entityId);
        RlAgentComponent rl = mRlAgent.get(entityId);

        if (!meta.alive) return;

        // 1. Compile Observation Vector
        rl.observations[0] = 0.5f; // Pheromone level placeholder
        rl.observations[3] = pos.x;
        rl.observations[4] = pos.y;
        rl.observations[5] = pos.z;
        rl.observations[9] = meta.energy / meta.maxEnergy;
        rl.observations[10] = meta.alive ? 1.0f : 0.0f;

        // 2. Execute Actions received from RL Inference Engine (PyTorch / ONNX)
        if (mVelocity != null && mVelocity.has(entityId)) {
            VelocityComponent vel = mVelocity.get(entityId);
            vel.dx = rl.actions[0];
            vel.dy = rl.actions[1];
            vel.dz = rl.actions[2];
        }

        // 3. Compute Reward (e.g. survival + energy gain)
        rl.cumulativeReward += meta.energy * 0.001f;
    }
}
