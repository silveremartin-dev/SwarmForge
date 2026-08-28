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
    private final org.swarmforge.core.simulation.TunnelNetwork tunnelNetwork;
    private final org.swarmforge.core.simulation.FungusGarden fungusGarden;
    private final org.swarmforge.core.structure.Nest nest; // New Nest Structure
    private final org.swarmforge.core.diplomacy.DiplomacyManager diplomacyManager;

    public Colony(Species species, Terrarium terrarium) {
        this(species, terrarium.getWidth() / 2f, terrarium.getHeight() / 2f, 0);
    }

    public Colony(Species species, float x, float y, float z) {
        this.id = new UUID(java.util.concurrent.ThreadLocalRandom.current().nextLong(), java.util.concurrent.ThreadLocalRandom.current().nextLong());
        this.species = species;
        this.speciesName = species.getScientificName(); // Fixed legacy accessor
        this.individuals = new CopyOnWriteArrayList<>();
        this.nestX = x;
        this.nestY = y;
        this.nestZ = z;
        this.tunnelNetwork = new org.swarmforge.core.simulation.TunnelNetwork(this);
        this.nest = new org.swarmforge.core.structure.Nest();
        this.diplomacyManager = new org.swarmforge.core.diplomacy.DiplomacyManager(this.id);

        // Seed Initial Nest
        var entrance = new org.swarmforge.core.structure.Chamber(id + "-ent",
                org.swarmforge.core.structure.Chamber.Type.ENTRANCE, nestX, nestY, nestZ, 10);
        var queenChamber = new org.swarmforge.core.structure.Chamber(id + "-qc",
                org.swarmforge.core.structure.Chamber.Type.QUEEN_QUARTERS, nestX, nestY - 20, nestZ, 50);
        var tunnel = new org.swarmforge.core.structure.Tunnel(entrance, queenChamber);

        this.nest.addChamber(entrance);
        this.nest.addChamber(queenChamber);
        this.nest.addTunnel(tunnel);

        // Initialize Fungus Garden if this is a leafcutter species
        if (speciesName.contains("Atta")) {
            this.fungusGarden = new org.swarmforge.core.simulation.FungusGarden(this);
        } else {
            this.fungusGarden = null;
        }
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

    public void applyGaussianAge(Individual ind, boolean isFoundingQueen) {
        if (ind == null) return;
        float maxLifespan = ind.getMaxLifespan();
        if (isFoundingQueen) {
            // Young founding queen: age between 0 and 0.05 * maxLifespan
            float youngAge = (float) (java.util.concurrent.ThreadLocalRandom.current().nextDouble(0.0, 0.05) * maxLifespan);
            ind.setAge(youngAge);
        } else {
            // Gaussian distribution across adult workforce (mean = 35%, stdDev = 15%)
            double mean = 0.35 * maxLifespan;
            double stdDev = 0.15 * maxLifespan;
            double sample = mean + stdDev * java.util.concurrent.ThreadLocalRandom.current().nextGaussian();
            float age = (float) Math.max(0.0, Math.min(0.85 * maxLifespan, sample));
            ind.setAge(age);
        }
    }

    public List<Individual> createQueens(int count) {
        return createQueens(count, true);
    }

    public List<Individual> createQueens(int count, boolean applyAgeDistribution) {
        if (count <= 0) return List.of();
        List<Individual> batch = new java.util.ArrayList<>(count);
        var qChamber = tunnelNetwork != null ? tunnelNetwork.getNearestChamber(org.swarmforge.core.simulation.TunnelNetwork.ChamberType.QUEEN_CHAMBER, nestX, nestY, nestZ) : null;
        float baseSx = qChamber != null ? qChamber.x() : nestX;
        float baseSy = qChamber != null ? qChamber.y() : nestY;
        float baseSz = qChamber != null ? qChamber.z() : nestZ;

        List<org.swarmforge.core.structure.Chamber> nestChambers = (nest != null && nest.getChambers() != null) ? nest.getChambers() : List.of();
        boolean isFoundingQueen = (count == 1);

        for (int i = 0; i < count; i++) {
            float sx = baseSx, sy = baseSy, sz = baseSz;
            if (!nestChambers.isEmpty()) {
                org.swarmforge.core.structure.Chamber targetChamber = nestChambers.get(i % nestChambers.size());
                sx = targetChamber.getX();
                sy = targetChamber.getY();
                sz = targetChamber.getZ();
            }
            sx = Math.max(1.0f, Math.min(mapWidth - 1.0f, sx));
            sy = Math.max(1.0f, Math.min(mapHeight - 1.0f, sy));

            Individual ind = new Individual(this.id, Individual.Caste.QUEEN, sx, sy, sz);
            ind.setSpecies(this.species);
            ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
            if (applyAgeDistribution) {
                applyGaussianAge(ind, isFoundingQueen);
            }
            batch.add(ind);
        }
        addIndividualsBulk(batch);
        return batch;
    }

    public int calculateSubterraneanCapacity() {
        if (nest != null && nest.getChambers() != null && !nest.getChambers().isEmpty()) {
            int cap = 0;
            for (var chamber : nest.getChambers()) {
                cap += Math.max(50, (int) chamber.getCapacity());
            }
            return Math.max(500, cap);
        }
        if (tunnelNetwork != null && tunnelNetwork.getNodes() != null && !tunnelNetwork.getNodes().isEmpty()) {
            return Math.max(500, tunnelNetwork.getNodes().size() * 50);
        }
        return 500;
    }

    public List<Individual> createWorkers(int count) {
        return createWorkers(count, true);
    }

    public List<Individual> createWorkers(int count, boolean applyAgeDistribution) {
        if (count <= 0) return List.of();
        List<Individual> batch = new java.util.ArrayList<>(count);
        var brood = tunnelNetwork != null ? tunnelNetwork.getNearestChamber(org.swarmforge.core.simulation.TunnelNetwork.ChamberType.BROOD_CHAMBER, nestX, nestY, nestZ) : null;
        var food = tunnelNetwork != null ? tunnelNetwork.getNearestChamber(org.swarmforge.core.simulation.TunnelNetwork.ChamberType.FOOD_STORAGE, nestX, nestY, nestZ) : null;
        var ent = tunnelNetwork != null ? tunnelNetwork.getNearestChamber(org.swarmforge.core.simulation.TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ) : null;

        int subterraneanCap = calculateSubterraneanCapacity();
        List<org.swarmforge.core.structure.Chamber> nestChambers = (nest != null && nest.getChambers() != null) ? nest.getChambers() : List.of();

        for (int i = 0; i < count; i++) {
            float sx, sy, sz;
            Individual.Job job = Individual.Job.NURSE;

            if (i < subterraneanCap) {
                if (!nestChambers.isEmpty()) {
                    org.swarmforge.core.structure.Chamber targetChamber = nestChambers.get(i % nestChambers.size());
                    double rAngle = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                    double rDist = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0.2, 2.5);
                    sx = (float) (targetChamber.getX() + Math.cos(rAngle) * rDist);
                    sy = (float) (targetChamber.getY() + Math.sin(rAngle) * rDist);
                    sz = targetChamber.getZ();
                } else {
                    var targetNode = (i % 3 == 0 && brood != null) ? brood : ((i % 3 == 1 && food != null) ? food : ent);
                    sx = targetNode != null ? targetNode.x() : nestX;
                    sy = targetNode != null ? targetNode.y() : nestY;
                    sz = targetNode != null ? targetNode.z() : nestZ;
                }
            } else {
                // Surface Spillover for excess population exceeding nest volume
                double angle = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                double dist = java.util.concurrent.ThreadLocalRandom.current().nextDouble(2.0, 8.0);
                sx = (float) (nestX + Math.cos(angle) * dist);
                sy = (float) (nestY + Math.sin(angle) * dist);
                sz = 0f; // Surface Z
                job = Individual.Job.FORAGER; // Excess population assigned to surface foraging
            }

            // Strictly clamp coordinates to terrarium boundary
            sx = Math.max(1.0f, Math.min(mapWidth - 1.0f, sx));
            sy = Math.max(1.0f, Math.min(mapHeight - 1.0f, sy));

            Individual ind = new Individual(this.id, Individual.Caste.WORKER, sx, sy, sz);
            ind.setSpecies(this.species);
            ind.setJob(job);
            ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
            if (applyAgeDistribution) {
                applyGaussianAge(ind, false);
            }
            batch.add(ind);
        }
        addIndividualsBulk(batch);
        return batch;
    }

    public List<Individual> createSoldiers(int count) {
        return createSoldiers(count, true);
    }

    public List<Individual> createSoldiers(int count, boolean applyAgeDistribution) {
        if (count <= 0) return List.of();
        List<Individual> batch = new java.util.ArrayList<>(count);
        var ent = tunnelNetwork != null ? tunnelNetwork.getNearestChamber(org.swarmforge.core.simulation.TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ) : null;
        int subterraneanCap = calculateSubterraneanCapacity();
        List<org.swarmforge.core.structure.Chamber> nestChambers = (nest != null && nest.getChambers() != null) ? nest.getChambers() : List.of();

        for (int i = 0; i < count; i++) {
            float sx, sy, sz;
            if (i < subterraneanCap / 4 && !nestChambers.isEmpty()) {
                org.swarmforge.core.structure.Chamber targetChamber = nestChambers.get(i % nestChambers.size());
                double rAngle = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                double rDist = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0.2, 2.0);
                sx = (float) (targetChamber.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (targetChamber.getY() + Math.sin(rAngle) * rDist);
                sz = targetChamber.getZ();
            } else {
                // Surface spillover around entrance for excess soldiers
                double angle = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                double dist = java.util.concurrent.ThreadLocalRandom.current().nextDouble(1.5, 6.0);
                sx = (float) (nestX + Math.cos(angle) * dist);
                sy = (float) (nestY + Math.sin(angle) * dist);
                sz = 0f;
            }

            sx = Math.max(1.0f, Math.min(mapWidth - 1.0f, sx));
            sy = Math.max(1.0f, Math.min(mapHeight - 1.0f, sy));

            Individual ind = new Individual(this.id, Individual.Caste.SOLDIER, sx, sy, sz);
            ind.setSpecies(this.species);
            ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
            if (applyAgeDistribution) {
                applyGaussianAge(ind, false);
            }
            batch.add(ind);
        }
        addIndividualsBulk(batch);
        return batch;
    }

    public List<Individual> createMales(int count) {
        return createMales(count, true);
    }

    public List<Individual> createMales(int count, boolean applyAgeDistribution) {
        if (count <= 0) return List.of();
        List<Individual> batch = new java.util.ArrayList<>(count);
        var ent = tunnelNetwork != null ? tunnelNetwork.getNearestChamber(org.swarmforge.core.simulation.TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ) : null;
        float baseSx = ent != null ? ent.x() : nestX;
        float baseSy = ent != null ? ent.y() : nestY;
        float baseSz = ent != null ? ent.z() : nestZ;

        List<org.swarmforge.core.structure.Chamber> nestChambers = (nest != null && nest.getChambers() != null) ? nest.getChambers() : List.of();

        for (int i = 0; i < count; i++) {
            float sx = baseSx, sy = baseSy, sz = baseSz;
            if (!nestChambers.isEmpty()) {
                org.swarmforge.core.structure.Chamber targetChamber = nestChambers.get(i % nestChambers.size());
                sx = targetChamber.getX();
                sy = targetChamber.getY();
                sz = targetChamber.getZ();
            }
            sx = Math.max(1.0f, Math.min(mapWidth - 1.0f, sx));
            sy = Math.max(1.0f, Math.min(mapHeight - 1.0f, sy));

            Individual ind = new Individual(this.id, Individual.Caste.MALE, sx, sy, sz);
            ind.setSpecies(this.species);
            ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
            if (applyAgeDistribution) {
                applyGaussianAge(ind, false);
            }
            batch.add(ind);
        }
        addIndividualsBulk(batch);
        return batch;
    }

    public List<Individual> createBrood(int count) {
        return createBrood(count, true);
    }

    public List<Individual> createBrood(int count, boolean applyAgeDistribution) {
        if (count <= 0) return List.of();
        List<Individual> batch = new java.util.ArrayList<>(count);
        var broodChamber = tunnelNetwork != null ? tunnelNetwork.getNearestChamber(org.swarmforge.core.simulation.TunnelNetwork.ChamberType.BROOD_CHAMBER, nestX, nestY, nestZ) : null;
        float baseSx = broodChamber != null ? broodChamber.x() : nestX;
        float baseSy = broodChamber != null ? broodChamber.y() : nestY;
        float baseSz = broodChamber != null ? broodChamber.z() : nestZ;

        List<org.swarmforge.core.structure.Chamber> nestChambers = (nest != null && nest.getChambers() != null) ? nest.getChambers() : List.of();

        for (int i = 0; i < count; i++) {
            float sx = baseSx, sy = baseSy, sz = baseSz;
            if (!nestChambers.isEmpty()) {
                org.swarmforge.core.structure.Chamber targetChamber = nestChambers.get(i % nestChambers.size());
                double rAngle = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
                double rDist = java.util.concurrent.ThreadLocalRandom.current().nextDouble(0.1, 1.5);
                sx = (float) (targetChamber.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (targetChamber.getY() + Math.sin(rAngle) * rDist);
                sz = targetChamber.getZ();
            }

            sx = Math.max(1.0f, Math.min(mapWidth - 1.0f, sx));
            sy = Math.max(1.0f, Math.min(mapHeight - 1.0f, sy));

            Individual ind = new Individual(this.id, Individual.Caste.WORKER, sx, sy, sz);
            ind.setSpecies(this.species);
            ind.setJob(Individual.Job.NONE);

            // Brood breakdown: 35% Eggs, 40% Larvae, 25% Pupae
            double r = java.util.concurrent.ThreadLocalRandom.current().nextDouble();
            if (r < 0.35) {
                ind.setLifeStage(Individual.LifeStage.EGG);
                ind.setMaturationThreshold(2000f);
                if (applyAgeDistribution) {
                    ind.setAge((float) (java.util.concurrent.ThreadLocalRandom.current().nextDouble(0, 1900)));
                }
            } else if (r < 0.75) {
                ind.setLifeStage(Individual.LifeStage.LARVA);
                ind.setMaturationThreshold(4000f);
                if (applyAgeDistribution) {
                    ind.setAge((float) (2000f + java.util.concurrent.ThreadLocalRandom.current().nextDouble(0, 3800)));
                }
            } else {
                ind.setLifeStage(Individual.LifeStage.PUPA);
                ind.setMaturationThreshold(6000f);
                if (applyAgeDistribution) {
                    ind.setAge((float) (4000f + java.util.concurrent.ThreadLocalRandom.current().nextDouble(0, 1900)));
                }
            }
            batch.add(ind);
        }
        addIndividualsBulk(batch);
        return batch;
    }

    /**
     * Spawns a unit based on a specific CasteTemplate.
     * Checks resource costs before spawning.
     */
    public Individual spawnUnit(CasteTemplate template) {
        if (proteinStored >= template.getProteinCost() &&
                carbohydrateStored >= template.getCarbohydrateCost() &&
                waterStored >= template.getWaterCost()) {

            proteinStored -= template.getProteinCost();
            carbohydrateStored -= template.getCarbohydrateCost();
            waterStored -= template.getWaterCost();

            Individual ind = new Individual(this.id, template, nestX, nestY, nestZ);
            ind.setSpecies(this.species);
            ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
            addIndividual(ind);
            return ind;
        }
        return null;
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
    private final List<ColonyListener> listeners = new CopyOnWriteArrayList<>();

    public void addListener(ColonyListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ColonyListener listener) {
        listeners.remove(listener);
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
        individuals.add(individual);
        totalBorn++;
        for (ColonyListener l : listeners) {
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
            for (ColonyListener l : listeners) {
                l.onDeath(this, ind);
            }
        }
        return dead.size();
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

    // Getters
    public UUID getId() {
        return id;
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

    // === Resource Management ===

    /**
     * Get specific resource amount.
     */
    public float getResourceAmount(ResourceType type) {
        return resources.getOrDefault(type, 0f);
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

    public void setFoodStored(float food) {
        resources.clear();
        resources.put(ResourceType.SEED, Math.max(0, food));
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

    private int queenSpermathecaSperm = 50000; // Finite sperm count in queen's spermatheca

    public int getQueenSpermathecaSperm() {
        return queenSpermathecaSperm;
    }

    /**
     * Process internal colony mechanics (Fungus Garden cultivation, Queen egg-laying based on protein storage & spermatheca reserves).
     */
    public void tick() {
        // 1. Process Fungus Garden if present
        if (fungusGarden != null) {
            fungusGarden.tick();
        }

        // 2. Queen Oviposition & Haplodiploid Determination
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
}
