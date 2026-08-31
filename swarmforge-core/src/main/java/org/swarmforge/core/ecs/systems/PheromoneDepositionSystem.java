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

    @Override
    protected void begin() {
        stepCounter++;
    }

    @Override
    protected void process(int entityId) {
        if (pheromoneGrid == null) return;

        PositionComponent pos = mPosition.get(entityId);
        boolean isCarryingFood = mInventory != null && mInventory.has(entityId) &&
                mInventory.get(entityId).carriedItem == InventoryComponent.ItemType.FOOD;

        // Rotational Interleaving: 1/10 of entities deposit per tick, scaled by dt
        // At 10,000 agents: 1,000 grid writes/tick (was 3,333). Biologically still realistic.
        int sampleInterval = 10;
        if ((stepCounter + entityId) % sampleInterval == 0) {
            float depositAmount = 0.5f * (simulationStepSeconds / 0.016666667f) * sampleInterval;
            int pType = isCarryingFood ? PheromoneType.FOOD_TRAIL.getIndex() : PheromoneType.HOME_TRAIL.getIndex();
            pheromoneGrid.deposit((int) pos.x, (int) pos.y, (int) pos.z, pType, depositAmount);
        }
    }
}
