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
    private final java.util.Map<Long, org.swarmforge.core.navigation.FlowFieldGrid> flowFieldCache = new java.util.concurrent.ConcurrentHashMap<>();
    private final org.swarmforge.core.ecs.EcsWorldManager ecsWorldManager;

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
        org.swarmforge.core.species.Species species = org.swarmforge.core.species.SpeciesRegistry.getInstance().getSpecies(speciesType);

        float surfaceZ = terrarium != null ? terrarium.getSurfaceElevation(x, y) : 0f;
        Colony colony = new Colony(species, x, y, surfaceZ);
        if (this.terrarium != null) {
            colony.setTerrarium(this.terrarium);
        }
        if (this.random != null) {
            colony.setRandom(new java.util.Random(this.random.nextLong()));
        }
        if (queens > 0) colony.createQueens(queens);
        if (workers > 0) colony.createWorkers(workers);
        if (soldiers > 0) colony.createSoldiers(soldiers);
        if (brood > 0) colony.createBrood(brood);
        colony.bootstrapDefaultResources();
        if (colony.getTunnelNetwork() != null) {
            colony.getTunnelNetwork().rebuildForArchitecture(x, y, surfaceZ, species != null ? species.getNestType() : "BURROW_UNDERGROUND", colony);
        }
        addColony(colony);
        return colony;
    }

    /**
     * Seeds initial surface vegetation and food clusters proportionally to map scale and population.
     */
    public void seedInitialEnvironmentResources() {
        if (terrarium == null) return;
        int width = (int) terrarium.getWidth();
        int height = (int) terrarium.getHeight();
        int totalPopulation = colonies.stream().mapToInt(Colony::getPopulation).sum();
        int foodClusters = Math.max(8, totalPopulation / 20);

        if (vegetationSystem != null && vegetationSystem.getPlantCount() == 0) {
            vegetationSystem.populate(Math.max(30, width / 2), org.swarmforge.core.world.VegetationSystem.PlantType.GRASS);
            vegetationSystem.populate(Math.max(15, width / 4), org.swarmforge.core.world.VegetationSystem.PlantType.SHRUB);
            vegetationSystem.populate(Math.max(20, width / 3), org.swarmforge.core.world.VegetationSystem.PlantType.FLOWER);
            vegetationSystem.populate(Math.max(8, width / 8), org.swarmforge.core.world.VegetationSystem.PlantType.TREE);
        }

        if (foodSources.isEmpty()) {
            java.util.Random rng = (this.random != null) ? this.random : new java.util.Random(12345);
            org.swarmforge.core.domain.ResourceType[] types = {
                org.swarmforge.core.domain.ResourceType.SUGAR,
                org.swarmforge.core.domain.ResourceType.SEED,
                org.swarmforge.core.domain.ResourceType.NECTAR,
                org.swarmforge.core.domain.ResourceType.INSECT
            };

            for (int i = 0; i < foodClusters; i++) {
                float fx = 5.0f + rng.nextFloat() * Math.max(1.0f, width - 10.0f);
                float fy = 5.0f + rng.nextFloat() * Math.max(1.0f, height - 10.0f);
                org.swarmforge.core.domain.ResourceType rType = types[i % types.length];
                float qty = 300.0f + rng.nextFloat() * 700.0f;
                foodSources.add(new org.swarmforge.core.domain.FoodSource(fx, fy, 0.0f, qty, rType));
            }
        }

        if (predatorManager != null && predatorManager.getPredatorCount() == 0) {
            // Seed initial accessory prey / predator species in the environment
            predatorManager.spawnPredator(org.swarmforge.core.domain.PredatorType.CATERPILLAR, width * 0.35f, height * 0.35f, 0.0f);
            predatorManager.spawnPredator(org.swarmforge.core.domain.PredatorType.BEETLE, width * 0.65f, height * 0.40f, 0.0f);
            predatorManager.spawnPredator(org.swarmforge.core.domain.PredatorType.SPIDER, width * 0.50f, height * 0.70f, 0.0f);
            predatorManager.spawnPredator(org.swarmforge.core.domain.PredatorType.LADYBUG_LARVA, width * 0.25f, height * 0.60f, 0.0f);
        }
    }

    // Inner class to avoid leaking Simulation reference if not careful, though here
    // it's fine.
    public void start() {
        seedInitialEnvironmentResources();
        if (state.compareAndSet(State.STOPPED, State.RUNNING) ||
                state.compareAndSet(State.PAUSED, State.RUNNING)) {
            org.swarmforge.core.event.EventBus.getInstance().publish(
                SimulationEvent.obtain(
                    SimulationEvent.EventType.SIMULATION_STARTED,
                    SimulationEvent.Severity.INFO,
                    getTickCount(),
                    "▶️ Simulation started",
                    null
                )
            );
            simulationThread = Thread.ofVirtual().name("simulation-loop").start(this::runLoop);
        }
    }

    public void pause() {
        if (state.compareAndSet(State.RUNNING, State.PAUSED)) {
            org.swarmforge.core.event.EventBus.getInstance().publish(
                SimulationEvent.obtain(
                    SimulationEvent.EventType.SIMULATION_PAUSED,
                    SimulationEvent.Severity.INFO,
                    getTickCount(),
                    "⏸️ Simulation paused",
                    null
                )
            );
        }
    }

    public org.swarmforge.core.gpu.SparsePheromoneGrid getPheromoneGrid() {
        return pheromoneGrid;
    }

    public org.swarmforge.core.world.WeatherSystem getWeather() {
        return weather;
    }

    public org.swarmforge.core.world.SoilHydricCoupling getSoilHydricCoupling() {
        return soilHydricCoupling;
    }

    public org.swarmforge.core.world.WaterTable getWaterTable() {
        return waterTable;
    }

    public org.swarmforge.core.ecology.WaterGrid getWaterGrid() {
        return waterGrid;
    }

    public SoilStructureSystem getSoilStructureSystem() {
        return soilStructureSystem;
    }

    public PheromoneClimateSystem getPheromoneClimateSystem() {
        return pheromoneClimateSystem;
    }

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
        ecsWorldManager.step(this.simulationStepSeconds);

        // Pre-populate spatial index for individuals before brain decision processing
        spatialIndex.clear();
        for (Colony colony : colonies) {
            for (Individual ind : colony.getIndividuals()) {
                if (ind.isAlive()) {
                    spatialIndex.insert(ind, ind.getX(), ind.getY(), ind.getZ());
                }
            }
        }

        // Rebuild food index
        foodIndex.clear();
        foodSources.removeIf(food -> {
            boolean depleted = food.isDepleted();
            if (depleted) {
                String resName = (food.getType() != null) ? food.getType().name() : "Nourriture";
                int xPos = (int) food.getX();
                int yPos = (int) food.getY();
                int zPos = (int) food.getZ();

                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("resource", resName);
                data.put("x", xPos);
                data.put("y", yPos);
                data.put("z", zPos);

                String msg = String.format("🥀 Food Depleted: %s source fully consumed at (%d, %d, %d)", resName, xPos, yPos, zPos);
                SimulationEvent evt = SimulationEvent.obtain(SimulationEvent.EventType.FOOD_DEPLETED, SimulationEvent.Severity.INFO, currentTick, msg, data);
                evt.setSimTimeSeconds(this.accumulatedSimulationSeconds);
                eventQueue.offer(evt);
                org.swarmforge.core.event.EventBus.getInstance().publish(evt);
            }
            return depleted;
        });
        for (FoodSource food : foodSources) {
            food.tick();
            foodIndex.insert(food, food.getX(), food.getY(), food.getZ());
        }

        if (currentTick % 60 == 0) {
            for (Colony colony : colonies) {
                colony.getStatistics().record(
                        currentTick,
                        colony.getPopulation(),
                        colony.getFoodStored(),
                        colony.getWaterStored(),
                        colony.getTotalDied(),
                        colony.getTotalBorn());
            }
        }

        // Use parallel streams with virtual thread executor for better scalability
        SimulationContext context = new SimulationContextImpl(this); // Create context once per tick
        java.util.concurrent.ConcurrentLinkedQueue<Individual> livingIndividuals = new java.util.concurrent.ConcurrentLinkedQueue<>();

        for (Colony colony : colonies) {
            colony.getIndividuals().forEach(individual -> {
                if (!individual.isAlive()) return;

                // Update thermodynamic ambient temperature and humidity from weather context (with subterranean microclimate buffering)
                float ambTemp = context.getTemperature();
                float ambHum = context.getRelativeHumidity(individual.getX(), individual.getY(), individual.getZ());
                float surfaceZ = terrarium != null ? terrarium.getSurfaceElevation(individual.getX(), individual.getY()) : 16.0f;
                if (individual.getZ() < surfaceZ) {
                    float optT = individual.getSpecies() != null ? individual.getSpecies().getOptimalTempCelsius() : 20.0f;
                    float optH = individual.getSpecies() != null ? individual.getSpecies().getOptimalHumidityPercent() : 85.0f;
                    float depthFactor = Math.min(1.0f, (surfaceZ - individual.getZ()) / 4.0f);
                    ambTemp = ambTemp * (1.0f - depthFactor) + optT * depthFactor;
                    ambHum = ambHum * (1.0f - depthFactor) + optH * depthFactor;
                }
                individual.setAmbientTemperatureC(ambTemp);
                individual.setAmbientHumidityPercent(ambHum);

                // Non-adult individuals (eggs, larvae, pupae) only undergo biological maturation and subterranean care
                if (individual.getLifeStage() != Individual.LifeStage.ADULT) {
                    processGrowth(individual);
                    individual.tick(simulationStepSeconds);

                    // Ensure brood remains strictly subterranean inside the nest
                    if (individual.getZ() >= surfaceZ) {
                        float chamberDepth = Math.max(1.0f, colony.getDynamicQueenChamberDepth() * 0.5f);
                        individual.setZ(Math.max(1.0f, surfaceZ - chamberDepth));
                    }
                    if (individual.isAlive()) {
                        livingIndividuals.add(individual);
                    }
                    return;
                }

                if (individual.getBrain() == null) {
                    individual.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
                }
                org.swarmforge.core.behavior.ReasoningArchitecture.Action action;
                boolean mustDecide = (currentTick < individual.getLastDecisionTick()
                        || currentTick - individual.getLastDecisionTick() >= individual.getDynamicDecisionInterval())
                        || individual.getCachedAction() == null;

                if (mustDecide && individual.getBrain() != null) {
                    action = individual.getBrain().decide(individual, context);
                    individual.setCachedAction(action);
                    individual.setLastDecisionTick(currentTick);
                } else {
                    action = individual.getCachedAction();
                    if (action == null) {
                        action = org.swarmforge.core.behavior.ReasoningArchitecture.Action.rest();
                    }
                }

                // 2. Action Execution
                org.swarmforge.core.behavior.ReasoningArchitecture.ActionResult result = individual
                        .executeAction(action, colony);

                // 3. Learning/Update
                if (mustDecide && individual.getBrain() != null) {
                    individual.getBrain().update(individual, action, result);
                }

                // 4. Pheromone Deposit (Dual-Channel Algorithm)
                if (action.type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.MOVE ||
                        action.type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.RETURN_HOME
                        ||
                        action.type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.FOLLOW_TRAIL) {

                    org.swarmforge.core.domain.PheromoneType typeToDeposit;
                    if (individual.isCarryingFood()) {
                        typeToDeposit = org.swarmforge.core.domain.PheromoneType.FOOD_TRAIL;
                    } else {
                        typeToDeposit = org.swarmforge.core.domain.PheromoneType.HOME_TRAIL;
                    }

                    int maxDepth = terrarium != null ? terrarium.getDepth() - 1 : 63;
                    int depZ = Math.max(0, Math.min(maxDepth, (int) individual.getZ()));
                    pheromoneGrid.deposit(
                            (int) individual.getX(),
                            (int) individual.getY(),
                            depZ,
                            typeToDeposit.getIndex(),
                            1.0f
                    );
                } else if (action
                        .type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.ATTACK) {
                    Object target = action.target();
                    if (target instanceof Individual) {
                        Individual targetInd = (Individual) target;
                        targetInd.takeDamage(individual.getAttackDamage(), individual);
                    } else if (target instanceof org.swarmforge.core.domain.Predator) {
                        org.swarmforge.core.domain.Predator targetPred = (org.swarmforge.core.domain.Predator) target;
                        targetPred.takeDamage(individual.getAttackDamage());
                        if (!targetPred.isAlive()) {
                            predatorManager.removePredator(targetPred);
                        }
                    }
                }

                // Coordinate boundary clamping to prevent out-of-bounds positioning
                if (terrarium != null) {
                    float maxX = Math.max(1.0f, terrarium.getWidth() - 1.0f);
                    float maxY = Math.max(1.0f, terrarium.getHeight() - 1.0f);
                    float maxDepth = Math.max(1.0f, (float) terrarium.getDepth() - 1.0f);
                    float minZ = 0.0f;
                    float maxZ = maxDepth;
                    float cx = Math.max(0.0f, Math.min(maxX, individual.getX()));
                    float cy = Math.max(0.0f, Math.min(maxY, individual.getY()));
                    float cz = Math.max(minZ, Math.min(maxZ, individual.getZ()));
                    if (cx != individual.getX() || cy != individual.getY() || cz != individual.getZ()) {
                        individual.setPosition(cx, cy, cz);
                        individual.setHeading(individual.getHeading() + (float) Math.PI);
                    }
                }

                // Inject master simulation deterministic PRNG
                individual.setRandom(this.random);

                // Update individual with construction context if applicable (calls tick())
                org.swarmforge.core.structure.ConstructionManager cm = constructionManagers.get(colony);
                if (cm != null) {
                    individual.update(cm, simulationStepSeconds);
                } else {
                    individual.tick(simulationStepSeconds);
                }

                // Collect living individuals for spatial index update and enforce spatial containment
                if (individual.isAlive()) {
                    if (terrarium != null) {
                        float maxX = Math.max(1.0f, terrarium.getWidth() - 1.0f);
                        float maxY = Math.max(1.0f, terrarium.getHeight() - 1.0f);
                        float maxDepth = Math.max(1.0f, (float) terrarium.getDepth() - 1.0f);
                        float minZ = 0.0f;
                        float maxZ = maxDepth;
                        float cx = Math.max(0.0f, Math.min(maxX, individual.getX()));
                        float cy = Math.max(0.0f, Math.min(maxY, individual.getY()));
                        float cz = Math.max(minZ, Math.min(maxZ, individual.getZ()));
                        if (cx != individual.getX() || cy != individual.getY() || cz != individual.getZ()) {
                            individual.setPosition(cx, cy, cz);
                        }
                    }

                    // Ground-snapping for non-flying terrestrial ants (eliminate floating at Z=10)
                    if (!individual.canFly() && !individual.isClimbingTree() && terrarium != null) {
                        if (individual.getZ() >= surfaceZ - 0.2f) {
                            individual.setZ(surfaceZ);
                        }
                    }

                    livingIndividuals.add(individual);

                    // Check environmental hazards (Floods & River water cells, lethal danger with drowning damage)
                    float waterLevel = context.getWaterLevel(individual.getX(), individual.getY(), individual.getZ());
                    boolean isWaterVoxel = false;
                    if (terrarium != null) {
                        int ix = Math.max(0, Math.min(terrarium.getWidth() - 1, Math.round(individual.getX())));
                        int iy = Math.max(0, Math.min(terrarium.getHeight() - 1, Math.round(individual.getY())));
                        int iz = Math.max(0, Math.min(terrarium.getDepth() - 1, Math.round(individual.getZ())));
                        TerrariumCell cell = terrarium.getCell(ix, iy, iz);
                        if (cell != null && cell.material() == TerrariumCell.Material.WATER) {
                            isWaterVoxel = true;
                        }
                    }

                    if ((waterLevel > 0.3f || isWaterVoxel) && !individual.canFly()) {
                        // Water avoidance steering
                        individual.setHeading(individual.getHeading() + (float) Math.PI * 0.75f);

                        // Drowning mortality
                        float drownDamage = 50.0f * simulationStepSeconds;
                        if (individual.getHealth() <= drownDamage) {
                            individual.die("Noyade / Cours d'eau (Drowning)");
                        }
                        individual.setHealth(Math.max(0.0f, individual.getHealth() - drownDamage));
                        if (!individual.isAlive()) {
                            eventQueue.offer(new SimulationEvent(SimulationEvent.EventType.DEATH,
                                    tickCount.get(), "Noyade dans le cours d'eau"));
                        }
                    }
                }
            });
            colony.removeDeadIndividuals();
            colony.tick(); // Update internal state (Fungus Gardens, Consumption)
            colony.tickPhysicalEngines(
                    weather != null ? weather.getTemperature() : 20.0f,
                    org.swarmforge.core.domain.TerrariumCell.DEFAULT_CO2,
                    weather != null ? weather.getWindSpeedMs() : 1.0f,
                    soilHydricCoupling != null ? (soilHydricCoupling.getMoistureAtDepth(0) / 100.0f) : 0.2f,
                    simulationStepSeconds,
                    this.random
            );
        }

        // Re-insert moved living individuals into spatial index
        spatialIndex.clear();
        Map<UUID, Individual> indMap = new HashMap<>();
        for (Individual ind : livingIndividuals) {
            spatialIndex.insert(ind, ind.getX(), ind.getY(), ind.getZ());
            indMap.put(ind.getId(), ind);
        }

        Map<UUID, Colony> colMap = new HashMap<>();
        for (Colony col : colonies) {
            colMap.put(col.getId(), col);
        }
        macroForagingSystem.update(simulationStepSeconds, indMap, colMap);

        processEvents();

        // Auto periodic checkpoint creation every autoCheckpointInterval ticks (default 50,000) for live checkpoint manager
        if (currentTick > 0 && currentTick % timeMachine.getAutoCheckpointInterval() == 0) {
            timeMachine.createCheckpoint("Auto-Snapshot (Tick #" + currentTick + ")");
        }

        // Record history snapshot periodically
        timeMachine.getHistory().recordIfNeeded(this);
    }

    private void processGrowth(Individual ind) {
        if (!ind.isAlive() || ind.getLifeStage() == Individual.LifeStage.ADULT)
            return;

        if (ind.getAgeInSeconds() >= ind.getMaturationThreshold()) {
            var sp = ind.getSpecies();
            switch (ind.getLifeStage()) {
                case EGG -> {
                    ind.setLifeStage(Individual.LifeStage.LARVA);
                    float larvaDur = sp != null ? sp.getLarvaStageDuration() : 600f;
                    if (larvaDur <= 30f) larvaDur *= 60.0f;
                    ind.setMaturationThreshold(ind.getAgeInSeconds() + larvaDur);
                    SimulationEvent evt = SimulationEvent.obtain(SimulationEvent.EventType.WORKER_BORN, SimulationEvent.Severity.INFO, tickCount.get(), "Hatching: Egg hatched into Larva", null);
                    evt.setSimTimeSeconds(this.accumulatedSimulationSeconds);
                    eventQueue.offer(evt);
                    org.swarmforge.core.event.EventBus.getInstance().publish(evt);
                }
                case LARVA -> {
                    if (ind.getEnergy() > 50) {
                        ind.setLifeStage(Individual.LifeStage.PUPA);
                        float pupaDur = sp != null ? sp.getPupaStageDuration() : 500f;
                        if (pupaDur <= 30f) pupaDur *= 60.0f;
                        ind.setMaturationThreshold(ind.getAgeInSeconds() + pupaDur);
                        SimulationEvent evt = SimulationEvent.obtain(SimulationEvent.EventType.WORKER_BORN, SimulationEvent.Severity.INFO, tickCount.get(), "Pupation: Larva pupated into Pupa", null);
                        evt.setSimTimeSeconds(this.accumulatedSimulationSeconds);
                        eventQueue.offer(evt);
                        org.swarmforge.core.event.EventBus.getInstance().publish(evt);
                    }
                }
                case PUPA -> {
                    ind.setLifeStage(Individual.LifeStage.ADULT);
                    if (ind.getCaste() == Individual.Caste.WORKER) {
                        float r = ind.getRandom().nextFloat();
                        if (r < 0.2f) {
                            ind.setJob(Individual.Job.NURSE);
                        } else if (r < 0.5f) {
                            ind.setJob(Individual.Job.GUARD);
                        } else {
                            ind.setJob(Individual.Job.FORAGER);
                        }
                    } else if (ind.getCaste() == Individual.Caste.SOLDIER) {
                        ind.setJob(Individual.Job.GUARD);
                    }
                    SimulationEvent evt = SimulationEvent.obtain(
                        ind.getCaste() == Individual.Caste.QUEEN ? SimulationEvent.EventType.QUEEN_BORN :
                        (ind.getCaste() == Individual.Caste.SOLDIER ? SimulationEvent.EventType.SOLDIER_BORN : SimulationEvent.EventType.WORKER_BORN),
                        SimulationEvent.Severity.INFO, tickCount.get(), "Emergence: New Adult (" + ind.getCaste() + ") emerged", null);
                    evt.setSimTimeSeconds(this.accumulatedSimulationSeconds);
                    eventQueue.offer(evt);
                    org.swarmforge.core.event.EventBus.getInstance().publish(evt);
                }
                case ADULT -> {
                    // unexpected
                }
            }
        }
    }

    private void updateEnvironment(long currentTick) {
        environmentUpdater.updateEnvironment(currentTick);
    }
    private void processEvents() {
        while (eventQueue.size() > MAX_QUEUED_EVENTS) {
            eventQueue.poll();
        }
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
        if (terrarium == null) return new float[]{0f, 0f, 0f};
        long key = (((long) targetX & 0xFFFFF) << 40) | (((long) targetY & 0xFFFFF) << 20) | ((long) targetZ & 0xFFFFF);
        org.swarmforge.core.navigation.FlowFieldGrid flowGrid = flowFieldCache.computeIfAbsent(key, k -> {
            org.swarmforge.core.navigation.FlowFieldGrid fg = new org.swarmforge.core.navigation.FlowFieldGrid(
                    terrarium.getWidth(), terrarium.getHeight(), terrarium.getDepth());
            fg.recompute(targetX, targetY, targetZ);
            return fg;
        });
        return flowGrid.getVector(x, y, z);
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
