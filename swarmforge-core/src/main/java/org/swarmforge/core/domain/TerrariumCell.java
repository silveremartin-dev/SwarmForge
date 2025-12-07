/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
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
 * @param x           X coordinate
 * @param y           Y coordinate
 * @param z           Z coordinate
 * @param material    Material type of this cell
 * @param pheromones  Pheromone levels indexed by type
 * @param temperature Temperature in Celsius
 * @param humidity    Humidity percentage (0-100)
 */
public record TerrariumCell(
        int x,
        int y,
        int z,
        Material material,
        float[] pheromones,
        float temperature,
        float humidity) implements Serializable {

    /** Number of pheromone types supported */
    public static final int PHEROMONE_TYPES = 8;

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
        CHAMBER // Nest chamber interior
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

    /**
     * Create an air cell at the given coordinates.
     */
    public static TerrariumCell air(int x, int y, int z) {
        return new TerrariumCell(x, y, z, Material.AIR, new float[PHEROMONE_TYPES], 20f, 50f);
    }

    /**
     * Create an earth cell at the given coordinates.
     */
    public static TerrariumCell earth(int x, int y, int z) {
        return new TerrariumCell(x, y, z, Material.EARTH, new float[PHEROMONE_TYPES], 15f, 70f);
    }

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
        return material == Material.AIR || material == Material.CHAMBER;
    }

    /**
     * Check if this cell is diggable.
     */
    public boolean isDiggable() {
        return material == Material.EARTH || material == Material.SAND;
    }
}
