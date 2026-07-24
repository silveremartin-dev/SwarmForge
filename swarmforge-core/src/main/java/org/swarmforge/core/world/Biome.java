/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import org.swarmforge.core.domain.TerrariumCell;

/**
 * Biome presets for terrain generation.
 * Each biome defines environmental parameters and material distributions
 * for realistic world generation.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public enum Biome {

    /**
     * Temperate deciduous forest.
     * Moderate temperature, high humidity, rich soil.
     */
    FOREST(
            "Temperate Forest",
            15f, 25f, // Temp range
            60f, 85f, // Humidity range
            48f, // Typical latitude
            new MaterialWeight[] {
                    new MaterialWeight(TerrariumCell.Material.EARTH, 0.5f),
                    new MaterialWeight(TerrariumCell.Material.LEAF_LITTER, 0.25f),
                    new MaterialWeight(TerrariumCell.Material.ORGANIC, 0.15f),
                    new MaterialWeight(TerrariumCell.Material.ROCK, 0.05f),
                    new MaterialWeight(TerrariumCell.Material.ROOT, 0.05f)
            },
            0.6f// Vegetation density
    ),

    /**
     * Hot, arid desert.
     * High temperature, low humidity, sandy soil.
     */
    DESERT(
            "Arid Desert",
            25f, 45f, // Temp range
            5f, 20f, // Humidity range
            25f, // Typical latitude
            new MaterialWeight[] {
                    new MaterialWeight(TerrariumCell.Material.SAND, 0.7f),
                    new MaterialWeight(TerrariumCell.Material.ROCK, 0.2f),
                    new MaterialWeight(TerrariumCell.Material.EARTH, 0.08f),
                    new MaterialWeight(TerrariumCell.Material.ORGANIC, 0.02f)
            },
            0.1f// Sparse vegetation
    ),

    /**
     * Open grassland/prairie.
     * Moderate temperature, moderate humidity.
     */
    GRASSLAND(
            "Grassland Prairie",
            10f, 30f, // Temp range
            40f, 70f, // Humidity range
            40f, // Typical latitude
            new MaterialWeight[] {
                    new MaterialWeight(TerrariumCell.Material.EARTH, 0.6f),
                    new MaterialWeight(TerrariumCell.Material.ORGANIC, 0.2f),
                    new MaterialWeight(TerrariumCell.Material.SAND, 0.1f),
                    new MaterialWeight(TerrariumCell.Material.ROOT, 0.1f)
            },
            0.8f// Dense grass cover
    ),

    /**
     * Tropical rainforest.
     * High temperature, very high humidity, dense vegetation.
     */
    TROPICAL(
            "Tropical Rainforest",
            24f, 35f, // Temp range
            80f, 100f, // Humidity range
            5f, // Near equator
            new MaterialWeight[] {
                    new MaterialWeight(TerrariumCell.Material.EARTH, 0.3f),
                    new MaterialWeight(TerrariumCell.Material.MUD, 0.2f),
                    new MaterialWeight(TerrariumCell.Material.LEAF_LITTER, 0.25f),
                    new MaterialWeight(TerrariumCell.Material.ORGANIC, 0.15f),
                    new MaterialWeight(TerrariumCell.Material.ROOT, 0.1f)
            },
            0.95f// Very dense vegetation
    ),

    /**
     * Mediterranean climate.
     * Warm, dry summers; mild, wet winters.
     */
    MEDITERRANEAN(
            "Mediterranean Scrubland",
            12f, 35f, // Temp range
            30f, 70f, // Humidity range
            35f, // Typical latitude
            new MaterialWeight[] {
                    new MaterialWeight(TerrariumCell.Material.EARTH, 0.4f),
                    new MaterialWeight(TerrariumCell.Material.CLAY, 0.2f),
                    new MaterialWeight(TerrariumCell.Material.ROCK, 0.2f),
                    new MaterialWeight(TerrariumCell.Material.ORGANIC, 0.1f),
                    new MaterialWeight(TerrariumCell.Material.SAND, 0.1f)
            },
            0.4f// Moderate scrub cover
    ),

    /**
     * Wetland/marsh environment.
     * High moisture, waterlogged soil.
     */
    WETLAND(
            "Wetland Marsh",
            10f, 28f, // Temp range
            85f, 100f, // Humidity range
            45f, // Typical latitude
            new MaterialWeight[] {
                    new MaterialWeight(TerrariumCell.Material.MUD, 0.4f),
                    new MaterialWeight(TerrariumCell.Material.WATER, 0.25f),
                    new MaterialWeight(TerrariumCell.Material.ORGANIC, 0.2f),
                    new MaterialWeight(TerrariumCell.Material.EARTH, 0.1f),
                    new MaterialWeight(TerrariumCell.Material.ROOT, 0.05f)
            },
            0.7f// Wetland plants
    ),

    /**
     * Cold tundra.
     * Very cold, low vegetation.
     */
    TUNDRA(
            "Arctic Tundra",
            -20f, 10f, // Temp range
            50f, 80f, // Humidity range
            70f, // High latitude
            new MaterialWeight[] {
                    new MaterialWeight(TerrariumCell.Material.ICE, 0.3f),
                    new MaterialWeight(TerrariumCell.Material.ROCK, 0.3f),
                    new MaterialWeight(TerrariumCell.Material.EARTH, 0.3f),
                    new MaterialWeight(TerrariumCell.Material.ORGANIC, 0.1f)
            },
            0.2f// Sparse vegetation
    );

    /**
     * Material distribution weight.
     */
    public record MaterialWeight(TerrariumCell.Material material, float weight) {
    }

    private final String displayName;
    private final float minTemp;
    private final float maxTemp;
    private final float minHumidity;
    private final float maxHumidity;
    private final float typicalLatitude;
    private final MaterialWeight[] materialDistribution;
    private final float vegetationDensity;

    Biome(String displayName, float minTemp, float maxTemp,
            float minHumidity, float maxHumidity, float typicalLatitude,
            MaterialWeight[] materialDistribution, float vegetationDensity) {
        this.displayName = displayName;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        this.typicalLatitude = typicalLatitude;
        this.materialDistribution = materialDistribution;
        this.vegetationDensity = vegetationDensity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public float getMinTemp() {
        return minTemp;
    }

    public float getMaxTemp() {
        return maxTemp;
    }

    public float getMinHumidity() {
        return minHumidity;
    }

    public float getMaxHumidity() {
        return maxHumidity;
    }

    public float getTypicalLatitude() {
        return typicalLatitude;
    }

    public MaterialWeight[] getMaterialDistribution() {
        return materialDistribution;
    }

    public float getVegetationDensity() {
        return vegetationDensity;
    }

    /**
     * Get average temperature for this biome.
     */
    public float getAverageTemp() {
        return (minTemp + maxTemp) / 2f;
    }

    /**
     * Get average humidity for this biome.
     */
    public float getAverageHumidity() {
        return (minHumidity + maxHumidity) / 2f;
    }

    /**
     * Select a random material based on distribution weights.
     */
    public TerrariumCell.Material selectMaterial(java.util.Random rand) {
        float r = rand.nextFloat();
        float cumulative = 0f;

        for (MaterialWeight mw : materialDistribution) {
            cumulative += mw.weight;
            if (r <= cumulative) {
                return mw.material;
            }
        }

        return materialDistribution[materialDistribution.length - 1].material;
    }

    /**
     * Determine the best biome for given latitude and conditions.
     */
    public static Biome forLatitude(double latitude) {
        double absLat = Math.abs(latitude);

        if (absLat > 60)
            return TUNDRA;
        if (absLat < 10)
            return TROPICAL;
        if (absLat < 30)
            return DESERT; // Simplified
        if (absLat < 45)
            return MEDITERRANEAN;
        return FOREST;
    }
}
