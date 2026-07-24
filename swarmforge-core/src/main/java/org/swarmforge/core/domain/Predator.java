/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.UUID;

/**
 * Base class for all predator entities in the simulation.
 * Predators hunt ants and pose threats to colonies.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class Predator {

    /**
     * Predator hunting states.
     */
    public enum HuntingState {
        IDLE, // Resting, not hunting
        STALKING, // Moving toward prey
        CHASING, // Actively pursuing
        ATTACKING, // In combat
        EATING, // Consuming prey
        FLEEING, // Running from soldiers
        DEAD // No longer active
    }

    private final UUID id;
    private final PredatorType type;

    // Position
    private float x, y, z;
    private float heading;

    // State
    private HuntingState state = HuntingState.IDLE;
    private float health;
    private float maxHealth;
    private float hunger = 0f;
    private float energy = 100f;
    private int age = 0;
    private boolean alive = true;

    // Hunting
    private Individual currentTarget;
    private int attackCooldown = 0;
    private int killCount = 0;

    // Trap (for AMBUSH/TRAP predators)
    private boolean trapBuilt = false;
    private float trapX, trapY, trapZ;
    private float trapRadius = 3f;

    public Predator(PredatorType type, float x, float y, float z) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.maxHealth = getBaseHealth();
        this.health = maxHealth;
    }

    private float getBaseHealth() {
        return switch (type) {
            case SPIDER -> 50f;
            case ANTLION -> 40f;
            case BEETLE -> 60f;
            case BIRD -> 80f;
            case LIZARD -> 150f;
            case WASP -> 40f;
        };
    }

    /**
     * Process one simulation tick.
     */
    public void tick() {
        if (!alive)
            return;

        age++;

        // Natural energy drain
        energy -= 0.02f;

        // Hunger increases over time
        hunger += 0.05f;

        // Starve if too hungry
        if (hunger >= 100f) {
            health -= 0.5f;
        }

        // Die from health loss
        if (health <= 0) {
            die();
        }

        // Cooldown management
        if (attackCooldown > 0)
            attackCooldown--;
    }

    /**
     * Move toward a target position.
     */
    public void moveToward(float targetX, float targetY, float speed) {
        float dx = targetX - x;
        float dy = targetY - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > 0.1f) {
            x += (dx / dist) * speed * type.getBaseSpeed();
            y += (dy / dist) * speed * type.getBaseSpeed();
            heading = (float) Math.atan2(dy, dx);
        }
    }

    /**
     * Attack a target individual.
     * 
     * @return true if target was killed
     */
    public boolean attack(Individual target) {
        if (target == null || !target.isAlive())
            return false;
        if (attackCooldown > 0)
            return false;

        float damage = type.getBaseDamage();
        boolean killed = target.takeDamage(damage);

        attackCooldown = 30; // Cooldown between attacks

        if (killed) {
            killCount++;
            hunger = Math.max(0, hunger - 30f); // Eating reduces hunger
            energy = Math.min(100f, energy + 20f);
            state = HuntingState.EATING;
        }

        return killed;
    }

    /**
     * Take damage from ant soldiers.
     */
    public void takeDamage(float amount) {
        health -= amount;
        if (health <= 0) {
            health = 0;
            alive = false;
            state = HuntingState.DEAD;
        } else if (health < maxHealth * 0.3f) {
            // Flee when low health
            state = HuntingState.FLEEING;
        }
    }

    /**
     * Build a trap at current location (for AMBUSH/TRAP predators).
     */
    public void buildTrap() {
        if (type.getHuntingStyle() == PredatorType.HuntingStyle.TRAP ||
                type.getHuntingStyle() == PredatorType.HuntingStyle.AMBUSH) {
            trapBuilt = true;
            trapX = x;
            trapY = y;
            trapZ = z;
        }
    }

    /**
     * Check if an ant is caught in this predator's trap.
     */
    public boolean isInTrap(Individual ant) {
        if (!trapBuilt)
            return false;

        float dx = ant.getX() - trapX;
        float dy = ant.getY() - trapY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        return dist <= trapRadius && Math.random() < type.getTrapChance();
    }

    /**
     * Calculate distance to an ant.
     */
    public float distanceTo(Individual ant) {
        float dx = ant.getX() - x;
        float dy = ant.getY() - y;
        float dz = ant.getZ() - z;
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    /**
     * Check if ant is within vision range.
     */
    public boolean canSee(Individual ant) {
        return distanceTo(ant) <= type.getVisionRange();
    }

    // === Getters and Setters ===

    public UUID getId() {
        return id;
    }

    public PredatorType getType() {
        return type;
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

    public HuntingState getState() {
        return state;
    }

    public float getHealth() {
        return health;
    }

    public float getMaxHealth() {
        return maxHealth;
    }

    public float getHunger() {
        return hunger;
    }

    public float getEnergy() {
        return energy;
    }

    public int getAge() {
        return age;
    }

    private void die() {
        this.alive = false;
        this.health = 0;
        this.state = HuntingState.DEAD;
    }

    public boolean isAlive() {
        return alive;
    }

    public Individual getCurrentTarget() {
        return currentTarget;
    }

    public int getKillCount() {
        return killCount;
    }

    public boolean isTrapBuilt() {
        return trapBuilt;
    }

    public float getTrapZ() {
        return trapZ;
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setState(HuntingState state) {
        this.state = state;
    }

    public void setCurrentTarget(Individual target) {
        this.currentTarget = target;
    }

    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(maxHealth, health));
    }

    public void setHunger(float hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger));
    }
}
