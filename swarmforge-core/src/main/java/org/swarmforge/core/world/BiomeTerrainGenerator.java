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
 * Biome-aware terrain generator.
 * Generates terrain based on biome presets with realistic material
 * distribution,
 * environmental parameters, and optional real elevation data.
 *
 * <p>
 * Generation features:
 * </p>
 * <ul>
 * <li>Perlin noise for natural height variation</li>
 * <li>Biome-based material selection</li>
 * <li>Light falloff with depth</li>
 * <li>Temperature variation with depth</li>
 * <li>Automatic vegetation placement</li>
 * </ul>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class BiomeTerrainGenerator {

    private final Biome biome;
    private final Random random;
    private final long seed;

    // Perlin noise octaves for natural variation
    private static final int OCTAVES = 4;
    private static final float PERSISTENCE = 0.5f;

    public BiomeTerrainGenerator(Biome biome, long seed) {
        this.biome = biome;
        this.seed = seed;
        this.random = new Random(seed);
    }

    public BiomeTerrainGenerator(Biome biome) {
        this(biome, System.currentTimeMillis());
    }

    /**
     * Generate terrain for the given terrarium.
     * 
     * @param terrarium       Target terrarium to fill
     * @param baseHeight      Base ground level (Z coordinate)
     * @param heightVariation Maximum height variation
     */
    public void generate(Terrarium terrarium, int baseHeight, int heightVariation) {
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        int depth = terrarium.getDepth();

        // Generate heightmap using Perlin noise
        float[][] heightmap = generateHeightmap(width, height, heightVariation);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int surfaceZ = baseHeight + (int) heightmap[x][y];
                surfaceZ = Math.max(1, Math.min(depth - 2, surfaceZ));

                for (int z = 0; z < depth; z++) {
                    TerrariumCell cell;

                    if (z > surfaceZ) {
                        // Above ground - air
                        cell = createAirCell(x, y, z, surfaceZ, depth);
                    } else if (z == surfaceZ) {
                        // Surface layer
                        cell = createSurfaceCell(x, y, z);
                    } else if (z > surfaceZ - 3) {
                        // Topsoil layer (top 3 blocks)
                        cell = createTopsoilCell(x, y, z, surfaceZ);
                    } else {
                        // Underground
                        cell = createUndergroundCell(x, y, z, surfaceZ);
                    }

                    terrarium.setCell(cell);
                }
            }
        }

        // Add vegetation/organic matter on surface
        addVegetation(terrarium, baseHeight, heightVariation);

        // Update ambient conditions
        terrarium.setAmbientTemperature(biome.getAverageTemp());
        terrarium.setAmbientHumidity(biome.getAverageHumidity());
        terrarium.setLatitude(biome.getTypicalLatitude());
    }

    private TerrariumCell createAirCell(int x, int y, int z, int surfaceZ, int depth) {
        float light = Math.min(1f, (float) (z - surfaceZ) / 10f);
        float windX = (random.nextFloat() - 0.5f) * 2f;
        float windY = (random.nextFloat() - 0.5f) * 2f;

        return new TerrariumCell(x, y, z, TerrariumCell.Material.AIR,
                new float[TerrariumCell.PHEROMONE_TYPES],
                biome.getAverageTemp(), biome.getAverageHumidity(),
                TerrariumCell.DEFAULT_CO2, TerrariumCell.DEFAULT_O2,
                light, windX, windY, TerrariumCell.DEFAULT_PRESSURE);
    }

    private TerrariumCell createSurfaceCell(int x, int y, int z) {
        TerrariumCell.Material material = biome.selectMaterial(random);
        float light = 0.8f; // Surface gets good light
        float temp = biome.getAverageTemp() + (random.nextFloat() - 0.5f) * 5f;
        float humidity = biome.getAverageHumidity() + (random.nextFloat() - 0.5f) * 10f;

        return new TerrariumCell(x, y, z, material,
                new float[TerrariumCell.PHEROMONE_TYPES],
                temp, humidity,
                TerrariumCell.DEFAULT_CO2, TerrariumCell.DEFAULT_O2,
                light, 0f, 0f, TerrariumCell.DEFAULT_PRESSURE);
    }

    private TerrariumCell createTopsoilCell(int x, int y, int z, int surfaceZ) {
        int depthFromSurface = surfaceZ - z;
        float light = Math.max(0f, 0.3f - depthFromSurface * 0.1f);
        float temp = biome.getAverageTemp() - depthFromSurface * 0.5f; // Cooler underground
        float humidity = Math.min(100f, biome.getAverageHumidity() + depthFromSurface * 5f);
        float co2 = TerrariumCell.DEFAULT_CO2 * (1 + depthFromSurface * 0.1f);
        float o2 = TerrariumCell.DEFAULT_O2 * (1 - depthFromSurface * 0.02f);

        TerrariumCell.Material material = biome.selectMaterial(random);
        // Topsoil is more likely to be earth
        if (random.nextFloat() < 0.7f) {
            material = TerrariumCell.Material.EARTH;
        }

        return new TerrariumCell(x, y, z, material,
                new float[TerrariumCell.PHEROMONE_TYPES],
                temp, humidity, co2, o2, light, 0f, 0f, TerrariumCell.DEFAULT_PRESSURE);
    }

    private TerrariumCell createUndergroundCell(int x, int y, int z, int surfaceZ) {
        int depthFromSurface = surfaceZ - z;
        float temp = 15f; // Underground is stable ~15°C
        float humidity = 85f; // High humidity underground
        float co2 = TerrariumCell.DEFAULT_CO2 * 1.5f;
        float o2 = TerrariumCell.DEFAULT_O2 * 0.8f;

        // Deep underground is more likely to be rock
        TerrariumCell.Material material;
        if (depthFromSurface > 20 || random.nextFloat() < 0.3f) {
            material = TerrariumCell.Material.ROCK;
        } else {
            material = TerrariumCell.Material.EARTH;
        }

        // Add occasional clay/sand layers
        if (random.nextFloat() < 0.1f) {
            material = random.nextBoolean() ? TerrariumCell.Material.CLAY : TerrariumCell.Material.SAND;
        }

        return new TerrariumCell(x, y, z, material,
                new float[TerrariumCell.PHEROMONE_TYPES],
                temp, humidity, co2, o2, 0f, 0f, 0f, TerrariumCell.DEFAULT_PRESSURE);
    }

    private void addVegetation(Terrarium terrarium, int baseHeight, int heightVariation) {
        float density = biome.getVegetationDensity();
        int width = terrarium.getWidth();
        int height = terrarium.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (random.nextFloat() > density)
                    continue;

                // Find surface
                for (int z = terrarium.getDepth() - 1; z > 0; z--) {
                    TerrariumCell cell = terrarium.getCell(x, y, z);
                    TerrariumCell above = terrarium.getCell(x, y, z + 1);

                    if (cell.isDiggable() && above.material() == TerrariumCell.Material.AIR) {
                        // Place organic matter above surface
                        if (random.nextFloat() < 0.3f) {
                            terrarium.setCell(TerrariumCell.organic(x, y, z + 1));
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Generate a heightmap using multi-octave Perlin noise.
     */
    private float[][] generateHeightmap(int width, int height, int variation) {
        float[][] heightmap = new float[width][height];

        for (int octave = 0; octave < OCTAVES; octave++) {
            float frequency = (float) Math.pow(2, octave);
            float amplitude = (float) Math.pow(PERSISTENCE, octave);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    float nx = x / (float) width * frequency;
                    float ny = y / (float) height * frequency;
                    heightmap[x][y] += noise(nx, ny, seed + octave) * amplitude * variation;
                }
            }
        }

        return heightmap;
    }

    /**
     * Simple 2D noise function (simplified Perlin).
     */
    private float noise(float x, float y, long seed) {
        int ix = (int) Math.floor(x);
        int iy = (int) Math.floor(y);
        float fx = x - ix;
        float fy = y - iy;

        // Smooth interpolation
        fx = fx * fx * (3 - 2 * fx);
        fy = fy * fy * (3 - 2 * fy);

        float n00 = pseudoRandom(ix, iy, seed);
        float n10 = pseudoRandom(ix + 1, iy, seed);
        float n01 = pseudoRandom(ix, iy + 1, seed);
        float n11 = pseudoRandom(ix + 1, iy + 1, seed);

        float nx0 = lerp(n00, n10, fx);
        float nx1 = lerp(n01, n11, fx);

        return lerp(nx0, nx1, fy);
    }

    private float pseudoRandom(int x, int y, long seed) {
        long n = x + y * 57 + seed * 131;
        n = (n << 13) ^ n;
        return (1.0f - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0f);
    }

    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    public Biome getBiome() {
        return biome;
    }

    public long getSeed() {
        return seed;
    }
}
