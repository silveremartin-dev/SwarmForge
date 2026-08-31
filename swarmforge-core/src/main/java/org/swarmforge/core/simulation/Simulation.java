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
import org.swarmforge.core.domain.FoodSource;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.event.SimulationEvent;
import java.util.List;
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
    private long tickDurationNanos;
    private int diffusionInterval = 5;
    private Thread simulationThread;
    private float speedMultiplier = 1.0f;
    private final SimulationHistory history;

    private final org.swarmforge.core.spatial.SpatialPartition<Individual> spatialIndex;
    private final CopyOnWriteArrayList<FoodSource> foodSources;
    private final org.swarmforge.core.spatial.SpatialPartition<FoodSource> foodIndex;
    private final org.swarmforge.core.world.WeatherSystem weather;
    private final org.swarmforge.core.spatial.AStarPathfinder pathfinder;
    private final org.swarmforge.core.world.SeasonManager seasonManager;
    private final TerritoryManager territoryManager;
    private final org.swarmforge.core.simulation.diseases.DiseaseManager diseaseManager;
    private final PredatorManager predatorManager;
    public static class NuptialFlightSystem { public NuptialFlightSystem(Simulation s) {} public void tick() {} }
    public static class DiapauseSystem { public DiapauseSystem(Simulation s) {} public void tick() {} }
    public static class SoilStructureSystem { public SoilStructureSystem(Simulation s) {} public void tick() {} }
    public static class PheromoneClimateSystem { public PheromoneClimateSystem(Simulation s) {} public void tick() {} }
    public static class SymbiosisSystem { public SymbiosisSystem(Simulation s) {} public void tick() {} }

    private final NuptialFlightSystem nuptialFlightSystem;
    private final DiapauseSystem diapauseSystem;
    private final SoilStructureSystem soilStructureSystem;
    private final PheromoneClimateSystem pheromoneClimateSystem;
    private final SymbiosisSystem symbiosisSystem;
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
        this.history = new SimulationHistory(1000, 1);
        this.predatorManager = new PredatorManager(this);
        this.territoryManager = new TerritoryManager(this);
        this.diseaseManager = new org.swarmforge.core.simulation.diseases.DiseaseManager(this);

        this.nuptialFlightSystem = new NuptialFlightSystem(this);
        this.diapauseSystem = new DiapauseSystem(this);
        this.soilStructureSystem = new SoilStructureSystem(this);
        this.pheromoneClimateSystem = new PheromoneClimateSystem(this);
        this.symbiosisSystem = new SymbiosisSystem(this);
        this.vegetationSystem = new org.swarmforge.core.world.VegetationSystem(
                terrarium != null ? terrarium.getWidth() : 100,
                terrarium != null ? terrarium.getDepth() : 100);

        this.dayNightCycle = new org.swarmforge.core.world.DayNightCycle();
        this.ecsWorldManager = new org.swarmforge.core.ecs.EcsWorldManager(this.pheromoneGrid);
    }

    public org.swarmforge.core.ecs.EcsWorldManager getEcsWorldManager() {
        return ecsWorldManager;
    }

    public void setClusterManager(org.swarmforge.core.compute.ComputeCluster clusterManager) {
        this.computeCluster = clusterManager;
    }

    private final java.util.Queue<SimulationEvent> eventQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();

    public void addColony(Colony colony) {
        if (colony != null) {
            colony.bootstrapDefaultResources();
        }
        colonies.add(colony);
        org.swarmforge.core.ecs.ColonyRegistry.register(colony);
        colony.addListener(new ColonyObserver());
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
        org.swarmforge.core.species.Species species;
        switch (speciesType) {
            case "FormicaRufa" -> species = new org.swarmforge.core.species.FormicaRufa();
            case "SolenopsisInvicta" -> species = new org.swarmforge.core.species.SolenopsisInvicta();
            case "AttaCephalotes" -> species = new org.swarmforge.core.species.AttaCephalotes();
            case "Camponotus" -> species = new org.swarmforge.core.species.Camponotus();
            default -> species = new org.swarmforge.core.species.LasiusNiger();
        }

        Colony colony = new Colony(species, x, y, 0); // Z=0 surface
        if (queens > 0) colony.createQueens(queens);
        if (workers > 0) colony.createWorkers(workers);
        if (soldiers > 0) colony.createSoldiers(soldiers);
        if (brood > 0) colony.createBrood(brood);
        colony.bootstrapDefaultResources();
        addColony(colony);
        return colony;
    }

    /**
     * Seeds initial surface vegetation and food clusters proportionally to map scale and population.
     */
    public void seedInitialEnvironmentResources() {
        if (terrarium == null) return;
        int width = (int) terrarium.getWidth();
        int depth = (int) terrarium.getDepth();
        int totalPopulation = colonies.stream().mapToInt(Colony::getPopulation).sum();
        int foodClusters = Math.max(8, totalPopulation / 20);

        if (vegetationSystem != null && vegetationSystem.getPlantCount() == 0) {
            vegetationSystem.populate(Math.max(30, width / 2), org.swarmforge.core.world.VegetationSystem.PlantType.GRASS);
            vegetationSystem.populate(Math.max(15, width / 4), org.swarmforge.core.world.VegetationSystem.PlantType.SHRUB);
            vegetationSystem.populate(Math.max(20, width / 3), org.swarmforge.core.world.VegetationSystem.PlantType.FLOWER);
            vegetationSystem.populate(Math.max(8, width / 8), org.swarmforge.core.world.VegetationSystem.PlantType.TREE);
        }

        if (foodSources.isEmpty()) {
            java.util.Random rng = new java.util.Random(12345);
            org.swarmforge.core.domain.ResourceType[] types = {
                org.swarmforge.core.domain.ResourceType.SUGAR,
                org.swarmforge.core.domain.ResourceType.SEED,
                org.swarmforge.core.domain.ResourceType.NECTAR,
                org.swarmforge.core.domain.ResourceType.INSECT
            };

            for (int i = 0; i < foodClusters; i++) {
                float fx = 5.0f + rng.nextFloat() * Math.max(1.0f, width - 10.0f);
                float fy = 5.0f + rng.nextFloat() * Math.max(1.0f, depth - 10.0f);
                org.swarmforge.core.domain.ResourceType rType = types[i % types.length];
                float qty = 300.0f + rng.nextFloat() * 700.0f;
                foodSources.add(new org.swarmforge.core.domain.FoodSource(fx, fy, 0.0f, qty, rType));
            }
        }
    }

    // Inner class to avoid leaking Simulation reference if not careful, though here
    // it's fine.
    private static String formatCaste(Individual.Caste caste) {
        if (caste == null) return "Individual";
        return switch (caste) {
            case QUEEN -> "Queen";
            case SOLDIER -> "Soldier";
            case MALE -> "Male";
            case NURSE -> "Nurse";
            case FORAGER -> "Forager";
            case WORKER -> "Worker";
        };
    }

    private class ColonyObserver implements org.swarmforge.core.domain.ColonyListener {
        @Override
        public void onBirth(Colony colony, Individual individual) {
            SimulationEvent.EventType type;
            String shortId = "#" + individual.getId().toString().substring(0, 8).toUpperCase();
            String colName = (colony.getSpeciesName() != null && !colony.getSpeciesName().isEmpty()) 
                ? colony.getSpeciesName() : "Colony #" + colony.getId().toString().substring(0, 5);
            String casteStr = formatCaste(individual.getCaste());
            int x = (int) individual.getX();
            int y = (int) individual.getY();
            int z = (int) individual.getZ();

            String message;
            if (individual.getLifeStage() == Individual.LifeStage.EGG) {
                type = SimulationEvent.EventType.WORKER_BORN;
                message = String.format("Egg Laying: Queen of %s laid an egg (%s %s) at (%d, %d, %d)",
                        colName, casteStr, shortId, x, y, z);
            } else {
                type = switch (individual.getCaste()) {
                    case QUEEN -> SimulationEvent.EventType.QUEEN_BORN;
                    case SOLDIER -> SimulationEvent.EventType.SOLDIER_BORN;
                    default -> SimulationEvent.EventType.WORKER_BORN;
                };
                message = String.format("Birth: %s %s in colony %s at (%d, %d, %d)",
                        casteStr, shortId, colName, x, y, z);
            }

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("colony", colName);
            data.put("colonyId", colony.getId().toString());
            data.put("individualId", shortId);
            data.put("caste", individual.getCaste().name());
            data.put("stage", individual.getLifeStage().name());
            data.put("job", individual.getJob().name());
            data.put("x", x);
            data.put("y", y);
            data.put("z", z);

            SimulationEvent event = SimulationEvent.obtain(type, SimulationEvent.Severity.INFO, tickCount.get(), message, data);
            eventQueue.offer(event);
            org.swarmforge.core.event.EventBus.getInstance().publish(event);
        }

        @Override
        public void onDeath(Colony colony, Individual individual) {
            SimulationEvent.EventType type = switch (individual.getCaste()) {
                case QUEEN -> SimulationEvent.EventType.QUEEN_DIED;
                case SOLDIER -> SimulationEvent.EventType.SOLDIER_DIED;
                default -> SimulationEvent.EventType.WORKER_DIED;
            };
            SimulationEvent.Severity severity = (individual.getCaste() == Individual.Caste.QUEEN)
                    ? SimulationEvent.Severity.CRITICAL
                    : SimulationEvent.Severity.WARNING;

            String shortId = "#" + individual.getId().toString().substring(0, 8).toUpperCase();
            String colName = (colony.getSpeciesName() != null && !colony.getSpeciesName().isEmpty()) 
                ? colony.getSpeciesName() : "Colony #" + colony.getId().toString().substring(0, 5);
            String casteStr = formatCaste(individual.getCaste());
            String cause = (individual.getCauseOfDeath() != null && !individual.getCauseOfDeath().isEmpty())
                ? individual.getCauseOfDeath() : "Unknown";
            long ageTicks = (long) individual.getAge();
            int x = (int) individual.getX();
            int y = (int) individual.getY();
            int z = (int) individual.getZ();

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("colony", colName);
            data.put("colonyId", colony.getId().toString());
            data.put("individualId", shortId);
            data.put("caste", individual.getCaste().name());
            data.put("ageTicks", ageTicks);
            data.put("cause", cause);
            data.put("x", x);
            data.put("y", y);
            data.put("z", z);

            String message = String.format("Death: %s %s (Cause: %s, Age: %d ticks) in colony %s at (%d, %d, %d)",
                    casteStr, shortId, cause, ageTicks, colName, x, y, z);
            SimulationEvent event = SimulationEvent.obtain(type, severity, tickCount.get(), message, data);
            eventQueue.offer(event);
            org.swarmforge.core.event.EventBus.getInstance().publish(event);
        }
    }

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

    public org.swarmforge.core.ecology.WaterGrid getWaterGrid() {
        return waterGrid;
    }

    public void stop() {
        if (state.compareAndSet(State.RUNNING, State.STOPPED) || state.compareAndSet(State.PAUSED, State.STOPPED)) {
            if (simulationThread != null) {
                simulationThread.interrupt();
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

    public void tick() {
        long currentTick = tickCount.incrementAndGet();
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
            colony.getIndividuals().parallelStream().forEach(individual -> {
                if (!individual.isAlive()) return;
                org.swarmforge.core.behavior.ReasoningArchitecture.Action action;
                boolean mustDecide = (currentTick - individual.getLastDecisionTick() >= individual.getDynamicDecisionInterval())
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

                    pheromoneGrid.deposit(
                            (int) individual.getX(),
                            (int) individual.getY(),
                            (int) individual.getZ(),
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
                    float maxX = terrarium.getWidth() - 1.0f;
                    float maxY = terrarium.getHeight() - 1.0f;
                    float maxZ = terrarium.getDepth() - 1.0f;
                    float cx = Math.max(0.0f, Math.min(maxX, individual.getX()));
                    float cy = Math.max(0.0f, Math.min(maxY, individual.getY()));
                    float cz = Math.max(0.0f, Math.min(maxZ, individual.getZ()));
                    if (cx != individual.getX() || cy != individual.getY() || cz != individual.getZ()) {
                        individual.setPosition(cx, cy, cz);
                        individual.setHeading(individual.getHeading() + (float) Math.PI);
                    }
                }

                // Inject master simulation deterministic PRNG
                individual.setRandom(this.random);

                // Update thermodynamic ambient temperature from weather context
                individual.setAmbientTemperatureC(context.getTemperature());

                // Process growth and lifecycle stage progression
                processGrowth(individual);

                // Update individual with construction context if applicable (calls tick())
                org.swarmforge.core.structure.ConstructionManager cm = constructionManagers.get(colony);
                if (cm != null) {
                    individual.update(cm, simulationStepSeconds);
                } else {
                    individual.tick(simulationStepSeconds);
                }

                // Collect living individuals for spatial index update
                if (individual.isAlive()) {
                    livingIndividuals.add(individual);

                    // Check environmental hazards (Floods, damage per second scaled by simulationStepSeconds)
                    float waterLevel = context.getWaterLevel(individual.getX(), individual.getY(), individual.getZ());
                    if (waterLevel > 0.5f) {
                        individual.setHealth(individual.getHealth() - 300.0f * simulationStepSeconds);
                        if (!individual.isAlive()) {
                            eventQueue.offer(new SimulationEvent(SimulationEvent.EventType.DEATH,
                                    tickCount.get(), "Drowned in flood"));
                        }
                    }
                }
            });
            colony.removeDeadIndividuals();
            colony.tick(); // Update internal state (Fungus Gardens, Consumption)
        }

        // Re-insert moved living individuals into spatial index
        spatialIndex.clear();
        for (Individual ind : livingIndividuals) {
            spatialIndex.insert(ind, ind.getX(), ind.getY(), ind.getZ());
        }

        processEvents();

        // Record history snapshot periodically
        history.recordIfNeeded(this);
    }

    private void processGrowth(Individual ind) {
        if (!ind.isAlive() || ind.getLifeStage() == Individual.LifeStage.ADULT)
            return;

        if (ind.getAge() > ind.getMaturationThreshold()) {
            var sp = ind.getSpecies();
            switch (ind.getLifeStage()) {
                case EGG -> {
                    ind.setLifeStage(Individual.LifeStage.LARVA);
                    float larvaDays = sp != null ? sp.getLarvaStageDuration() : 14f;
                    ind.setMaturationThreshold(ind.getAge() + larvaDays * 1440.0f);
                    SimulationEvent evt = SimulationEvent.obtain(SimulationEvent.EventType.WORKER_BORN, SimulationEvent.Severity.INFO, tickCount.get(), "Hatching: Egg hatched into Larva", null);
                    eventQueue.offer(evt);
                    org.swarmforge.core.event.EventBus.getInstance().publish(evt);
                }
                case LARVA -> {
                    if (ind.getEnergy() > 50) {
                        ind.setLifeStage(Individual.LifeStage.PUPA);
                        float pupaDays = sp != null ? sp.getPupaStageDuration() : 14f;
                        ind.setMaturationThreshold(ind.getAge() + pupaDays * 1440.0f);
                        SimulationEvent evt = SimulationEvent.obtain(SimulationEvent.EventType.WORKER_BORN, SimulationEvent.Severity.INFO, tickCount.get(), "Pupation: Larva pupated into Pupa", null);
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
        pheromoneGrid.tick();
        pathfinder.tick();

        // Process construction
        for (org.swarmforge.core.structure.ConstructionManager cm : constructionManagers.values()) {
            cm.tick();
        }

        predatorManager.tick(simulationStepSeconds);

        if (diseaseManager != null)
            diseaseManager.tick(simulationStepSeconds);

        // Process active multi-tick disasters
        if (!activeDisasters.isEmpty()) {
            activeDisasters.removeIf(disaster -> {
                disaster.tick(this, terrarium);
                return disaster.isFinished();
            });
        }

        // Water Simulation
        if (tickCount.get() % 10 == 0) { // Every 10 ticks is enough for fluid dynamics
            // Collect tunnel networks
            java.util.List<org.swarmforge.core.simulation.TunnelNetwork> tunnels = colonies.stream()
                    .map(Colony::getTunnelNetwork)
                    .toList();

            if (weather.isRaining()) {
                waterGrid.addRain(0.1f); // Arbitrary rain amount per 10 ticks
            }
            waterGrid.tick(tunnels);
        }

        // Distributed Pheromone Logic
        boolean pheromonesProcessed = false;

        if (this.computeCluster != null) {
            try {
                float[] pheromoneData = terrarium.exportPheromones();
                pheromonesProcessed = this.computeCluster.dispatchPheromoneTask(
                        terrarium.getWidth(), terrarium.getHeight(), terrarium.getDepth(),
                        pheromoneData);
                if (pheromonesProcessed) {
                    terrarium.importPheromones(pheromoneData);
                }
            } catch (Exception e) {
                System.err.println("Distributed Compute warning: " + e.getMessage());
            }
        }

        // Local Diffusion (fallback)
        if (!pheromonesProcessed && currentTick % diffusionInterval == 0) {
            pheromoneGrid.diffuse();
            // pheromoneGrid.evaporate(evaporationRate); // Removed: handled by tick/lazy
            // decay
        }

        if (territoryManager != null) {
            territoryManager.tick();
        }

        nuptialFlightSystem.tick();
        diapauseSystem.tick();
        soilStructureSystem.tick();
        pheromoneClimateSystem.tick();
        symbiosisSystem.tick();
        if (vegetationSystem != null) {
            vegetationSystem.tick(weather.getTemperature(), weather.getHumidity());
        }

        if (dayNightCycle != null) {
            dayNightCycle.tick();
        }
        if (seasonManager != null) {
            seasonManager.tick();
        }

        // Advance weather continuously every tick (1 tick = 1 second => 1/3600 hour)
        if (weather != null) {
            weather.advanceTime(1.0f / 3600.0f);
            if (dayNightCycle != null) {
                dayNightCycle.setPhase(weather.getTimeOfDay() / 24.0f);
            }

            org.swarmforge.core.world.WeatherMarkovChain.WeatherState currentWState = weather.getWeatherState();
            if (currentWState != null && currentWState != lastWeatherState) {
                boolean isSevere = (currentWState == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.THUNDERSTORM ||
                        currentWState == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.BLIZZARD ||
                        currentWState == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.TEMPEST ||
                        currentWState == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.SANDSTORM ||
                        currentWState == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.HAIL ||
                        currentWState == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.HEATWAVE ||
                        currentWState == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.DROUGHT);

                SimulationEvent.EventType evtType = isSevere
                        ? SimulationEvent.EventType.DISASTER_OCCURRED
                        : SimulationEvent.EventType.WEATHER_CHANGED;

                SimulationEvent.Severity severity = isSevere
                        ? SimulationEvent.Severity.WARNING
                        : SimulationEvent.Severity.INFO;

                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("weatherState", currentWState.label);
                data.put("temperature", Math.round(weather.getTemperature() * 10.0f) / 10.0f);
                data.put("humidity", Math.round(weather.getHumidity() * 10.0f) / 10.0f);
                data.put("windSpeed", Math.round(weather.getWindSpeed() * 10.0f) / 10.0f);
                data.put("windDirection", weather.getWindDirection());
                data.put("rainfall", Math.round(weather.getRainfall() * 10.0f) / 10.0f);
                data.put("snowfall", Math.round(weather.getSnowfall() * 10.0f) / 10.0f);
                data.put("pressure", Math.round(weather.getPressure() * 10.0f) / 10.0f);
                data.put("flightSuitability", Math.round(currentWState.flightSuitability * 100.0f) + "%");

                String msg = String.format("%s: %s (T: %.1f°C, Wind: %.1f km/h %s, Humidity: %.0f%%, Rain: %.1f mm/h, Pressure: %.1f hPa)",
                        (isSevere ? "🌋 Ecological Disaster" : "🌧️ Weather Shift"),
                        currentWState.label, weather.getTemperature(), weather.getWindSpeed(), weather.getWindDirection(),
                        weather.getHumidity(), weather.getRainfall(), weather.getPressure());

                SimulationEvent evt = SimulationEvent.obtain(evtType, severity, currentTick, msg, data);
                eventQueue.offer(evt);
                org.swarmforge.core.event.EventBus.getInstance().publish(evt);

                lastWeatherState = currentWState;
            }
        }
    }

    private void processEvents() {
        while (eventQueue.poll() != null) {
            // Events are consumed - clients can use pollEvents() to retrieve them
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

    // Getters
    public org.swarmforge.core.world.VegetationSystem getVegetationSystem() {
        return vegetationSystem;
    }

    public long getTickCount() {
        return tickCount.get();
    }

    public State getState() {
        return state.get();
    }

    public org.swarmforge.core.world.WeatherSystem getWeather() {
        return weather;
    }

    public org.swarmforge.core.world.SeasonManager getSeasonManager() {
        return seasonManager;
    }

    public Terrarium getTerrarium() {
        return terrarium;
    }

    // public SparsePheromoneGrid getPheromoneGrid() { return pheromoneGrid; } //
    // Already defined above

    public org.swarmforge.core.spatial.SpatialPartition<Individual> getSpatialIndex() {
        return spatialIndex;
    }

    public List<Colony> getColonies() {
        return List.copyOf(colonies);
    }

    public Colony getColony(java.util.UUID id) {
        for (Colony c : colonies) {
            if (c.getId().equals(id))
                return c;
        }
        return null; // Should not happen for valid IDs
    }

    public boolean isRunning() {
        return state.get() == State.RUNNING;
    }

    public int getTicksPerSecond() {
        return ticksPerSecond;
    }

    // Setters
    public void setTicksPerSecond(int tps) {
        this.ticksPerSecond = tps;
        this.tickDurationNanos = 1_000_000_000L / tps;
    }

    public void setDiffusionInterval(int interval) {
        this.diffusionInterval = Math.max(1, interval);
    }

    // Food management
    public void spawnFood(float x, float y, float z, float quantity, org.swarmforge.core.domain.ResourceType type) {
        foodSources.add(new FoodSource(x, y, z, quantity, type));
    }

    public void addFoodSource(FoodSource foodSource) {
        foodSources.add(foodSource);
    }

    public org.swarmforge.core.spatial.SpatialPartition<FoodSource> getFoodIndex() {
        return foodIndex;
    }

    public List<FoodSource> getFoodSources() {
        return List.copyOf(foodSources);
    }

    public org.swarmforge.core.spatial.AStarPathfinder getPathfinder() {
        return pathfinder;
    }

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

    public PredatorManager getPredatorManager() {
        return predatorManager;
    }

    public TerritoryManager getTerritoryManager() {
        return territoryManager;
    }

    public void setMasterSeed(long seed) {
        this.masterSeed = seed;
        this.random = new java.util.Random(seed);
    }

    public long getMasterSeed() {
        return masterSeed;
    }

    public java.util.Random getRandom() {
        return random;
    }

    /**
     * Reset the simulation state.
     */
    public void reset(long tick) {
        this.tickCount.set(tick);
        this.colonies.clear();
        this.foodSources.clear();
        this.spatialIndex.clear();
        this.foodIndex.clear();
        this.random = new java.util.Random(this.masterSeed);
        if (this.pheromoneGrid != null) {
            this.pheromoneGrid.clear();
        }
        if (this.waterGrid != null) {
            this.waterGrid.clear();
        }
        if (this.history != null) {
            this.history.clear();
        }
        if (this.predatorManager != null) {
            this.predatorManager.clearPredators();
        }
        if (this.activeDisasters != null) {
            this.activeDisasters.clear();
        }
        this.interventionJournal.clear();
        this.checkpoints.clear();
        this.eventQueue.clear();
    }

    /**
     * Record initial snapshot right after setup.
     */
    public void recordInitialSnapshot() {
        if (this.history != null) {
            this.history.record(SimulationSnapshot.capture(this));
        }
    }

    /**
     * Get highest tick recorded in history or current tick.
     */
    public long getHighestRecordedTick() {
        if (this.history != null) {
            long hTick = this.history.getHighestRecordedTick();
            return Math.max(this.tickCount.get(), hTick);
        }
        return this.tickCount.get();
    }

    // === Speed Control ===

    /**
     * Set the simulation speed multiplier.
     * 
     * @param multiplier Speed multiplier (0.1 to 10.0). 1.0 = normal, 2.0 = double
     *                   speed
     */
    public void setSpeedMultiplier(float multiplier) {
        this.speedMultiplier = Math.max(0.1f, Math.min(10.0f, multiplier));
        this.tickDurationNanos = (long) (1_000_000_000L / (ticksPerSecond * speedMultiplier));
    }

    /**
     * Get the current speed multiplier.
     */
    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    /**
     * Speed up simulation (increases multiplier by 0.5).
     */
    public void speedUp() {
        setSpeedMultiplier(speedMultiplier + 0.5f);
    }

    /**
     * Slow down simulation (decreases multiplier by 0.5).
     */
    public void slowDown() {
        setSpeedMultiplier(speedMultiplier - 0.5f);
    }

    /**
     * Reset to normal speed.
     */
    public void normalSpeed() {
        setSpeedMultiplier(1.0f);
    }

    // === Rewind/History Control ===

    /**
     * Get the simulation history buffer.
     */
    public SimulationHistory getHistory() {
        return history;
    }

    /**
     * Rewind simulation by N steps.
     * 
     * @param steps Number of snapshot steps to rewind
     * @return true if rewind was successful
     */
    public boolean rewind(int steps) {
        State oldState = state.get();
        if (oldState == State.RUNNING) {
            pause();
        }

        boolean success = history.rewind(this, steps);

        if (success) {
            eventQueue.offer(new SimulationEvent(org.swarmforge.core.event.SimulationEvent.EventType.MILESTONE_REACHED,
                    tickCount.get(), "Rewound " + steps + " steps to tick " + tickCount.get()));
        }

        return success;
    }

    public java.util.Queue<org.swarmforge.core.event.SimulationEvent> getEventQueue() {
        return eventQueue;
    }

    /**
     * Seek to a specific tick in history.
     * 
     * @param tick Target tick
     * @return true if seek was successful
     */
    public boolean seekToTick(long tick) {
        State oldState = state.get();
        if (oldState == State.RUNNING) {
            pause();
        }

        boolean success = history.seekToTick(this, tick);

        if (success) {
            eventQueue.offer(new SimulationEvent(org.swarmforge.core.event.SimulationEvent.EventType.MILESTONE_REACHED,
                    tickCount.get(), "Seeked to tick " + tick));
        }

        return success;
    }

    private final java.util.List<org.swarmforge.core.event.GodModeIntervention> interventionJournal = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
    private final java.util.List<SimulationCheckpoint> checkpoints = java.util.Collections.synchronizedList(new java.util.ArrayList<>());

    public void logIntervention(org.swarmforge.core.event.GodModeIntervention intervention) {
        if (intervention != null) {
            interventionJournal.add(intervention);
            org.swarmforge.core.event.EventBus.getInstance().publish(
                org.swarmforge.core.event.SimulationEvent.obtain(
                    org.swarmforge.core.event.SimulationEvent.EventType.GOD_MODE_INTERVENTION,
                    org.swarmforge.core.event.SimulationEvent.Severity.INFO,
                    getTickCount(),
                    "⚡ Intervention Mode Divin (" + intervention.actionType() + ") enregistrée au tick #" + getTickCount(),
                    null
                )
            );
        }
    }

    public java.util.List<org.swarmforge.core.event.GodModeIntervention> getInterventionJournal() {
        return new java.util.ArrayList<>(interventionJournal);
    }

    public SimulationCheckpoint createCheckpoint(String name) {
        SimulationSnapshot snap = SimulationSnapshot.capture(this);
        SimulationCheckpoint cp = new SimulationCheckpoint(name, getTickCount(), snap, interventionJournal);
        checkpoints.add(cp);
        return cp;
    }

    public boolean restoreCheckpoint(SimulationCheckpoint cp) {
        if (cp == null || cp.getSnapshot() == null) return false;
        cp.getSnapshot().restore(this);
        this.interventionJournal.clear();
        this.interventionJournal.addAll(cp.getInterventionsRecorded());
        return true;
    }

    public java.util.List<SimulationCheckpoint> getCheckpoints() {
        return new java.util.ArrayList<>(checkpoints);
    }

    /**
     * Record a snapshot to history.
     */
    public void recordSnapshot() {
        history.record(SimulationSnapshot.capture(this));
    }

    public float getSimulationStepSeconds() {
        return simulationStepSeconds;
    }

    public void setSimulationStepSeconds(float stepSeconds) {
        this.simulationStepSeconds = Math.max(0.001f, stepSeconds);
        if (this.pheromoneGrid != null) {
            this.pheromoneGrid.setSimulationStepSeconds(this.simulationStepSeconds);
        }
    }

    /**
     * Queue an event for processing.
     * 
     * @param event The event to queue
     */
    public void queueEvent(org.swarmforge.core.event.SimulationEvent event) {
        eventQueue.offer(event);
    }
}
