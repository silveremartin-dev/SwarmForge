package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.AiComponent;
import org.swarmforge.core.ecs.components.ColonyComponent;
import org.swarmforge.core.ecs.components.EthologyComponent;
import org.swarmforge.core.ecs.components.MetabolismComponent;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.util.FastDeterministicRandom;

/**
 * Continuous Vectorized Neural/FSM Decision Engine for Eusocial Swarm Entities.
 * Maps 8 sensory inputs (light, humidity, temperature, pheromone gradient, energy,
 * health, stridulation signal, intruder proximity) through a lightweight matrix dot-product
 * into continuous behavioral activation weights.
 *
 * Uses FastDeterministicRandom to guarantee 100% deterministic decision loops
 * across all execution runs.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class SwarmNeuralDecisionEngine extends IteratingSystem {

    private ComponentMapper<PositionComponent>   mPosition;
    private ComponentMapper<MetabolismComponent> mMetabolism;
    private ComponentMapper<EthologyComponent>   mEthology;
    private ComponentMapper<AiComponent>          mAi;
    private ComponentMapper<ColonyComponent>      mColony;

    private SubterraneanHydrologySystem hydrologySystem;
    private final FastDeterministicRandom deterministicRng = new FastDeterministicRandom(42L);

    // Static Neural Weight Matrix: 8 sensory inputs -> 4 action output logits
    // Inputs:  [0:Energy, 1:Health, 2:Temp, 3:Moisture, 4:Pheromone, 5:Depth, 6:Intruder, 7:Noise]
    // Outputs: [0:Forage, 1:Flee, 2:Rest, 3:NurseryGroom]
    private static final float[][] WEIGHT_MATRIX = {
        {  0.8f,  0.4f,  0.1f,  0.2f,  0.6f, -0.3f, -0.4f,  0.0f }, // Forage (High energy & health encourage foraging)
        { -0.5f, -0.8f,  0.9f, -0.2f, -0.3f,  0.1f,  0.95f, 0.4f }, // Flee
        { -0.9f, -0.5f, -0.4f,  0.3f, -0.5f,  0.8f, -0.7f, -0.2f }, // Rest (Low energy or low health encourages resting)
        {  0.3f,  0.5f,  0.4f,  0.8f,  0.2f,  0.5f, -0.8f, -0.1f }  // NurseryGroom
    };

    private static final float SAMPLE_INTERVAL_SEC = 0.5f; // Fixed 2Hz decision update rate
    private float accumulatorSec = 0f;
    private boolean decisionFrame = false;

    public SwarmNeuralDecisionEngine() {
        super(Aspect.all(PositionComponent.class, MetabolismComponent.class, EthologyComponent.class, AiComponent.class));
    }

    private final float[] sensoryBuffer = new float[8];
    private SpatialPartitioningSystem spatialSystem;
    private org.swarmforge.core.gpu.SparsePheromoneGrid pheromoneGrid;

    public void setPheromoneGrid(org.swarmforge.core.gpu.SparsePheromoneGrid grid) {
        this.pheromoneGrid = grid;
    }

    @Override
    protected void begin() {
        accumulatorSec += world.getDelta();
        if (accumulatorSec >= SAMPLE_INTERVAL_SEC) {
            accumulatorSec -= SAMPLE_INTERVAL_SEC;
            decisionFrame = true;
        } else {
            decisionFrame = false;
        }

        if (hydrologySystem == null) {
            hydrologySystem = world.getSystem(SubterraneanHydrologySystem.class);
        }
        if (spatialSystem == null) {
            spatialSystem = world.getSystem(SpatialPartitioningSystem.class);
        }
    }

    @Override
    protected void process(int entityId) {
        if (!decisionFrame) return;

        MetabolismComponent meta = mMetabolism.get(entityId);
        if (!meta.alive) return;

        PositionComponent pos = mPosition.get(entityId);
        EthologyComponent eth = mEthology.get(entityId);
        AiComponent ai = mAi.get(entityId);

        // 1. Assemble Sensory Vector into zero-allocation instance buffer
        float temp = hydrologySystem != null ? hydrologySystem.getSoilTemperature(pos.x, pos.y, pos.z) : 18.0f;
        float moisture = hydrologySystem != null ? hydrologySystem.getSoilMoisture(pos.x, pos.y, pos.z) : 0.4f;

        float pheroConcentration = 0.0f;
        if (pheromoneGrid != null) {
            int px = Math.max(0, (int) pos.x);
            int py = Math.max(0, (int) pos.y);
            int pz = Math.max(0, (int) pos.z);
            pheroConcentration = pheromoneGrid.getConcentration(px, py, pz, org.swarmforge.core.domain.PheromoneType.FOOD_TRAIL.getIndex());
        }

        // Count only foreign / enemy entities as intruders (exclude nestmates)
        int foreignCount = 0;
        if (spatialSystem != null) {
            java.util.UUID myColonyId = mColony != null && mColony.has(entityId) ? mColony.get(entityId).colonyId : null;
            var nearby = spatialSystem.getNearbyEntities(pos.x, pos.y, pos.z);
            for (int neighborId : nearby) {
                if (neighborId == entityId) continue;
                if (mColony != null && mColony.has(neighborId)) {
                    java.util.UUID neighborColonyId = mColony.get(neighborId).colonyId;
                    if (myColonyId != null && !myColonyId.equals(neighborColonyId)) {
                        foreignCount++;
                    }
                }
            }
        }

        float surfaceZ = 16.0f;
        if (pheromoneGrid != null && pheromoneGrid.getTerrarium() != null) {
            surfaceZ = pheromoneGrid.getTerrarium().getSurfaceElevation(pos.x, pos.y);
        }
        float depthRatio = Math.max(0.0f, (surfaceZ - pos.z) / Math.max(1.0f, surfaceZ));

        sensoryBuffer[0] = meta.energy / 100.0f;
        sensoryBuffer[1] = meta.health / 100.0f;
        sensoryBuffer[2] = (temp - 10.0f) / 30.0f;
        sensoryBuffer[3] = moisture;
        sensoryBuffer[4] = Math.min(1.0f, pheroConcentration);
        sensoryBuffer[5] = Math.min(1.0f, depthRatio);
        sensoryBuffer[6] = foreignCount > 5 ? 1.0f : (foreignCount / 5.0f);
        sensoryBuffer[7] = eth.isStridulating ? 1.0f : 0.0f;

        // 2. Matrix Vector Multiplication (Dot Product)
        float bestLogit = -999f;
        int bestAction = 0;

        for (int out = 0; out < 4; out++) {
            float logit = 0.0f;
            float[] weights = WEIGHT_MATRIX[out];
            for (int in = 0; in < 8; in++) {
                logit += weights[in] * sensoryBuffer[in];
            }
            // Deterministic micro-tie-breaker noise
            logit += (deterministicRng.nextFloat() - 0.5f) * 0.01f;

            if (logit > bestLogit) {
                bestLogit = logit;
                bestAction = out;
            }
        }

        // 3. Apply Decision Action State to Entity
        switch (bestAction) {
            case 0 -> ai.state = AiComponent.State.FORAGING;
            case 1 -> ai.state = AiComponent.State.RETREATING;
            case 2 -> ai.state = AiComponent.State.IDLE;
            case 3 -> ai.state = AiComponent.State.NURSING;
        }
    }
}
