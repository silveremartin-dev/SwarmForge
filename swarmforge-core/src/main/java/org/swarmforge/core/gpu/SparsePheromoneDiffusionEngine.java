/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.gpu;

import org.swarmforge.core.spatial.Morton3D;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles 3D anisotropic advection (wind-coupled) and isotropic molecular diffusion across the sparse pheromone grid.
 */
final class SparsePheromoneDiffusionEngine {

    private SparsePheromoneDiffusionEngine() {}

    static void diffuse(SparsePheromoneGrid grid) {
        ConcurrentHashMap<Long, float[]> deltas = new ConcurrentHashMap<>();
        final float curWindVx = grid.getWindVx();
        final float curWindVy = grid.getWindVy();
        final float windSpeed = (float) Math.sqrt(curWindVx * curWindVx + curWindVy * curWindVy);

        grid.getGrid().entrySet().parallelStream().forEach(e -> {
            Long key = e.getKey();
            SparsePheromoneGrid.PheromoneEntry entry = e.getValue();
            int[] coords = Morton3D.decode(key);
            int x = coords[0], y = coords[1], z = coords[2];

            float surfaceZ = grid.getTerrarium() != null ? grid.getTerrarium().getSurfaceElevation(x, y) : 16.0f;

            for (int t = 0; t < SparsePheromoneGrid.PHEROMONE_TYPES; t++) {
                float conc = grid.computeDecayInternal(entry.getConcentration(t), entry.getLastUpdatedTick(t), t, x, y, z);
                if (conc < 0.001f)
                    continue;

                int passable = countPassableNeighbors(grid, x, y, z, t);
                if (passable == 0)
                    continue;

                float spreadAmount = conc * grid.getDiffusionRates()[t];

                if (z >= surfaceZ && windSpeed > 0.01f) {
                    float advectionStrength = Math.min(0.6f, windSpeed * 0.08f);
                    float wLeft  = grid.isPassable(x - 1, y, z, t) ? Math.max(0.05f, 1.0f - curWindVx * advectionStrength) : 0f;
                    float wRight = grid.isPassable(x + 1, y, z, t) ? Math.max(0.05f, 1.0f + curWindVx * advectionStrength) : 0f;
                    float wDown  = grid.isPassable(x, y - 1, z, t) ? Math.max(0.05f, 1.0f - curWindVy * advectionStrength) : 0f;
                    float wUp    = grid.isPassable(x, y + 1, z, t) ? Math.max(0.05f, 1.0f + curWindVy * advectionStrength) : 0f;
                    float wBelow = grid.isPassable(x, y, z - 1, t) ? 1.0f : 0f;
                    float wAbove = grid.isPassable(x, y, z + 1, t) ? 1.0f : 0f;

                    float sumW = wLeft + wRight + wDown + wUp + wBelow + wAbove;
                    if (sumW > 0.0001f) {
                        if (wLeft > 0f)  accumulateSpread(grid, deltas, x - 1, y, z, t, spreadAmount * (wLeft / sumW));
                        if (wRight > 0f) accumulateSpread(grid, deltas, x + 1, y, z, t, spreadAmount * (wRight / sumW));
                        if (wDown > 0f)  accumulateSpread(grid, deltas, x, y - 1, z, t, spreadAmount * (wDown / sumW));
                        if (wUp > 0f)    accumulateSpread(grid, deltas, x, y + 1, z, t, spreadAmount * (wUp / sumW));
                        if (wBelow > 0f) accumulateSpread(grid, deltas, x, y, z - 1, t, spreadAmount * (wBelow / sumW));
                        if (wAbove > 0f) accumulateSpread(grid, deltas, x, y, z + 1, t, spreadAmount * (wAbove / sumW));
                        entry.update(t, Math.max(0.0f, conc - spreadAmount), grid.getCurrentTick());
                    }
                } else {
                    float amountPerNeighbor = spreadAmount / passable;
                    if (amountPerNeighbor >= 0.001f) {
                        accumulateSpread(grid, deltas, x - 1, y, z, t, amountPerNeighbor);
                        accumulateSpread(grid, deltas, x + 1, y, z, t, amountPerNeighbor);
                        accumulateSpread(grid, deltas, x, y - 1, z, t, amountPerNeighbor);
                        accumulateSpread(grid, deltas, x, y + 1, z, t, amountPerNeighbor);
                        accumulateSpread(grid, deltas, x, y, z - 1, t, amountPerNeighbor);
                        accumulateSpread(grid, deltas, x, y, z + 1, t, amountPerNeighbor);
                        entry.update(t, Math.max(0.0f, conc - spreadAmount), grid.getCurrentTick());
                    }
                }
            }
        });

        deltas.entrySet().parallelStream().forEach(e -> {
            long key = e.getKey();
            float[] amounts = e.getValue();
            int[] pos = Morton3D.decode(key);
            for (int t = 0; t < SparsePheromoneGrid.PHEROMONE_TYPES; t++) {
                if (amounts[t] > 0) {
                    grid.deposit(pos[0], pos[1], pos[2], t, amounts[t]);
                }
            }
        });
    }

    private static void accumulateSpread(SparsePheromoneGrid grid, ConcurrentHashMap<Long, float[]> deltas, int x, int y, int z, int type, float amount) {
        if (grid.isPassable(x, y, z, type)) {
            long key = Morton3D.encode(x, y, z);
            deltas.compute(key, (k, v) -> {
                if (v == null)
                    v = new float[SparsePheromoneGrid.PHEROMONE_TYPES];
                v[type] += amount;
                return v;
            });
        }
    }

    private static int countPassableNeighbors(SparsePheromoneGrid grid, int x, int y, int z, int type) {
        int count = 0;
        if (grid.isPassable(x - 1, y, z, type)) count++;
        if (grid.isPassable(x + 1, y, z, type)) count++;
        if (grid.isPassable(x, y - 1, z, type)) count++;
        if (grid.isPassable(x, y + 1, z, type)) count++;
        if (grid.isPassable(x, y, z - 1, type)) count++;
        if (grid.isPassable(x, y, z + 1, type)) count++;
        return count;
    }
}
