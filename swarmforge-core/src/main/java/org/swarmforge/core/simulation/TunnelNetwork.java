/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

/**
 * Manages the underground tunnel system for a colony.
 * Tracks chambers, nodes, and connectivity.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class TunnelNetwork implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    public enum ChamberType {
        ENTRANCE,
        QUEEN_CHAMBER,
        BROOD_CHAMBER,
        FOOD_STORAGE,
        WASTE_DUMP,
        TUNNEL
    }

    public record TunnelNode(
            UUID id,
            float x, float y, float z,
            ChamberType type,
            float temperature,
            float humidity) implements java.io.Serializable {
    }

    public record TunnelEdge(
            UUID fromNode,
            UUID toNode,
            float length) implements java.io.Serializable {
    }

    private final Map<UUID, TunnelNode> nodes = new HashMap<>();
    private final List<TunnelEdge> edges = new ArrayList<>();
    private final float maxDepth = 50.0f;

    public TunnelNetwork(Colony colony) {
        float nx = colony.getNestX();
        float ny = colony.getNestY();
        float nz = colony.getNestZ();

        UUID entrance = createNode(nx, ny, -0.1f, ChamberType.ENTRANCE);
        UUID shaft = createNode(nx, ny, -5.0f, ChamberType.TUNNEL);
        createEdge(entrance, shaft);

        UUID queenChamber = createNode(nx + 3.0f, ny, -12.0f, ChamberType.QUEEN_CHAMBER);
        createEdge(shaft, queenChamber);

        UUID broodChamber = createNode(nx - 3.0f, ny + 2.0f, -8.0f, ChamberType.BROOD_CHAMBER);
        createEdge(shaft, broodChamber);

        UUID foodStorage = createNode(nx + 2.0f, ny - 3.0f, -6.0f, ChamberType.FOOD_STORAGE);
        createEdge(shaft, foodStorage);

        UUID wasteDump = createNode(nx - 4.0f, ny - 2.0f, -15.0f, ChamberType.WASTE_DUMP);
        createEdge(queenChamber, wasteDump);
    }

    /**
     * Dig a new chamber or tunnel extension.
     */
    public UUID dig(UUID parentId, float dx, float dy, float dz, ChamberType type) {
        TunnelNode parent = nodes.get(parentId);
        if (parent == null)
            return null;

        float nx = parent.x() + dx;
        float ny = parent.y() + dy;
        float nz = Math.max(-maxDepth, Math.min(-0.1f, parent.z() + dz));

        UUID newNodeId = createNode(nx, ny, nz, type);
        createEdge(parentId, newNodeId);

        return newNodeId;
    }

    private UUID createNode(float x, float y, float z, ChamberType type) {
        UUID id = UUID.randomUUID();
        // Calculate microclimate based on depth
        float depthFactor = Math.abs(z) / maxDepth;
        float temp = 20f - (5f * depthFactor); // Cooler deeper
        float hum = 50f + (30f * depthFactor); // More humid deeper

        TunnelNode node = new TunnelNode(id, x, y, z, type, temp, hum);
        nodes.put(id, node);
        return id;
    }

    private void createEdge(UUID from, UUID to) {
        TunnelNode n1 = nodes.get(from);
        TunnelNode n2 = nodes.get(to);
        float dist = (float) Math.sqrt(
                Math.pow(n1.x() - n2.x(), 2) +
                        Math.pow(n1.y() - n2.y(), 2) +
                        Math.pow(n1.z() - n2.z(), 2));

        edges.add(new TunnelEdge(from, to, dist));
    }

    public List<TunnelNode> getNodes() {
        return new ArrayList<>(nodes.values());
    }

    public List<TunnelEdge> getEdges() {
        return new ArrayList<>(edges);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    /**
     * Find the nearest chamber of a specific type.
     * Returns null if no such chamber exists.
     */
    public TunnelNode getNearestChamber(ChamberType type, float x, float y, float z) {
        TunnelNode nearest = null;
        float minDistSq = Float.MAX_VALUE;

        for (TunnelNode node : nodes.values()) {
            if (node.type() == type) {
                float dx = node.x() - x;
                float dy = node.y() - y;
                float dz = node.z() - z;
                float distSq = dx * dx + dy * dy + dz * dz;

                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearest = node;
                }
            }
        }
        return nearest;
    }
}
