/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.domain.FoodSource;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.event.SimulationEvent;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Core simulation engine for SwarmForge.
 * Manages the simulation loop, tick processing, and world state.
 * 
 * Uses sparse pheromone grid with terrain-aware diffusion.
 * Supports CPU multithreaded fallback when GPU is not available.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class Simulation {

    public enum State {
        STOPPED, RUNNING, PAUSED
    }

    private final Terrarium terrarium;
    private final org.swarmforge.core.gpu.SparsePheromoneGrid pheromoneGrid;
    private final org.swarmforge.core.ecology.WaterGrid waterGrid;
    private final CopyOnWriteArrayList<Colony> colonies;
    private final AtomicLong tickCount;
    private final AtomicReference<State> state;

    private long masterSeed = 1337L;
    private java.util.Random random = new java.util.Random(1337L);

    private int ticksPerSecond = 60;
    private float simulationStepSeconds = 0.016666667f;
    private double accumulatedSimulationSeconds = 0.0;
    private long tickDurationNanos;
    private int diffusionInterval = 5;
    private Thread simulationThread;
    private float speedMultiplier = 1.0f;
    private final SimulationTimeMachine timeMachine;
    private final SimulationEnvironmentUpdater environmentUpdater = new SimulationEnvironmentUpdater(this);
    private final SimulationEntityUpdater entityUpdater = new SimulationEntityUpdater(this);

    private volatile double measuredTps = 0.0;
    private long lastTpsSampleTime = System.nanoTime();
    private long ticksSinceLastSample = 0;

    public double getMeasuredTps() {
        return getActualTicksPerSecond();
    }

    public double getActualTicksPerSecond() {
        if (state.get() != State.RUNNING) {
            return 0.0;
        }
        if (System.nanoTime() - lastTpsSampleTime > 2_000_000_000L) {
            return 0.0;
        }
        return measuredTps;
    }

    public int getTicksPerSecond() {
        return ticksPerSecond;
    }

    public int getTargetTicksPerSecond() {
        return ticksPerSecond;
    }

    public void setTicksPerSecond(int tps) {
        this.ticksPerSecond = Math.max(1, Math.min(240, tps));
        this.tickDurationNanos = (long) (1_000_000_000L / (this.ticksPerSecond * speedMultiplier));
    }

    private final org.swarmforge.core.spatial.SpatialPartition<Individual> spatialIndex;
    private final CopyOnWriteArrayList<FoodSource> foodSources;
    private final org.swarmforge.core.spatial.SpatialPartition<FoodSource> foodIndex;
    private final org.swarmforge.core.world.WeatherSystem weather;
    private final org.swarmforge.core.world.SoilHydricCoupling soilHydricCoupling;
    private final org.swarmforge.core.world.WaterTable waterTable;
    private final org.swarmforge.core.spatial.AStarPathfinder pathfinder;
    private final org.swarmforge.core.world.SeasonManager seasonManager;
    private final TerritoryManager territoryManager;
    private final org.swarmforge.core.simulation.diseases.DiseaseManager diseaseManager;
    private final PredatorManager predatorManager;
    private final NuptialFlightSystem nuptialFlightSystem;
    private final DiapauseSystem diapauseSystem;
    private final SoilStructureSystem soilStructureSystem;
    private final PheromoneClimateSystem pheromoneClimateSystem;
    private final SymbiosisSystem symbiosisSystem;
    private final NestMicroclimateSystem nestMicroclimateSystem;
    private final MacroForagingSystem macroForagingSystem;
    private final org.swarmforge.core.world.VegetationSystem vegetationSystem;
    private final java.util.Map<org.swarmforge.core.domain.Colony, org.swarmforge.core.structure.ConstructionManager> constructionManagers = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.List<org.swarmforge.core.simulation.disasters.DisasterEvent> activeDisasters = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final FlowFieldCacheManager flowFieldCache = new FlowFieldCacheManager();
    private final org.swarmforge.core.ecs.EcsWorldManager ecsWorldManager;
    private final org.swarmforge.core.event.EventBus eventBus = new org.swarmforge.core.event.EventBus("simulation");

    public org.swarmforge.core.event.EventBus getEventBus() {
        return eventBus;
    }

    public void triggerDisaster(org.swarmforge.core.simulation.disasters.DisasterEvent disaster) {
        if (disaster != null) {
            disaster.trigger(this, terrarium);
            if (!disaster.isFinished()) {
                activeDisasters.add(disaster);
            }
        }
    }

    public java.util.List<org.swarmforge.core.simulation.disasters.DisasterEvent> getActiveDisasters() {
        return activeDisasters;
    }

    // Cluster
    private org.swarmforge.core.compute.ComputeCluster computeCluster;

    // Environment
    private final org.swarmforge.core.world.DayNightCycle dayNightCycle;
    private org.swarmforge.core.world.WeatherMarkovChain.WeatherState lastWeatherState = null;

    public org.swarmforge.core.world.DayNightCycle getDayNightCycle() {
        return dayNightCycle;
    }

    public org.swarmforge.core.world.WeatherSystem getWeatherSystem() {
        return weather;
    }

    public Simulation(Terrarium terrarium) {
        this.terrarium = terrarium;
        this.pheromoneGrid = new SparsePheromoneGrid(
                terrarium.getWidth(), terrarium.getHeight(), terrarium.getDepth());
        this.pheromoneGrid.setTerrarium(terrarium);
        this.pheromoneGrid.setSimulationStepSeconds(this.simulationStepSeconds);

        this.waterGrid = new org.swarmforge.core.ecology.WaterGrid(terrarium);

        this.state = new AtomicReference<>(State.STOPPED);
        // Default to Equator for neutral weather
        this.weather = new org.swarmforge.core.world.WeatherSystem(0.0, 0.0);
        this.soilHydricCoupling = new org.swarmforge.core.world.SoilHydricCoupling(terrarium != null ? terrarium.getDepth() : 64);
        this.waterTable = new org.swarmforge.core.world.WaterTable(terrarium != null ? terrarium.getWidth() : 100, terrarium != null ? terrarium.getDepth() : 64);
        this.seasonManager = new org.swarmforge.core.world.SeasonManager(this);

        // Initialize spatial indices
        float cellSize = 5.0f; // Tuned for interaction range (approx 2.0 to 4.0)
        this.spatialIndex = new org.swarmforge.core.spatial.SpatialHashMap<>(cellSize);
        this.foodIndex = new org.swarmforge.core.spatial.SpatialHashMap<>(cellSize);

        this.foodSources = new CopyOnWriteArrayList<>();
        this.colonies = new CopyOnWriteArrayList<>();
        this.tickCount = new AtomicLong(0);
        this.tickDurationNanos = 1_000_000_000L / ticksPerSecond;

        this.pathfinder = new org.swarmforge.core.spatial.AStarPathfinder(terrarium);
        this.timeMachine = new SimulationTimeMachine(this, 10000, 1);
        this.predatorManager = new PredatorManager(this);
        this.territoryManager = new TerritoryManager(this);
        this.diseaseManager = new org.swarmforge.core.simulation.diseases.DiseaseManager(this);

        this.nuptialFlightSystem = new NuptialFlightSystem(this);
        this.diapauseSystem = new DiapauseSystem(this);
        this.soilStructureSystem = new SoilStructureSystem(this);
        this.pheromoneClimateSystem = new PheromoneClimateSystem(this);
        this.symbiosisSystem = new SymbiosisSystem(this);
        this.nestMicroclimateSystem = new NestMicroclimateSystem(this);
        this.macroForagingSystem = new MacroForagingSystem();
        this.vegetationSystem = new org.swarmforge.core.world.VegetationSystem(
                terrarium != null ? terrarium.getWidth() : 100,
                terrarium != null ? terrarium.getHeight() : 100);

        this.dayNightCycle = new org.swarmforge.core.world.DayNightCycle();
        this.ecsWorldManager = new org.swarmforge.core.ecs.EcsWorldManager(this.pheromoneGrid);
        this.pluginManager = new org.swarmforge.core.plugin.PluginManager();
        this.pluginManager.setContext(new org.swarmforge.core.plugin.PluginContext(this, this.pluginManager));
    }

    public Simulation(Terrarium terrarium, long masterSeed) {
        this(terrarium);
        setMasterSeed(masterSeed);
    }

    private final org.swarmforge.core.plugin.PluginManager pluginManager;

    public org.swarmforge.core.plugin.PluginManager getPluginManager() {
        return pluginManager;
    }

    public org.swarmforge.core.ecs.EcsWorldManager getEcsWorldManager() {
        return ecsWorldManager;
    }

    public void setClusterManager(org.swarmforge.core.compute.ComputeCluster clusterManager) {
        this.computeCluster = clusterManager;
    }

    private static final int MAX_QUEUED_EVENTS = 1000;
    private final java.util.Queue<SimulationEvent> eventQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();

    public void addColony(Colony colony) {
        if (colony != null) {
            if (this.random != null && colony.getRandom() == null) {
                colony.setRandom(new java.util.Random(this.random.nextLong()));
            }
            colony.bootstrapDefaultResources();
        }
        colonies.add(colony);
        org.swarmforge.core.ecs.ColonyRegistry.register(colony);
        colony.addListener(new SimulationColonyObserver(this));
        seedInitialEnvironmentResources();
    }

    public Colony addColony(String speciesType) {
        return addColony(speciesType, 1, 150, 20);
    }

    public Colony addColony(String speciesType, int queens, int workers, int soldiers) {
        int nextColonyIndex = colonies.size();
        org.swarmforge.core.spatial.OptimalColonyPlacementEngine.PlacementResult pos = 
            org.swarmforge.core.spatial.OptimalColonyPlacementEngine.calculateOptimalPosition(terrarium, speciesType, nextColonyIndex, nextColonyIndex + 1, "Optimal Multi-Territory Cluster");
        return addColony(speciesType, queens, workers, soldiers, pos.x(), pos.y());
    }

    public Colony addColony(String speciesType, int queens, int workers, int soldiers, float x, float y) {
        return addColony(speciesType, queens, workers, soldiers, 0, x, y);
    }

    public Colony addColony(String speciesType, int queens, int workers, int soldiers, int brood, float x, float y) {
        return environmentUpdater.createColonyWithCastes(speciesType, queens, workers, soldiers, brood, x, y);
    }

    /**
     * Seeds initial surface vegetation and food clusters proportionally to map scale and population.
     */
    public void seedInitialEnvironmentResources() {
        environmentUpdater.seedInitialEnvironmentResources();
    }

    public void start() {
        seedInitialEnvironmentResources();
        if (state.compareAndSet(State.STOPPED, State.RUNNING) ||
                state.compareAndSet(State.PAUSED, State.RUNNING)) {
            org.swarmforge.core.event.EventBus.getInstance().publish(
                SimulationEvent.obtain(SimulationEvent.EventType.SIMULATION_STARTED, SimulationEvent.Severity.INFO, getTickCount(), "▶️ Simulation started", null)
            );
            simulationThread = Thread.ofVirtual().name("simulation-loop").start(this::runLoop);
        }
    }

    public void pause() {
        if (state.compareAndSet(State.RUNNING, State.PAUSED)) {
            org.swarmforge.core.event.EventBus.getInstance().publish(
                SimulationEvent.obtain(SimulationEvent.EventType.SIMULATION_PAUSED, SimulationEvent.Severity.INFO, getTickCount(), "⏸️ Simulation paused", null)
            );
        }
    }

    public org.swarmforge.core.gpu.SparsePheromoneGrid getPheromoneGrid() { return pheromoneGrid; }
    public org.swarmforge.core.world.WeatherSystem getWeather() { return weather; }
    public org.swarmforge.core.world.SoilHydricCoupling getSoilHydricCoupling() { return soilHydricCoupling; }
    public org.swarmforge.core.world.WaterTable getWaterTable() { return waterTable; }
    public org.swarmforge.core.ecology.WaterGrid getWaterGrid() { return waterGrid; }
    public SoilStructureSystem getSoilStructureSystem() { return soilStructureSystem; }
    public PheromoneClimateSystem getPheromoneClimateSystem() { return pheromoneClimateSystem; }

    public void stop() {
        if (state.compareAndSet(State.RUNNING, State.STOPPED) || state.compareAndSet(State.PAUSED, State.STOPPED)) {
            if (simulationThread != null) {
                simulationThread.interrupt();
                try {
                    simulationThread.join(300);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            org.swarmforge.core.event.EventBus.getInstance().publish(
                SimulationEvent.obtain(
                    SimulationEvent.EventType.SIMULATION_STOPPED,
                    SimulationEvent.Severity.INFO,
                    getTickCount(),
                    "⏹️ Simulation stopped",
                    null
                )
            );
        }
    }

    private void runLoop() {
        while (state.get() == State.RUNNING) {
            long startTime = System.nanoTime();
            tick();
            long elapsed = System.nanoTime() - startTime;
            long sleepNanos = tickDurationNanos - elapsed;
            if (sleepNanos > 0) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                // MAX Speed mode: Yield CPU to keep JavaFX UI thread and system fully responsive!
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    public void tick(float dtSeconds) {
        setSimulationStepSeconds(dtSeconds);
        tick();
    }

    public void tick() {
        long currentTick = tickCount.incrementAndGet();
        accumulatedSimulationSeconds += this.simulationStepSeconds;

        ticksSinceLastSample++;
        long now = System.nanoTime();
        long elapsedNano = now - lastTpsSampleTime;
        if (elapsedNano >= 1_000_000_000L) {
            measuredTps = (ticksSinceLastSample * 1_000_000_000.0) / elapsedNano;
            ticksSinceLastSample = 0;
            lastTpsSampleTime = now;
        }

        updateEnvironment(currentTick);
        SimulationTickExecutor.executeTick(
                this,
                currentTick,
                this.simulationStepSeconds,
                this.accumulatedSimulationSeconds,
                this.eventQueue,
                this.entityUpdater,
                this.timeMachine
        );
    }

    public void processGrowth(Individual ind) {
        entityUpdater.processGrowth(ind, tickCount.get(), accumulatedSimulationSeconds, eventQueue);
    }

    private void updateEnvironment(long currentTick) {
        environmentUpdater.updateEnvironment(currentTick);
    }

    public List<SimulationEvent> pollEvents() {
        java.util.List<SimulationEvent> events = new java.util.ArrayList<>();
        SimulationEvent e;
        while ((e = eventQueue.poll()) != null) {
            events.add(e);
        }
        return events;
    }

    // Getters & Setters
    public NestMicroclimateSystem getNestMicroclimateSystem() { return nestMicroclimateSystem; }
    public org.swarmforge.core.world.VegetationSystem getVegetationSystem() { return vegetationSystem; }
    public long getTickCount() { return tickCount.get(); }
    public void setTickCount(long ticks) { this.tickCount.set(ticks); }
    public float getElapsedSeconds() { return (float) (accumulatedSimulationSeconds > 0 ? accumulatedSimulationSeconds : (tickCount.get() * simulationStepSeconds)); }
    public double getElapsedSimulationSeconds() { return accumulatedSimulationSeconds > 0 ? accumulatedSimulationSeconds : (tickCount.get() * (double) simulationStepSeconds); }
    public void setElapsedSimulationSeconds(double seconds) { this.accumulatedSimulationSeconds = Math.max(0.0, seconds); }
    public State getState() { return state.get(); }
    public org.swarmforge.core.world.SeasonManager getSeasonManager() { return seasonManager; }
    public MacroForagingSystem getMacroForagingSystem() { return macroForagingSystem; }
    public SymbiosisSystem getSymbiosisSystem() { return symbiosisSystem; }
    public Terrarium getTerrarium() { return terrarium; }
    public org.swarmforge.core.spatial.SpatialPartition<Individual> getSpatialIndex() { return spatialIndex; }
    public List<Colony> getColonies() { return List.copyOf(colonies); }

    public Colony getColony(java.util.UUID id) {
        for (Colony c : colonies) {
            if (c.getId().equals(id)) return c;
        }
        return null;
    }

    public boolean isRunning() { return state.get() == State.RUNNING; }
    public void setDiffusionInterval(int interval) { this.diffusionInterval = Math.max(1, interval); }
    public void spawnFood(float x, float y, float z, float quantity, org.swarmforge.core.domain.ResourceType type) { foodSources.add(new FoodSource(x, y, z, quantity, type)); }
    public void addFoodSource(FoodSource foodSource) { foodSources.add(foodSource); }
    public org.swarmforge.core.spatial.SpatialPartition<FoodSource> getFoodIndex() { return foodIndex; }
    public List<FoodSource> getFoodSources() { return List.copyOf(foodSources); }
    public org.swarmforge.core.spatial.AStarPathfinder getPathfinder() { return pathfinder; }

    public float[] getFlowVector(float x, float y, float z, int targetX, int targetY, int targetZ) {
        return flowFieldCache.getFlowVector(terrarium, x, y, z, targetX, targetY, targetZ);
    }

    public PredatorManager getPredatorManager() { return predatorManager; }
    public TerritoryManager getTerritoryManager() { return territoryManager; }

    public void setMasterSeed(long seed) {
        this.masterSeed = seed;
        this.random = new java.util.Random(seed);
        if (this.vegetationSystem != null) {
            this.vegetationSystem.setSeed(seed ^ 0x5DEECE66DL);
            this.vegetationSystem.getPlants().clear();
        }
        this.foodSources.clear();
        if (this.predatorManager != null) {
            this.predatorManager.clearPredators();
        }
        seedInitialEnvironmentResources();
    }

    public long getMasterSeed() { return masterSeed; }
    public java.util.Random getRandom() { return random; }
    public void setRandom(java.util.Random random) { this.random = random; }
    public void clearColonies() { this.colonies.clear(); }
    public void clearFoodSources() { this.foodSources.clear(); }
    public SimulationTimeMachine getTimeMachine() { return timeMachine; }
    public void restoreState(long tick) { timeMachine.restoreState(tick); }
    public void reset(long tick) { timeMachine.reset(tick); }
    public void recordInitialSnapshot() { timeMachine.recordInitialSnapshot(); }
    public long getHighestRecordedTick() { return timeMachine.getHighestRecordedTick(); }

    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = Math.max(0.1f, Math.min(10.0f, multiplier));
        this.tickDurationNanos = (long) (1_000_000_000L / (ticksPerSecond * speedMultiplier));
    }

    public float getSpeedMultiplier() { return speedMultiplier; }
    public void speedUp() { setSpeedMultiplier(speedMultiplier + 0.5f); }
    public void slowDown() { setSpeedMultiplier(speedMultiplier - 0.5f); }
    public void normalSpeed() { setSpeedMultiplier(1.0f); }

    public SimulationHistory getHistory() { return timeMachine.getHistory(); }
    public boolean rewind(int steps) { return timeMachine.rewind(steps); }
    public java.util.Queue<org.swarmforge.core.event.SimulationEvent> getEventQueue() { return eventQueue; }
    public boolean seekToTick(long tick) { return timeMachine.seekToTick(tick); }
    public int getAutoCheckpointInterval() { return timeMachine.getAutoCheckpointInterval(); }
    public void setAutoCheckpointInterval(int interval) { timeMachine.setAutoCheckpointInterval(interval); }
    public void logIntervention(org.swarmforge.core.event.GodModeIntervention intervention) { timeMachine.logIntervention(intervention); }
    public java.util.List<org.swarmforge.core.event.GodModeIntervention> getInterventionJournal() { return timeMachine.getInterventionJournal(); }
    public synchronized SimulationCheckpoint createCheckpoint(String name) { return timeMachine.createCheckpoint(name); }
    public boolean restoreCheckpoint(SimulationCheckpoint cp) { return timeMachine.restoreCheckpoint(cp); }
    public java.util.List<SimulationCheckpoint> getCheckpoints() { return timeMachine.getCheckpoints(); }
    public void recordSnapshot() { timeMachine.recordSnapshot(); }
    public java.util.Map<org.swarmforge.core.domain.Colony, org.swarmforge.core.structure.ConstructionManager> getConstructionManagers() { return constructionManagers; }
    public org.swarmforge.core.compute.ComputeCluster getComputeCluster() { return computeCluster; }
    public int getDiffusionInterval() { return diffusionInterval; }
    public float getSimulationStepSeconds() { return simulationStepSeconds; }
    public double getAccumulatedSimulationSeconds() { return accumulatedSimulationSeconds; }

    public void setSimulationStepSeconds(float stepSeconds) {
        this.simulationStepSeconds = Math.max(0.001f, stepSeconds);
        if (this.pheromoneGrid != null) {
            this.pheromoneGrid.setSimulationStepSeconds(this.simulationStepSeconds);
        }
    }

    public void queueEvent(org.swarmforge.core.event.SimulationEvent event) {
        if (event != null) {
            if (event.getSimTimeSeconds() < 0) {
                event.setSimTimeSeconds(this.accumulatedSimulationSeconds);
            }
            eventQueue.offer(event);
            org.swarmforge.core.event.EventBus.getInstance().publishAsync(event);
        }
    }

    public float getSurfaceElevation(float x, float y) { return terrarium != null ? terrarium.getSurfaceElevation(x, y) : 0.0f; }
    public org.swarmforge.core.simulation.diseases.DiseaseManager getDiseaseManager() { return diseaseManager; }
}
