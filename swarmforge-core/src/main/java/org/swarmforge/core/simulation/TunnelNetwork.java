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

        String nestType = (colony.getSpecies() != null && colony.getSpecies().getNestType() != null)
                ? colony.getSpecies().getNestType().toUpperCase()
                : "BURROW_UNDERGROUND";

        buildNetworkForArchitecture(nx, ny, nz, nestType);
    }

    public void rebuildForArchitecture(float nx, float ny, float nz, String architectureType) {
        nodes.clear();
        edges.clear();
        buildNetworkForArchitecture(nx, ny, nz, architectureType);
    }

    private void buildNetworkForArchitecture(float nx, float ny, float nz, String nestType) {
        if (nestType.contains("WOOD") || nestType.contains("TREE") || nestType.contains("TRUNK") || nestType.contains("HOLLOW")) {
            // Arboreal / Hollow Tree Cavity (Camponotus / Carpenter ants)
            UUID entrance = createNode(nx, ny - 1.5f, nz + 1.5f, ChamberType.ENTRANCE);
            UUID shaft1 = createNode(nx, ny, nz + 4.0f, ChamberType.TUNNEL);
            UUID shaft2 = createNode(nx, ny, nz + 8.0f, ChamberType.TUNNEL);
            createEdge(entrance, shaft1);
            createEdge(shaft1, shaft2);

            UUID queenChamber = createNode(nx, ny, nz + 6.0f, ChamberType.QUEEN_CHAMBER);
            createEdge(shaft1, queenChamber);

            UUID brood1 = createNode(nx + 2.5f, ny, nz + 3.5f, ChamberType.BROOD_CHAMBER);
            UUID brood2 = createNode(nx - 2.5f, ny, nz + 5.5f, ChamberType.BROOD_CHAMBER);
            createEdge(shaft1, brood1);
            createEdge(queenChamber, brood2);

            UUID foodStorage = createNode(nx + 2.0f, ny, nz + 9.0f, ChamberType.FOOD_STORAGE);
            createEdge(shaft2, foodStorage);

            UUID wasteDump = createNode(nx, ny, nz + 0.5f, ChamberType.WASTE_DUMP);
            createEdge(entrance, wasteDump);

        } else if (nestType.contains("WAX_COMB") || nestType.contains("BEEHIVE") || nestType.contains("HEXAGONAL")) {
            // Honeybee Hexagonal Comb / Beehive (Apis)
            UUID entrance = createNode(nx, ny, nz + 0.5f, ChamberType.ENTRANCE);
            UUID frameShaft = createNode(nx, ny, nz + 3.5f, ChamberType.TUNNEL);
            createEdge(entrance, frameShaft);

            UUID queenCell = createNode(nx, ny, nz + 2.0f, ChamberType.QUEEN_CHAMBER);
            createEdge(entrance, queenCell);

            UUID brood1 = createNode(nx - 2.0f, ny, nz + 4.5f, ChamberType.BROOD_CHAMBER);
            UUID brood2 = createNode(nx + 2.0f, ny, nz + 4.5f, ChamberType.BROOD_CHAMBER);
            createEdge(frameShaft, brood1);
            createEdge(frameShaft, brood2);

            UUID honeyVault = createNode(nx, ny, nz + 7.5f, ChamberType.FOOD_STORAGE);
            createEdge(frameShaft, honeyVault);

        } else if (nestType.contains("WAX_POTS") || nestType.contains("POTS_CLUSTER") || nestType.contains("BOMBUS")) {
            // Bumblebee Pot Cluster (Bombus)
            UUID entrance = createNode(nx, ny, nz + 0.2f, ChamberType.ENTRANCE);
            UUID hub = createNode(nx, ny, nz - 0.5f, ChamberType.TUNNEL);
            createEdge(entrance, hub);

            UUID queenChamber = createNode(nx, ny, nz - 1.2f, ChamberType.QUEEN_CHAMBER);
            createEdge(hub, queenChamber);

            UUID brood1 = createNode(nx + 1.5f, ny + 1.0f, nz - 1.0f, ChamberType.BROOD_CHAMBER);
            UUID brood2 = createNode(nx - 1.5f, ny - 1.0f, nz - 1.0f, ChamberType.BROOD_CHAMBER);
            createEdge(hub, brood1);
            createEdge(hub, brood2);

            UUID foodPots = createNode(nx + 1.0f, ny - 1.5f, nz - 0.8f, ChamberType.FOOD_STORAGE);
            createEdge(hub, foodPots);

            UUID wastePot = createNode(nx - 1.0f, ny + 1.5f, nz - 1.5f, ChamberType.WASTE_DUMP);
            createEdge(queenChamber, wastePot);

        } else if (nestType.contains("PAPER") || nestType.contains("PEDUNCULATE") || nestType.contains("VESPA")) {
            // Paper Wasp Hanging Nest (Vespidae)
            UUID peduncle = createNode(nx, ny, nz + 7.5f, ChamberType.TUNNEL);
            UUID entrance = createNode(nx, ny, nz + 1.5f, ChamberType.ENTRANCE);
            UUID centralSpire = createNode(nx, ny, nz + 4.5f, ChamberType.TUNNEL);
            createEdge(peduncle, centralSpire);
            createEdge(centralSpire, entrance);

            UUID queenCell = createNode(nx, ny, nz + 6.0f, ChamberType.QUEEN_CHAMBER);
            createEdge(centralSpire, queenCell);

            UUID brood1 = createNode(nx - 2.0f, ny, nz + 4.0f, ChamberType.BROOD_CHAMBER);
            UUID brood2 = createNode(nx + 2.0f, ny, nz + 4.0f, ChamberType.BROOD_CHAMBER);
            createEdge(centralSpire, brood1);
            createEdge(centralSpire, brood2);

            UUID foodVault = createNode(nx, ny + 2.0f, nz + 5.0f, ChamberType.FOOD_STORAGE);
            createEdge(centralSpire, foodVault);

        } else if (nestType.contains("CATHEDRAL") || nestType.contains("TERMITE") || nestType.contains("STERCORAL")) {
            // Termite Cathedral Mound (Isoptera)
            UUID entrance = createNode(nx, ny, nz + 0.5f, ChamberType.ENTRANCE);
            UUID spire = createNode(nx, ny, nz + 9.0f, ChamberType.TUNNEL);
            UUID shaft = createNode(nx, ny, nz - 3.0f, ChamberType.TUNNEL);
            createEdge(entrance, spire);
            createEdge(entrance, shaft);

            UUID royalCell = createNode(nx, ny, nz - 8.0f, ChamberType.QUEEN_CHAMBER);
            createEdge(shaft, royalCell);

            UUID nursery = createNode(nx + 3.0f, ny, nz - 5.0f, ChamberType.BROOD_CHAMBER);
            createEdge(shaft, nursery);

            UUID foodFungus = createNode(nx - 3.0f, ny, nz - 6.0f, ChamberType.FOOD_STORAGE);
            createEdge(shaft, foodFungus);

            UUID wasteVault = createNode(nx, ny + 3.0f, nz - 12.0f, ChamberType.WASTE_DUMP);
            createEdge(royalCell, wasteVault);

        } else if (nestType.contains("FUNGI") || nestType.contains("VAULT") || nestType.contains("ATTA")) {
            // Leafcutter Ant Subterranean Fungi Vault (Atta)
            UUID entrance1 = createNode(nx - 3.0f, ny, nz + 0.1f, ChamberType.ENTRANCE);
            UUID entrance2 = createNode(nx + 3.0f, ny, nz + 0.1f, ChamberType.ENTRANCE);
            UUID mainShaft = createNode(nx, ny, nz - 6.0f, ChamberType.TUNNEL);
            createEdge(entrance1, mainShaft);
            createEdge(entrance2, mainShaft);

            UUID fungusVault1 = createNode(nx - 5.0f, ny + 2.0f, nz - 10.0f, ChamberType.FOOD_STORAGE);
            UUID fungusVault2 = createNode(nx + 5.0f, ny - 2.0f, nz - 12.0f, ChamberType.BROOD_CHAMBER);
            createEdge(mainShaft, fungusVault1);
            createEdge(mainShaft, fungusVault2);

            UUID royalVault = createNode(nx, ny, nz - 14.0f, ChamberType.QUEEN_CHAMBER);
            createEdge(mainShaft, royalVault);

            UUID wastePit = createNode(nx, ny, nz - 20.0f, ChamberType.WASTE_DUMP);
            createEdge(royalVault, wastePit);

        } else if (nestType.contains("MOUND") || nestType.contains("SURFACE_MOUND") || nestType.contains("FORMICA")) {
            // Thatch Mound & Galleries (Formica rufa)
            UUID ent1 = createNode(nx - 2.0f, ny, nz + 0.5f, ChamberType.ENTRANCE);
            UUID ent2 = createNode(nx + 2.0f, ny, nz + 0.5f, ChamberType.ENTRANCE);
            UUID solarium = createNode(nx, ny, nz + 4.0f, ChamberType.BROOD_CHAMBER);
            UUID shaft = createNode(nx, ny, nz - 3.0f, ChamberType.TUNNEL);
            createEdge(ent1, shaft);
            createEdge(ent2, shaft);
            createEdge(shaft, solarium);

            UUID queenWinter = createNode(nx, ny, nz - 10.0f, ChamberType.QUEEN_CHAMBER);
            createEdge(shaft, queenWinter);

            UUID subBrood = createNode(nx - 3.0f, ny + 1.0f, nz - 6.0f, ChamberType.BROOD_CHAMBER);
            createEdge(shaft, subBrood);

            UUID foodPantry = createNode(nx + 2.0f, ny - 2.0f, nz - 4.0f, ChamberType.FOOD_STORAGE);
            createEdge(shaft, foodPantry);

            UUID wastePit = createNode(nx - 2.0f, ny - 3.0f, nz - 12.0f, ChamberType.WASTE_DUMP);
            createEdge(queenWinter, wastePit);

        } else if (nestType.contains("SILK") || nestType.contains("LEAF") || nestType.contains("OECOPHYLLA")) {
            // Arboreal Weaver Ant Leaf Nest (Oecophylla)
            UUID branch = createNode(nx, ny, nz + 6.0f, ChamberType.ENTRANCE);
            UUID canopyHub = createNode(nx, ny, nz + 10.0f, ChamberType.TUNNEL);
            createEdge(branch, canopyHub);

            UUID queenLeaf = createNode(nx, ny, nz + 9.0f, ChamberType.QUEEN_CHAMBER);
            createEdge(canopyHub, queenLeaf);

            UUID broodLeaf = createNode(nx - 2.5f, ny, nz + 8.5f, ChamberType.BROOD_CHAMBER);
            UUID foodLeaf = createNode(nx + 2.5f, ny, nz + 8.5f, ChamberType.FOOD_STORAGE);
            createEdge(canopyHub, broodLeaf);
            createEdge(canopyHub, foodLeaf);

        } else if (nestType.contains("CARTON")) {
            // Carton Wood Nest (Crematogaster)
            UUID entrance = createNode(nx, ny, nz + 4.0f, ChamberType.ENTRANCE);
            UUID core = createNode(nx, ny, nz + 6.0f, ChamberType.TUNNEL);
            createEdge(entrance, core);

            UUID queenCell = createNode(nx, ny, nz + 5.5f, ChamberType.QUEEN_CHAMBER);
            createEdge(core, queenCell);

            UUID broodG = createNode(nx - 2.0f, ny, nz + 7.0f, ChamberType.BROOD_CHAMBER);
            UUID foodG = createNode(nx + 2.0f, ny, nz + 7.0f, ChamberType.FOOD_STORAGE);
            createEdge(core, broodG);
            createEdge(core, foodG);

        } else if (nestType.contains("BAMBOO") || nestType.contains("STEM")) {
            // Bamboo / Plant Stem Nest (Colobopsis)
            UUID entrance = createNode(nx - 6.0f, ny, nz + 2.0f, ChamberType.ENTRANCE);
            UUID stemTunnel = createNode(nx, ny, nz + 2.0f, ChamberType.TUNNEL);
            createEdge(entrance, stemTunnel);

            UUID queenChamber = createNode(nx + 2.0f, ny, nz + 2.0f, ChamberType.QUEEN_CHAMBER);
            createEdge(stemTunnel, queenChamber);

            UUID broodChamber = createNode(nx + 5.0f, ny, nz + 2.0f, ChamberType.BROOD_CHAMBER);
            createEdge(queenChamber, broodChamber);

            UUID foodStorage = createNode(nx - 2.0f, ny, nz + 2.0f, ChamberType.FOOD_STORAGE);
            createEdge(stemTunnel, foodStorage);

        } else if (nestType.contains("BIVOUAC")) {
            // Living Army Ant Bivouac (Eciton)
            UUID anchor = createNode(nx, ny, nz + 3.0f, ChamberType.TUNNEL);
            UUID entrance = createNode(nx, ny, nz + 1.5f, ChamberType.ENTRANCE);
            createEdge(anchor, entrance);

            UUID protectedCore = createNode(nx, ny, nz + 0.5f, ChamberType.QUEEN_CHAMBER);
            createEdge(entrance, protectedCore);

            UUID broodCluster = createNode(nx - 2.0f, ny, nz - 0.5f, ChamberType.BROOD_CHAMBER);
            UUID foodCluster = createNode(nx + 2.0f, ny, nz - 0.5f, ChamberType.FOOD_STORAGE);
            createEdge(protectedCore, broodCluster);
            createEdge(protectedCore, foodCluster);

        } else {
            // Default Subterranean Burrow (Lasius / Solenopsis)
            UUID entrance = createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE);
            UUID shaft = createNode(nx, ny, nz - 5.0f, ChamberType.TUNNEL);
            createEdge(entrance, shaft);

            UUID queenChamber = createNode(nx + 3.0f, ny, nz - 12.0f, ChamberType.QUEEN_CHAMBER);
            createEdge(shaft, queenChamber);

            UUID broodChamber = createNode(nx - 3.0f, ny + 2.0f, nz - 8.0f, ChamberType.BROOD_CHAMBER);
            createEdge(shaft, broodChamber);

            UUID foodStorage = createNode(nx + 2.0f, ny - 3.0f, nz - 6.0f, ChamberType.FOOD_STORAGE);
            createEdge(shaft, foodStorage);

            UUID wasteDump = createNode(nx - 4.0f, ny - 2.0f, nz - 15.0f, ChamberType.WASTE_DUMP);
            createEdge(queenChamber, wasteDump);
        }
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
        float nz = parent.z() + dz;

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
