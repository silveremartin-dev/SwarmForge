package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.InventoryComponent;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.domain.PheromoneType;

/**
 * ECS System handling rotational-interleaved chemical trail deposition into SparsePheromoneGrid.
 * Enables zero-allocation mass pheromone deposition for high-density agent populations.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PheromoneDepositionSystem extends IteratingSystem {

    private ComponentMapper<PositionComponent> mPosition;
    private ComponentMapper<InventoryComponent> mInventory;

    private SparsePheromoneGrid pheromoneGrid;
    private long stepCounter = 0;
    private float simulationStepSeconds = 0.016666667f;

    public PheromoneDepositionSystem() {
        super(Aspect.all(PositionComponent.class));
    }

    public void setPheromoneGrid(SparsePheromoneGrid grid) {
        this.pheromoneGrid = grid;
    }

    public void setSimulationStepSeconds(float dt) {
        this.simulationStepSeconds = Math.max(0.001f, dt);
    }

    private static final float SAMPLE_INTERVAL_SEC = 0.1666667f; // Fixed 166.7ms interval (~6 Hz)
    private float sampleAccumulatorSec = 0f;
    private boolean samplingFrame = false;

    @Override
    protected void begin() {
        sampleAccumulatorSec += world.getDelta();
        if (sampleAccumulatorSec >= SAMPLE_INTERVAL_SEC) {
            sampleAccumulatorSec -= SAMPLE_INTERVAL_SEC;
            samplingFrame = true;
            stepCounter++;
        } else {
            samplingFrame = false;
        }
    }

    @Override
    protected void process(int entityId) {
        if (pheromoneGrid == null || !samplingFrame) return;

        PositionComponent pos = mPosition.get(entityId);
        boolean isCarryingFood = mInventory != null && mInventory.has(entityId) &&
                mInventory.get(entityId).carriedItem == InventoryComponent.ItemType.FOOD;

        // Rotational Interleaving: all entities deposit across rotating frames
        int sampleModulo = 4;
        if ((entityId + stepCounter) % sampleModulo == 0) {
            PheromoneType pType = isCarryingFood ? PheromoneType.FOOD_TRAIL : PheromoneType.HOME_TRAIL;
            float depositAmount = 0.5f * (world.getDelta() / 0.016666667f) * sampleModulo;
            int px = Math.max(0, Math.min(pheromoneGrid.getWidth() - 1, (int) pos.x));
            int py = Math.max(0, Math.min(pheromoneGrid.getHeight() - 1, (int) pos.y));
            int pz = Math.max(0, Math.min(pheromoneGrid.getDepth() - 1, (int) pos.z));
            pheromoneGrid.deposit(px, py, pz, pType.getIndex(), depositAmount);
        }
    }
}
