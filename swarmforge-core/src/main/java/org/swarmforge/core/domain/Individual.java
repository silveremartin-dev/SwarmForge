/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
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

    // Brain
    private org.swarmforge.core.behavior.ReasoningArchitecture brain;

    // Personality & Genetics
    private Personality personality;
    private Genome genome;
    private org.swarmforge.core.species.Species species;

    // Memory optimization: Removed per-instance Random
    private AiState state = AiState.IDLE;
    private ResourceType carriedResourceType = null;

    // Combat Stats
    private float maxHealth = 100f;
    private float attackDamage = 5f;
    private float defense = 0f;

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

        if (energy <= 0 || hunger >= 100 || thirst >= 100) {
            alive = false; // Starved/Dehydrated
        }

        if (species != null) {
            if (caste == Caste.WORKER && age > species.getWorkerLifespan()) {
                alive = false; // Old age
            } else if (caste == Caste.QUEEN && age > species.getQueenLifespan()) {
                alive = false;
            }
        }

        // Disease logic moved to external DiseaseManager handling
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

    public void setAttackDamage(float attackDamage) {
        this.attackDamage = attackDamage;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(float maxHealth) {
        this.maxHealth = maxHealth;
    }
}
