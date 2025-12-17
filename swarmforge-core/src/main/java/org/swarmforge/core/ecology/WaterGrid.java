/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.ecology;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.TunnelNetwork;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
    private final Map<UUID, Float> tunnelWaterLevels = new HashMap<>();

    public WaterGrid(Terrarium terrarium) {
        this.width = (int) terrarium.getWidth(); // Assuming grid resolution matches 1 unit
        this.height = (int) terrarium.getHeight();
        this.surfaceWater = new float[width * height];
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
                                // Water enters tunnel
                                float inflow = Math.min(surfaceWater[idx], 0.5f);
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

        // 4. Evaporation
        for (int i = 0; i < surfaceWater.length; i++) {
            surfaceWater[i] = Math.max(0, surfaceWater[i] - 0.001f);
        }
    }

    private void processTunnelFlow(TunnelNetwork network) {
        for (Map.Entry<UUID, Float> entry : tunnelWaterLevels.entrySet()) {

            if (entry.getValue() <= 0)
                continue;

            // Water flow logic would go here (e.g. moving water to connected lower nodes).
            // Current implementation: Water remains static in the node until evaporation.
        }

        // Simple evaporation in tunnels
        tunnelWaterLevels.replaceAll((k, v) -> Math.max(0, v - 0.005f));
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
