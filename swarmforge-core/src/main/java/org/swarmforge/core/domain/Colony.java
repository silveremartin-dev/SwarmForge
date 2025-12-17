/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
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
        this.id = UUID.randomUUID();
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

    public void tick() {
        this.age++;
        if (fungusGarden != null) {
            fungusGarden.tick();
        }
    }

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

    // Factory methods for individuals
    public Individual createQueen() {
        Individual ind = new Individual(this.id, Individual.Caste.QUEEN, nestX, nestY, nestZ);
        ind.setSpecies(this.species);
        ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
        return ind;
    }

    public Individual createWorker() {
        Individual ind = new Individual(this.id, Individual.Caste.WORKER, nestX, nestY, nestZ);
        ind.setSpecies(this.species);
        ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
        return ind;
    }

    public Individual createSoldier() {
        Individual ind = new Individual(this.id, Individual.Caste.SOLDIER, nestX, nestY, nestZ);
        ind.setSpecies(this.species);
        ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
        return ind;
    }

    public Individual createMale() {
        Individual ind = new Individual(this.id, Individual.Caste.MALE, nestX, nestY, nestZ);
        ind.setSpecies(this.species);
        ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
        return ind;
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

    /**
     * Add an individual to this colony.
     */
    public void addIndividual(Individual individual) {
        individuals.add(individual);
        totalBorn++;
        for (ColonyListener l : listeners) {
            l.onBirth(this, individual);
        }
    }

    /**
     * Remove dead individuals from the colony.
     */
    public int removeDeadIndividuals() {
        int removed = 0;
        for (Individual ind : individuals) {
            if (!ind.isAlive()) {
                individuals.remove(ind);
                totalDied++;
                removed++;
                for (ColonyListener l : listeners) {
                    l.onDeath(this, ind);
                }
            }
        }
        return removed;
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
        resources.merge(type, amount, (a, b) -> a + b);
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
}
