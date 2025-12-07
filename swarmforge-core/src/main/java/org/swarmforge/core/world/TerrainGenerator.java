/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
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
                        material = TerrariumCell.Material.EARTH; // Topsoil
                    } else if (z > surfaceZ - 3) {
                        material = TerrariumCell.Material.EARTH;
                    } else if (z > surfaceZ - 10) {
                        material = random.nextFloat() < 0.7f ? TerrariumCell.Material.EARTH
                                : TerrariumCell.Material.SAND;
                    } else {
                        material = random.nextFloat() < 0.3f ? TerrariumCell.Material.ROCK
                                : TerrariumCell.Material.EARTH;
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
