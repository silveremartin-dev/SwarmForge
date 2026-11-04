/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.swarmforge.core.species.Species;

/**
 * Represents a colony of eusocial insects.
 * Manages all individuals and colony-level resources.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class Colony implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String speciesName;
    private final CopyOnWriteArrayList<Individual> individuals;

    // Colony location (nest entrance)
    private float nestX, nestY, nestZ;

    // Resource Manager
    private final ColonyResourceManager resourceManager = new ColonyResourceManager();

    private transient Species species; // Re-inject on load via speciesName

    // Statistics
    private int totalBorn;
    private int totalDied;

    private final ColonyStatistics statistics = new ColonyStatistics();
    private transient final org.swarmforge.core.simulation.TunnelNetwork tunnelNetwork;
    private transient final org.swarmforge.core.simulation.FungusGarden fungusGarden;
    private transient final org.swarmforge.core.structure.Nest nest;
    private transient final org.swarmforge.core.diplomacy.DiplomacyManager diplomacyManager;
    private transient final org.swarmforge.core.structure.physics.NestVoxelGrid nestVoxelGrid;
    private transient final org.swarmforge.core.epidemiology.SocialImmunityManager socialImmunityManager;
    private transient final org.swarmforge.core.structure.physics.PassiveVentilationEngine ventilationEngine;
    private transient final org.swarmforge.core.structure.physics.StructuralStabilityAnalyzer stabilityAnalyzer;
    private final Map<UUID, org.swarmforge.core.epidemiology.IndividualInfection> infectionRegistry = new java.util.concurrent.ConcurrentHashMap<>();
    private static final AtomicLong COLONY_COUNTER = new AtomicLong(1);
    private transient Random random;

    public void setRandom(Random random) {
        this.random = random;
    }

    public Random getRandom() {
        if (this.random != null) {
            return this.random;
        }
        long seed = id != null ? id.getLeastSignificantBits() : 1337L;
        this.random = new Random(seed);
        return this.random;
    }

    public Colony(Species species, Terrarium terrarium) {
        this(species, terrarium.getWidth() / 2f, terrarium.getHeight() / 2f, 0);
        this.terrarium = terrarium;
    }

    public Colony(Species species, float x, float y, float z) {
        long specHash = (species != null && species.getScientificName() != null) ? species.getScientificName().hashCode() : 1337L;
        long coordHash = ((long) Float.floatToIntBits(x) << 32) ^ ((long) Float.floatToIntBits(y) * 31L + Float.floatToIntBits(z) * 7L);
        this.id = new UUID(specHash, coordHash);
        this.random = new Random(this.id.getLeastSignificantBits());
        this.species = species;
        this.speciesName = species != null ? species.getScientificName() : "UnknownSpecies";
        this.individuals = new CopyOnWriteArrayList<>();
        this.nestX = x;
        this.nestY = y;
        this.nestZ = z;
        this.tunnelNetwork = new org.swarmforge.core.simulation.TunnelNetwork(this);
        this.nest = new org.swarmforge.core.structure.Nest();
        this.diplomacyManager = new org.swarmforge.core.diplomacy.DiplomacyManager(this.id);
        this.nestVoxelGrid = new org.swarmforge.core.structure.physics.NestVoxelGrid(16, 16, 16, getResolvedNestType());
        this.socialImmunityManager = new org.swarmforge.core.epidemiology.SocialImmunityManager(nestX + 10.0f, nestY + 10.0f, 0.0f);
        this.ventilationEngine = new org.swarmforge.core.structure.physics.PassiveVentilationEngine();
        this.stabilityAnalyzer = new org.swarmforge.core.structure.physics.StructuralStabilityAnalyzer();

        // Seed Initial Nest
        var entrance = new org.swarmforge.core.structure.Chamber(id + "-ent",
                org.swarmforge.core.structure.Chamber.Type.ENTRANCE, nestX, nestY, nestZ, 10);
        float queenZ = getDynamicQueenChamberDepth();
        var queenChamber = new org.swarmforge.core.structure.Chamber(id + "-qc",
                org.swarmforge.core.structure.Chamber.Type.QUEEN_QUARTERS, nestX, nestY - 2, queenZ, 50);
        var tunnel = new org.swarmforge.core.structure.Tunnel(entrance, queenChamber);

        this.nest.addChamber(entrance);
        this.nest.addChamber(queenChamber);
        this.nest.addTunnel(tunnel);

        // Initialize Fungus Garden if this is a leafcutter species
        if ((species != null && species.canFarmFungus()) || (speciesName != null && (speciesName.toLowerCase().contains("atta") || speciesName.toLowerCase().contains("acromyrmex")))) {
            this.fungusGarden = new org.swarmforge.core.simulation.FungusGarden(this);
        } else {
            this.fungusGarden = null;
        }

        bootstrapDefaultResources();
    }

    public org.swarmforge.core.structure.physics.NestType getResolvedNestType() {
        return ColonyNestArchitectureResolver.resolveNestType(species, speciesName);
    }

    public float getDynamicQueenChamberDepth() {
        return ColonyNestArchitectureResolver.calculateDynamicQueenChamberDepth(species, speciesName, nestZ);
    }

    private int age = 0;

    public int getAgeInTicks() {
        return age;
    }

    public float getTotalBiomass() {
        return resourceManager.calculateTotalBiomass(individuals.size());
    }

    private float mapWidth = 64.0f;
    private float mapHeight = 64.0f;

    public void setMapBounds(float width, float height) {
        this.mapWidth = width;
        this.mapHeight = height;
    }

    public void setMapBounds(int width, int height) {
        setMapBounds((float) width, (float) height);
    }

    public float getMapWidth() { return mapWidth; }
    public float getMapHeight() { return mapHeight; }

    private transient Terrarium terrarium;

    public void setTerrarium(Terrarium terrarium) {
        this.terrarium = terrarium;
    }

    public Terrarium getTerrarium() {
        return terrarium;
    }

    public float getSurfaceZAt(float x, float y) {
        if (terrarium != null) {
            return terrarium.getSurfaceElevation(x, y);
        }
        return nestZ;
    }

    private final ColonyDemographicsSpawner demographicsSpawner = new ColonyDemographicsSpawner(this);

    public ColonyDemographicsSpawner getDemographicsSpawner() {
        return demographicsSpawner;
    }

    public void applyGaussianAge(Individual ind, boolean isFoundingQueen) {
        demographicsSpawner.applyGaussianAge(ind, isFoundingQueen);
    }

    public void applyGaussianHealth(Individual ind, boolean isFoundingQueen) {
        demographicsSpawner.applyGaussianHealth(ind, isFoundingQueen);
    }

    public List<Individual> createQueens(int count) { return demographicsSpawner.createQueens(count); }
    public List<Individual> createQueens(int count, boolean applyAgeDistribution) { return demographicsSpawner.createQueens(count, applyAgeDistribution); }
    public double calculateSurfaceActivityRatio(int dayOfYear, float hourOfDay, float ambientTemp, double latitude) { return demographicsSpawner.calculateSurfaceActivityRatio(dayOfYear, hourOfDay, ambientTemp, latitude); }
    public double calculateSurfaceActivityRatio(int dayOfYear, float hourOfDay, float ambientTemp) { return demographicsSpawner.calculateSurfaceActivityRatio(dayOfYear, hourOfDay, ambientTemp); }
    public float[] getSpawningCoordinates(Individual.Caste caste, Individual.Job job, int index) { return demographicsSpawner.getSpawningCoordinates(caste, job, index); }
    public float[] getSpawningCoordinates(Individual.Caste caste, Individual.Job job, int index, int dayOfYear, float hourOfDay, float ambientTemp) { return demographicsSpawner.getSpawningCoordinates(caste, job, index, dayOfYear, hourOfDay, ambientTemp); }
    public int calculateSubterraneanCapacity() { return demographicsSpawner.calculateSubterraneanCapacity(); }
    public List<Individual> createWorkers(int count) { return demographicsSpawner.createWorkers(count); }
    public List<Individual> createWorkers(int count, boolean applyAgeDistribution) { return demographicsSpawner.createWorkers(count, applyAgeDistribution); }
    public List<Individual> createSoldiers(int count) { return demographicsSpawner.createSoldiers(count); }
    public List<Individual> createSoldiers(int count, boolean applyAgeDistribution) { return demographicsSpawner.createSoldiers(count, applyAgeDistribution); }
    public List<Individual> createMales(int count) { return demographicsSpawner.createMales(count); }
    public List<Individual> createMales(int count, boolean applyAgeDistribution) { return demographicsSpawner.createMales(count, applyAgeDistribution); }
    public List<Individual> createBrood(int count) { return demographicsSpawner.createBrood(count); }
    public List<Individual> createBrood(int count, boolean applyAgeDistribution) { return demographicsSpawner.createBrood(count, applyAgeDistribution); }
    public Individual spawnUnit(CasteTemplate template) { return demographicsSpawner.spawnUnit(template); }

    public ColonyResourceManager getResourceManager() {
        return resourceManager;
    }

    public float getProteinStored() { return resourceManager.getProteinStored(); }
    public void setProteinStored(float v) { resourceManager.setProteinStored(v); }
    public void addProtein(float v) { resourceManager.addProtein(v); }
    public float getCarbohydrateStored() { return resourceManager.getCarbohydrateStored(); }
    public void setCarbohydrateStored(float v) { resourceManager.setCarbohydrateStored(v); }
    public float getSugarStored() { return resourceManager.getSugarStored(); }
    public void setSugarStored(float v) { resourceManager.setSugarStored(v); }
    public void addCarbohydrate(float v) { resourceManager.addCarbohydrate(v); }
    public float getWaterStored() { return resourceManager.getWaterStored(); }
    public void setWaterStored(float v) { resourceManager.setWaterStored(v); }

    // Listeners
    @com.fasterxml.jackson.annotation.JsonIgnore
    private transient List<ColonyListener> listeners = new CopyOnWriteArrayList<>();

    private List<ColonyListener> getListeners() {
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
        }
        return listeners;
    }

    public void addListener(ColonyListener listener) {
        if (listener != null) {
            getListeners().add(listener);
        }
    }

    public void removeListener(ColonyListener listener) {
        if (listener != null) {
            getListeners().remove(listener);
        }
    }

    public Individual createQueen() {
        List<Individual> list = createQueens(1);
        return list.isEmpty() ? null : list.get(0);
    }

    public Individual createWorker() {
        List<Individual> list = createWorkers(1);
        return list.isEmpty() ? null : list.get(0);
    }

    public Individual createSoldier() {
        List<Individual> list = createSoldiers(1);
        return list.isEmpty() ? null : list.get(0);
    }

    public Individual createMale() {
        List<Individual> list = createMales(1);
        return list.isEmpty() ? null : list.get(0);
    }

    void incrementTotalBorn() { this.totalBorn++; }
    void addTotalBorn(int count) { this.totalBorn += count; }
    void addTotalDied(int count) { this.totalDied += count; }

    public void addIndividual(Individual individual) {
        ColonyPopulationManager.addIndividual(this, individuals, individual, getListeners());
    }

    public void addIndividualsBulk(java.util.Collection<Individual> batch) {
        ColonyPopulationManager.addIndividualsBulk(this, individuals, batch);
    }

    public int removeDeadIndividuals() {
        return ColonyPopulationManager.removeDeadIndividuals(this, individuals, getListeners());
    }

    /**
     * Remove an individual (e.g. for cross-terrarium migration).
     */
    public boolean removeIndividual(Individual individual) {
        if (individual == null) return false;
        return individuals.remove(individual);
    }

    /**
     * Get count of individuals by caste.
     */
    public int countByCaste(Individual.Caste caste) {
        return ColonyCensusTracker.countByCaste(individuals, caste);
    }

    /**
     * Get average age of individuals in a specific caste.
     */
    public float getAverageAgeByCaste(Individual.Caste caste) {
        return ColonyCensusTracker.getAverageAgeByCaste(individuals, caste);
    }

    /**
     * Get count of individuals in a specific life stage.
     */
    public int getBroodCountByStage(Individual.LifeStage stage) {
        return ColonyCensusTracker.getBroodCountByStage(individuals, stage);
    }

    /**
     * Records a detailed demographic and economic snapshot for history graphs.
     */
    public void recordDetailedSnapshot(long currentTick, float nestTemp, float nestCo2, float nestO2) {
        ColonyCensusTracker.recordDetailedSnapshot(this, statistics, currentTick, nestTemp, nestCo2, nestO2);
    }

    /**
     * Get all living individuals.
     */
    public List<Individual> getLivingIndividuals() {
        return individuals.stream()
                .filter(Individual::isAlive)
                .toList();
    }

    /**
     * Get raw backing list of individuals for zero-allocation tick loops.
     */
    public List<Individual> getIndividuals() {
        return individuals;
    }

    /**
     * Get total population (living only).
     */
    public int getPopulation() {
        return (int) individuals.stream().filter(Individual::isAlive).count();
    }

    /**
     * Check if colony has a living queen.
     */
    public boolean hasQueen() {
        return individuals.stream()
                .anyMatch(i -> i.isAlive() && i.getCaste() == Individual.Caste.QUEEN);
    }

    private String participantName;

    // Getters & Resource Management Delegations
    public UUID getId() { return id; }
    public String getParticipantName() { return participantName; }
    public void setParticipantName(String participantName) { this.participantName = participantName; }
    public String getSpeciesName() { return speciesName; }
    public Species getSpecies() { return species; }
    public float getNestX() { return nestX; }
    public float getNestY() { return nestY; }
    public float getNestZ() { return nestZ; }

    public void setNestPosition(float x, float y, float z) {
        this.nestX = x;
        this.nestY = y;
        this.nestZ = z;
    }

    private float metabolicRate = 1.0f;

    public float getMetabolicRate() { return metabolicRate; }
    public void setMetabolicRate(float metabolicRate) { this.metabolicRate = Math.max(0.01f, metabolicRate); }
    public float getResourceAmount(ResourceType type) { return resourceManager.getResourceAmount(type); }
    public void setResourceAmount(ResourceType type, float amount) { resourceManager.setResourceAmount(type, amount); }
    public void addResource(ResourceType type, float amount) { resourceManager.addResource(type, amount); }
    public float consumeResource(ResourceType type, float amount) { return resourceManager.consumeResource(type, amount); }
    public float getFoodStored() { return resourceManager.getFoodStored(); }
    public float consumeFood(float amount) { return resourceManager.consumeFood(amount); }
    public void setFoodStored(float food) { resourceManager.setFoodStored(food); }
    public void bootstrapDefaultResources() { resourceManager.bootstrapDefaultResources(species, speciesName, getPopulation()); }
    public int getTotalBorn() { return totalBorn; }
    public int getTotalDied() { return totalDied; }
    public org.swarmforge.core.simulation.TunnelNetwork getTunnelNetwork() { return tunnelNetwork; }
    public ColonyStatistics getStatistics() { return statistics; }
    public org.swarmforge.core.structure.Nest getNest() { return nest; }
    public boolean sendResource(Colony target, ResourceType type, float amount) { return resourceManager.sendResource(this, target, type, amount); }
    public org.swarmforge.core.diplomacy.DiplomacyManager getDiplomacy() { return diplomacyManager; }
    public org.swarmforge.core.diplomacy.DiplomacyManager getDiplomacyManager() { return diplomacyManager; }
    public org.swarmforge.core.simulation.FungusGarden getFungusGarden() { return fungusGarden; }

    private int queenSpermathecaSperm = 50000;

    public int getQueenSpermathecaSperm() { return queenSpermathecaSperm; }

    /**
     * Process internal colony mechanics (Fungus Garden cultivation, Queen egg-laying, ontogeny expansion).
     */
    public void tick() {
        if (fungusGarden != null) {
            fungusGarden.tick();
        }

        if (tunnelNetwork != null && (age % 300 == 0)) {
            tunnelNetwork.checkAndExpandOntogeny(this);
        }

        if (hasQueen() && getProteinStored() >= 5.0f && (age % 600 == 0)) {
            float eggCost = 5.0f;
            resourceManager.setProteinStored(resourceManager.getProteinStored() - eggCost);

            Individual newEgg;
            if (queenSpermathecaSperm > 0) {
                queenSpermathecaSperm--;
                newEgg = createWorker();
            } else {
                newEgg = createMale();
            }

            if (newEgg != null) {
                newEgg.setLifeStage(Individual.LifeStage.EGG);
            }
        }

        age++;
    }

    private int broodCount = 50;
    private int enslavedPupaeCount = 0;

    public int getBroodCount() { return broodCount; }
    public void decrementBroodCount() { if (broodCount > 0) broodCount--; }
    public void incrementEnslavedPupaeCount() { enslavedPupaeCount++; }
    public int getEnslavedPupaeCount() { return enslavedPupaeCount; }

    public org.swarmforge.core.structure.physics.NestVoxelGrid getNestVoxelGrid() {
        return nestVoxelGrid;
    }

    public org.swarmforge.core.epidemiology.SocialImmunityManager getSocialImmunityManager() {
        return socialImmunityManager;
    }

    public org.swarmforge.core.structure.physics.PassiveVentilationEngine getVentilationEngine() {
        return ventilationEngine;
    }

    public org.swarmforge.core.structure.physics.StructuralStabilityAnalyzer getStabilityAnalyzer() {
        return stabilityAnalyzer;
    }

    public Map<UUID, org.swarmforge.core.epidemiology.IndividualInfection> getInfectionRegistry() {
        return infectionRegistry;
    }

    public void tickPhysicalEngines(float ambientTemp, float ambientCo2, float windSpeed,
                                   float waterSaturation, float deltaSeconds, Random rng) {
        ColonyPhysicalEngineRunner.tickPhysicalEngines(
                nestVoxelGrid,
                ventilationEngine,
                stabilityAnalyzer,
                socialImmunityManager,
                infectionRegistry,
                getLivingIndividuals(),
                getPopulation(),
                age,
                ambientTemp,
                ambientCo2,
                windSpeed,
                waterSaturation,
                deltaSeconds,
                rng
        );
    }
}
