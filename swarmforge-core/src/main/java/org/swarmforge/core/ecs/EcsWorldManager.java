package org.swarmforge.core.ecs;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import org.swarmforge.core.ecs.systems.*;
import org.swarmforge.core.gpu.SparsePheromoneGrid;

/**
 * Unified ECS Manager encapsulating the Artemis-odb World lifecycle and system pipeline.
 * Serves as the single high-performance engine for all SwarmForge simulations (v2.0).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EcsWorldManager {

    private final World world;
    private final EcsColonyFactory colonyFactory;

    // Systems
    private final MovementSystem movementSystem;
    private final SpatialPartitioningSystem spatialPartitioningSystem;
    private final TrophallaxisSystem trophallaxisSystem;
    private final PheromoneDepositionSystem pheromoneDepositionSystem;
    private final MandibularBiomechanicsSystem mandibularBiomechanicsSystem;
    private final MetabolismSystem metabolismSystem;
    private final AgingSystem agingSystem;
    private final AiSystem aiSystem;
    private final ForagingSystem foragingSystem;
    private final SoilSystem soilSystem;
    private final ParasiteSystem parasiteSystem;
    private final RlBridgeSystem rlBridgeSystem;
    private final SubterraneanHydrologySystem subterraneanHydrologySystem;
    private final EthologyEcsSystem ethologyEcsSystem;
    private final org.swarmforge.core.spatial.SpatialChunkManager chunkManager;

    public EcsWorldManager() {
        this(null);
    }

    public EcsWorldManager(SparsePheromoneGrid pheromoneGrid) {
        this.movementSystem = new MovementSystem();
        this.spatialPartitioningSystem = new SpatialPartitioningSystem();
        this.trophallaxisSystem = new TrophallaxisSystem();
        this.pheromoneDepositionSystem = new PheromoneDepositionSystem();
        this.mandibularBiomechanicsSystem = new MandibularBiomechanicsSystem();
        this.metabolismSystem = new MetabolismSystem();
        this.agingSystem = new AgingSystem();
        this.aiSystem = new AiSystem();
        this.foragingSystem = new ForagingSystem();
        this.soilSystem = new SoilSystem();
        this.parasiteSystem = new ParasiteSystem();
        this.rlBridgeSystem = new RlBridgeSystem();
        this.subterraneanHydrologySystem = new SubterraneanHydrologySystem();
        this.ethologyEcsSystem = new EthologyEcsSystem();
        this.chunkManager = new org.swarmforge.core.spatial.SpatialChunkManager();

        if (pheromoneGrid != null) {
            this.pheromoneDepositionSystem.setPheromoneGrid(pheromoneGrid);
        }

        WorldConfigurationBuilder config = new WorldConfigurationBuilder()
                .with(
                        movementSystem,
                        spatialPartitioningSystem,
                        trophallaxisSystem,
                        pheromoneDepositionSystem,
                        mandibularBiomechanicsSystem,
                        metabolismSystem,
                        agingSystem,
                        aiSystem,
                        foragingSystem,
                        soilSystem,
                        parasiteSystem,
                        rlBridgeSystem,
                        subterraneanHydrologySystem,
                        ethologyEcsSystem
                );

        this.world = new World(config.build());
        this.colonyFactory = new EcsColonyFactory(world);
    }

    public void setSparsePheromoneGrid(SparsePheromoneGrid grid) {
        this.pheromoneDepositionSystem.setPheromoneGrid(grid);
    }

    private boolean enableRenderYieldGuard = false;

    public void setEnableRenderYieldGuard(boolean enable) {
        this.enableRenderYieldGuard = enable;
    }

    /**
     * Ticks the entire unified ECS simulation pipeline with delta time scaling.
     */
    public void step(float deltaSeconds) {
        world.setDelta(deltaSeconds);
        pheromoneDepositionSystem.setSimulationStepSeconds(deltaSeconds);
        world.process();
        if (enableRenderYieldGuard) {
            chunkManager.enforceRenderYieldGuard();
        }
    }

    public World getWorld() {
        return world;
    }

    public EcsColonyFactory getColonyFactory() {
        return colonyFactory;
    }

    public SpatialPartitioningSystem getSpatialPartitioningSystem() {
        return spatialPartitioningSystem;
    }
}
