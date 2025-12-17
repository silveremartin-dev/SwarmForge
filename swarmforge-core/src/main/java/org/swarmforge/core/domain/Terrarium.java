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
@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class Terrarium {

    private int width;
    private int height;
    private int depth;
    private ConcurrentHashMap<Long, TerrariumCell> cells;

    // Environmental parameters
    private double latitude;
    private double longitude;
    private double altitude;
    private float ambientTemperature;
    private float ambientHumidity;

    private ConcurrentHashMap<java.util.UUID, Colony> colonies;

    public Terrarium() {
        this.cells = new ConcurrentHashMap<>();
        this.colonies = new ConcurrentHashMap<>();
    }

    /**
     * Create a new terrarium with the given dimensions.
     *
     * @param width  Width in cells (X axis)
     * @param height Height in cells (Y axis)
     * @param depth  Depth in cells (Z axis)
     */
    public Terrarium(int width, int height, int depth) {
        this();
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.ambientTemperature = 20f;
        this.ambientHumidity = 50f;
    }

    public void addColony(Colony colony) {
        colonies.put(colony.getId(), colony);
    }

    public java.util.Collection<Colony> getColonies() {
        return colonies.values();
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

    public java.util.Collection<TerrariumCell> getAllCells() {
        return cells.values();
    }

    public void clear() {
        cells.clear();
        colonies.clear();
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

    // === GPU Support ===

    /**
     * Export pheromone data to a dense flattened array for GPU processing.
     * Format: [ (x + y*w + z*w*h) * types + typeIndex ]
     * 
     * @return Flattened float array
     */
    public float[] exportPheromones() {
        int totalSize = width * height * depth * TerrariumCell.PHEROMONE_TYPES;
        float[] data = new float[totalSize];

        // Iterate only existing cells to populate
        cells.values().forEach(cell -> {
            int index = (cell.x() + cell.y() * width + cell.z() * width * height) * TerrariumCell.PHEROMONE_TYPES;
            float[] p = cell.pheromones();
            // Manual copy is faster than System.arraycopy for small size 8?
            for (int i = 0; i < TerrariumCell.PHEROMONE_TYPES; i++) {
                data[index + i] = p[i];
            }
        });

        return data;
    }

    /**
     * Import processed pheromone data from GPU dense array.
     * Updates in-place if possible, creates new cells if needed.
     */
    public void importPheromones(float[] data) {
        // Iterate dense array
        // We can optimize by only checking cells that were active or just brute force
        // iterating the array?
        // Iterating 64^3 (262k) blocks is fast on CPU.
        // But 128^3 (2M) might differ.

        int w = width;
        int wh = width * height;
        int types = TerrariumCell.PHEROMONE_TYPES;

        for (int i = 0; i < data.length; i += types) {
            // Check if any pheromone is non-zero (above functional epsilon)
            boolean hasPheromone = false;
            for (int t = 0; t < types; t++) {
                if (data[i + t] > 0.001f) {
                    hasPheromone = true;
                    break;
                }
            }

            int cellIndex = i / types;
            int z = cellIndex / wh;
            int rem = cellIndex % wh;
            int y = rem / w;
            int x = rem % w;

            if (hasPheromone) {
                // Update or Create
                long key = Morton3D.encode(x, y, z);
                TerrariumCell cell = cells.get(key);

                if (cell != null) {
                    // Update existing array in-place
                    float[] p = cell.pheromones();
                    for (int t = 0; t < types; t++) {
                        p[t] = data[i + t];
                    }
                } else {
                    // Create new AIR cell
                    // Note: This breaks "Sparse" nature if pheromone diffuses everywhere.
                    // But if it's significant, it must exist.
                    float[] newP = new float[types];
                    for (int t = 0; t < types; t++) {
                        newP[t] = data[i + t];
                    }
                    // Create AIR node
                    cells.put(key, new TerrariumCell(
                            x, y, z,
                            TerrariumCell.Material.AIR,
                            newP, 20f, 50f,
                            TerrariumCell.DEFAULT_CO2, TerrariumCell.DEFAULT_O2,
                            1f, 0f, 0f, TerrariumCell.DEFAULT_PRESSURE));
                }
            } else {
                // If 0, and cell exists?
                // If it was just an AIR cell for pheromone, we could remove it to save memory.
                // But safer to leave it for now to avoid ConcurrentModification if we were
                // iterating map (we are not).
                // Cleanup can happen separately.
            }
        }
    }
}
