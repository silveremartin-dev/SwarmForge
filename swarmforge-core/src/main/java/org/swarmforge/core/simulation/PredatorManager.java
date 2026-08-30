/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Predator;
import org.swarmforge.core.domain.BossPredator;
import org.swarmforge.core.domain.PredatorType;
import org.swarmforge.core.event.SimulationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages all predators in the simulation.
 * Handles spawning, AI updates, and predator-ant interactions.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class PredatorManager {

    private final Simulation simulation;
    private final java.util.Set<Predator> predators = java.util.concurrent.ConcurrentHashMap.newKeySet();
    private final Random random = new Random();

    // Configuration
    private int maxPredators = 10;
    private int spawnCooldown = 0;
    private int spawnInterval = 1000; // Ticks between spawn attempts
    private float spawnChance = 0.3f;
    private int suppressionTicksRemaining = 0;

    // Statistics
    private int totalSpawned = 0;
    private int totalKills = 0;
    private int predatorsKilled = 0;

    // Strategies
    private final org.swarmforge.core.ecology.ai.HuntingStrategy ambushStrategy = new org.swarmforge.core.ecology.ai.AmbushStrategy();
    private final org.swarmforge.core.ecology.ai.HuntingStrategy trapStrategy = new org.swarmforge.core.ecology.ai.TrapStrategy();
    private final org.swarmforge.core.ecology.ai.HuntingStrategy chaseStrategy = new org.swarmforge.core.ecology.ai.ChaseStrategy();
    private final org.swarmforge.core.ecology.ai.HuntingStrategy swoopStrategy = new org.swarmforge.core.ecology.ai.SwoopStrategy();

    public PredatorManager(Simulation simulation) {
        this.simulation = simulation;
    }

    /**
     * Activates predator suppression / sanctuary protection for the given duration in ticks.
     * Clears existing predators and prevents new predator spawns.
     *
     * @param durationTicks Duration of protection in ticks
     */
    public void suppressPredators(int durationTicks) {
        this.suppressionTicksRemaining = Math.max(this.suppressionTicksRemaining, durationTicks);
        clearPredators();
        if (simulation != null) {
            simulation.queueEvent(new SimulationEvent(
                    SimulationEvent.EventType.INFO,
                    simulation.getTickCount(),
                    "🛡️ Predator Suppression Active: Sanctuary protection established (" + Math.max(1, durationTicks / 60) + " min)."));
        }
    }

    public boolean isSuppressionActive() {
        return suppressionTicksRemaining > 0;
    }

    public int getSuppressionTicksRemaining() {
        return suppressionTicksRemaining;
    }

    /**
     * Process all predators for one tick.
     */
    public void tick() {
        tick(0.016666667f);
    }

    public void tick(float deltaSeconds) {
        // Handle predator suppression & sanctuary protection
        if (suppressionTicksRemaining > 0) {
            suppressionTicksRemaining--;
            if (!predators.isEmpty()) {
                clearPredators();
            }
        } else {
            // Handle spawning
            if (spawnCooldown > 0) {
                spawnCooldown--;
            } else {
                // Tiny chance for Boss
                if (random.nextFloat() < 0.001f) { // 0.1% chance per spawn interval
                    spawnBoss(BossPredator.BossType.values()[random.nextInt(BossPredator.BossType.values().length)]);
                } else {
                    attemptSpawn();
                }
                spawnCooldown = spawnInterval;
            }
        }

        // Update each predator
        List<Predator> dead = new ArrayList<>();

        for (Predator predator : predators) {
            predator.tick(deltaSeconds);

            if (!predator.isAlive()) {
                dead.add(predator);
                continue;
            }

            updatePredatorAI(predator);

            // Enforce terrarium boundary clamping to prevent predator escape or out-of-bounds positioning
            if (simulation != null && simulation.getTerrarium() != null) {
                var terrarium = simulation.getTerrarium();
                float maxX = terrarium.getWidth() - 1.0f;
                float maxY = terrarium.getHeight() - 1.0f;
                float maxZ = terrarium.getDepth() - 1.0f;
                float cx = Math.max(0.0f, Math.min(maxX, predator.getX()));
                float cy = Math.max(0.0f, Math.min(maxY, predator.getY()));
                float cz = Math.max(0.0f, Math.min(maxZ, predator.getZ()));
                if (cx != predator.getX() || cy != predator.getY() || cz != predator.getZ()) {
                    predator.setPosition(cx, cy, cz);
                }
            }
        }

        // Remove dead predators
        for (Predator p : dead) {
            predators.remove(p);
            predatorsKilled++;
        }
    }

    public void removePredator(Predator predator) {
        if (predators.remove(predator)) {
            predatorsKilled++;
            simulation.getEventQueue().offer(new SimulationEvent(
                    SimulationEvent.EventType.DEATH,
                    simulation.getTickCount(),
                    "Predator killed by colony!"));
        }
    }

    /**
     * Update predator AI based on hunting style.
     */
    private void updatePredatorAI(Predator predator) {
        switch (predator.getType().getHuntingStyle()) {
            case AMBUSH -> ambushStrategy.update(predator, simulation);
            case TRAP -> trapStrategy.update(predator, simulation);
            case CHASE -> chaseStrategy.update(predator, simulation);
            case SWOOP -> swoopStrategy.update(predator, simulation);
        }
    }

    // Old monolithic AI methods removed in favor of HuntingStrategy
    // implementations.

    /**
     * Attempt to spawn a new predator.
     */
    private void attemptSpawn() {
        if (suppressionTicksRemaining > 0 || predators.size() >= maxPredators)
            return;
        if (random.nextFloat() > spawnChance)
            return;

        // Spawn at random edge of terrarium
        var terrarium = simulation.getTerrarium();
        float x, y, z;

        if (random.nextBoolean()) {
            x = random.nextBoolean() ? 0 : terrarium.getWidth() - 1;
            y = random.nextFloat() * terrarium.getHeight();
        } else {
            x = random.nextFloat() * terrarium.getWidth();
            y = random.nextBoolean() ? 0 : terrarium.getHeight() - 1;
        }
        z = terrarium.getDepth() - 1; // Surface level

        // Choose random type
        PredatorType[] types = PredatorType.values();
        PredatorType type = types[random.nextInt(types.length)];

        // Skip antlion if not sandy terrain
        if (type == PredatorType.ANTLION) {
            // Could check terrain here
        }

        Predator predator = new Predator(type, x, y, z);
        predators.add(predator);
        totalSpawned++;

        simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.INFO,
                simulation.getTickCount(),
                type.getDisplayName() + " appeared at (" + (int) x + ", " + (int) y + ")"));
    }

    // === Helper Methods ===

    // === Getters ===

    public List<Predator> getPredators() {
        return new ArrayList<>(predators);
    }

    public int getPredatorCount() {
        return predators.size();
    }

    public int getTotalSpawned() {
        return totalSpawned;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public int getPredatorsKilled() {
        return predatorsKilled;
    }

    // === Configuration ===

    public void setMaxPredators(int max) {
        this.maxPredators = max;
    }

    public void setSpawnInterval(int ticks) {
        this.spawnInterval = ticks;
    }

    public void setSpawnChance(float chance) {
        this.spawnChance = chance;
    }

    /**
     * Manually spawn a predator of specific type.
     */
    public Predator spawnPredator(PredatorType type, float x, float y, float z) {
        Predator predator = new Predator(type, x, y, z);
        predators.add(predator);
        totalSpawned++;
        return predator;
    }

    public void spawnBoss(org.swarmforge.core.domain.BossPredator.BossType type) {
        float x = random.nextFloat() * simulation.getTerrarium().getWidth();
        float y = random.nextFloat() * simulation.getTerrarium().getHeight();

        org.swarmforge.core.domain.BossPredator boss = new org.swarmforge.core.domain.BossPredator(x, y, type);
        predators.add(boss);

        simulation.getEventQueue().offer(new SimulationEvent(
                SimulationEvent.EventType.SYSTEM,
                simulation.getTickCount(),
                "BOSS SPAWNED: " + type));
    }

    /**
     * Clear all predators.
     */
    public void clearPredators() {
        predators.clear();
    }
}
