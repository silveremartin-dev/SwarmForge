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
    private float spawnCooldownSeconds = 0.0f;
    private float spawnIntervalSeconds = 60.0f; // Seconds between spawn evaluations
    private float spawnChance = 0.35f;
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
            // Handle spawning interval in SECONDS
            if (spawnCooldownSeconds > 0.0f) {
                spawnCooldownSeconds -= deltaSeconds;
            } else {
                // Tiny chance for Boss
                if (random.nextFloat() < 0.001f) { // 0.1% chance per spawn evaluation
                    spawnBoss(BossPredator.BossType.values()[random.nextInt(BossPredator.BossType.values().length)]);
                } else {
                    attemptSpawn();
                }
                spawnCooldownSeconds = spawnIntervalSeconds;
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

    /**
     * Attempt to spawn a new predator or accessory prey species.
     */
    private void attemptSpawn() {
        if (suppressionTicksRemaining > 0 || predators.size() >= maxPredators)
            return;
        if (random.nextFloat() > spawnChance)
            return;

        int totalColonyAnts = 0;
        if (simulation != null && simulation.getColonies() != null) {
            for (var col : simulation.getColonies()) {
                if (col != null) totalColonyAnts += col.getPopulation();
            }
        }

        // Count current active dangerous predators
        long dangerousCount = predators.stream().filter(p -> isDangerousPredator(p.getType())).count();

        // 70% chance to select a harmless prey / accessory organism vs 30% for a dangerous hunter
        boolean spawnDangerous = (random.nextFloat() < 0.30f) && (totalColonyAnts >= 50) && (dangerousCount < 3);

        PredatorType type;
        if (spawnDangerous) {
            List<PredatorType> dangerousCandidates = new ArrayList<>();
            for (PredatorType candidate : new PredatorType[]{
                PredatorType.SPIDER, PredatorType.ANTLION, PredatorType.BEETLE, PredatorType.BIRD,
                PredatorType.LIZARD, PredatorType.WASP, PredatorType.ASIAN_HORNET, PredatorType.BEE_WOLF
            }) {
                if (isSeasonAndHabitatCompatible(candidate)) {
                    dangerousCandidates.add(candidate);
                }
            }
            if (!dangerousCandidates.isEmpty()) {
                type = dangerousCandidates.get(random.nextInt(dangerousCandidates.size()));
            } else {
                type = PredatorType.BEETLE;
            }
        } else {
            List<PredatorType> preyCandidates = new ArrayList<>();
            for (PredatorType candidate : new PredatorType[]{
                PredatorType.CATERPILLAR, PredatorType.SYRPHID_LARVA, PredatorType.LADYBUG_LARVA,
                PredatorType.MYRMECOPHILE_BEETLE, PredatorType.KLEPTOPARASITE_THRIPS
            }) {
                if (isSeasonAndHabitatCompatible(candidate)) {
                    preyCandidates.add(candidate);
                }
            }
            if (!preyCandidates.isEmpty()) {
                type = preyCandidates.get(random.nextInt(preyCandidates.size()));
            } else {
                type = PredatorType.MYRMECOPHILE_BEETLE;
            }
        }

        // Spawn at random edge of terrarium surface
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

        Predator predator = new Predator(type, x, y, z);
        predators.add(predator);
        totalSpawned++;

        simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.INFO,
                simulation.getTickCount(),
                type.getDisplayName() + " appeared at (" + (int) x + ", " + (int) y + ")"));
    }

    private boolean isSeasonAndHabitatCompatible(PredatorType type) {
        if (simulation == null) return true;
        var seasonMgr = simulation.getSeasonManager();
        org.swarmforge.core.world.Season season = seasonMgr != null ? seasonMgr.getCurrentSeason() : org.swarmforge.core.world.Season.SPRING;

        // Winter diapause: most active insects & reptiles enter dormancy
        if (season == org.swarmforge.core.world.Season.WINTER) {
            if (type == PredatorType.CATERPILLAR || type == PredatorType.LADYBUG_LARVA ||
                type == PredatorType.SYRPHID_LARVA || type == PredatorType.LIZARD ||
                type == PredatorType.ASIAN_HORNET || type == PredatorType.BEE_WOLF ||
                type == PredatorType.HONEY_BUZZARD || type == PredatorType.WASP) {
                return false;
            }
        }

        // Summer / Fall specific insects
        if (type == PredatorType.ASIAN_HORNET || type == PredatorType.BEE_WOLF) {
            if (season != org.swarmforge.core.world.Season.SUMMER && season != org.swarmforge.core.world.Season.FALL) {
                return false;
            }
        }

        // Honey Buzzard raptor active in Spring and Summer
        if (type == PredatorType.HONEY_BUZZARD) {
            if (season != org.swarmforge.core.world.Season.SPRING && season != org.swarmforge.core.world.Season.SUMMER) {
                return false;
            }
        }

        // Terrain checks: Antlions strictly require Sand
        if (type == PredatorType.ANTLION) {
            var terrarium = simulation.getTerrarium();
            if (terrarium == null) return false;
            boolean hasSand = false;
            int w = terrarium.getWidth();
            int h = terrarium.getHeight();
            int z = terrarium.getDepth() - 1;
            for (int i = 0; i < 15; i++) {
                int rx = random.nextInt(w);
                int ry = random.nextInt(h);
                var cell = terrarium.getCell(rx, ry, z);
                if (cell != null && cell.material() == org.swarmforge.core.domain.TerrariumCell.Material.SAND) {
                    hasSand = true;
                    break;
                }
            }
            if (!hasSand) return false;
        }

        return true;
    }

    private boolean isDangerousPredator(PredatorType type) {
        return switch (type) {
            case SPIDER, ANTLION, BIRD, LIZARD, WASP, ASIAN_HORNET, BEE_WOLF, VARROA_MITE,
                 HONEY_BUZZARD, MEGAPONERA_RAIDER, AARDVARK_MOUND_BREAKER, WOODPECKER -> true;
            default -> false;
        };
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
        this.spawnIntervalSeconds = ticks * 0.016666667f;
    }

    public float getSpawnIntervalSeconds() {
        return spawnIntervalSeconds;
    }

    public void setSpawnIntervalSeconds(float seconds) {
        this.spawnIntervalSeconds = seconds;
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
