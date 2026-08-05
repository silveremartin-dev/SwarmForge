/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.structure.physics;

import java.io.Serializable;

/**
 * 3D Physics Voxel Grid for Nest Simulation.
 * Holds materials, overburden stress, temperature, CO2 concentration, propolis coatings, and pathogen spore load.
 */
public class NestVoxelGrid implements Serializable {
    private static final long serialVersionUID = 1L;

    public static class VoxelCell implements Serializable {
        private static final long serialVersionUID = 1L;

        private VoxelMaterial material;
        private float overburdenStressKPa;
        private float temperatureC;
        private float co2Ppm;
        private float propolisCoating; // 0.0 to 1.0
        private float fungalSporeLoad; // 0.0 to 1.0 (pathogen spores)
        private boolean collapsed;

        public VoxelCell(VoxelMaterial material, float temperatureC, float co2Ppm) {
            this.material = material;
            this.temperatureC = temperatureC;
            this.co2Ppm = co2Ppm;
            this.overburdenStressKPa = 0.0f;
            this.propolisCoating = 0.0f;
            this.fungalSporeLoad = 0.0f;
            this.collapsed = false;
        }

        public VoxelMaterial getMaterial() {
            return material;
        }

        public void setMaterial(VoxelMaterial material) {
            this.material = material;
        }

        public float getOverburdenStressKPa() {
            return overburdenStressKPa;
        }

        public void setOverburdenStressKPa(float stress) {
            this.overburdenStressKPa = stress;
        }

        public float getTemperatureC() {
            return temperatureC;
        }

        public void setTemperatureC(float temp) {
            this.temperatureC = temp;
        }

        public float getCo2Ppm() {
            return co2Ppm;
        }

        public void setCo2Ppm(float co2Ppm) {
            this.co2Ppm = Math.max(350.0f, co2Ppm);
        }

        public float getPropolisCoating() {
            return propolisCoating;
        }

        public void setPropolisCoating(float propolisCoating) {
            this.propolisCoating = Math.max(0.0f, Math.min(1.0f, propolisCoating));
        }

        public float getFungalSporeLoad() {
            return fungalSporeLoad;
        }

        public void setFungalSporeLoad(float load) {
            this.fungalSporeLoad = Math.max(0.0f, Math.min(1.0f, load));
        }

        public boolean isCollapsed() {
            return collapsed;
        }

        public void setCollapsed(boolean collapsed) {
            this.collapsed = collapsed;
        }
    }

    private final int width;
    private final int height; // Y dimension
    private final int depth;  // Z dimension
    private final VoxelCell[][][] voxels;
    private final NestType nestType;

    public NestVoxelGrid(int width, int height, int depth, NestType nestType) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.nestType = nestType;
        this.voxels = new VoxelCell[width][height][depth];

        // Initialize grid: Air inside tunnels/chambers, Soil elsewhere by default
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    VoxelMaterial mat = (y >= height - 2) ? VoxelMaterial.AIR : VoxelMaterial.SOIL;
                    voxels[x][y][z] = new VoxelCell(mat, 20.0f, 400.0f);
                }
            }
        }
    }

    public boolean isValidCoord(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    public VoxelCell getVoxel(int x, int y, int z) {
        if (!isValidCoord(x, y, z)) {
            return null;
        }
        return voxels[x][y][z];
    }

    public void setMaterial(int x, int y, int z, VoxelMaterial mat) {
        if (isValidCoord(x, y, z)) {
            voxels[x][y][z].setMaterial(mat);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public NestType getNestType() {
        return nestType;
    }
}
