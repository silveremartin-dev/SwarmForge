/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import java.util.Random;

/**
 * Procedural vegetation system with growth and spreading.
 *
 * @author Gemini AI Assistant
 */
public class VegetationSystem {

    public enum PlantType {
        GRASS(1, 0.1f, 50),
        SHRUB(3, 0.05f, 200),
        TREE(10, 0.01f, 1000),
        FLOWER(2, 0.2f, 30),
        MOSS(1, 0.15f, 20);

        private final int maxHeight;
        private final float spreadRate;
        private final int growthTicks;

        PlantType(int maxHeight, float spreadRate, int growthTicks) {
            this.maxHeight = maxHeight;
            this.spreadRate = spreadRate;
            this.growthTicks = growthTicks;
        }

        public int getMaxHeight() {
            return maxHeight;
        }

        public float getSpreadRate() {
            return spreadRate;
        }

        public int getGrowthTicks() {
            return growthTicks;
        }
    }

    public static class Plant {
        public PlantType type;
        public int x, y, z;
        public float growth; // 0-1
        public int age;
        public float health = 1.0f;

        public Plant(PlantType type, int x, int y, int z) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.growth = 0;
            this.age = 0;
        }

        public boolean isMature() {
            return growth >= 1.0f;
        }

        public int getCurrentHeight() {
            return (int) (type.getMaxHeight() * growth);
        }

        public float harvestFoliage(float amount) {
            float available = Math.max(0.1f, health * growth);
            float harvested = Math.min(available, amount);
            health = Math.max(0.1f, health - harvested * 0.05f);
            return harvested;
        }

        public float harvestSeeds(float amount) {
            if (type == PlantType.GRASS || type == PlantType.FLOWER) {
                float available = Math.max(0.1f, growth);
                float harvested = Math.min(available, amount);
                growth = Math.max(0.1f, growth - harvested * 0.1f);
                return harvested;
            }
            return 0.0f;
        }
    }

    private final java.util.List<Plant> plants = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final Random rng = new Random();
    private final int worldWidth, worldDepth;

    public VegetationSystem(int width, int depth) {
        this.worldWidth = Math.max(1, width);
        this.worldDepth = Math.max(1, depth);
    }

    /**
     * Process one simulation tick.
     */
    public void tick(float temperature, float moisture) {
        // Growth rate based on conditions
        float growthMod = calculateGrowthModifier(temperature, moisture);

        java.util.List<Plant> newPlants = new java.util.ArrayList<>();

        for (Plant plant : plants) {
            plant.age++;

            // Grow
            if (plant.growth < 1.0f) {
                plant.growth += growthMod / plant.type.getGrowthTicks();
                plant.growth = Math.min(1.0f, plant.growth);
            }

            // Spread seeds if mature
            if (plant.isMature() && rng.nextFloat() < plant.type.getSpreadRate() * growthMod) {
                Plant offspring = spreadSeed(plant);
                if (offspring != null) {
                    newPlants.add(offspring);
                }
            }

            // Die based on conditions
            if (temperature < -10 || moisture < 0.1f) {
                plant.health -= 0.01f;
            }
        }

        // Remove dead plants
        plants.removeIf(p -> p.health <= 0);

        // Add new plants
        plants.addAll(newPlants);
    }

    /**
     * Spawn initial vegetation.
     */
    public void populate(int count, PlantType type) {
        for (int i = 0; i < count; i++) {
            int x = rng.nextInt(worldWidth);
            int z = rng.nextInt(worldDepth);
            Plant p = new Plant(type, x, 0, z);
            p.growth = 1.0f; // Initial plants are mature
            plants.add(p);
        }
    }

    /**
     * Find nearest plant to specific position.
     */
    public Plant findNearestPlant(float x, float z, float maxDist, PlantType filterType) {
        Plant nearest = null;
        float minDistSq = maxDist * maxDist;
        for (Plant p : plants) {
            if (filterType != null && p.type != filterType) continue;
            float dx = p.x - x;
            float dz = p.z - z;
            float d2 = dx * dx + dz * dz;
            if (d2 < minDistSq) {
                minDistSq = d2;
                nearest = p;
            }
        }
        return nearest;
    }

    private Plant spreadSeed(Plant parent) {
        int dx = rng.nextInt(11) - 5;
        int dz = rng.nextInt(11) - 5;
        int nx = parent.x + dx;
        int nz = parent.z + dz;

        if (nx >= 0 && nx < worldWidth && nz >= 0 && nz < worldDepth) {
            // Check no plant already there
            for (Plant p : plants) {
                if (p.x == nx && p.z == nz)
                    return null;
            }
            return new Plant(parent.type, nx, 0, nz);
        }
        return null;
    }

    private float calculateGrowthModifier(float temp, float moisture) {
        // Optimal: 20°C, 50% moisture
        float tempFactor = 1.0f - Math.abs(temp - 20) / 30f;
        float moistFactor = 1.0f - Math.abs(moisture - 0.5f);
        return Math.max(0, tempFactor * moistFactor);
    }

    public java.util.List<Plant> getPlants() {
        return plants;
    }

    public int getPlantCount() {
        return plants.size();
    }

    /**
     * Root Evapotranspiration Coupling: Computes deep subterranean soil moisture suction by plant root systems.
     * Transpiration rate (liters/m²/tick) scales with plant density, foliage maturity, and solar irradiance.
     */
    public float calculateRootEvapotranspiration(float solarIrradiance) {
        if (plants.isEmpty()) return 0.0f;
        float totalTranspiration = 0.0f;
        for (Plant p : plants) {
            float rootScale = switch (p.type) {
                case TREE -> 1.5f;
                case SHRUB -> 0.8f;
                case GRASS -> 0.3f;
                case FLOWER -> 0.2f;
                case MOSS -> 0.05f;
            };
            totalTranspiration += rootScale * p.growth * p.health * (0.2f + 0.8f * solarIrradiance);
        }
        return totalTranspiration * 0.01f;
    }
}

