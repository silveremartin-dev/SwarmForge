package org.swarmforge.core.engine;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Vector3f;
import org.swarmforge.core.ecs.EcsColonyFactory;
import org.swarmforge.core.ecs.EcsWorldManager;
import org.swarmforge.core.ecs.components.PositionComponent;
import org.swarmforge.core.ecs.systems.SpatialPartitioningSystem;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.species.Species;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pure Java 21 Artemis-odb ECS implementation of {@link SimulationEngine}.
 *
 * Provides high-throughput entity iteration, Morton-hashed spatial indexing,
 * zero GC allocations during steady-state ticks, and full platform portability.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class JavaEcsEngine implements SimulationEngine {

    private static final Logger log = LoggerFactory.getLogger(JavaEcsEngine.class);

    private EcsWorldManager worldManager;
    private SparsePheromoneGrid pheromoneGrid;
    private int widthMeters;
    private int depthMeters;
    private int heightMeters;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private boolean initialized = false;

    private long globalTickCount = 0L;
    private double accumulatedSimTimeSec = 0.0;
    private double lastTickDurationMs = 0.0;
    private double rollingAvgTickDurationMs = 0.0;
    private static final double EMA_ALPHA = 0.05; // Exponential moving average smoothing factor

    private ComponentMapper<PositionComponent> mPosition;
    private EntitySubscription allEntitiesSubscription;

    private final CompactDodEntityBuffer dodBuffer = new CompactDodEntityBuffer();
    private boolean useDodCompaction = true; // Enabled by default for maximum cache locality & zero GC

    private final java.util.concurrent.atomic.AtomicInteger activeEntityCount = new java.util.concurrent.atomic.AtomicInteger(0);

    public JavaEcsEngine() {
        // Default constructor
    }

    public void setDodCompactionEnabled(boolean enabled) {
        this.useDodCompaction = enabled;
        log.info("JavaEcsEngine DOD Memory Compaction: {}", enabled ? "ENABLED (SoA Contiguous Layout)" : "DISABLED (Artemis OOP ECS)");
    }

    public boolean isDodCompactionEnabled() {
        return useDodCompaction;
    }

    public CompactDodEntityBuffer getDodBuffer() {
        return dodBuffer;
    }

    @Override
    public synchronized void initialize(int worldWidthMeters, int worldDepthMeters, int worldHeightMeters, SparsePheromoneGrid pheromoneGrid) {
        this.widthMeters = worldWidthMeters;
        this.depthMeters = worldDepthMeters;
        this.heightMeters = worldHeightMeters;
        this.pheromoneGrid = pheromoneGrid;

        this.dodBuffer.clear();
        this.worldManager = new EcsWorldManager(this.pheromoneGrid);
        World world = this.worldManager.getWorld();
        this.mPosition = world.getMapper(PositionComponent.class);
        this.allEntitiesSubscription = world.getAspectSubscriptionManager().get(Aspect.all());

        this.initialized = true;
        log.info("JavaEcsEngine initialized: domain={}x{}x{}m, DOD Compaction={}, Artemis ECS world ready.",
                worldWidthMeters, worldDepthMeters, worldHeightMeters, useDodCompaction);
    }

    @Override
    public void start() {
        if (!initialized) {
            throw new IllegalStateException("Cannot start uninitialized JavaEcsEngine");
        }
        running.set(true);
        log.info("JavaEcsEngine started.");
    }

    @Override
    public void pause() {
        running.set(false);
        log.info("JavaEcsEngine paused.");
    }

    @Override
    public void stop() {
        running.set(false);
        log.info("JavaEcsEngine stopped at tick {}", globalTickCount);
    }

    @Override
    public synchronized void reset() {
        stop();
        dodBuffer.clear();
        if (worldManager != null) {
            this.worldManager = new EcsWorldManager(this.pheromoneGrid);
            World world = this.worldManager.getWorld();
            this.mPosition = world.getMapper(PositionComponent.class);
            this.allEntitiesSubscription = world.getAspectSubscriptionManager().get(Aspect.all());
        }
        globalTickCount = 0L;
        accumulatedSimTimeSec = 0.0;
        lastTickDurationMs = 0.0;
        rollingAvgTickDurationMs = 0.0;
        activeEntityCount.set(0);
        log.info("JavaEcsEngine state reset.");
    }

    private static final int MORTON_SORT_INTERVAL_TICKS = 50;
    private int parallelThreads = Runtime.getRuntime().availableProcessors();

    public void setParallelThreads(int threads) {
        this.parallelThreads = Math.max(1, threads);
    }

    public int getParallelThreads() {
        return parallelThreads;
    }

    @Override
    public void step(float deltaSeconds) {
        if (!initialized) {
            throw new IllegalStateException("JavaEcsEngine is not initialized");
        }

        long startNanos = System.nanoTime();

        if (useDodCompaction && dodBuffer.getCount() > 0) {
            // Periodic Morton 3D Z-order spatial cache line compaction (every 50 ticks)
            if (globalTickCount > 0 && globalTickCount % MORTON_SORT_INTERVAL_TICKS == 0) {
                dodBuffer.sortSpatialCache();
            }

            // High-throughput cache-aligned SIMD vectorized parallel physical step
            if (parallelThreads > 1) {
                dodBuffer.stepParallel(deltaSeconds, (float) widthMeters, (float) depthMeters, (float) heightMeters, parallelThreads);
            } else {
                dodBuffer.step(deltaSeconds, (float) widthMeters, (float) depthMeters, (float) heightMeters);
            }
        } else if (worldManager != null) {
            worldManager.step(deltaSeconds);
        }

        long endNanos = System.nanoTime();
        double durationMs = (endNanos - startNanos) / 1_000_000.0;

        this.lastTickDurationMs = durationMs;
        if (this.rollingAvgTickDurationMs == 0.0) {
            this.rollingAvgTickDurationMs = durationMs;
        } else {
            this.rollingAvgTickDurationMs = (EMA_ALPHA * durationMs) + ((1.0 - EMA_ALPHA) * this.rollingAvgTickDurationMs);
        }

        this.globalTickCount++;
        this.accumulatedSimTimeSec += deltaSeconds;
    }

    @Override
    public int spawnAnt(UUID colonyId, Individual.Caste caste, Individual.Job job,
                        float x, float y, float z, Species species) {
        if (!initialized) return -1;
        if (useDodCompaction) {
            int entityId = dodBuffer.spawn(colonyId, caste, job, x, y, z);
            activeEntityCount.incrementAndGet();
            return entityId;
        } else {
            EcsColonyFactory colonyFactory = worldManager.getColonyFactory();
            int entityId = colonyFactory.createIndividual(colonyId, caste, job, x, y, z, species);
            if (entityId >= 0) {
                activeEntityCount.incrementAndGet();
            }
            return entityId;
        }
    }

    @Override
    public int spawnIndividual(Individual individual) {
        if (!initialized || individual == null) return -1;
        return spawnAnt(
                individual.getColonyId(),
                individual.getCaste(),
                individual.getJob(),
                individual.getX(),
                individual.getY(),
                individual.getZ(),
                individual.getSpecies()
        );
    }

    @Override
    public boolean despawnEntity(int entityId) {
        if (!initialized || entityId < 0) return false;
        World world = worldManager.getWorld();
        if (world.getEntityManager().isActive(entityId)) {
            world.delete(entityId);
            activeEntityCount.decrementAndGet();
            return true;
        }
        return false;
    }

    @Override
    public int getActiveEntityCount() {
        return activeEntityCount.get();
    }

    @Override
    public List<Integer> queryEntitiesInRadius(Vector3f center, float radius) {
        if (!initialized || center == null) return List.of();
        if (useDodCompaction) {
            List<Integer> list = new ArrayList<>();
            float r2 = radius * radius;
            int n = dodBuffer.getCount();
            float[] px = dodBuffer.posX;
            float[] py = dodBuffer.posY;
            float[] pz = dodBuffer.posZ;
            for (int i = 0; i < n; i++) {
                float dx = px[i] - center.x();
                float dy = py[i] - center.y();
                float dz = pz[i] - center.z();
                if ((dx * dx + dy * dy + dz * dz) <= r2) {
                    list.add(i);
                }
            }
            return list;
        } else {
            SpatialPartitioningSystem spatial = worldManager.getSpatialPartitioningSystem();
            if (spatial == null) return List.of();
            List<Integer> nearby = spatial.getNearbyEntities(center.x(), center.y(), center.z());
            return new ArrayList<>(nearby);
        }
    }

    @Override
    public Vector3f getEntityPosition(int entityId) {
        if (!initialized || entityId < 0) return null;
        if (useDodCompaction) {
            if (entityId < dodBuffer.getCount()) {
                return new Vector3f(dodBuffer.posX[entityId], dodBuffer.posY[entityId], dodBuffer.posZ[entityId]);
            }
            return null;
        } else {
            if (mPosition == null || !worldManager.getWorld().getEntityManager().isActive(entityId)) return null;
            PositionComponent pos = mPosition.get(entityId);
            if (pos == null) return null;
            return new Vector3f(pos.x, pos.y, pos.z);
        }
    }

    @Override
    public void updateEnvironmentalBoundaryConditions(float surfaceTemp, float surfaceMoistureRatio, float leafLitterBiomass) {
        if (worldManager != null) {
            worldManager.updateEnvironmentalBoundaryConditions(surfaceTemp, surfaceMoistureRatio, leafLitterBiomass);
        }
    }

    @Override
    public SimulationEngineType getEngineType() {
        return SimulationEngineType.JAVA_ECS;
    }

    @Override
    public EngineTelemetry getTelemetry() {
        double ups = rollingAvgTickDurationMs > 0.0 ? (1000.0 / rollingAvgTickDurationMs) : 0.0;
        int active = getActiveEntityCount();
        long memBytes = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        return new EngineTelemetry(
                SimulationEngineType.JAVA_ECS,
                globalTickCount,
                accumulatedSimTimeSec,
                active,
                lastTickDurationMs,
                rollingAvgTickDurationMs,
                ups,
                memBytes,
                running.get()
        );
    }

    @Override
    public boolean isOperational() {
        return initialized;
    }

    public EcsWorldManager getWorldManager() {
        return worldManager;
    }
}
