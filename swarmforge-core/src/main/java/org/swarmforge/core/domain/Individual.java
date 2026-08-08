/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.UUID;
import org.swarmforge.core.behavior.ReasoningArchitecture.Action;
import org.swarmforge.core.behavior.ReasoningArchitecture.ActionResult;
import org.swarmforge.core.genetics.Genome;

import org.swarmforge.core.behavior.AgentView;

/**
 * Represents an individual eusocial insect in the simulation.
 * Uses a sealed interface for type-safe caste representation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class Individual implements java.io.Serializable, AgentView {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final Caste caste;
    private final UUID colonyId;

    // Position
    private float x, y, z;
    private float heading; // Direction in radians

    // State
    private float health = 100f;
    private float maxEnergy = 100f;
    private float energy = 100f;
    private float age; // In simulation ticks
    private boolean alive = true;

    // Needs
    private float hunger;
    private float thirst;
    private float fatigue;

    // Carrying
    private CarriedItem carriedItem = CarriedItem.NONE;

    // Home position (nest)
    private float homeX, homeY, homeZ;
    // Life Cycle
    private LifeStage lifeStage = LifeStage.ADULT; // Default for now
    private Job job = Job.IDLE; // Default
    private float maturationThreshold = 2000f; // Ticks between stages

    // Tree Climbing & Vegetation Interactivity
    private boolean climbingTree = false;
    private float treeClimbHeight = 0.0f;

    // Brain
    private org.swarmforge.core.behavior.ReasoningArchitecture brain;

    // Personality & Genetics
    private Personality personality;
    private Genome genome;
    private org.swarmforge.core.genetics.HaplodiploidGenome haplodiploidGenome;
    private org.swarmforge.core.species.Species species;

    // Memory optimization: Removed per-instance Random
    private AiState state = AiState.IDLE;
    private ResourceType carriedResourceType = null;

    // Combat Stats
    private float maxHealth = 100f;
    private float attackDamage = 5f;
    private float defense = 0f;

    // Cuticular Hydrocarbon (CHC) Gestalt Odor Profile
    private float[] chcProfile = org.swarmforge.core.simulation.CuticularHydrocarbonSystem.generateColonyProfile();

    public float[] getChcProfile() {
        return chcProfile;
    }

    public boolean isClimbingTree() {
        return climbingTree;
    }

    public float getTreeClimbHeight() {
        return treeClimbHeight;
    }

    public void climbTree(float targetHeight) {
        this.climbingTree = true;
        this.treeClimbHeight = Math.max(0.0f, targetHeight);
        this.z = Math.max(this.z, this.treeClimbHeight);
    }

    public void descendTree() {
        this.climbingTree = false;
        this.treeClimbHeight = 0.0f;
        this.z = 0.0f;
    }

    public boolean harvestPlant(org.swarmforge.core.world.VegetationSystem.Plant plant) {
        if (plant == null || !alive) return false;
        if (plant.type == org.swarmforge.core.world.VegetationSystem.PlantType.GRASS ||
            plant.type == org.swarmforge.core.world.VegetationSystem.PlantType.FLOWER) {
            float seeds = plant.harvestSeeds(0.5f);
            if (seeds > 0) {
                setCarriedItem(CarriedItem.FOOD);
                setCarriedResourceType(ResourceType.SEED);
                return true;
            }
        } else if (plant.type == org.swarmforge.core.world.VegetationSystem.PlantType.TREE ||
                   plant.type == org.swarmforge.core.world.VegetationSystem.PlantType.SHRUB) {
            float foliage = plant.harvestFoliage(0.5f);
            if (foliage > 0) {
                climbTree(plant.getCurrentHeight() * 0.5f);
                setCarriedItem(CarriedItem.FOOD);
                setCarriedResourceType(ResourceType.LEAF);
                return true;
            }
        }
        return false;
    }

    public void setChcProfile(float[] profile) {
        this.chcProfile = profile;
    }

    // Disease Status - Handled by DiseaseManager externally

    /**
     * AI States for the FSM.
     */
    public enum AiState {
        IDLE,
        WANDER,
        FORAGE,
        RETURN_HOME,
        FLEE,
        TEND_BROOD,
        PATROL,
        DIG
    }

    /**
     * Caste types for eusocial insects.
     */
    public enum Caste {
        QUEEN,
        MALE,
        WORKER,
        SOLDIER,
        NURSE,
        FORAGER
    }

    /**
     * Items that can be carried.
     */
    public enum CarriedItem {
        NONE,
        FOOD,
        WATER,
        EARTH,
        BROOD,
        DEAD_ANT
    }

    /**
     * Life stages of an ant.
     */
    public enum LifeStage {
        EGG,
        LARVA,
        PUPA,
        ADULT
    }

    /**
     * Jobs for adult ants.
     */
    public enum Job {
        NONE, // For immature stages
        NURSE,
        BUILDER,
        FORAGER,
        GUARD,
        IDLE // Added for compatibility with tests
    }

    private CasteTemplate casteTemplate;

    public Individual(UUID colonyId, Caste caste, float x, float y, float z) {
        this.id = UUID.randomUUID();
        this.colonyId = colonyId;
        this.caste = caste;
        this.x = x;
        this.y = y;
        this.z = z;
        this.alive = true;
        this.health = 100f;
        this.energy = 100f;
    }

    public Individual(UUID colonyId, CasteTemplate template, float x, float y, float z) {
        this(colonyId, Caste.WORKER, x, y, z); // Default to WORKER enum for now
        this.casteTemplate = template;
        this.maxHealth = template.getBaseHealth();
        this.attackDamage = template.getBaseDamage();
        this.defense = template.getBaseDefense();
        this.health = this.maxHealth;
    }

    public CasteTemplate getCasteTemplate() {
        return casteTemplate;
    }

    public void setCasteTemplate(CasteTemplate casteTemplate) {
        this.casteTemplate = casteTemplate;
        if (casteTemplate != null) {
            this.maxHealth = casteTemplate.getBaseHealth();
            this.attackDamage = casteTemplate.getBaseDamage();
            this.defense = casteTemplate.getBaseDefense();
        }
    }

    // Getter for hunger needed by brain
    public float getHunger() {
        return hunger;
    }

    public float getThirst() {
        return thirst;
    }

    public float getFatigue() {
        return fatigue;
    }

    /**
     * Update position based on heading and speed.
     */
    public void move(float speed) {
        float effectiveSpeed = speed;
        if (genome != null) {
            effectiveSpeed *= genome.getSpeedMultiplier();
        }
        x += Math.cos(heading) * effectiveSpeed;
        y += Math.sin(heading) * effectiveSpeed;
    }

    /**
     * Turn towards a target heading.
     */
    public void turnTowards(float targetHeading, float turnRate) {
        float diff = targetHeading - heading;
        // Normalize to -PI to PI
        while (diff > Math.PI)
            diff -= 2 * Math.PI;
        while (diff < -Math.PI)
            diff += 2 * Math.PI;
        heading += Math.signum(diff) * Math.min(Math.abs(diff), turnRate);
    }

    /**
     * Increment age by 1 tick.
     */
    public void incrementAge() {
        age++;
    }

    /**
     * Consume energy and update needs.
     */
    public void tick() {
        if (!alive)
            return;
        age++;

        float metabolism = species != null ? species.getMetabolism() : 1.0f;
        if (genome != null) {
            metabolism *= genome.getMetabolismRate();
        }

        energy -= 0.1f * metabolism;
        hunger += 0.05f * metabolism;
        thirst += 0.03f * metabolism;

        // 1. Starvation & Dehydration Mortality (Hunger = 100 or Energy = 0)
        if (energy <= 0 || hunger >= 100 || thirst >= 100) {
            die(); // Mortality from starvation / exhaustion
            return;
        }

        // 2. Biological Aging Mortality Across Castes
        float maxLifespan = 7500f; // Default worker lifespan
        if (species != null) {
            maxLifespan = switch (caste) {
                case QUEEN -> species.getQueenLifespan();
                case SOLDIER -> species.getWorkerLifespan() * 1.5f;
                case MALE -> species.getWorkerLifespan() * 0.4f;
                case WORKER, FORAGER, NURSE -> species.getWorkerLifespan();
            };
        } else {
            maxLifespan = switch (caste) {
                case QUEEN -> 50000f;
                case SOLDIER -> 10000f;
                case MALE -> 3000f;
                case WORKER, FORAGER, NURSE -> 7500f;
            };
        }

        if (age >= maxLifespan) {
            die(); // Mortality from old age
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getColonyId() {
        return colonyId;
    }

    public Caste getCaste() {
        return caste;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getHeading() {
        return heading;
    }

    public float getHealth() {
        return health;
    }

    public float getEnergy() {
        return energy;
    }

    public float getAge() {
        return age;
    }

    public boolean isAlive() {
        return alive;
    }

    public CarriedItem getCarriedItem() {
        return carriedItem;
    }

    public float getHomeX() {
        return homeX;
    }

    public float getHomeY() {
        return homeY;
    }

    public float getHomeZ() {
        return homeZ;
    }

    public void setHomePosition(float homeX, float homeY, float homeZ) {
        this.homeX = homeX;
        this.homeY = homeY;
        this.homeZ = homeZ;
    }
    
    @Override
    public boolean isAtNest() {
        return (Math.abs(x - homeX) < 2.0 && Math.abs(y - homeY) < 2.0);
    }

    @Override
    public float getEnergyLevel() {
        return (float) (energy / maxEnergy);
    }

    @Override
    public boolean isSoldier() {
        return caste == Caste.SOLDIER;
    }

    @Override
    public java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        if (species != null) {
            return species.getForagingTypes();
        }
        return java.util.Set.of(ResourceType.SEED);
    }

    @Override
    public String getAgentId() {
        return id.toString();
    }

    public boolean isCarryingFood() {
        return carriedItem == CarriedItem.FOOD;
    }

    // Setters
    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setHeading(float heading) {
        this.heading = heading;
    }

    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(100, health));
        if (this.health <= 0) {
            this.alive = false;
        }
    }

    public void setEnergy(float energy) {
        this.energy = Math.max(0, Math.min(maxEnergy, energy));
    }

    public void setMaxEnergy(float maxEnergy) {
        this.maxEnergy = maxEnergy;
    }

    public void setHunger(float hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger));
    }

    public void setCarriedItem(CarriedItem item) {
        this.carriedItem = item;
        if (item == CarriedItem.NONE) {
            this.carriedResourceType = null;
        }
    }

    public ResourceType getCarriedResourceType() {
        return carriedResourceType;
    }

    public void setCarriedResourceType(ResourceType type) {
        this.carriedResourceType = type;
    }

    public org.swarmforge.core.behavior.ReasoningArchitecture getBrain() {
        return brain;
    }

    public void setBrain(org.swarmforge.core.behavior.ReasoningArchitecture brain) {
        this.brain = brain;
    }

    public void dropFood() {
        if (carriedItem == CarriedItem.FOOD) {
            carriedItem = CarriedItem.NONE;
            // Logic for actually placing food in world should be handled by Simulation
            // event or caller
        }
    }

    public void setReasoningArchitecture(org.swarmforge.core.behavior.ReasoningArchitecture reasoningArchitecture) {
        this.brain = reasoningArchitecture;
    }

    public org.swarmforge.core.behavior.ReasoningArchitecture getReasoningArchitecture() {
        return brain;
    }

    public AiState getState() {
        return state;
    }

    public void setState(AiState state) {
        this.state = state;
    }

    public java.util.Random getRandom() {
        return java.util.concurrent.ThreadLocalRandom.current();
    }

    public org.swarmforge.core.species.Species getSpecies() {
        return species;
    }

    public void setSpecies(org.swarmforge.core.species.Species species) {
        this.species = species;
    }

    public LifeStage getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(LifeStage lifeStage) {
        this.lifeStage = lifeStage;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public float getMaturationThreshold() {
        return maturationThreshold;
    }

    public void setMaturationThreshold(float maturationThreshold) {
        this.maturationThreshold = maturationThreshold;
    }

    public void setAttackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
    }

    public void setDefense(float defense) {
        this.defense = defense;
    }

    /**
     * Apply damage to this individual.
     * 
     * @param amount Damage amount to apply
     * @return true if the individual died from this damage
     */
    public boolean takeDamage(float amount) {
        if (!alive)
            return false;
        health -= amount;
        if (health <= 0) {
            health = 0;
            alive = false;
            return true;
        }
        return false;
    }

    /**
     * Heal this individual.
     * 
     * @param amount Amount to heal
     */
    public void heal(float amount) {
        if (!alive)
            return;
        health = Math.min(100f, health + amount);
    }

    /**
     * Execute an action decided by the brain.
     * 
     * @param action The action to execute
     * @param colony The colony (for depositing resources)
     * @return Result of the action
     */
    public ActionResult executeAction(Action action, org.swarmforge.core.domain.Colony colony) {
        if (!alive)
            return ActionResult.failure("Dead");

        switch (action.type()) {
            case MOVE -> {
                // Directional move
                this.x += action.directionX() * action.intensity();
                this.y += action.directionY() * action.intensity();
                // update heading
                if (action.intensity() > 0) {
                    this.heading = (float) Math.atan2(action.directionY(), action.directionX());
                }
                return ActionResult.ok();
            }
            case FORAGE -> {
                return ActionResult.ok(); // Intent registered
            }
            case RETURN_HOME -> {
                turnTowards(getHomeX(), getHomeY(), 0.1f);
                move(1.0f);
                return ActionResult.ok();
            }
            case REST -> {
                energy = Math.min(100, energy + 0.5f);
                return ActionResult.ok();
            }
            case ATTACK -> {
                if (action.target() instanceof Individual target) {
                    // Diplomacy Check
                    if (colony.getDiplomacy().isAlly(target.getColonyId())) {
                        return ActionResult.failure("Target is an Ally!");
                    }

                    // Range Check
                    float dx = target.getX() - x;
                    float dy = target.getY() - y;
                    float dz = target.getZ() - z;
                    float distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq < 4.0f) { // Range 2.0
                        target.takeDamage(this.attackDamage, this);
                        return ActionResult.ok();
                    }
                    return ActionResult.failure("Target out of range");
                }
                return ActionResult.failure("Invalid Target");
            }
            case FOLLOW_TRAIL -> {
                this.x += action.directionX() * action.intensity();
                this.y += action.directionY() * action.intensity();
                return ActionResult.ok();
            }
            case DEPOSIT_FOOD -> {
                if (isCarryingFood() && colony != null) {
                    if (carriedResourceType != null) {
                        colony.addResource(carriedResourceType, 1.0f);
                    } else {
                        // Default to SEED if unspecified
                        colony.addResource(ResourceType.SEED, 1.0f);
                    }
                    setCarriedItem(CarriedItem.NONE);
                    return ActionResult.ok();
                }
                return ActionResult.failure("Not carrying food");
            }
            default -> {
                return ActionResult.ok();
            }
        }
    }

    private void turnTowards(float targetX, float targetY, float rate) {
        float dx = targetX - x;
        float dy = targetY - y;
        float targetHeading = (float) Math.atan2(dy, dx);
        turnTowards(targetHeading, rate);
    }

    // === Personality & Genetics ===

    public Personality getPersonality() {
        return personality;
    }

    public void setPersonality(Personality p) {
        this.personality = p;
    }

    public Genome getGenome() {
        return genome;
    }

    public void setGenome(Genome g) {
        this.genome = g;
    }

    public org.swarmforge.core.genetics.HaplodiploidGenome getHaplodiploidGenome() {
        return haplodiploidGenome;
    }

    public void setHaplodiploidGenome(org.swarmforge.core.genetics.HaplodiploidGenome g) {
        this.haplodiploidGenome = g;
    }

    /**
     * Initialize genetics for this individual (call after construction).
     */
    /**
     * Initialize genetics for this individual (call after construction).
     */
    public void initializeGenetics(java.util.Random rng) {
        this.genome = new Genome();
        genome.mutate(0.1f); // Initial variation
        // Personality derived from genome? For now keep separate or link them later.
        // this.personality = new Personality(genome);
    }

    /**
     * Create offspring genetics from two parents.
     */
    public void inheritGenetics(Individual parent1, Individual parent2, java.util.Random rng) {
        if (parent1.genome != null && parent2.genome != null) {
            this.genome = Genome.crossover(parent1.genome, parent2.genome);
            this.genome.mutate(0.05f); // Small mutation chance
        } else {
            initializeGenetics(rng);
        }
    }

    public void update(org.swarmforge.core.structure.ConstructionManager constructionManager) {
        if (!alive || lifeStage != LifeStage.ADULT)
            return;

        jobUpdate(constructionManager);

        // Movement
        move(1.0f); // Default speed

        // Energy consumption
        tick();
    }

    private void jobUpdate(org.swarmforge.core.structure.ConstructionManager constructionManager) {
        if (job == Job.BUILDER) {
            java.util.Optional<org.swarmforge.core.structure.ConstructionTask> taskOpt = constructionManager
                    .getAvailableTask(x, y, z);
            if (taskOpt.isPresent()) {
                org.swarmforge.core.structure.ConstructionTask task = taskOpt.get();
                if (!task.isAssigned()) {
                    task.setAssigned(true);
                    // Move to task
                    float dx = task.getX() - x;
                    float dy = task.getY() - y;
                    float dz = task.getZ() - z;
                    float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

                    if (dist < 1.0f) {
                        task.work(1.0f); // Work on task
                    } else {
                        // Move towards
                        this.heading = (float) Math.atan2(dy, dx);
                        // Z movement? For now 2D movement logic only
                    }
                }
            }
        }
    }

    // Combat Logic
    public void takeDamage(float amount, Individual attacker) {
        float effectiveDamage = Math.max(0, amount - this.defense);
        this.health -= effectiveDamage;
        if (this.health <= 0) {
            die();
        }
    }

    public void die() {
        this.alive = false;
        // Optional: clear references, etc.
    }

    public float getAttackDamage() {
        return attackDamage;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }

    // === Environmental Sensory Perception (World & Weather Sampling) ===

    /**
     * Samples the local voxel terrarium and atmospheric weather using the species' sensory parameters.
     */
    public SensorySample sampleSensoryEnvironment(Terrarium terrarium, org.swarmforge.core.world.WeatherSystem weather) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        int iz = (int) Math.floor(z);

        TerrariumCell currentCell = terrarium != null ? terrarium.getCell(ix, iy, iz) : null;

        float feltTemp = currentCell != null ? currentCell.temperature() : 20.0f;
        float feltHumidity = currentCell != null ? currentCell.humidity() : 0.5f;
        float feltCo2Ppm = currentCell != null ? currentCell.co2() : 400.0f;
        float feltLightLux = currentCell != null ? currentCell.light() * 1000.0f : 500.0f;

        // Magnetoreception
        float geomagIntensity = (weather != null && species != null && species.hasMagnetoreception())
                ? weather.getMagneticField() : 0.0f;

        // Substrate Vibration
        float vibrationDb = (species != null && species.hasSubstrateVibrationSensing()) ? 12.0f : 0.0f;

        // Electroception
        float electricFieldVolts = (weather != null && species != null && species.hasElectrosensing())
                ? (weather.getWeatherState() == org.swarmforge.core.world.WeatherMarkovChain.WeatherState.THUNDERSTORM ? 300.0f : 20.0f) : 0.0f;

        // Polarized UV Compass Angle
        float solarAzimuthRad = (weather != null && species != null && species.hasPolarizedLightNavigation())
                ? (float) Math.toRadians(weather.getSunAngle() * 15.0f) : heading;

        return new SensorySample(feltTemp, feltHumidity, feltCo2Ppm, feltLightLux, geomagIntensity, vibrationDb, electricFieldVolts, solarAzimuthRad);
    }

    public record SensorySample(
        float temperatureCelsius,
        float humidityRelative,
        float co2Ppm,
        float lightLux,
        float geomagIntensityMicrotesla,
        float vibrationDb,
        float electricFieldVoltsPerMeter,
        float solarAzimuthRad
    ) {}
}
