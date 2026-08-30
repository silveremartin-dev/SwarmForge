/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

import java.util.Random;

/**
 * Procedural terrain generator for natural landscapes.
 * Uses Perlin-like noise for realistic terrain features.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TerrainGenerator {

    private final Random random;
    private final int[] permutation;

    public TerrainGenerator() {
        this(System.currentTimeMillis());
    }

    public TerrainGenerator(long seed) {
        this.random = new Random(seed);
        this.permutation = new int[512];
        initPermutation();
    }

    private void initPermutation() {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++)
            p[i] = i;
        // Shuffle
        for (int i = 255; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        System.arraycopy(p, 0, permutation, 0, 256);
        System.arraycopy(p, 0, permutation, 256, 256);
    }

    /**
     * Generate terrain in the terrarium.
     *
     * @param terrarium   Target terrarium
     * @param groundLevel Base ground level (Z coordinate)
     * @param amplitude   Height variation amplitude
     * @param scale       Noise scale (smaller = smoother)
     */
    public void generate(Terrarium terrarium, int groundLevel, float amplitude, float scale) {
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        int depth = terrarium.getDepth();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Generate height using noise
                float noiseVal = noise(x * scale, y * scale);
                int surfaceZ = groundLevel + (int) (noiseVal * amplitude);

                for (int z = 0; z < depth; z++) {
                    TerrariumCell.Material material;

                    if (z > surfaceZ) {
                        material = TerrariumCell.Material.AIR;
                    } else if (z == surfaceZ) {
                        // Horizon O/A: Topsoil, Peat & Organic leaf litter
                        float nVal = noise(x * 0.1f, y * 0.1f);
                        material = (nVal > 0.3f) ? TerrariumCell.Material.PEAT : TerrariumCell.Material.EARTH;
                    } else if (z > surfaceZ - 4) {
                        // Horizon A: Earth & Silt (Limon)
                        float nVal = noise((x + 100) * 0.08f, (y + 100) * 0.08f);
                        material = (nVal > 0.2f) ? TerrariumCell.Material.SILT : TerrariumCell.Material.EARTH;
                    } else if (z > surfaceZ - 12) {
                        // Horizon B: Subsoil Clay, Sand lenses, Gravel deposits & Natural Cavities
                        float nVal = noise((x + 50) * 0.05f, (y + 50) * 0.05f + z * 0.1f);
                        if (nVal > 0.45f) {
                            material = TerrariumCell.Material.CLAY;
                        } else if (nVal > 0.15f) {
                            material = TerrariumCell.Material.SAND;
                        } else if (nVal < -0.4f) {
                            material = TerrariumCell.Material.CAVITY; // Pre-existing natural cavity / void
                        } else if (nVal < -0.2f) {
                            material = TerrariumCell.Material.GRAVEL;
                        } else {
                            material = TerrariumCell.Material.EARTH;
                        }
                    } else {
                        // Horizon C/R: Bedrock, Deep Gravel, Dense Clay & Rock
                        float nVal = noise((x + 200) * 0.04f, (y + 200) * 0.04f);
                        if (nVal > 0.1f) {
                            material = TerrariumCell.Material.ROCK;
                        } else if (nVal < -0.3f) {
                            material = TerrariumCell.Material.GRAVEL;
                        } else {
                            material = TerrariumCell.Material.CLAY;
                        }
                    }

                    if (material != TerrariumCell.Material.AIR) {
                        TerrariumCell cell = new TerrariumCell(
                                x, y, z, material,
                                new float[TerrariumCell.PHEROMONE_TYPES],
                                15f + (surfaceZ - z) * 0.1f, // Temperature gradient
                                60f + (surfaceZ - z) * 0.5f // Humidity gradient
                        );
                        terrarium.setCell(cell);
                    }
                }
            }
        }
    }

    /**
     * Generate terrain from real-world elevation data provided by OpenTopography.
     * 
     * @param terrarium Target terrarium
     * @param provider  Elevation provider instance
     * @param lat       Center latitude
     * @param lon       Center longitude
     * @param scale     Meters per voxel (e.g. 1.0 = 1m/voxel)
     */
    public void generateFromRealWorld(Terrarium terrarium, ElevationProvider provider,
            double lat, double lon, float scale) {

        // TerrainGenerator uses width/height for loop, and Z for depth/up?
        // Original: width (x), height (y), depth (z). Wait.
        // LOOP: for x < width, for y < height, for z < depth
        // Surface Z is height.
        // HEIGHT usually means vertical extent in 3D.
        // Let's check original loop: "for x ... width, for y ... height .. float
        // noise(x, y)"
        // And z goes up to depth? "for z ... depth".
        // It seems Y is 2nd horizontal dimension and Z is vertical?
        // Sim coords: X, Y (horizontal), Z (vertical) ?
        // Or X, Z (horizontal), Y (vertical)?
        // Previous JME code: "cam.setLocation(..., h+20...)" implies h is vertical.
        // Code: "int surfaceZ = groundLevel + noise..." -> Z is vertical.
        // So Width = X, Height = Y (horizontal), Depth = Z (vertical).
        // Confusing naming. "Terrarium.getHeight()" usually implicitly Y.

        int w = terrarium.getWidth();
        int h = terrarium.getHeight();

        // Let's re-read generate():
        // width = terrarium.getWidth(); height = terrarium.getHeight();
        // for x < width, for y < height
        // noise(x, y)
        // for z < depth (terrarium.getDepth())
        // surfaceZ = ...
        // material(z > surfaceZ)...

        // So: X, Y are horizontal. Z is vertical (Depth).
        // Confirmed.

        // Real world mapping:
        // 1 degree lat approx 111km.
        // 1 degree lon approx 111km * cos(lat).

        double metersPerDegLat = 111132.0;
        double metersPerDegLon = 111132.0 * Math.cos(Math.toRadians(lat));

        double widthMeters = w * scale;
        double lengthMeters = h * scale; // Y is horizontal length

        double latSpan = lengthMeters / metersPerDegLat;
        double lonSpan = widthMeters / metersPerDegLon;

        double minLat = lat - latSpan / 2.0;
        double maxLat = lat + latSpan / 2.0;
        double minLon = lon - lonSpan / 2.0;
        double maxLon = lon + lonSpan / 2.0;

        float[][] elevations = provider.getElevationGrid(minLat, maxLat, minLon, maxLon, Math.max(w, h));

        // Map grid to voxels
        if (elevations.length == 0)
            return;

        int gridRows = elevations.length;
        int gridCols = elevations[0].length;

        // Find min elevation to normalize to 0 or groundLevel
        float minElev = Float.MAX_VALUE;
        for (float[] row : elevations)
            for (float val : row)
                if (!Float.isNaN(val))
                    minElev = Math.min(minElev, val);

        int depthLim = terrarium.getDepth(); // Z limit

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                // Sample from elevation grid (bilinear interpolation or nearest)
                // Map x / w -> col
                // Map y / h -> row

                // Note: AAIGrid usually starts typically top-left? or bottom-left?
                // Standard is Top-Left (North-West).
                // Y in simulation increases?

                int c = (int) ((x / (float) w) * (gridCols - 1));
                int r = (int) ((y / (float) h) * (gridRows - 1));
                // Clamp
                c = Math.max(0, Math.min(c, gridCols - 1));
                r = Math.max(0, Math.min(r, gridRows - 1));

                float rawElev = elevations[r][c];
                if (Float.isNaN(rawElev))
                    rawElev = minElev;

                int surfaceZ = (int) ((rawElev - minElev) / scale); // Scale elevation to voxels
                surfaceZ = Math.min(surfaceZ, depthLim - 1);
                surfaceZ = Math.max(0, surfaceZ);

                for (int z = 0; z < depthLim; z++) {
                    TerrariumCell.Material material;

                    if (z > surfaceZ) {
                        material = TerrariumCell.Material.AIR;
                    } else if (z == surfaceZ) {
                        material = TerrariumCell.Material.EARTH;
                    } else {
                        material = TerrariumCell.Material.ROCK;
                    }

                    if (material != TerrariumCell.Material.AIR) {
                        terrarium.setCell(new TerrariumCell(
                                x, y, z, material,
                                new float[TerrariumCell.PHEROMONE_TYPES],
                                15f, 50f));
                    }
                }
            }
        }
    }

    /**
     * Add water features (ponds, streams).
     */
    public void addWater(Terrarium terrarium, int waterLevel, float coverage) {
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float n = noise(x * 0.05f, y * 0.05f);
                if (n < coverage - 0.5f) {
                    for (int z = waterLevel - 2; z <= waterLevel; z++) {
                        TerrariumCell cell = new TerrariumCell(
                                x, y, z, TerrariumCell.Material.WATER,
                                new float[TerrariumCell.PHEROMONE_TYPES], 15f, 100f);
                        terrarium.setCell(cell);
                    }
                }
            }
        }
    }

    // Simplified Perlin noise
    private float noise(float x, float y) {
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;

        x -= Math.floor(x);
        y -= Math.floor(y);

        float u = fade(x);
        float v = fade(y);

        int A = permutation[X] + Y;
        int B = permutation[X + 1] + Y;

        return lerp(v,
                lerp(u, grad(permutation[A], x, y), grad(permutation[B], x - 1, y)),
                lerp(u, grad(permutation[A + 1], x, y - 1), grad(permutation[B + 1], x - 1, y - 1)));
    }

    private float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }

    private float grad(int hash, float x, float y) {
        int h = hash & 3;
        float u = h < 2 ? x : y;
        float v = h < 2 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
