/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.swarmforge.core.spatial.Morton3D;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the 3D voxel world where the simulation takes place.
 * Uses Morton encoding for efficient sparse voxel storage.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class Terrarium {

    private final int width;
    private final int height;
    private final int depth;
    private final ConcurrentHashMap<Long, TerrariumCell> cells;

    // Environmental parameters
    private double latitude;
    private double longitude;
    private double altitude;
    private float ambientTemperature;
    private float ambientHumidity;

    /**
     * Create a new terrarium with the given dimensions.
     *
     * @param width  Width in cells (X axis)
     * @param height Height in cells (Y axis)
     * @param depth  Depth in cells (Z axis)
     */
    public Terrarium(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.cells = new ConcurrentHashMap<>();
        this.ambientTemperature = 20f;
        this.ambientHumidity = 50f;
    }

    /**
     * Get cell at the specified coordinates.
     * Returns an air cell if no cell exists at that position.
     */
    public TerrariumCell getCell(int x, int y, int z) {
        if (!inBounds(x, y, z)) {
            return TerrariumCell.earth(x, y, z); // Out of bounds = rock
        }
        long key = Morton3D.encode(x, y, z);
        return cells.getOrDefault(key, TerrariumCell.air(x, y, z));
    }

    /**
     * Set cell at the specified coordinates.
     */
    public void setCell(TerrariumCell cell) {
        long key = Morton3D.encode(cell.x(), cell.y(), cell.z());
        cells.put(key, cell);
    }

    /**
     * Check if coordinates are within bounds.
     */
    public boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    /**
     * Get the number of cells currently stored (sparse count).
     */
    public int getCellCount() {
        return cells.size();
    }

    // Getters
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public float getAmbientTemperature() {
        return ambientTemperature;
    }

    public float getAmbientHumidity() {
        return ambientHumidity;
    }

    // Setters
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setAmbientTemperature(float t) {
        this.ambientTemperature = t;
    }

    public void setAmbientHumidity(float h) {
        this.ambientHumidity = h;
    }
}
