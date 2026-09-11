/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.compute;

import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.spatial.Morton3D;

import java.util.HashMap;
import java.util.Map;

/**
 * Boundary Halo Synchronizer for Multi-Terrarium Clusters (Megaterrarium).
 * Extracts and blends boundary slices of pheromone grids along contiguous borders (N/S/E/W).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class BoundaryHaloSync {

    public enum BoundaryDirection {
        NORTH, // Y = height - 1 <-> Y = 0 (adjacent NORTH)
        SOUTH, // Y = 0 <-> Y = height - 1 (adjacent SOUTH)
        EAST,  // X = width - 1 <-> X = 0 (adjacent EAST)
        WEST   // X = 0 <-> X = width - 1 (adjacent WEST)
    }

    /**
     * Extracts the 1-cell border slice along the specified direction.
     */
    public Map<Long, float[]> extractBoundarySlice(SparsePheromoneGrid grid, int width, int height, BoundaryDirection dir) {
        Map<Long, float[]> boundaryMap = new HashMap<>();
        if (grid == null) return boundaryMap;

        Map<Long, float[]> all = grid.getAllEntries();
        for (Map.Entry<Long, float[]> entry : all.entrySet()) {
            int[] coords = Morton3D.decode(entry.getKey());
            int x = coords[0];
            int y = coords[1];
            int z = coords[2];

            boolean isBoundary = switch (dir) {
                case NORTH -> (y == height - 1);
                case SOUTH -> (y == 0);
                case EAST  -> (x == width - 1);
                case WEST  -> (x == 0);
            };

            if (isBoundary) {
                boundaryMap.put(entry.getKey(), entry.getValue().clone());
            }
        }
        return boundaryMap;
    }

    /**
     * Applies and blends incoming boundary pheromones from an adjacent terrarium tile.
     */
    public void applyAndBlendBoundarySlice(SparsePheromoneGrid grid, Map<Long, float[]> incomingSlice,
                                           int width, int height, BoundaryDirection fromDir, float blendWeight) {
        if (grid == null || incomingSlice == null || incomingSlice.isEmpty()) return;

        float weight = Math.max(0.0f, Math.min(1.0f, blendWeight));

        for (Map.Entry<Long, float[]> entry : incomingSlice.entrySet()) {
            int[] coords = Morton3D.decode(entry.getKey());
            int x = coords[0];
            int y = coords[1];
            int z = coords[2];

            // Translate incoming boundary coordinate to local boundary coordinate
            int localX = x;
            int localY = y;
            switch (fromDir) {
                case NORTH -> localY = height - 1;
                case SOUTH -> localY = 0;
                case EAST  -> localX = width - 1;
                case WEST  -> localX = 0;
            }

            float[] remoteVals = entry.getValue();
            float[] localVals = grid.getPheromones(localX, localY, z);

            float[] blended = new float[Math.max(localVals.length, remoteVals.length)];
            for (int ch = 0; ch < blended.length; ch++) {
                float loc = ch < localVals.length ? localVals[ch] : 0.0f;
                float rem = ch < remoteVals.length ? remoteVals[ch] : 0.0f;
                blended[ch] = loc * (1.0f - weight) + rem * weight;
            }

            grid.setPheromones(localX, localY, z, blended);
        }
    }
}
