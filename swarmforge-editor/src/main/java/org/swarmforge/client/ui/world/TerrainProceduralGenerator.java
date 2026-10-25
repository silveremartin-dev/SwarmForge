/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * High-fidelity mathematical and procedural generator for geological, pedological,
 * and hydrological voxel landscapes in SwarmForge.
 */
public final class TerrainProceduralGenerator {

    private TerrainProceduralGenerator() {
        // Utility class
    }

    // =========================================================================
    // 3D VALUE NOISE & FRACTAL BROWNIAN MOTION (FBM)
    // =========================================================================

    public static double fBmNoise(double x, double y, int octaves, double persistence, double lacunarity) {
        double total = 0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxValue = 0;
        for (int i = 0; i < octaves; i++) {
            total += valueNoise3D(x * frequency + i * 17.3, y * frequency + i * 31.7, i * 11.1) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return total / maxValue;
    }

    public static double fBmNoiseSeeded(double x, double y, int octaves, double persistence, double lacunarity, long seed) {
        double total = 0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxValue = 0;
        double offset = (seed % 10000) * 0.1;
        for (int i = 0; i < octaves; i++) {
            total += valueNoise3D(x * frequency + i * 17.3 + offset, y * frequency + i * 31.7 + offset, i * 11.1 + offset) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= lacunarity;
        }
        return total / maxValue;
    }

    public static double valueNoise3D(double x, double y, double z) {
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y), iz = (int) Math.floor(z);
        double fx = x - ix, fy = y - iy, fz = z - iz;
        fx = fx * fx * (3 - 2 * fx);
        fy = fy * fy * (3 - 2 * fy);
        fz = fz * fz * (3 - 2 * fz);
        double c000 = h3(ix, iy, iz), c100 = h3(ix + 1, iy, iz), c010 = h3(ix, iy + 1, iz), c110 = h3(ix + 1, iy + 1, iz);
        double c001 = h3(ix, iy, iz + 1), c101 = h3(ix + 1, iy, iz + 1), c011 = h3(ix, iy + 1, iz + 1), c111 = h3(ix + 1, iy + 1, iz + 1);
        return lrp(bilerp(c000, c100, c010, c110, fx, fy), bilerp(c001, c101, c011, c111, fx, fy), fz);
    }

    private static double h3(int x, int y, int z) {
        int n = x * 1301 + y * 14057 + z * 199999;
        n ^= (n >>> 8);
        n *= 0x45d9f3b;
        n ^= (n >>> 8);
        return ((n & 0x7fffffff) / (double) 0x7fffffff);
    }

    public static double lrp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private static double bilerp(double c00, double c10, double c01, double c11, double u, double v) {
        return lrp(lrp(c00, c10, u), lrp(c01, c11, u), v);
    }

    // =========================================================================
    // HYDROLOGY: RIVER PATH & CANYON BED CARVING
    // =========================================================================

    public static List<int[]> computeRiverPath(long seed, int gridSize) {
        List<int[]> path = new ArrayList<>();
        Random rand = new Random(seed);
        int startX = 15 + rand.nextInt(Math.max(1, gridSize - 30));
        int cx = startX, cy = 0;

        while (cy < gridSize && cx >= 0 && cx < gridSize) {
            path.add(new int[]{cx, cy});
            if (cy == gridSize - 1) break;
            cy++;
            double meanderProb = 0.35 + (rand.nextDouble() * 0.30);
            if (rand.nextDouble() < meanderProb) {
                cx += (rand.nextBoolean() ? 1 : -1);
            }
            cx = Math.max(4, Math.min(gridSize - 5, cx));
        }

        return path;
    }

    public static boolean isNearRiver(List<int[]> riverPath, int x, int y, int distMax) {
        if (riverPath == null || riverPath.isEmpty()) return false;
        for (int[] pt : riverPath) {
            if (Math.abs(pt[0] - x) <= distMax && Math.abs(pt[1] - y) <= distMax) {
                if ((pt[0] - x) * (pt[0] - x) + (pt[1] - y) * (pt[1] - y) <= distMax * distMax) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void carveRiverBed(double[][] heightGrid, List<int[]> riverPath, double riverWidthMm, int gridSize) {
        if (riverPath == null || riverPath.isEmpty()) return;
        double riverWidthVox = Math.max(2.0, riverWidthMm / 25.0);
        double maxCarveDepth = 0.045; // Gentle natural riverbed/canyon incision depth
        double maxR = Math.max(1.0, riverWidthVox / 2.0) + 1.5;

        double[][] minRiverDist = new double[gridSize][gridSize];
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                minRiverDist[x][y] = Double.MAX_VALUE;
            }
        }

        for (int[] pt : riverPath) {
            int rx = pt[0], ry = pt[1];
            int rRad = (int) Math.ceil(maxR);
            for (int dx = -rRad; dx <= rRad; dx++) {
                for (int dy = -rRad; dy <= rRad; dy++) {
                    int cx = rx + dx, cy = ry + dy;
                    if (cx >= 0 && cx < gridSize && cy >= 0 && cy < gridSize) {
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist < minRiverDist[cx][cy]) {
                            minRiverDist[cx][cy] = dist;
                        }
                    }
                }
            }
        }

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                double dist = minRiverDist[x][y];
                if (dist <= maxR) {
                    double profile = 1.0 - (dist / maxR) * (dist / maxR);
                    double incision = maxCarveDepth * profile;
                    heightGrid[x][y] = Math.max(0.02, heightGrid[x][y] - incision);
                }
            }
        }
    }

    // =========================================================================
    // PEDOLOGY & SOIL STRATIGRAPHY (HORIZONS O, A, B, C, R)
    // =========================================================================

    public static byte pickSurfaceMaterial(int[] pct, double rand01) {
        double r = rand01 * 100;
        if (r < pct[0]) return 0; // Earth
        r -= pct[0];
        if (r < pct[1]) return 1; // Sand
        r -= pct[1];
        if (r < pct[2]) return 2; // Clay
        r -= pct[2];
        if (r < pct[3]) return 3; // Stone
        return 5; // Horizon O (Litter / Organic matter)
    }

    public static byte pickSurfaceMaterialCoherent(int x, int y, int[] surfacePct, double[][] heightGrid, float[][][] humidityGrid, List<int[]> riverPath) {
        if (isNearRiver(riverPath, x, y, 3)) {
            double rNoise = valueNoise3D(x * 0.15, y * 0.15, 88);
            return rNoise < 0.6 ? (byte) 1 : (byte) 3;
        }
        double h = heightGrid[x][y];
        if (h > 0.72) return 3;
        if (h < 0.28 && humidityGrid != null && humidityGrid[x][y][0] > 0.45) return 2;

        double spatialNoise = valueNoise3D(x * 0.05, y * 0.05, 12);
        return pickSurfaceMaterial(surfacePct, spatialNoise);
    }

    public static void generateSoilLayersSeeded(byte[][][] soilLayers, double[][] heightGrid, float[][][] humidityGrid,
                                               List<int[]> riverPath, boolean hasRiver, double stratification, double mixing,
                                               int[] surfacePct, int organicPct, double riverWidthMm, long seed, int gridSize, int soilDepth) {
        double organicHorizonLimit = (organicPct > 0) ? Math.min(0.06, 0.01 + (organicPct / 100.0) * 0.05) : 0.0;
        double offset = (seed % 10000) * 0.1;
        int rAvoid = (int) Math.max(4, Math.round(riverWidthMm / 25.0 / 2.0) + 3);

        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                boolean isRiverNear = hasRiver && isNearRiver(riverPath, x, y, rAvoid);
                for (int d = 0; d < soilDepth; d++) {
                    double depthRatio = (double) d / (soilDepth - 1);
                    double noise = valueNoise3D(x * 0.22 + offset, y * 0.22 + offset, d * 0.5 + 10 + offset);

                    // Authentic Pedological Stratigraphy (Horizons du Sol):
                    // Horizon O (0-3% depth): Litière Organique / Détritus (5)
                    // Horizon A (3-20% depth): Sol de Surface / Humus / Sable / Terre
                    // Horizon B (20-55% depth): Argile Limoneuse (2)
                    // Horizon C (55-80% depth): Gravier & Cailloutis d'Altération (4)
                    // Horizon R (80-100% depth): Bedrock / Roche Mère Continue (3)
                    double blend = (noise - 0.5) * mixing * 1.4;
                    double effectiveDepth = Math.max(0.0, Math.min(1.0, depthRatio + blend * (1.0 - stratification * 0.65)));

                    byte mat;
                    if (effectiveDepth < organicHorizonLimit) {
                        mat = 5; // Horizon O
                    } else if (effectiveDepth < 0.20) {
                        mat = isRiverNear ? (byte) 1 : pickSurfaceMaterialCoherent(x, y, surfacePct, heightGrid, humidityGrid, riverPath);
                    } else if (effectiveDepth < 0.55) {
                        mat = 2; // Horizon B (Clay)
                    } else if (effectiveDepth < 0.80) {
                        mat = 4; // Horizon C (Gravel)
                    } else {
                        mat = 3; // Horizon R (Bedrock)
                    }
                    soilLayers[x][y][d] = mat;
                }
            }
        }
    }

    // =========================================================================
    // HYDROLOGY: 3D MOISTURE TENSOR & CAPILLARY WATER TABLE
    // =========================================================================

    public static void generateHumidity(float[][][] humidityGrid, double[][] heightGrid, byte[][][] soilLayers,
                                        List<int[]> riverPath, boolean hasRiver, double baseHumidity,
                                        double wtDepthMeters, double totalDepthM, int gridSize, int soilDepth) {
        double avgHeight = 0.0;
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                avgHeight += heightGrid[x][y];
            }
        }
        avgHeight /= (gridSize * gridSize);

        for (int x = 0; x < gridSize; x++) {
            int xPrev = Math.max(0, x - 1);
            int xNext = Math.min(gridSize - 1, x + 1);

            for (int y = 0; y < gridSize; y++) {
                int yPrev = Math.max(0, y - 1);
                int yNext = Math.min(gridSize - 1, y + 1);

                double dzdx = heightGrid[xNext][y] - heightGrid[xPrev][y];
                double dzdy = heightGrid[x][yNext] - heightGrid[x][yPrev];
                double slope = Math.sqrt(dzdx * dzdx + dzdy * dzdy);

                double slopeDrainage = -Math.min(0.35, slope * 2.2);
                double valleyAccumulation = (heightGrid[x][y] < avgHeight) ? (avgHeight - heightGrid[x][y]) * 0.40 : 0.0;

                boolean nearRiver = hasRiver && isNearRiver(riverPath, x, y, 4);
                double riverBoost = nearRiver ? 0.40 : 0.0;

                for (int d = 0; d < soilDepth; d++) {
                    double voxelDepthMeters = ((double) d / (soilDepth - 1)) * totalDepthM;
                    double distToWaterTable = wtDepthMeters - voxelDepthMeters;

                    double waterTableMoisture = 0.0;
                    if (distToWaterTable <= 0.0) {
                        waterTableMoisture = 1.0;
                    } else if (distToWaterTable < 2.0) {
                        waterTableMoisture = 1.0 - (distToWaterTable / 2.0);
                    }

                    byte mat = soilLayers != null ? soilLayers[x][y][d] : 0;
                    double matRetention = switch (mat) {
                        case 2 -> 0.15;  // Clay
                        case 6 -> 0.35;  // Peat
                        case 1 -> -0.15; // Sand
                        case 0 -> 0.08;  // Earth
                        default -> 0.0;
                    };

                    double noise = valueNoise3D(x * 0.15, y * 0.15, d * 0.25 + 99);
                    double topsoilMoisture = baseHumidity + slopeDrainage + valleyAccumulation + matRetention + riverBoost + (noise - 0.5) * 0.15;

                    humidityGrid[x][y][d] = (float) Math.max(0.0, Math.min(1.0,
                            Math.max(topsoilMoisture, waterTableMoisture)));
                }
            }
        }
    }

    // =========================================================================
    // VOIDS, TEMPERATURE & PH FIELDS
    // =========================================================================

    public static void generateVoids(boolean[][][] voidGrid, byte[][][] soilLayers, double density, int gridSize, int soilDepth) {
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                for (int d = 0; d < soilDepth; d++) {
                    voidGrid[x][y][d] = false;
                    if (d < 2) continue;
                    double noise = valueNoise3D(x * 0.3, y * 0.3, d * 0.8);
                    double threshold = 1.0 - density * (0.5 + 0.5 * d / soilDepth);
                    voidGrid[x][y][d] = (noise > threshold);
                    if (voidGrid[x][y][d] && soilLayers != null && soilLayers[x][y][d] == 1) {
                        voidGrid[x][y][d] = false; // Sand cannot hold void chambers without collapse
                    }
                }
            }
        }
    }

    public static void generateTemperatureGrid(float[][][] tempGrid, float[][][] humidityGrid, double surfaceTemp, int gridSize, int soilDepth) {
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                for (int d = 0; d < soilDepth; d++) {
                    double depthRatio = (double) d / (soilDepth - 1);
                    double depthCooling = depthRatio * 4.5;
                    double moistureCooling = (humidityGrid != null) ? humidityGrid[x][y][d] * 1.5 : 0.0;
                    double noise = valueNoise3D(x * 0.15, y * 0.15, d * 0.3 + 200);
                    tempGrid[x][y][d] = (float) Math.max(-5.0, surfaceTemp - depthCooling - moistureCooling + (noise - 0.5) * 1.2);
                }
            }
        }
    }

    public static void generatePHGrid(float[][][] phGrid, byte[][][] soilLayers, boolean pineLitter, double basePh, int gridSize, int soilDepth) {
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                for (int d = 0; d < soilDepth; d++) {
                    byte mat = soilLayers != null ? soilLayers[x][y][d] : 0;
                    double depthRatio = (double) d / (soilDepth - 1);
                    double phOffset = switch (mat) {
                        case 0 -> -0.3; // Humus organic slightly acidic
                        case 1 -> 0.1;  // Sand neutral
                        case 2 -> -0.4; // Clay acidic
                        case 3 -> 0.9;  // Limestone stone alkaline
                        case 4 -> 0.4;  // Gravel alkaline
                        case 5 -> -0.6; // Organic litter acidic
                        default -> 0.0;
                    };
                    if (pineLitter && d < 3) {
                        phOffset -= 0.5; // Pine needles acidify top soil
                    }
                    double depthAlkalinity = depthRatio * 0.45; // Deep bedrock tends towards alkaline
                    double noise = valueNoise3D(x * 0.1, y * 0.1, d * 0.2 + 350);
                    phGrid[x][y][d] = (float) Math.max(4.0, Math.min(9.0, basePh + phOffset + depthAlkalinity + (noise - 0.5) * 0.6));
                }
            }
        }
    }
}
