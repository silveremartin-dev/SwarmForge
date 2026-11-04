/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.navigation.FlowFieldGrid;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches 3D navigation flow fields for goal coordinates.
 */
class FlowFieldCacheManager {

    private final Map<Long, FlowFieldGrid> flowFieldCache = new ConcurrentHashMap<>();

    float[] getFlowVector(Terrarium terrarium, float x, float y, float z, int targetX, int targetY, int targetZ) {
        if (terrarium == null) return new float[]{0f, 0f, 0f};
        long key = (((long) targetX & 0xFFFFF) << 40) | (((long) targetY & 0xFFFFF) << 20) | ((long) targetZ & 0xFFFFF);
        FlowFieldGrid flowGrid = flowFieldCache.computeIfAbsent(key, k -> {
            FlowFieldGrid fg = new FlowFieldGrid(
                    terrarium.getWidth(), terrarium.getHeight(), terrarium.getDepth());
            fg.recompute(targetX, targetY, targetZ);
            return fg;
        });
        return flowGrid.getVector(x, y, z);
    }

    void clear() {
        flowFieldCache.clear();
    }
}
