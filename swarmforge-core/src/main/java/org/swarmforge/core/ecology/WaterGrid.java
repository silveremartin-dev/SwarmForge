/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.ecology;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.TunnelNetwork;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages water simulation including surface flow and tunnel flooding.
 * Uses a simplified cellular automata for surface water and graph flow for
 * tunnels.
 */
public class WaterGrid {

    private final int width;
    private final int height;
    private final float[] surfaceWater; // 1D array for 2D grid [x + y * width]

    // Tunnel water levels (0.0 to 1.0)
    private final Map<UUID, Float> tunnelWaterLevels = new ConcurrentHashMap<>();

    public WaterGrid(Terrarium terrarium) {
        this.width = (int) terrarium.getWidth(); // Assuming grid resolution matches 1 unit
        this.height = (int) terrarium.getHeight();
        this.surfaceWater = new float[width * height];
    }

    /**
     * Clear all water (surface and subterranean tunnels).
     */
    public void clear() {
        java.util.Arrays.fill(surfaceWater, 0f);
        tunnelWaterLevels.clear();
    }

    /**
     * Add rain to the surface.
     * 
     * @param amount Amount of water to add uniformly.
     */
    public void addRain(float amount) {
        for (int i = 0; i < surfaceWater.length; i++) {
            surfaceWater[i] += amount;
        }
    }

    /**
     * Update water physics (flow and infiltration).
     */
    public void tick(Iterable<TunnelNetwork> tunnelNetworks) {
        tick(tunnelNetworks, 0.05f); // 20 FPS default tick duration (0.05s)
    }

    public void tick(Iterable<TunnelNetwork> tunnelNetworks, float deltaSeconds) {
        float effectiveDelta = Math.max(0.001f, Math.min(1.0f, deltaSeconds));

        // 1. Surface Flow (Simplified Diffusion)
        // In a real sim, we'd check terrain height, but for now just diffuse

        // 2. Infiltration to Tunnels
        if (tunnelNetworks != null) {
            for (TunnelNetwork tunnelNetwork : tunnelNetworks) {
                for (TunnelNetwork.TunnelNode node : tunnelNetwork.getNodes()) {
                    if (node.type() == TunnelNetwork.ChamberType.ENTRANCE) {
                        int gx = (int) node.x();
                        int gy = (int) node.y();

                        if (isValid(gx, gy)) {
                            int idx = gx + gy * width;
                            if (surfaceWater[idx] > 0.1f) {
                                // Water enters tunnel scaled by time
                                float inflow = Math.min(surfaceWater[idx], 10.0f * effectiveDelta);
                                surfaceWater[idx] -= inflow;
                                addTunnelWater(node.id(), inflow);
                            }
                        }
                    }
                }

                // 3. Tunnel Flow (Gravity)
                processTunnelFlow(tunnelNetwork);
            }
        }

        // 4. Tunnel Evaporation (time-based rate per second = 0.10f)
        float tunnelEvapRate = 0.10f * effectiveDelta;
        tunnelWaterLevels.replaceAll((k, v) -> Math.max(0, v - tunnelEvapRate));

        // 5. Surface Evaporation & Soil Infiltration/Drainage (time-based rate per second = 0.04f)
        float surfaceDrainRate = 0.04f * effectiveDelta;
        for (int i = 0; i < surfaceWater.length; i++) {
            surfaceWater[i] = Math.max(0, surfaceWater[i] - surfaceDrainRate);
        }
    }

    private void processTunnelFlow(TunnelNetwork network) {
        for (Map.Entry<UUID, Float> entry : tunnelWaterLevels.entrySet()) {

            if (entry.getValue() <= 0)
                continue;

            // Water flow logic would go here (e.g. moving water to connected lower nodes).
            // Current implementation: Water remains static in the node until evaporation.
        }
    }

    public void addTunnelWater(UUID nodeId, float amount) {
        tunnelWaterLevels.merge(nodeId, amount, (a, b) -> a + b);
    }

    public float getTunnelWaterLevel(UUID nodeId) {
        return tunnelWaterLevels.getOrDefault(nodeId, 0f);
    }

    public float getSurfaceWaterAt(int x, int y) {
        if (!isValid(x, y))
            return 0f;
        return surfaceWater[x + y * width];
    }

    public float getWaterAt(float x, float y, float z) {
        if (z >= 0) {
            return getSurfaceWaterAt((int) x, (int) y);
        } else {
            // Spatial lookup for tunnels is expensive without a persistent index.
            // For now, we assume underground areas are dry unless explicitly tracked by
            // NodeID.
            return 0f;
        }
    }

    private boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}
