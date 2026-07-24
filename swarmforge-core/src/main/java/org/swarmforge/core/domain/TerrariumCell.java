/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.io.Serializable;

/**
 * Represents a single cell (voxel) in the terrarium.
 * Each cell has a material type, pheromone levels, and environmental
 * properties.
 *
 * <p>
 * Environmental properties:
 * </p>
 * <ul>
 * <li>Temperature (°C) - affects metabolism and movement speed</li>
 * <li>Humidity (%) - affects water needs and comfort</li>
 * <li>CO2 level (ppm) - indicator of colony activity</li>
 * <li>O2 level (%) - indicator of ventilation</li>
 * <li>Light level (0-1) - affects circadian behavior</li>
 * <li>Wind (x,y components) - affects pheromone diffusion</li>
 * <li>Pressure (relative) - for altitude simulation</li>
 * </ul>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public record TerrariumCell(
        int x,
        int y,
        int z,
        Material material,
        float[] pheromones,
        float temperature,
        float humidity,
        float co2,
        float o2,
        float light,
        float windX,
        float windY,
        float pressure) implements Serializable {

    /** Number of pheromone types supported */
    public static final int PHEROMONE_TYPES = 8;

    /** Default atmospheric CO2 (ppm) */
    public static final float DEFAULT_CO2 = 400f;

    /** Default atmospheric O2 (%) */
    public static final float DEFAULT_O2 = 21f;

    /** Default surface pressure */
    public static final float DEFAULT_PRESSURE = 1.0f;

    /**
     * Compact constructor for backward compatibility (without new fields).
     */
    public TerrariumCell(int x, int y, int z, Material material, float[] pheromones,
            float temperature, float humidity) {
        this(x, y, z, material, pheromones, temperature, humidity,
                DEFAULT_CO2, DEFAULT_O2, 0f, 0f, 0f, DEFAULT_PRESSURE);
    }

    /**
     * Material types for cells.
     */
    public enum Material {
        AIR,
        EARTH,
        SAND,
        ROCK,
        WATER,
        ORGANIC, // Food, dead matter
        NEST_WALL, // Colony structure
        CHAMBER, // Nest chamber interior
        ICE, // Frozen water
        MUD, // Wet earth
        CLAY, // Dense diggable material
        LEAF_LITTER, // Surface organic layer
        ROOT, // Plant root material
        WOOD_PULP_PAPER, // Wasp/Hornet paper nest material
        BEESWAX, // Honeybee comb wax material
        STERCORAL_CEMENT, // Termite mound saliva-soil cement
        SILK_WEAVE, // Weaver ant larval silk
        PROPOLIS // Bee/Bumblebee plant resin seal
    }

    /**
     * Pheromone type indices.
     */
    public static final int PHEROMONE_FOOD = 0;
    public static final int PHEROMONE_HOME = 1;
    public static final int PHEROMONE_ALARM = 2;
    public static final int PHEROMONE_TRAIL = 3;
    public static final int PHEROMONE_QUEEN = 4;
    public static final int PHEROMONE_BROOD = 5;
    public static final int PHEROMONE_DEATH = 6;
    public static final int PHEROMONE_TERRITORY = 7;

    // === Factory Methods ===

    /**
     * Create an air cell at the given coordinates.
     */
    public static TerrariumCell air(int x, int y, int z) {
        return new TerrariumCell(x, y, z, Material.AIR, new float[PHEROMONE_TYPES],
                20f, 50f, DEFAULT_CO2, DEFAULT_O2, 1.0f, 0f, 0f, DEFAULT_PRESSURE);
    }

    /**
     * Create an earth cell at the given coordinates.
     */
    public static TerrariumCell earth(int x, int y, int z) {
        return new TerrariumCell(x, y, z, Material.EARTH, new float[PHEROMONE_TYPES],
                15f, 70f, DEFAULT_CO2 * 1.5f, DEFAULT_O2 * 0.9f, 0f, 0f, 0f, DEFAULT_PRESSURE);
    }

    /**
     * Create a rock cell at the given coordinates.
     */
    public static TerrariumCell rock(int x, int y, int z) {
        return new TerrariumCell(x, y, z, Material.ROCK, new float[PHEROMONE_TYPES],
                12f, 30f, DEFAULT_CO2, DEFAULT_O2, 0f, 0f, 0f, DEFAULT_PRESSURE);
    }

    /**
     * Create a water cell at the given coordinates.
     */
    public static TerrariumCell water(int x, int y, int z) {
        return new TerrariumCell(x, y, z, Material.WATER, new float[PHEROMONE_TYPES],
                18f, 100f, DEFAULT_CO2, DEFAULT_O2 * 0.3f, 0.5f, 0f, 0f, DEFAULT_PRESSURE);
    }

    /**
     * Create a nest chamber cell.
     */
    public static TerrariumCell chamber(int x, int y, int z) {
        return new TerrariumCell(x, y, z, Material.CHAMBER, new float[PHEROMONE_TYPES],
                25f, 80f, DEFAULT_CO2 * 2f, DEFAULT_O2 * 0.85f, 0f, 0f, 0f, DEFAULT_PRESSURE);
    }

    /**
     * Create sand cell.
     */
    public static TerrariumCell sand(int x, int y, int z) {
        return new TerrariumCell(x, y, z, Material.SAND, new float[PHEROMONE_TYPES],
                22f, 30f, DEFAULT_CO2, DEFAULT_O2, 0.7f, 0f, 0f, DEFAULT_PRESSURE);
    }

    /**
     * Create organic matter cell.
     */
    public static TerrariumCell organic(int x, int y, int z) {
        return new TerrariumCell(x, y, z, Material.ORGANIC, new float[PHEROMONE_TYPES],
                20f, 60f, DEFAULT_CO2 * 1.3f, DEFAULT_O2 * 0.95f, 0.3f, 0f, 0f, DEFAULT_PRESSURE);
    }

    // === Accessors ===

    /**
     * Get pheromone level for a specific type.
     */
    public float getPheromone(int type) {
        return pheromones[type];
    }

    /**
     * Check if this cell is passable for ants.
     */
    public boolean isPassable() {
        return material == Material.AIR || material == Material.CHAMBER ||
                material == Material.LEAF_LITTER;
    }

    /**
     * Check if this cell is diggable.
     */
    public boolean isDiggable() {
        return material == Material.EARTH || material == Material.SAND ||
                material == Material.MUD || material == Material.CLAY ||
                material == Material.LEAF_LITTER;
    }

    /**
     * Check if this cell blocks light.
     */
    public boolean blocksLight() {
        return material != Material.AIR && material != Material.WATER &&
                material != Material.CHAMBER;
    }

    /**
     * Get combined wind magnitude.
     */
    public float getWindMagnitude() {
        return (float) Math.sqrt(windX * windX + windY * windY);
    }

    /**
     * Get wind direction in radians.
     */
    public float getWindDirection() {
        return (float) Math.atan2(windY, windX);
    }

    /**
     * Calculate habitability score for ants (0-1).
     * Based on temperature, humidity, and oxygen levels.
     */
    public float getHabitabilityScore() {
        // Ideal: 20-28°C, 60-80% humidity, good O2
        float tempScore = 1f - Math.abs(temperature - 24f) / 30f;
        float humidScore = 1f - Math.abs(humidity - 70f) / 50f;
        float o2Score = o2 / DEFAULT_O2;

        return Math.max(0f, Math.min(1f, (tempScore + humidScore + o2Score) / 3f));
    }

    /**
     * Create a copy with modified temperature.
     */
    public TerrariumCell withTemperature(float newTemp) {
        return new TerrariumCell(x, y, z, material, pheromones, newTemp, humidity,
                co2, o2, light, windX, windY, pressure);
    }

    /**
     * Create a copy with modified humidity.
     */
    public TerrariumCell withHumidity(float newHumidity) {
        return new TerrariumCell(x, y, z, material, pheromones, temperature, newHumidity,
                co2, o2, light, windX, windY, pressure);
    }

    /**
     * Create a copy with modified light level.
     */
    public TerrariumCell withLight(float newLight) {
        return new TerrariumCell(x, y, z, material, pheromones, temperature, humidity,
                co2, o2, newLight, windX, windY, pressure);
    }

    /**
     * Create a copy with modified wind.
     */
    public TerrariumCell withWind(float newWindX, float newWindY) {
        return new TerrariumCell(x, y, z, material, pheromones, temperature, humidity,
                co2, o2, light, newWindX, newWindY, pressure);
    }
}
