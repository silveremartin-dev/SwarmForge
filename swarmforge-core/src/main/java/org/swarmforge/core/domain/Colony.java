/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.UUID;
import org.swarmforge.core.species.Species;
import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a colony of eusocial insects.
 * Manages all individuals and colony-level resources.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class Colony implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String speciesName;
    private final CopyOnWriteArrayList<Individual> individuals;

    // Colony location (nest entrance)
    private float nestX, nestY, nestZ;

    // Resources
    private final java.util.Map<ResourceType, Float> resources = new java.util.concurrent.ConcurrentHashMap<>();

    private transient Species species; // Re-inject on load via speciesName

    // Statistics
    private int totalBorn;
    private int totalDied;

    private final ColonyStatistics statistics = new ColonyStatistics();
    private transient final org.swarmforge.core.simulation.TunnelNetwork tunnelNetwork;
    private transient final org.swarmforge.core.simulation.FungusGarden fungusGarden;
    private transient final org.swarmforge.core.structure.Nest nest; // New Nest Structure
    private transient final org.swarmforge.core.diplomacy.DiplomacyManager diplomacyManager;
    private transient final org.swarmforge.core.structure.physics.NestVoxelGrid nestVoxelGrid;
    private transient final org.swarmforge.core.epidemiology.SocialImmunityManager socialImmunityManager;
    private transient final org.swarmforge.core.structure.physics.PassiveVentilationEngine ventilationEngine;
    private transient final org.swarmforge.core.structure.physics.StructuralStabilityAnalyzer stabilityAnalyzer;
    private final java.util.Map<UUID, org.swarmforge.core.epidemiology.IndividualInfection> infectionRegistry = new java.util.concurrent.ConcurrentHashMap<>();
    private static final java.util.concurrent.atomic.AtomicLong COLONY_COUNTER = new java.util.concurrent.atomic.AtomicLong(1);
    private transient java.util.Random random;

    public void setRandom(java.util.Random random) {
        this.random = random;
    }

    public java.util.Random getRandom() {
        if (this.random != null) {
            return this.random;
        }
        long seed = id != null ? id.getLeastSignificantBits() : 1337L;
        this.random = new java.util.Random(seed);
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
        this.random = new java.util.Random(this.id.getLeastSignificantBits());
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
        String arch = species != null ? species.getNestType() : (speciesName != null ? speciesName : "SUBTERRANEAN_SIMPLE");
        if (arch != null) {
            String u = arch.toUpperCase();
            if (u.contains("MOUND") || u.contains("FORMICA")) return org.swarmforge.core.structure.physics.NestType.SUBTERRANEAN_MOUND;
            if (u.contains("CATHEDRAL") || u.contains("TERMITE") || u.contains("MACROTERMES")) return org.swarmforge.core.structure.physics.NestType.TERMITE_CATHEDRAL;
            if (u.contains("TREE") || u.contains("WOOD") || u.contains("CAVITY") || u.contains("CAMPONOTUS")) return org.swarmforge.core.structure.physics.NestType.CAVITY_TREE;
            if (u.contains("WEAVER") || u.contains("OECOPHYLLA")) return org.swarmforge.core.structure.physics.NestType.ARBOREAL_WEAVER;
            if (u.contains("CARTON") || u.contains("CREMATOGASTER")) return org.swarmforge.core.structure.physics.NestType.ARBOREAL_CARTON;
            if (u.contains("CRATER") || u.contains("POGONOMYRMEX")) return org.swarmforge.core.structure.physics.NestType.CRATER_NEST;
            if (u.contains("TOWER") || u.contains("CHIMNEY") || u.contains("ODONTOMACHUS")) return org.swarmforge.core.structure.physics.NestType.TOWER_CHIMNEY;
            if (u.contains("CATAGLYPHIS") || u.contains("DESERT")) return org.swarmforge.core.structure.physics.NestType.CATAGLYPHIS_DEEP;
            if (u.contains("ROCK") || u.contains("LITHOPHILIC")) return org.swarmforge.core.structure.physics.NestType.LITHOPHILIC;
            if (u.contains("BAMBOO")) return org.swarmforge.core.structure.physics.NestType.SUSPENDED_BAMBOO;
            if (u.contains("POLYDOMOUS") || u.contains("LINEPITHEMA")) return org.swarmforge.core.structure.physics.NestType.POLYDOMOUS_NETWORK;
            if (u.contains("POCKET") || u.contains("SOLENOPSIS")) return org.swarmforge.core.structure.physics.NestType.HYPOGAEIC_POCKET;
        }
        return org.swarmforge.core.structure.physics.NestType.SUBTERRANEAN_SIMPLE;
    }

    public float getDynamicQueenChamberDepth() {
        String arch = species != null ? species.getNestType() : (speciesName != null ? speciesName : "MATURE");
        if (arch != null) {
            String u = arch.toUpperCase();
            if (u.contains("FUNGI") || u.contains("ATTA")) return nestZ - 14.0f;
            if (u.contains("MOUND") || u.contains("FORMICA")) return nestZ - 10.0f;
            if (u.contains("CATHEDRAL") || u.contains("TERMITE")) return nestZ - 8.0f;
            if (u.contains("TREE") || u.contains("WOOD") || u.contains("ARBOREAL")) return nestZ + 6.0f;
            if (u.contains("PAPER") || u.contains("VESPA")) return nestZ + 4.0f;
            if (u.contains("WAX")) return nestZ + 2.0f;
        }
        return nestZ - 6.0f; // Safe default subterranean depth well below surface frost line
    }

    private int age = 0;

    public int getAgeInTicks() {
        return age;
    }

    public float getTotalBiomass() {
        float total = getFoodStored() + getWaterStored() + getResourceAmount(ResourceType.PROTEIN)
                + getResourceAmount(ResourceType.CARBOHYDRATE);
        // Estimate ant biomass (average 10mg per ant)
        total += individuals.size() * 10.0f;
        return total;
    }

    // Resources
    private float waterStored;
    private float proteinStored;
    private float carbohydrateStored;

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

    public List<Individual> createQueens(int count) {
        return demographicsSpawner.createQueens(count);
    }

    public List<Individual> createQueens(int count, boolean applyAgeDistribution) {
        return demographicsSpawner.createQueens(count, applyAgeDistribution);
    }

    public double calculateSurfaceActivityRatio(int dayOfYear, float hourOfDay, float ambientTemp, double latitude) {
        return demographicsSpawner.calculateSurfaceActivityRatio(dayOfYear, hourOfDay, ambientTemp, latitude);
    }

    public double calculateSurfaceActivityRatio(int dayOfYear, float hourOfDay, float ambientTemp) {
        return demographicsSpawner.calculateSurfaceActivityRatio(dayOfYear, hourOfDay, ambientTemp);
    }

    public float[] getSpawningCoordinates(Individual.Caste caste, Individual.Job job, int index) {
        return demographicsSpawner.getSpawningCoordinates(caste, job, index);
    }

    public float[] getSpawningCoordinates(Individual.Caste caste, Individual.Job job, int index, int dayOfYear, float hourOfDay, float ambientTemp) {
        return demographicsSpawner.getSpawningCoordinates(caste, job, index, dayOfYear, hourOfDay, ambientTemp);
    }

    public int calculateSubterraneanCapacity() {
        return demographicsSpawner.calculateSubterraneanCapacity();
    }

    public List<Individual> createWorkers(int count) {
        return demographicsSpawner.createWorkers(count);
    }

    public List<Individual> createWorkers(int count, boolean applyAgeDistribution) {
        return demographicsSpawner.createWorkers(count, applyAgeDistribution);
    }

    public List<Individual> createSoldiers(int count) {
        return demographicsSpawner.createSoldiers(count);
    }

    public List<Individual> createSoldiers(int count, boolean applyAgeDistribution) {
        return demographicsSpawner.createSoldiers(count, applyAgeDistribution);
    }

    public List<Individual> createMales(int count) {
        return demographicsSpawner.createMales(count);
    }

    public List<Individual> createMales(int count, boolean applyAgeDistribution) {
        return demographicsSpawner.createMales(count, applyAgeDistribution);
    }

    public List<Individual> createBrood(int count) {
        return demographicsSpawner.createBrood(count);
    }

    public List<Individual> createBrood(int count, boolean applyAgeDistribution) {
        return demographicsSpawner.createBrood(count, applyAgeDistribution);
    }

    public Individual spawnUnit(CasteTemplate template) {
        return demographicsSpawner.spawnUnit(template);
    }
    public float getProteinStored() {
        return proteinStored;
    }

    public void setProteinStored(float v) {
        this.proteinStored = v;
    }

    public void addProtein(float v) {
        this.proteinStored += v;
    }

    public float getCarbohydrateStored() {
        return carbohydrateStored;
    }

    public void setCarbohydrateStored(float v) {
        this.carbohydrateStored = v;
    }

    public float getSugarStored() {
        return getCarbohydrateStored();
    }

    public void setSugarStored(float v) {
        setCarbohydrateStored(v);
    }

    public void addCarbohydrate(float v) {
        this.carbohydrateStored += v;
    }

    public float getWaterStored() {
        return waterStored;
    }

    public void setWaterStored(float v) {
        this.waterStored = v;
    }

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

    public void addIndividual(Individual individual) {
        if (individual == null || individuals.contains(individual)) return;
        if (individual.getSpecies() == null && this.species != null) {
            individual.setSpecies(this.species);
        }
        individual.setRandom(new java.util.Random(getRandom().nextLong()));
        individuals.add(individual);
        totalBorn++;
        for (ColonyListener l : getListeners()) {
            l.onBirth(this, individual);
        }
    }

    /**
     * Bulk add individuals without single-element array copying or O(N^2) list search.
     */
    public void addIndividualsBulk(java.util.Collection<Individual> batch) {
        if (batch == null || batch.isEmpty()) return;
        java.util.List<Individual> toAdd;
        if (individuals.isEmpty()) {
            toAdd = batch.stream().filter(java.util.Objects::nonNull).toList();
        } else {
            java.util.Set<Individual> existing = new java.util.HashSet<>(individuals);
            toAdd = batch.stream().filter(ind -> ind != null && !existing.contains(ind)).toList();
        }
        if (toAdd.isEmpty()) return;
        toAdd.forEach(ind -> {
            if (this.species != null && ind.getSpecies() == null) {
                ind.setSpecies(this.species);
            }
            ind.setRandom(new java.util.Random(getRandom().nextLong()));
        });
        individuals.addAll(toAdd);
        totalBorn += toAdd.size();
    }

    /**
     * Remove dead individuals from the colony.
     */
    public int removeDeadIndividuals() {
        java.util.List<Individual> dead = new java.util.ArrayList<>();
        for (Individual ind : individuals) {
            if (!ind.isAlive()) {
                dead.add(ind);
            }
        }
        if (dead.isEmpty()) return 0;

        individuals.removeAll(dead);
        totalDied += dead.size();
        for (Individual ind : dead) {
            for (ColonyListener l : getListeners()) {
                l.onDeath(this, ind);
            }
        }
        return dead.size();
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
        return (int) individuals.stream()
                .filter(i -> i.isAlive() && i.getCaste() == caste)
                .count();
    }

    /**
     * Get average age of individuals in a specific caste.
     */
    public float getAverageAgeByCaste(Individual.Caste caste) {
        List<Individual> casteInds = individuals.stream()
                .filter(i -> i.isAlive() && i.getCaste() == caste)
                .toList();
        if (casteInds.isEmpty()) return 0f;
        double sum = casteInds.stream().mapToDouble(Individual::getAge).sum();
        return (float) (sum / casteInds.size());
    }

    /**
     * Get count of individuals in a specific life stage.
     */
    public int getBroodCountByStage(Individual.LifeStage stage) {
        return (int) individuals.stream()
                .filter(i -> i.isAlive() && i.getLifeStage() == stage)
                .count();
    }

    /**
     * Records a detailed demographic and economic snapshot for history graphs.
     */
    public void recordDetailedSnapshot(long currentTick, float nestTemp, float nestCo2, float nestO2) {
        java.util.Map<Individual.Caste, Integer> casteMap = new java.util.HashMap<>();
        java.util.Map<Individual.Caste, Float> ageMap = new java.util.HashMap<>();
        for (Individual.Caste c : Individual.Caste.values()) {
            int count = countByCaste(c);
            casteMap.put(c, count);
            if (count > 0) {
                ageMap.put(c, getAverageAgeByCaste(c));
            } else {
                ageMap.put(c, 0f);
            }
        }

        java.util.Map<Individual.LifeStage, Integer> broodMap = new java.util.HashMap<>();
        for (Individual.LifeStage s : Individual.LifeStage.values()) {
            broodMap.put(s, getBroodCountByStage(s));
        }

        int chamberCount = (nest != null && nest.getChambers() != null) ? nest.getChambers().size() : 0;

        ColonyStatistics.DetailedDataPoint point = new ColonyStatistics.DetailedDataPoint(
                currentTick,
                age,
                getPopulation(),
                casteMap,
                ageMap,
                broodMap,
                getFoodStored(),
                proteinStored,
                carbohydrateStored,
                waterStored,
                nestTemp,
                nestCo2,
                nestO2,
                chamberCount,
                totalBorn,
                totalDied
        );

        statistics.recordDetailed(point);
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

    // Getters
    public UUID getId() {
        return id;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public Species getSpecies() {
        return species;
    }

    public float getNestX() {
        return nestX;
    }

    public float getNestY() {
        return nestY;
    }

    public float getNestZ() {
        return nestZ;
    }

    public void setNestPosition(float x, float y, float z) {
        this.nestX = x;
        this.nestY = y;
        this.nestZ = z;
    }

    private float metabolicRate = 1.0f;

    public float getMetabolicRate() {
        return metabolicRate;
    }

    public void setMetabolicRate(float metabolicRate) {
        this.metabolicRate = Math.max(0.01f, metabolicRate);
    }

    // === Resource Management ===

    /**
     * Get specific resource amount.
     */
    public float getResourceAmount(ResourceType type) {
        return resources.getOrDefault(type, 0f);
    }

    /**
     * Set specific resource amount.
     */
    public void setResourceAmount(ResourceType type, float amount) {
        resources.put(type, amount);
    }

    /**
     * Add resource to colony storage.
     */
    public void addResource(ResourceType type, float amount) {
        resources.merge(type, amount, Float::sum);
        if (type != null) {
            if (type == ResourceType.PROTEIN || type == ResourceType.INSECT) {
                proteinStored += amount;
            } else if (type == ResourceType.CARBOHYDRATE || type == ResourceType.SUGAR || type == ResourceType.NECTAR || type == ResourceType.HONEYDEW) {
                carbohydrateStored += amount;
            } else if (type == ResourceType.WATER) {
                waterStored += amount;
            }
        }
    }

    /**
     * Consume resource from storage.
     * 
     * @return Access amount actually consumed
     */
    public float consumeResource(ResourceType type, float amount) {
        float current = getResourceAmount(type);
        float toTake = Math.min(current, amount);
        if (toTake > 0) {
            resources.put(type, current - toTake);
            if (type != null) {
                if (type == ResourceType.PROTEIN || type == ResourceType.INSECT) {
                    proteinStored = Math.max(0f, proteinStored - toTake);
                } else if (type == ResourceType.CARBOHYDRATE || type == ResourceType.SUGAR || type == ResourceType.NECTAR || type == ResourceType.HONEYDEW) {
                    carbohydrateStored = Math.max(0f, carbohydrateStored - toTake);
                } else if (type == ResourceType.WATER) {
                    waterStored = Math.max(0f, waterStored - toTake);
                }
            }
        }
        return toTake;
    }

    /**
     * Get total food stored (sum of all food-type resources).
     */
    public float getFoodStored() {
        // Sum of all food types (excluding WATER)
        return (float) resources.entrySet().stream()
                .filter(e -> e.getKey() != ResourceType.WATER)
                .mapToDouble(java.util.Map.Entry::getValue)
                .sum();
    }

    /**
     * Consume food from colony storage across available food types.
     * 
     * @param amount Desired amount of food to consume
     * @return Amount actually consumed
     */
    public float consumeFood(float amount) {
        if (amount <= 0) return 0f;
        float remaining = amount;
        ResourceType[] foodTypes = { ResourceType.SUGAR, ResourceType.CARBOHYDRATE, ResourceType.SEED, ResourceType.NECTAR, ResourceType.PROTEIN, ResourceType.INSECT, ResourceType.FUNGUS, ResourceType.HONEYDEW };
        for (ResourceType type : foodTypes) {
            if (remaining <= 0) break;
            float available = getResourceAmount(type);
            if (available > 0) {
                float taken = consumeResource(type, Math.min(available, remaining));
                remaining -= taken;
            }
        }
        return amount - remaining;
    }

    public void setFoodStored(float food) {
        float f = Math.max(0, food);
        for (ResourceType type : ResourceType.values()) {
            if (type != ResourceType.WATER) {
                resources.put(type, 0f);
            }
        }
        carbohydrateStored = f * 0.5f;
        proteinStored = f * 0.5f;
        setResourceAmount(ResourceType.SEED, f * 0.5f);
        setResourceAmount(ResourceType.SUGAR, f * 0.5f);
    }

    /**
     * Bootstraps critical resource stores (food, water, protein, fungus, brood)
     * based on colony population scale to prevent immediate initial collapse.
     */
    public void bootstrapDefaultResources() {
        int pop = Math.max(20, getPopulation());
        float baseAmount = Math.max(1000.0f, pop * 20.0f);

        // Fill primary stored metrics
        this.waterStored = baseAmount;
        this.proteinStored = baseAmount * 0.75f;
        this.carbohydrateStored = baseAmount * 0.75f;

        // Fill inventory map with essential resources
        addResource(ResourceType.SEED, baseAmount * 0.4f);
        addResource(ResourceType.SUGAR, baseAmount * 0.4f);
        addResource(ResourceType.NECTAR, baseAmount * 0.3f);
        addResource(ResourceType.PROTEIN, baseAmount * 0.35f);
        addResource(ResourceType.INSECT, baseAmount * 0.35f);
        addResource(ResourceType.WATER, baseAmount);

        // Leafcutter & Fungus-cultivating species initialization
        if ((species != null && ("FUNGUS".equalsIgnoreCase(species.getPrimaryDiet()) || "Atta".equalsIgnoreCase(species.getGenus())))
                || (speciesName != null && speciesName.contains("Atta"))) {
            float fungusAmount = Math.max(400.0f, pop * 10.0f);
            addResource(ResourceType.FUNGUS, fungusAmount);
            addResource(ResourceType.LEAF, 300.0f);
            addResource(ResourceType.MULCH, 200.0f);
        }
    }

    public int getTotalBorn() {
        return totalBorn;
    }

    public int getTotalDied() {
        return totalDied;
    }

    public org.swarmforge.core.simulation.TunnelNetwork getTunnelNetwork() {
        return tunnelNetwork;
    }

    public ColonyStatistics getStatistics() {
        return statistics;
    }

    public org.swarmforge.core.structure.Nest getNest() {
        return nest;
    }

    /**
     * Send resources to another colony.
     * Checks if target is an enemy and if sender has enough resources.
     */
    public boolean sendResource(Colony target, ResourceType type, float amount) {
        if (target == null || type == null || amount <= 0)
            return false;

        // Cannot trade with enemies? Or maybe Tribute is allowed even if enemy?
        // Let's assume you can always try to send tribute to appease an enemy.
        // But maybe blocked if it's strictly "Trade". For now, allow all transfers as
        // "Tribute/Trade".

        // Check balance
        float current = getResourceAmount(type);
        if (current < amount)
            return false;

        // Execute Transfer
        this.consumeResource(type, amount);
        target.addResource(type, amount);

        return true;
    }

    public org.swarmforge.core.diplomacy.DiplomacyManager getDiplomacy() {
        return diplomacyManager;
    }

    public org.swarmforge.core.diplomacy.DiplomacyManager getDiplomacyManager() {
        return diplomacyManager;
    }

    public org.swarmforge.core.simulation.FungusGarden getFungusGarden() {
        return fungusGarden;
    }

    private int queenSpermathecaSperm = 50000; // Finite sperm count in queen's spermatheca

    public int getQueenSpermathecaSperm() {
        return queenSpermathecaSperm;
    }

    /**
     * Process internal colony mechanics (Fungus Garden cultivation, Queen egg-laying, ontogeny expansion).
     */
    public void tick() {
        // 1. Process Fungus Garden if present
        if (fungusGarden != null) {
            fungusGarden.tick();
        }

        // 2. Ontogeny nest expansion (checked periodically)
        if (tunnelNetwork != null && (age % 300 == 0)) {
            tunnelNetwork.checkAndExpandOntogeny(this);
        }

        // 3. Queen Oviposition & Haplodiploid Determination
        if (hasQueen() && proteinStored >= 5.0f && (age % 600 == 0)) { // Every ~10 seconds
            float eggCost = 5.0f;
            proteinStored -= eggCost;
            
            Individual newEgg;
            if (queenSpermathecaSperm > 0) {
                // Diploid female worker
                queenSpermathecaSperm--;
                newEgg = createWorker();
            } else {
                // Spermatheca depleted: Arrhenotokous Parthenogenesis -> Haploid Male
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

    public java.util.Map<UUID, org.swarmforge.core.epidemiology.IndividualInfection> getInfectionRegistry() {
        return infectionRegistry;
    }

    /**
     * Executes passive stack-effect ventilation, Mohr-Coulomb stability analysis,
     * and social immunity transmission cycles across the colony's 3D voxel nest.
     */
    public void tickPhysicalEngines(float ambientTemp, float ambientCo2, float windSpeed,
                                   float waterSaturation, float deltaSeconds, java.util.Random rng) {
        if (nestVoxelGrid == null) return;

        // 1. Passive Stack-Effect Nest Ventilation
        if (ventilationEngine != null) {
            ventilationEngine.simulateVentilationStep(nestVoxelGrid, ambientTemp, ambientCo2, windSpeed, getPopulation());
        }

        // 2. Structural Mechanics & Cave-In Risk under Moisture Saturation
        if (stabilityAnalyzer != null && age % 60 == 0) { // Evaluate every ~1 sec
            stabilityAnalyzer.evaluateAndTriggerCaveIns(nestVoxelGrid, waterSaturation, rng != null ? rng : new java.util.Random());
        }

        // 3. Social Immunity & Epizootic Management
        if (socialImmunityManager != null && age % 30 == 0) { // Evaluate every ~0.5 sec
            socialImmunityManager.simulateEpidemiologyStep(infectionRegistry, getLivingIndividuals(), nestVoxelGrid, rng != null ? rng : new java.util.Random(), deltaSeconds);
        }
    }
}
