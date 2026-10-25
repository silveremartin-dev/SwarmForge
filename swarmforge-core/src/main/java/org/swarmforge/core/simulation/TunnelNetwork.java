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
 * Manages the underground and arboreal tunnel system for a colony.
 * Tracks chambers, nodes, lenticular dimensions, meandering waypoints, and connectivity.
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
        FUNGUS_GARDEN,
        HIBERNATION,
        SOLARIUM,
        HONEYCOMB,
        POLLEN_POT,
        VENTILATION_CHIMNEY,
        LEAF_CACHE,
        GALL_NURSERY,
        BIVOUAC_CORE,
        TUNNEL
    }

    public record TunnelNode(
            UUID id,
            float x, float y, float z,
            ChamberType type,
            float temperature,
            float humidity,
            float radiusX, float radiusY, float radiusZ) implements java.io.Serializable {

        public TunnelNode(UUID id, float x, float y, float z, ChamberType type, float temperature, float humidity) {
            this(id, x, y, z, type, temperature, humidity,
                    defaultRadius(type, true), defaultRadius(type, true), defaultRadius(type, false));
        }

        public TunnelNode(UUID id, float x, float y, float z, ChamberType type, float radiusX, float radiusY, float radiusZ) {
            this(id, x, y, z, type, 22.0f, 65.0f, radiusX, radiusY, radiusZ);
        }

        public TunnelNode(UUID id, float x, float y, float z, ChamberType type, float radius) {
            this(id, x, y, z, type, 22.0f, 65.0f, radius, radius, radius);
        }

        public TunnelNode(UUID id, float x, float y, float z, ChamberType type) {
            this(id, x, y, z, type, 20.0f, 0.5f);
        }

        private static float defaultRadius(ChamberType type, boolean horizontal) {
            return switch (type) {
                case QUEEN_CHAMBER -> horizontal ? 0.55f : 0.30f;
                case FUNGUS_GARDEN -> horizontal ? 0.50f : 0.28f;
                case BROOD_CHAMBER -> horizontal ? 0.40f : 0.22f;
                case FOOD_STORAGE, LEAF_CACHE -> horizontal ? 0.40f : 0.22f;
                case WASTE_DUMP -> horizontal ? 0.35f : 0.20f;
                case HIBERNATION -> horizontal ? 0.45f : 0.24f;
                case BIVOUAC_CORE -> horizontal ? 0.50f : 0.30f;
                case SOLARIUM -> horizontal ? 0.45f : 0.24f;
                case HONEYCOMB -> horizontal ? 0.40f : 0.25f;
                case POLLEN_POT -> horizontal ? 0.35f : 0.22f;
                case VENTILATION_CHIMNEY -> horizontal ? 0.22f : 0.45f;
                case GALL_NURSERY -> horizontal ? 0.30f : 0.20f;
                case ENTRANCE -> horizontal ? 0.25f : 0.18f;
                case TUNNEL -> 0.15f;
            };
        }

        public float radius() {
            return (radiusX + radiusY + radiusZ) / 3.0f;
        }
    }

    public record TunnelEdge(
            UUID fromNode,
            UUID toNode,
            float length,
            List<float[]> pathPoints) implements java.io.Serializable {

        public TunnelEdge(UUID fromNode, UUID toNode, float length) {
            this(fromNode, toNode, length, new ArrayList<>());
        }
    }

    private final Map<UUID, TunnelNode> nodes = new HashMap<>();
    private final List<TunnelEdge> edges = new ArrayList<>();
    private float maxDepthSetting = 50.0f;
    private float tunnelWidthSetting = 2.0f;
    private float scaleFactorSetting = 1.0f;
    private float waterTableDepth = 18.0f;
    private float bedrockDepth = 25.0f;

    public float getMaxDepthSetting() { return maxDepthSetting; }
    public void setMaxDepthSetting(float maxDepth) { this.maxDepthSetting = Math.max(2.0f, maxDepth); }
    public float getTunnelWidthSetting() { return tunnelWidthSetting; }
    public void setTunnelWidthSetting(float width) { this.tunnelWidthSetting = Math.max(1.0f, width); }
    public float getScaleFactorSetting() { return scaleFactorSetting; }
    public void setScaleFactorSetting(float scale) { this.scaleFactorSetting = Math.max(0.1f, scale); }
    public float getWaterTableDepth() { return waterTableDepth; }
    public void setWaterTableDepth(float depth) { this.waterTableDepth = Math.max(2.0f, depth); }
    public float getBedrockDepth() { return bedrockDepth; }
    public void setBedrockDepth(float depth) { this.bedrockDepth = Math.max(3.0f, depth); }

    public TunnelNode getNode(UUID id) { return nodes.get(id); }
    public TunnelNode getNode(String id) {
        try {
            return nodes.get(UUID.fromString(id));
        } catch (Exception e) {
            return null;
        }
    }

    public TunnelNetwork(Colony colony) {
        float nx = colony != null ? colony.getNestX() : 32.0f;
        float ny = colony != null ? colony.getNestY() : 32.0f;
        float nz = colony != null ? colony.getNestZ() : 0.0f;
        if (nz <= 0.5f && colony != null && colony.getTerrarium() != null) {
            nz = colony.getTerrarium().getSurfaceElevation(nx, ny);
        }
        if (nz <= 0.5f) {
            nz = 10.0f;
        }

        String nestType = (colony != null && colony.getSpecies() != null && colony.getSpecies().getNestType() != null)
                ? colony.getSpecies().getNestType().toUpperCase()
                : "BURROW_UNDERGROUND";

        rebuildForArchitecture(nx, ny, nz, nestType, colony);
    }

    public void rebuildForArchitecture(float nx, float ny, float nz, String architectureType) {
        rebuildForArchitecture(nx, ny, nz, architectureType, (org.swarmforge.core.structure.Nest) null);
    }

    public enum OntogenyStage {
        FOUNDING_CLAUSTRAL, // 1 queen founding alone (Stage 0: 1 queen, 0 workers)
        INCIPIENT,          // First nanitics (Stage 1: 2-25 workers)
        DEVELOPING,         // Expanding colony (Stage 2: 26-100 workers)
        MATURE_CLIMAX       // Mature climax supercolony (Stage 3: >100 workers)
    }

    private OntogenyStage currentOntogenyStage = OntogenyStage.FOUNDING_CLAUSTRAL;

    public OntogenyStage getOntogenyStage() { return currentOntogenyStage; }

    public void setOntogenyStage(OntogenyStage stage) { this.currentOntogenyStage = stage; }

    public void rebuildForArchitecture(float nx, float ny, float nz, String architectureType, Colony colony) {
        rebuildForArchitecture(nx, ny, nz, architectureType, colony, maxDepthSetting, tunnelWidthSetting, scaleFactorSetting);
    }

    public void rebuildForArchitecture(float nx, float ny, float nz, String architectureType, Colony colony, float maxDepth, float tunnelWidth, float scaleFactor) {
        this.maxDepthSetting = maxDepth;
        this.tunnelWidthSetting = tunnelWidth;
        this.scaleFactorSetting = scaleFactor;
        if (colony != null) {
            colony.setNestPosition(nx, ny, nz);
        }
        int pop = (colony != null) ? Math.max(colony.getPopulation(), colony.getLivingIndividuals().size()) : 500;
        rebuildForArchitecture(nx, ny, nz, architectureType, colony != null ? colony.getNest() : null, pop);
        if (colony != null && colony.getTerrarium() != null) {
            carveIntoTerrarium(colony.getTerrarium());
        }
    }

    public void rebuildForArchitecture(float nx, float ny, float nz, String architectureType, org.swarmforge.core.structure.Nest nest) {
        rebuildForArchitecture(nx, ny, nz, architectureType, nest, 500);
    }

    public void rebuildForArchitecture(float nx, float ny, float nz, String architectureType, org.swarmforge.core.structure.Nest nest, int population) {
        nodes.clear();
        edges.clear();
        buildNetworkForArchitecture(nx, ny, nz, architectureType != null ? architectureType : "BURROW_UNDERGROUND", population);
        if (nest != null) {
            syncToNest(nest);
        }
    }

    private void buildNetworkForArchitecture(float nx, float ny, float nz, String nestType, int population) {
        TunnelNetworkArchitect.buildNetworkForArchitecture(this, nx, ny, nz, nestType, population);
    }
    public void checkAndExpandOntogeny(Colony colony) {
        if (colony == null) return;
        int pop = Math.max(colony.getPopulation(), colony.getLivingIndividuals().size());
        OntogenyStage targetStage;
        if (pop <= 3) targetStage = OntogenyStage.FOUNDING_CLAUSTRAL;
        else if (pop <= 25) targetStage = OntogenyStage.INCIPIENT;
        else if (pop <= 100) targetStage = OntogenyStage.DEVELOPING;
        else targetStage = OntogenyStage.MATURE_CLIMAX;

        if (targetStage.ordinal() > currentOntogenyStage.ordinal()) {
            this.currentOntogenyStage = targetStage;
            String arch = colony.getSpecies() != null ? colony.getSpecies().getNestType() : "BURROW_UNDERGROUND";
            rebuildForArchitecture(colony.getNestX(), colony.getNestY(), colony.getNestZ(), arch, colony);
        }
    }

    public void syncToNest(org.swarmforge.core.structure.Nest nest) {
        if (nest == null) return;
        nest.clear();

        Map<UUID, org.swarmforge.core.structure.Chamber> chamberMap = new HashMap<>();
        float popScale = 1.0f;

        for (TunnelNode node : nodes.values()) {
            org.swarmforge.core.structure.Chamber.Type cType = switch (node.type()) {
                case ENTRANCE, VENTILATION_CHIMNEY -> org.swarmforge.core.structure.Chamber.Type.ENTRANCE;
                case QUEEN_CHAMBER, BIVOUAC_CORE -> org.swarmforge.core.structure.Chamber.Type.QUEEN_QUARTERS;
                case BROOD_CHAMBER, HIBERNATION, SOLARIUM, GALL_NURSERY -> org.swarmforge.core.structure.Chamber.Type.NURSERY;
                case FOOD_STORAGE, FUNGUS_GARDEN, HONEYCOMB, POLLEN_POT, LEAF_CACHE -> org.swarmforge.core.structure.Chamber.Type.FOOD_STORAGE;
                case WASTE_DUMP -> org.swarmforge.core.structure.Chamber.Type.WASTE_DUMP;
                case TUNNEL -> org.swarmforge.core.structure.Chamber.Type.NURSERY;
                default -> org.swarmforge.core.structure.Chamber.Type.NURSERY;
            };

            float capacity = switch (node.type()) {
                case QUEEN_CHAMBER, BIVOUAC_CORE -> 2000.0f * popScale;
                case BROOD_CHAMBER, SOLARIUM, GALL_NURSERY -> 3000.0f * popScale;
                case FOOD_STORAGE, FUNGUS_GARDEN, HONEYCOMB, POLLEN_POT, LEAF_CACHE -> 6000.0f * popScale;
                case WASTE_DUMP -> 2500.0f * popScale;
                case HIBERNATION -> 4000.0f * popScale;
                case ENTRANCE, VENTILATION_CHIMNEY -> 1500.0f * popScale;
                case TUNNEL -> 600.0f * popScale;
                default -> 1000.0f * popScale;
            };

            org.swarmforge.core.structure.Chamber chamber = new org.swarmforge.core.structure.Chamber(
                    node.id().toString(), cType, node.x(), node.y(), node.z(), capacity);
            chamberMap.put(node.id(), chamber);
            nest.addChamber(chamber);
        }

        for (TunnelEdge edge : edges) {
            org.swarmforge.core.structure.Chamber start = chamberMap.get(edge.fromNode());
            org.swarmforge.core.structure.Chamber end = chamberMap.get(edge.toNode());
            if (start != null && end != null) {
                nest.addTunnel(new org.swarmforge.core.structure.Tunnel(start, end));
            }
        }
    }

    /**
     * Synchronizes physical Terrarium cells with this TunnelNetwork.
     * Carves lenticular ellipsoids at nodes and interpolates along curved pathPoints,
     * guaranteeing that spawned ants are never stuck in solid earth.
     */
    public void carveIntoTerrarium(org.swarmforge.core.domain.Terrarium terrarium) {
        if (terrarium == null) return;

        int tunnelRadius = Math.max(1, Math.round(tunnelWidthSetting / 2.0f));

        // 1. Carve node chambers as 3D lenticular ellipsoids
        for (TunnelNode node : nodes.values()) {
            int cx = Math.round(node.x());
            int cy = Math.round(node.y());
            int cz = Math.round(node.z());

            float rx = Math.max(0.6f, node.radiusX() * scaleFactorSetting);
            float ry = Math.max(0.6f, node.radiusY() * scaleFactorSetting);
            float rz = Math.max(0.5f, node.radiusZ() * scaleFactorSetting);

            int irx = (int) Math.ceil(rx);
            int iry = (int) Math.ceil(ry);
            int irz = (int) Math.ceil(rz);

            float surfElev = terrarium.getSurfaceElevation(cx, cy);
            org.swarmforge.core.domain.TerrariumCell.Material mat = (node.type() == ChamberType.ENTRANCE || node.z() >= surfElev)
                    ? org.swarmforge.core.domain.TerrariumCell.Material.AIR
                    : org.swarmforge.core.domain.TerrariumCell.Material.CHAMBER;

            for (int dx = -irx; dx <= irx; dx++) {
                for (int dy = -iry; dy <= iry; dy++) {
                    for (int dz = -irz; dz <= irz; dz++) {
                        float normDist = (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) + (dz * dz) / (rz * rz);
                        if (normDist <= 1.05f) {
                            int px = cx + dx;
                            int py = cy + dy;
                            int pz = cz + dz;
                            if (terrarium.inBounds(px, py, pz)) {
                                var existing = terrarium.getCell(px, py, pz);
                                if (existing == null || existing.material() != org.swarmforge.core.domain.TerrariumCell.Material.ROCK) {
                                    terrarium.setCell(new org.swarmforge.core.domain.TerrariumCell(px, py, pz, mat,
                                            new float[org.swarmforge.core.domain.TerrariumCell.PHEROMONE_TYPES], 20.0f, 60.0f));
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Carve connecting tunnel edges along curved pathPoints
        for (TunnelEdge edge : edges) {
            List<float[]> pts = (edge.pathPoints() != null && !edge.pathPoints().isEmpty())
                    ? edge.pathPoints()
                    : defaultSegmentPoints(nodes.get(edge.fromNode()), nodes.get(edge.toNode()));

            for (int i = 0; i < pts.size() - 1; i++) {
                float[] pA = pts.get(i);
                float[] pB = pts.get(i + 1);
                carveSegment(terrarium, pA[0], pA[1], pA[2], pB[0], pB[1], pB[2], tunnelRadius);
            }
        }
    }

    private List<float[]> defaultSegmentPoints(TunnelNode n1, TunnelNode n2) {
        List<float[]> res = new ArrayList<>();
        if (n1 != null) res.add(new float[]{n1.x(), n1.y(), n1.z()});
        if (n2 != null) res.add(new float[]{n2.x(), n2.y(), n2.z()});
        return res;
    }

    private void carveSegment(org.swarmforge.core.domain.Terrarium terrarium,
                              float x1, float y1, float z1, float x2, float y2, float z2, int tunnelRadius) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(1, (int) (dist * 2.5f));

        for (int step = 0; step <= steps; step++) {
            float t = (float) step / steps;
            int tx = Math.round(x1 + dx * t);
            int ty = Math.round(y1 + dy * t);
            int tz = Math.round(z1 + dz * t);

            for (int rx = tx - tunnelRadius; rx <= tx + tunnelRadius; rx++) {
                for (int ry = ty - tunnelRadius; ry <= ty + tunnelRadius; ry++) {
                    for (int rz = tz - tunnelRadius; rz <= tz + tunnelRadius; rz++) {
                        if ((rx - tx) * (rx - tx) + (ry - ty) * (ry - ty) + (rz - tz) * (rz - tz) <= tunnelRadius * tunnelRadius + 1) {
                            if (terrarium.inBounds(rx, ry, rz)) {
                                var existing = terrarium.getCell(rx, ry, rz);
                                if (existing == null || existing.material() == org.swarmforge.core.domain.TerrariumCell.Material.EARTH
                                        || existing.material() == org.swarmforge.core.domain.TerrariumCell.Material.ROCK) {
                                    terrarium.setCell(new org.swarmforge.core.domain.TerrariumCell(rx, ry, rz,
                                            org.swarmforge.core.domain.TerrariumCell.Material.AIR,
                                            new float[org.swarmforge.core.domain.TerrariumCell.PHEROMONE_TYPES], 20.0f, 60.0f));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void buildNetworkForArchitecture(float nx, float ny, float nz, String nestType) {
        TunnelNetworkArchitect.buildNetworkForArchitecture(this, nx, ny, nz, nestType);
    }

    /**
     * Dig a new chamber or tunnel extension dynamically.
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

    UUID createNode(float x, float y, float z, ChamberType type) {
        return createNode(x, y, z, type,
                TunnelNode.defaultRadius(type, true),
                TunnelNode.defaultRadius(type, true),
                TunnelNode.defaultRadius(type, false));
    }

    UUID createNode(float x, float y, float z, ChamberType type, float rx, float ry, float rz) {
        UUID id = UUID.randomUUID();
        // Bedrock & Water Table safety constraints:
        // Queen, Brood, and Fungus chambers must strictly stay ABOVE water table & bedrock
        if (type == ChamberType.QUEEN_CHAMBER || type == ChamberType.BROOD_CHAMBER || type == ChamberType.FUNGUS_GARDEN) {
            float minZ = Math.max(-waterTableDepth + 1.5f, -bedrockDepth + 1.5f);
            if (z < minZ) {
                z = minZ;
            }
        }

        // Calculate microclimate based on depth
        float depthFactor = Math.abs(z) / Math.max(1.0f, maxDepthSetting);
        float temp = 20f - (5f * depthFactor);
        float hum = (z < -waterTableDepth) ? 100.0f : (50f + (30f * depthFactor));

        TunnelNode node = new TunnelNode(id, x, y, z, type, temp, hum, rx, ry, rz);
        nodes.put(id, node);
        return id;
    }

    /**
     * Connects two nearby TunnelNetworks if their nodes pass within proximity threshold (e.g. 4m).
     */
    public static void bridgeNearbyNetworks(TunnelNetwork netA, TunnelNetwork netB, boolean isHostile) {
        if (netA == null || netB == null || netA == netB) return;

        for (TunnelNode nA : netA.nodes.values()) {
            for (TunnelNode nB : netB.nodes.values()) {
                float dx = nA.x() - nB.x();
                float dy = nA.y() - nB.y();
                float dz = nA.z() - nB.z();
                float distSq = dx * dx + dy * dy + dz * dz;

                if (distSq <= 16.0f) { // Within 4.0 meters
                    netA.createEdge(nA.id(), nB.id());
                    netB.createEdge(nB.id(), nA.id());
                    return; // Bridge closest junction
                }
            }
        }
    }

    void createEdge(UUID from, UUID to) {
        TunnelNode n1 = nodes.get(from);
        TunnelNode n2 = nodes.get(to);
        if (n1 == null || n2 == null) return;
        float dx = n2.x() - n1.x();
        float dy = n2.y() - n1.y();
        float dz = n2.z() - n1.z();
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Build sinuous organic meandering points
        List<float[]> pathPoints = new ArrayList<>();
        pathPoints.add(new float[]{n1.x(), n1.y(), n1.z()});

        int segments = Math.max(3, (int) (dist / 2.0f));
        float px = -dy;
        float py = dx;
        float pLen = (float) Math.hypot(px, py);
        if (pLen > 0.001f) {
            px /= pLen;
            py /= pLen;
        } else {
            px = 1.0f;
            py = 0.0f;
        }

        long seed = (long) (n1.x() * 31.0 + n1.y() * 17.0 + n2.x() * 13.0 + n2.y() * 7.0);
        java.util.Random rnd = new java.util.Random(seed);

        for (int i = 1; i < segments; i++) {
            float t = (float) i / segments;
            float wave = (float) Math.sin(t * Math.PI);
            float meanderOffset = (rnd.nextFloat() - 0.5f) * 1.5f * wave * scaleFactorSetting;
            float vertOffset = (rnd.nextFloat() - 0.5f) * 0.5f * wave;
            pathPoints.add(new float[]{
                    n1.x() + dx * t + px * meanderOffset,
                    n1.y() + dy * t + py * meanderOffset,
                    n1.z() + dz * t + vertOffset
            });
        }
        pathPoints.add(new float[]{n2.x(), n2.y(), n2.z()});
        edges.add(new TunnelEdge(from, to, dist, pathPoints));
    }

    public void clear() {
        nodes.clear();
        edges.clear();
    }

    public void populate(List<TunnelNode> newNodes, List<TunnelEdge> newEdges, Colony colony) {
        clear();
        for (TunnelNode n : newNodes) {
            nodes.put(n.id(), n);
        }
        edges.addAll(newEdges);
        if (colony != null) {
            syncToNest(colony.getNest());
            if (colony.getTerrarium() != null) {
                carveIntoTerrarium(colony.getTerrarium());
            }
        }
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
     * Return all chambers matching a specific type.
     */
    public List<TunnelNode> getChambersOfType(ChamberType type) {
        List<TunnelNode> result = new ArrayList<>();
        for (TunnelNode node : nodes.values()) {
            if (node.type() == type) {
                result.add(node);
            }
        }
        return result;
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