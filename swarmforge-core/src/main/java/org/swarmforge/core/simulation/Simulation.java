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
    private final NuptialFlightSystem nuptialFlightSystem;
    private final DiapauseSystem diapauseSystem;
    private final SoilStructureSystem soilStructureSystem;
    private final PheromoneClimateSystem pheromoneClimateSystem;
    private final SymbiosisSystem symbiosisSystem;
    private final org.swarmforge.core.world.VegetationSystem vegetationSystem;
    private final java.util.Map<org.swarmforge.core.domain.Colony, org.swarmforge.core.structure.ConstructionManager> constructionManagers = new java.util.concurrent.ConcurrentHashMap<>();

    // Cluster
    private org.swarmforge.core.compute.ComputeCluster computeCluster;

    // Environment
    private final org.swarmforge.core.world.DayNightCycle dayNightCycle;

    public org.swarmforge.core.world.DayNightCycle getDayNightCycle() {
        return dayNightCycle;
    }

    public Simulation(Terrarium terrarium) {
        this.terrarium = terrarium;
        this.pheromoneGrid = new SparsePheromoneGrid(
                terrarium.getWidth(), terrarium.getHeight(), terrarium.getDepth());
        this.pheromoneGrid.setTerrarium(terrarium);

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
        this.history = new SimulationHistory(1000, 60);
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
    }

    public void setClusterManager(org.swarmforge.core.compute.ComputeCluster clusterManager) {
        this.computeCluster = clusterManager;
    }

    private final java.util.Queue<SimulationEvent> eventQueue = new java.util.concurrent.ConcurrentLinkedQueue<>();

    public void addColony(Colony colony) {
        colonies.add(colony);
        org.swarmforge.core.ecs.ColonyRegistry.register(colony);
        colony.addListener(new ColonyObserver());
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
        org.swarmforge.core.species.Species species;
        switch (speciesType) {
            case "FormicaRufa" -> species = new org.swarmforge.core.species.FormicaRufa();
            case "SolenopsisInvicta" -> species = new org.swarmforge.core.species.SolenopsisInvicta();
            case "AttaCephalotes" -> species = new org.swarmforge.core.species.AttaCephalotes();
            case "Camponotus" -> species = new org.swarmforge.core.species.Camponotus();
            default -> species = new org.swarmforge.core.species.LasiusNiger();
        }

        Colony colony = new Colony(species, x, y, 0); // Z=0 surface
        for (int i = 0; i < queens; i++) {
            colony.createQueen();
        }
        for (int i = 0; i < workers; i++) {
            colony.createWorker();
        }
        for (int i = 0; i < soldiers; i++) {
            colony.createSoldier();
        }
        addColony(colony);
        return colony;
    }

    // Inner class to avoid leaking Simulation reference if not careful, though here
    // it's fine.
    private class ColonyObserver implements org.swarmforge.core.domain.ColonyListener {
        @Override
        public void onBirth(Colony colony, Individual individual) {
            SimulationEvent.EventType type;
            String message;
            if (individual.getLifeStage() == Individual.LifeStage.EGG) {
                type = SimulationEvent.EventType.WORKER_BORN;
                message = "Ponte : La reine de " + colony.getSpeciesName() + " a pondu un œuf (" + individual.getCaste() + ")";
            } else {
                type = switch (individual.getCaste()) {
                    case QUEEN -> SimulationEvent.EventType.QUEEN_BORN;
                    case SOLDIER -> SimulationEvent.EventType.SOLDIER_BORN;
                    default -> SimulationEvent.EventType.WORKER_BORN;
                };
                message = "Naissance : " + individual.getCaste() + " dans la colonie " + colony.getSpeciesName();
            }

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("colony", colony.getSpeciesName());
            data.put("caste", individual.getCaste().name());
            data.put("stage", individual.getLifeStage().name());

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

            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("colony", colony.getSpeciesName());
            data.put("caste", individual.getCaste().name());

            String message = "Décès : " + individual.getCaste() + " de la colonie " + colony.getSpeciesName();
            SimulationEvent event = SimulationEvent.obtain(type, severity, tickCount.get(), message, data);
            eventQueue.offer(event);
            org.swarmforge.core.event.EventBus.getInstance().publish(event);
        }
    }

    public void start() {
        if (state.compareAndSet(State.STOPPED, State.RUNNING) ||
                state.compareAndSet(State.PAUSED, State.RUNNING)) {
            org.swarmforge.core.event.EventBus.getInstance().publish(
                SimulationEvent.obtain(
                    SimulationEvent.EventType.SIMULATION_STARTED,
                    SimulationEvent.Severity.INFO,
                    getTickCount(),
                    "▶️ Simulation démarrée",
                    null
                )
            );
            simulationThread = Thread.ofVirtual().name("simulation-loop").start(this::runLoop);
        }
    }

    public void pause() {
        state.set(State.PAUSED);
        org.swarmforge.core.event.EventBus.getInstance().publish(
            SimulationEvent.obtain(
                SimulationEvent.EventType.SIMULATION_PAUSED,
                SimulationEvent.Severity.INFO,
                getTickCount(),
                "⏸️ Simulation mise en pause",
                null
            )
        );
    }

    public org.swarmforge.core.gpu.SparsePheromoneGrid getPheromoneGrid() {
        return pheromoneGrid;
    }

    public org.swarmforge.core.ecology.WaterGrid getWaterGrid() {
        return waterGrid;
    }

    public void stop() {
        state.set(State.STOPPED);
        if (simulationThread != null) {
            simulationThread.interrupt();
        }
        org.swarmforge.core.event.EventBus.getInstance().publish(
            SimulationEvent.obtain(
                SimulationEvent.EventType.SIMULATION_STOPPED,
                SimulationEvent.Severity.INFO,
                getTickCount(),
                "⏹️ Simulation arrêtée",
                null
            )
        );
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
            }
        }
    }

    public void tick() {
        long currentTick = tickCount.incrementAndGet();
        updateEnvironment(currentTick);

        // Rebuild spatial index for individuals
        spatialIndex.clear();

        // Rebuild food index
        foodIndex.clear();
        foodSources.removeIf(FoodSource::isDepleted);
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

        // Process colonies in parallel using virtual threads
        // Collect individuals to insert into spatial index after parallel processing
        java.util.List<Individual> livingIndividuals = java.util.Collections
                .synchronizedList(new java.util.ArrayList<>());

        // Use parallel streams with virtual thread executor for better scalability
        SimulationContext context = new SimulationContextImpl(this); // Create context once per tick

        colonies.parallelStream().forEach(colony -> {
            // Process individuals within each colony in parallel
            colony.getLivingIndividuals().parallelStream().forEach(individual -> {
                if (individual.getBrain() != null) {
                    // 1. Brain Decision
                    org.swarmforge.core.behavior.ReasoningArchitecture.Action action = individual.getBrain()
                            .decide(individual, context);

                    // 2. Action Execution
                    org.swarmforge.core.behavior.ReasoningArchitecture.ActionResult result = individual
                            .executeAction(action, colony);

                    // 3. Learning/Update
                    individual.getBrain().update(individual, action, result);

                    // 4. Pheromone Deposit (Dual-Channel Algorithm)
                    if (action.type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.MOVE ||
                            action.type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.RETURN_HOME
                            ||
                            action.type() == org.swarmforge.core.behavior.ReasoningArchitecture.Action.ActionType.FOLLOW_TRAIL) {

                        org.swarmforge.core.domain.PheromoneType typeToDeposit;
                        if (individual.isCarryingFood()) {
                            // If carrying food, I came from food, so I leave a FOOD trail for others to
                            // find it
                            typeToDeposit = org.swarmforge.core.domain.PheromoneType.FOOD_TRAIL;
                        } else {
                            // If not carrying food (exploring), I leave a HOME trail so I (and others) can
                            // find home
                            typeToDeposit = org.swarmforge.core.domain.PheromoneType.HOME_TRAIL;
                        }

                        pheromoneGrid.deposit(
                                (int) individual.getX(),
                                (int) individual.getY(),
                                (int) individual.getZ(),
                                typeToDeposit.getIndex(),
                                1.0f // Intensity unit
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
                }

                // Update thermodynamic ambient temperature from weather context
                individual.setAmbientTemperatureC(context.getTemperature());

                // Process growth and lifecycle stage progression
                processGrowth(individual);

                // Update individual with construction context if applicable (calls tick())
                org.swarmforge.core.structure.ConstructionManager cm = constructionManagers.get(colony);
                if (cm != null) {
                    individual.update(cm);
                } else {
                    individual.tick();
                }

                // Collect living individuals for spatial index update
                if (individual.isAlive()) {
                    livingIndividuals.add(individual);

                    // Check environmental hazards (Floods)
                    // We can reuse context created outside the loop
                    float waterLevel = context.getWaterLevel(individual.getX(), individual.getY(), individual.getZ());
                    if (waterLevel > 0.5f) {
                        // Drowning damage
                        individual.setHealth(individual.getHealth() - 5.0f);
                        if (!individual.isAlive()) {
                            eventQueue.offer(new SimulationEvent(SimulationEvent.EventType.DEATH,
                                    tickCount.get(), "Drowned in flood"));
                        }
                    }
                }
            });
            colony.removeDeadIndividuals();
            colony.tick(); // Update internal state (Fungus Gardens, Consumption)
        });

        // Update spatial index (synchronized operation, done after parallel processing)
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
            switch (ind.getLifeStage()) {
                case EGG -> {
                    ind.setLifeStage(Individual.LifeStage.LARVA);
                    ind.setMaturationThreshold(ind.getMaturationThreshold() * 2); // Larva stage is longer
                    SimulationEvent evt = SimulationEvent.obtain(SimulationEvent.EventType.WORKER_BORN, SimulationEvent.Severity.INFO, tickCount.get(), "Éclosion : L'œuf a éclos en Larve", null);
                    eventQueue.offer(evt);
                    org.swarmforge.core.event.EventBus.getInstance().publish(evt);
                }
                case LARVA -> {
                    if (ind.getEnergy() > 50) {
                        ind.setLifeStage(Individual.LifeStage.PUPA);
                        ind.setMaturationThreshold(ind.getMaturationThreshold() * 1.5f);
                        SimulationEvent evt = SimulationEvent.obtain(SimulationEvent.EventType.WORKER_BORN, SimulationEvent.Severity.INFO, tickCount.get(), "Métamorphose : La larve s'est métamorphosée en Nymphe", null);
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
                        SimulationEvent.Severity.INFO, tickCount.get(), "Émergence : Nouvel Adulte (" + ind.getCaste() + ") émergé", null);
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

        // Update time (assuming 60 TPS, 1 day = 1440 ticks = 24 mins)
        // 1 tick = 1 minute simulation time?
        // If 1440 ticks = 24 hours. 1 tick = 24/1440 = 1/60 hours = 1 minute.
        weather.advanceTime(1f / 60f);
        dayNightCycle.tick();
        // seasonManager.tick(); // Already called via update? No, seasonManager.tick()
        // is correct.
        seasonManager.tick();

        // Process construction
        for (org.swarmforge.core.structure.ConstructionManager cm : constructionManagers.values()) {
            cm.tick();
        }

        predatorManager.tick();

        if (diseaseManager != null)
            diseaseManager.tick();

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

        // Advance weather (1 tick = 1 second, 3600 ticks = 1 hour)
        if (currentTick % 360 == 0) {
            weather.advanceTime(0.1f); // 0.1 hours = 6 minutes
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

    /**
     * Queue an event for processing.
     * 
     * @param event The event to queue
     */
    public void queueEvent(org.swarmforge.core.event.SimulationEvent event) {
        eventQueue.offer(event);
    }
}
