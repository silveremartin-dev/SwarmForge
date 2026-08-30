/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.navigation;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * Deterministic Flow Field (Dijkstra Map) grid for mass agent navigation.
 * Generates O(1) vector direction lookup for thousands of agents heading to a target (food source / nest entrance).
 * 100% deterministic, thread-safe for reads.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class FlowFieldGrid {

    private final int width;
    private final int height;
    private final int depth;

    // Vector direction components (-1.0f to 1.0f) stored in flat primitive arrays for zero cache miss
    private final float[] fieldX;
    private final float[] fieldY;
    private final float[] fieldZ;
    private final int[] distanceMap;

    private int targetX = -1;
    private int targetY = -1;
    private int targetZ = -1;
    private boolean valid = false;

    public FlowFieldGrid(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;

        int totalCells = width * height * depth;
        this.fieldX = new float[totalCells];
        this.fieldY = new float[totalCells];
        this.fieldZ = new float[totalCells];
        this.distanceMap = new int[totalCells];
    }

    /**
     * Deterministically recomputes the flow field distances and directions towards target (x, y, z).
     * Uses 3D Breadth-First-Search (BFS) / Dijkstra integration.
     */
    public synchronized void recompute(int targetX, int targetY, int targetZ) {
        if (targetX < 0 || targetX >= width || targetY < 0 || targetY >= height || targetZ < 0 || targetZ >= depth) {
            return;
        }

        this.targetX = targetX;
        this.targetY = targetY;
        this.targetZ = targetZ;

        int totalCells = width * height * depth;
        Arrays.fill(distanceMap, Integer.MAX_VALUE);
        Arrays.fill(fieldX, 0f);
        Arrays.fill(fieldY, 0f);
        Arrays.fill(fieldZ, 0f);

        int targetIdx = getIndex(targetX, targetY, targetZ);
        distanceMap[targetIdx] = 0;

        Queue<Integer> queue = new ArrayDeque<>();
        queue.add(targetIdx);

        // 26-connected 3D grid directions for deterministic BFS expansion
        final int[] dx = {-1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1, -1, 0, 1};
        final int[] dy = {-1,-1,-1,  0, 0, 0,  1, 1, 1, -1,-1,-1,  0, 0,  1, 1, 1, -1,-1,-1,  0, 0, 0,  1, 1, 1};
        final int[] dz = {-1,-1,-1, -1,-1,-1, -1,-1,-1,  0, 0, 0,  0, 0,  0, 0, 0,  1, 1, 1,  1, 1, 1,  1, 1, 1};

        while (!queue.isEmpty()) {
            int current = queue.poll();
            int cx = current % width;
            int cy = (current / width) % height;
            int cz = current / (width * height);
            int currentDist = distanceMap[current];

            for (int i = 0; i < 26; i++) {
                int nx = cx + dx[i];
                int ny = cy + dy[i];
                int nz = cz + dz[i];

                if (nx >= 0 && nx < width && ny >= 0 && ny < height && nz >= 0 && nz < depth) {
                    int nIdx = getIndex(nx, ny, nz);
                    if (distanceMap[nIdx] == Integer.MAX_VALUE) {
                        distanceMap[nIdx] = currentDist + 1;
                        queue.add(nIdx);
                    }
                }
            }
        }

        // Compute direction vectors towards neighboring cell with lowest distance
        for (int z = 0; z < depth; z++) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = getIndex(x, y, z);
                    int currentDist = distanceMap[idx];
                    if (currentDist == 0 || currentDist == Integer.MAX_VALUE) continue;

                    int bestDx = 0, bestDy = 0, bestDz = 0;
                    int minDist = currentDist;

                    for (int i = 0; i < 26; i++) {
                        int nx = x + dx[i];
                        int ny = y + dy[i];
                        int nz = z + dz[i];

                        if (nx >= 0 && nx < width && ny >= 0 && ny < height && nz >= 0 && nz < depth) {
                            int nDist = distanceMap[getIndex(nx, ny, nz)];
                            if (nDist < minDist) {
                                minDist = nDist;
                                bestDx = dx[i];
                                bestDy = dy[i];
                                bestDz = dz[i];
                            }
                        }
                    }

                    float len = (float) Math.sqrt(bestDx * bestDx + bestDy * bestDy + bestDz * bestDz);
                    if (len > 0.001f) {
                        fieldX[idx] = bestDx / len;
                        fieldY[idx] = bestDy / len;
                        fieldZ[idx] = bestDz / len;
                    }
                }
            }
        }

        this.valid = true;
    }

    /**
     * O(1) deterministic vector query for entity coordinate (x, y, z).
     * Returns direction component array [dx, dy, dz].
     */
    public float[] getVector(float x, float y, float z) {
        int ix = Math.max(0, Math.min(width - 1, (int) x));
        int iy = Math.max(0, Math.min(height - 1, (int) y));
        int iz = Math.max(0, Math.min(depth - 1, (int) z));

        int idx = getIndex(ix, iy, iz);
        return new float[]{fieldX[idx], fieldY[idx], fieldZ[idx]};
    }

    private int getIndex(int x, int y, int z) {
        return x + y * width + z * width * height;
    }

    public boolean isValid() {
        return valid;
    }

    public int getTargetX() {
        return targetX;
    }

    public int getTargetY() {
        return targetY;
    }

    public int getTargetZ() {
        return targetZ;
    }
}
