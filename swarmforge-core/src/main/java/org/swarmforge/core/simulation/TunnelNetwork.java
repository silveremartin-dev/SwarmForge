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
                case QUEEN_CHAMBER -> horizontal ? 4.8f : 2.4f;
                case FUNGUS_GARDEN -> horizontal ? 4.2f : 2.2f;
                case BROOD_CHAMBER -> horizontal ? 3.4f : 1.8f;
                case FOOD_STORAGE, LEAF_CACHE -> horizontal ? 3.6f : 1.9f;
                case WASTE_DUMP -> horizontal ? 3.2f : 1.6f;
                case HIBERNATION -> horizontal ? 3.8f : 2.0f;
                case BIVOUAC_CORE -> horizontal ? 4.5f : 2.5f;
                case SOLARIUM -> horizontal ? 4.0f : 2.0f;
                case HONEYCOMB -> horizontal ? 3.5f : 2.5f;
                case POLLEN_POT -> horizontal ? 2.8f : 2.0f;
                case VENTILATION_CHIMNEY -> horizontal ? 1.5f : 3.5f;
                case GALL_NURSERY -> horizontal ? 2.5f : 1.5f;
                case ENTRANCE -> horizontal ? 2.0f : 1.5f;
                case TUNNEL -> 1.2f;
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
        String arch = nestType.toUpperCase();
        float scale = Math.max(0.5f, scaleFactorSetting);

        if (population <= 3) {
            currentOntogenyStage = OntogenyStage.FOUNDING_CLAUSTRAL;
            buildFoundingClaustralNest(nx, ny, nz, scale, arch);
        } else if (population <= 25) {
            currentOntogenyStage = OntogenyStage.INCIPIENT;
            buildIncipientNest(nx, ny, nz, scale, arch);
        } else if (population <= 100) {
            currentOntogenyStage = OntogenyStage.DEVELOPING;
            buildDevelopingNest(nx, ny, nz, scale, arch);
        } else {
            currentOntogenyStage = OntogenyStage.MATURE_CLIMAX;
            buildMatureNestForArchitecture(nx, ny, nz, arch, scale);
        }
    }

    // ── 0. Claustral Founding Nest (Solitary Queen Stage) ──────────────────────
    private void buildFoundingClaustralNest(float nx, float ny, float nz, float s, String arch) {
        String a = arch != null ? arch.toUpperCase() : "BURROW";
        if (a.contains("PAPER") || a.contains("PEDUNCULATE") || a.contains("VESPA")) {
            // Solitary foundress on hanging branch: single pedicel + 1 embryonic comb cell
            UUID branch = createNode(nx - 2.0f * s, ny, nz + 6.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID pedicel = createNode(nx, ny, nz + 5.5f * s, ChamberType.ENTRANCE, 1.2f * s, 1.2f * s, 1.4f * s);
            UUID claustrum = createNode(nx, ny, nz + 4.5f * s, ChamberType.QUEEN_CHAMBER, 2.0f * s, 2.0f * s, 1.4f * s);
            createEdge(branch, pedicel);
            createEdge(pedicel, claustrum);
        } else if (a.contains("WAX_COMB") || a.contains("BEEHIVE") || a.contains("HEXAGONAL")) {
            // Solitary foundress in tree cavity with single starter wax comb
            UUID ent = createNode(nx, ny, nz + 2.0f * s, ChamberType.ENTRANCE, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID comb = createNode(nx, ny, nz + 3.5f * s, ChamberType.QUEEN_CHAMBER, 2.2f * s, 2.2f * s, 1.5f * s);
            createEdge(ent, comb);
        } else if (a.contains("WAX_POTS") || a.contains("POTS_CLUSTER") || a.contains("BOMBUS")) {
            // Solitary bumblebee queen in shallow moss hollow with 1 honeypot
            UUID ent = createNode(nx, ny, nz + 0.1f, ChamberType.ENTRANCE, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID pot = createNode(nx, ny, nz - 0.8f * s, ChamberType.QUEEN_CHAMBER, 2.4f * s, 2.4f * s, 1.6f * s);
            createEdge(ent, pot);
        } else if (a.contains("CATHEDRAL") || a.contains("TERMITE") || a.contains("STERCORAL")) {
            // Founding royal pair sealed in subterranean copularium cavity
            UUID plug = createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 1.2f * s, 1.2f * s, 1.0f * s);
            UUID copularium = createNode(nx, ny, nz - 2.5f * s, ChamberType.QUEEN_CHAMBER, 2.5f * s, 2.5f * s, 1.6f * s);
            createEdge(plug, copularium);
        } else if (a.contains("FUNGI") || a.contains("VAULT") || a.contains("ATTA")) {
            // Solitary foundress leafcutter queen in vertical claustral tube with founding fungal pellet
            UUID plug = createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 1.2f * s, 1.2f * s, 1.0f * s);
            UUID fungusCell = createNode(nx, ny, nz - 4.5f * s, ChamberType.QUEEN_CHAMBER, 2.6f * s, 2.6f * s, 1.8f * s);
            createEdge(plug, fungusCell);
        } else if (a.contains("SILK") || a.contains("LEAF") || a.contains("OECOPHYLLA") || a.contains("ARBOREAL_SILK")) {
            // Solitary weaver queen in single curled green leaf
            UUID branch = createNode(nx, ny, nz + 5.0f * s, ChamberType.ENTRANCE, 1.5f * s, 1.5f * s, 1.2f * s);
            UUID leafCell = createNode(nx, ny, nz + 4.2f * s, ChamberType.QUEEN_CHAMBER, 2.2f * s, 2.2f * s, 1.4f * s);
            createEdge(branch, leafCell);
        } else if (a.contains("CARTON")) {
            // Solitary foundress on branch knot with starter carton paste cell
            UUID ent = createNode(nx, ny, nz + 4.0f * s, ChamberType.ENTRANCE, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID cartonCell = createNode(nx, ny, nz + 4.5f * s, ChamberType.QUEEN_CHAMBER, 2.2f * s, 2.2f * s, 1.4f * s);
            createEdge(ent, cartonCell);
        } else if (a.contains("BAMBOO") || a.contains("STEM") || a.contains("GALL")) {
            // Solitary foundress in hollow plant internode
            UUID pore = createNode(nx - 2.0f * s, ny, nz + 2.0f * s, ChamberType.ENTRANCE, 1.2f * s, 1.0f * s, 1.0f * s);
            UUID stemCell = createNode(nx + 1.0f * s, ny, nz + 2.0f * s, ChamberType.QUEEN_CHAMBER, 2.4f * s, 1.4f * s, 1.2f * s);
            createEdge(pore, stemCell);
        } else if (a.contains("BIVOUAC") || a.contains("ARMY") || a.contains("ECITON")) {
            // Solitary queen / nomad cluster under leaf shelter
            UUID ent = createNode(nx, ny, nz + 1.0f * s, ChamberType.ENTRANCE, 1.5f * s, 1.5f * s, 1.2f * s);
            UUID cluster = createNode(nx, ny, nz + 0.3f * s, ChamberType.QUEEN_CHAMBER, 2.4f * s, 2.4f * s, 1.6f * s);
            createEdge(ent, cluster);
        } else if (a.contains("WOOD") || a.contains("TREE") || a.contains("TRUNK") || a.contains("HOLLOW_TRUNK")) {
            // Solitary foundress in soft bark slit
            UUID slit = createNode(nx, ny, nz + 1.5f * s, ChamberType.ENTRANCE, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID xylemCell = createNode(nx, ny, nz + 2.5f * s, ChamberType.QUEEN_CHAMBER, 2.2f * s, 2.2f * s, 1.4f * s);
            createEdge(slit, xylemCell);
        } else {
            // Solitary claustral copula underground (Formica rufa, Pogonomyrmex, Lasius, Supercolony)
            UUID ent = createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID claustrum = createNode(nx, ny, nz - 3.5f * s, ChamberType.QUEEN_CHAMBER, 2.4f * s, 2.4f * s, 1.5f * s);
            createEdge(ent, claustrum);
        }
    }

    // ── 1. Incipient Colony Nest (First Nanitic Workers) ─────────────────────────
    private void buildIncipientNest(float nx, float ny, float nz, float s, String arch) {
        String a = arch != null ? arch.toUpperCase() : "BURROW";
        if (a.contains("PAPER") || a.contains("PEDUNCULATE") || a.contains("VESPA")) {
            UUID branch = createNode(nx - 2.5f * s, ny, nz + 6.5f * s, ChamberType.TUNNEL, 1.5f * s, 1.5f * s, 1.2f * s);
            UUID pedicel = createNode(nx, ny, nz + 6.0f * s, ChamberType.TUNNEL, 1.2f * s, 1.2f * s, 1.4f * s);
            UUID combDisc = createNode(nx, ny, nz + 4.8f * s, ChamberType.HONEYCOMB, 2.6f * s, 2.6f * s, 1.6f * s);
            UUID queen = createNode(nx + 1.2f * s, ny, nz + 4.5f * s, ChamberType.QUEEN_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID brood = createNode(nx - 1.2f * s, ny, nz + 4.5f * s, ChamberType.BROOD_CHAMBER, 2.5f * s, 2.5f * s, 1.4f * s);
            UUID entry = createNode(nx, ny, nz + 3.6f * s, ChamberType.ENTRANCE, 1.6f * s, 1.6f * s, 1.2f * s);
            createEdge(branch, pedicel);
            createEdge(pedicel, combDisc);
            createEdge(combDisc, queen);
            createEdge(combDisc, brood);
            createEdge(combDisc, entry);
        } else if (a.contains("WAX_COMB") || a.contains("BEEHIVE") || a.contains("HEXAGONAL")) {
            UUID ent = createNode(nx, ny, nz + 1.5f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID hub = createNode(nx, ny, nz + 3.0f * s, ChamberType.TUNNEL, 1.5f * s, 1.5f * s, 1.2f * s);
            UUID comb1 = createNode(nx - 1.5f * s, ny, nz + 4.5f * s, ChamberType.HONEYCOMB, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID comb2 = createNode(nx + 1.5f * s, ny, nz + 4.5f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID queen = createNode(nx, ny, nz + 3.8f * s, ChamberType.QUEEN_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            createEdge(ent, hub);
            createEdge(hub, comb1);
            createEdge(hub, comb2);
            createEdge(hub, queen);
        } else if (a.contains("WAX_POTS") || a.contains("POTS_CLUSTER") || a.contains("BOMBUS")) {
            UUID ent = createNode(nx, ny, nz + 0.1f, ChamberType.ENTRANCE, 1.6f * s, 1.6f * s, 1.2f * s);
            UUID hub = createNode(nx, ny, nz - 0.6f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID honeyPot = createNode(nx + 1.4f * s, ny, nz - 0.9f * s, ChamberType.POLLEN_POT, 2.2f * s, 2.2f * s, 1.5f * s);
            UUID broodPot = createNode(nx - 1.4f * s, ny, nz - 0.9f * s, ChamberType.BROOD_CHAMBER, 2.4f * s, 2.4f * s, 1.5f * s);
            UUID queen = createNode(nx, ny, nz - 1.4f * s, ChamberType.QUEEN_CHAMBER, 3.0f * s, 3.0f * s, 1.8f * s);
            createEdge(ent, hub);
            createEdge(hub, honeyPot);
            createEdge(hub, broodPot);
            createEdge(hub, queen);
        } else if (a.contains("CATHEDRAL") || a.contains("TERMITE") || a.contains("STERCORAL")) {
            UUID turret = createNode(nx, ny, nz + 1.5f * s, ChamberType.VENTILATION_CHIMNEY, 1.6f * s, 1.6f * s, 1.8f * s);
            UUID subHub = createNode(nx, ny, nz - 1.5f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.4f * s);
            UUID fungus = createNode(nx + 2.0f * s, ny, nz - 3.0f * s, ChamberType.FUNGUS_GARDEN, 3.0f * s, 3.0f * s, 1.8f * s);
            UUID royal = createNode(nx - 1.5f * s, ny, nz - 5.0f * s, ChamberType.QUEEN_CHAMBER, 3.6f * s, 3.6f * s, 2.0f * s);
            createEdge(turret, subHub);
            createEdge(subHub, fungus);
            createEdge(subHub, royal);
        } else if (a.contains("FUNGI") || a.contains("VAULT") || a.contains("ATTA")) {
            UUID crater = createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID shaft = createNode(nx, ny, nz - 3.0f * s, ChamberType.TUNNEL, 1.5f * s, 1.5f * s, 1.4f * s);
            UUID fungus = createNode(nx + 2.5f * s, ny, nz - 5.0f * s, ChamberType.FUNGUS_GARDEN, 3.4f * s, 3.4f * s, 2.0f * s);
            UUID queen = createNode(nx - 2.0f * s, ny, nz - 8.0f * s, ChamberType.QUEEN_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
            createEdge(crater, shaft);
            createEdge(shaft, fungus);
            createEdge(shaft, queen);
        } else if (a.contains("MOUND") || a.contains("SURFACE_MOUND") || a.contains("SOLAR") || a.contains("RUFA")) {
            UUID ent = createNode(nx, ny, nz + 0.4f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID solarium = createNode(nx + 1.5f * s, ny, nz + 1.8f * s, ChamberType.SOLARIUM, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID hub = createNode(nx, ny, nz - 2.0f * s, ChamberType.TUNNEL, 1.5f * s, 1.5f * s, 1.3f * s);
            UUID brood = createNode(nx - 2.0f * s, ny, nz - 4.0f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID queen = createNode(nx, ny, nz - 7.0f * s, ChamberType.QUEEN_CHAMBER, 3.6f * s, 3.6f * s, 2.0f * s);
            createEdge(ent, solarium);
            createEdge(ent, hub);
            createEdge(hub, brood);
            createEdge(hub, queen);
        } else if (a.contains("SILK") || a.contains("LEAF") || a.contains("OECOPHYLLA") || a.contains("ARBOREAL_SILK")) {
            UUID branch = createNode(nx, ny, nz + 5.0f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID pav1 = createNode(nx - 2.0f * s, ny, nz + 5.5f * s, ChamberType.BROOD_CHAMBER, 3.0f * s, 3.0f * s, 1.8f * s);
            UUID pav2 = createNode(nx + 2.0f * s, ny, nz + 5.5f * s, ChamberType.QUEEN_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
            createEdge(branch, pav1);
            createEdge(branch, pav2);
        } else if (a.contains("CARTON")) {
            UUID ent = createNode(nx, ny, nz + 3.5f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID core = createNode(nx, ny, nz + 4.5f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.4f * s);
            UUID brood = createNode(nx - 1.8f * s, ny, nz + 5.0f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID queen = createNode(nx + 1.8f * s, ny, nz + 5.0f * s, ChamberType.QUEEN_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            createEdge(ent, core);
            createEdge(core, brood);
            createEdge(core, queen);
        } else if (a.contains("BAMBOO") || a.contains("STEM") || a.contains("GALL")) {
            UUID pore = createNode(nx - 3.0f * s, ny, nz + 2.0f * s, ChamberType.ENTRANCE, 1.4f * s, 1.2f * s, 1.2f * s);
            UUID node1 = createNode(nx, ny, nz + 2.0f * s, ChamberType.BROOD_CHAMBER, 2.6f * s, 1.5f * s, 1.4f * s);
            UUID node2 = createNode(nx + 3.0f * s, ny, nz + 2.0f * s, ChamberType.QUEEN_CHAMBER, 3.0f * s, 1.6f * s, 1.4f * s);
            createEdge(pore, node1);
            createEdge(node1, node2);
        } else if (a.contains("BIVOUAC") || a.contains("ARMY") || a.contains("ECITON")) {
            UUID anchor = createNode(nx, ny, nz + 1.8f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID core = createNode(nx, ny, nz + 0.6f * s, ChamberType.QUEEN_CHAMBER, 3.4f * s, 3.4f * s, 2.0f * s);
            UUID brood = createNode(nx, ny, nz - 0.2f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            createEdge(anchor, core);
            createEdge(core, brood);
        } else if (a.contains("WOOD") || a.contains("TREE") || a.contains("TRUNK") || a.contains("HOLLOW_TRUNK")) {
            UUID slit = createNode(nx, ny, nz + 1.5f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID shaft = createNode(nx, ny, nz + 3.0f * s, ChamberType.TUNNEL, 1.5f * s, 1.5f * s, 1.5f * s);
            UUID brood = createNode(nx + 2.0f * s, ny, nz + 3.5f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID queen = createNode(nx - 2.0f * s, ny, nz + 4.5f * s, ChamberType.QUEEN_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
            createEdge(slit, shaft);
            createEdge(shaft, brood);
            createEdge(shaft, queen);
        } else {
            // Standard subterranean burrow (Lasius, Messor, Pogonomyrmex)
            UUID ent = createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID hub = createNode(nx, ny, nz - 2.5f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.2f * s);
            UUID brood = createNode(nx - 2.5f * s, ny + 1.0f * s, nz - 4.5f * s, ChamberType.BROOD_CHAMBER, 2.8f * s, 2.8f * s, 1.6f * s);
            UUID food = createNode(nx + 2.5f * s, ny - 1.0f * s, nz - 4.0f * s, ChamberType.FOOD_STORAGE, 2.6f * s, 2.6f * s, 1.5f * s);
            UUID queen = createNode(nx, ny, nz - 7.5f * s, ChamberType.QUEEN_CHAMBER, 3.6f * s, 3.6f * s, 2.0f * s);
            createEdge(ent, hub);
            createEdge(hub, brood);
            createEdge(hub, food);
            createEdge(hub, queen);
        }
    }

    // ── 2. Developing Colony Nest (Expanding Population) ────────────────────────
    private void buildDevelopingNest(float nx, float ny, float nz, float s, String arch) {
        String a = arch != null ? arch.toUpperCase() : "BURROW";
        if (a.contains("PAPER") || a.contains("PEDUNCULATE") || a.contains("VESPA")) {
            UUID branch = createNode(nx - 3.5f * s, ny, nz + 7.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.4f * s);
            UUID pedicel = createNode(nx, ny, nz + 7.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.6f * s);
            UUID topDisc = createNode(nx, ny, nz + 5.5f * s, ChamberType.HONEYCOMB, 3.4f * s, 3.4f * s, 1.8f * s);
            UUID lowDisc = createNode(nx, ny, nz + 3.8f * s, ChamberType.BROOD_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID queen = createNode(nx + 2.0f * s, ny, nz + 5.0f * s, ChamberType.QUEEN_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
            UUID entry = createNode(nx, ny, nz + 2.2f * s, ChamberType.ENTRANCE, 1.8f * s, 1.8f * s, 1.4f * s);
            createEdge(branch, pedicel);
            createEdge(pedicel, topDisc);
            createEdge(topDisc, lowDisc);
            createEdge(topDisc, queen);
            createEdge(lowDisc, entry);
        } else if (a.contains("WAX_COMB") || a.contains("BEEHIVE") || a.contains("HEXAGONAL")) {
            UUID ent = createNode(nx, ny, nz + 1.2f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID shaft = createNode(nx, ny, nz + 3.5f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.5f * s);
            UUID honey1 = createNode(nx - 2.5f * s, ny, nz + 6.0f * s, ChamberType.FOOD_STORAGE, 3.5f * s, 3.5f * s, 1.8f * s);
            UUID honey2 = createNode(nx + 2.5f * s, ny, nz + 6.0f * s, ChamberType.FOOD_STORAGE, 3.5f * s, 3.5f * s, 1.8f * s);
            UUID brood = createNode(nx, ny, nz + 4.8f * s, ChamberType.BROOD_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID queen = createNode(nx, ny, nz + 2.8f * s, ChamberType.QUEEN_CHAMBER, 4.0f * s, 4.0f * s, 2.0f * s);
            createEdge(ent, shaft);
            createEdge(shaft, honey1);
            createEdge(shaft, honey2);
            createEdge(shaft, brood);
            createEdge(shaft, queen);
        } else if (a.contains("CATHEDRAL") || a.contains("TERMITE") || a.contains("STERCORAL")) {
            UUID spireApex = createNode(nx, ny, nz + 5.5f * s, ChamberType.VENTILATION_CHIMNEY, 1.8f * s, 1.8f * s, 2.5f * s);
            UUID subHub = createNode(nx, ny, nz - 2.0f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.6f * s);
            UUID nursery = createNode(nx + 3.0f * s, ny, nz - 4.5f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
            UUID fungus = createNode(nx - 3.0f * s, ny, nz - 6.5f * s, ChamberType.FUNGUS_GARDEN, 4.2f * s, 4.2f * s, 2.2f * s);
            UUID royal = createNode(nx, ny, nz - 9.0f * s, ChamberType.QUEEN_CHAMBER, 4.5f * s, 4.5f * s, 2.2f * s);
            createEdge(spireApex, subHub);
            createEdge(subHub, nursery);
            createEdge(subHub, fungus);
            createEdge(subHub, royal);
        } else if (a.contains("FUNGI") || a.contains("VAULT") || a.contains("ATTA")) {
            UUID cr1 = createNode(nx - 2.5f * s, ny, nz - 0.1f, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID cr2 = createNode(nx + 2.5f * s, ny, nz - 0.1f, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID hub = createNode(nx, ny, nz - 3.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.6f * s);
            UUID leafCache = createNode(nx + 3.5f * s, ny, nz - 4.5f * s, ChamberType.LEAF_CACHE, 3.4f * s, 3.4f * s, 1.8f * s);
            UUID fungus1 = createNode(nx - 4.0f * s, ny, nz - 7.5f * s, ChamberType.FUNGUS_GARDEN, 4.2f * s, 4.2f * s, 2.2f * s);
            UUID fungus2 = createNode(nx + 4.0f * s, ny, nz - 8.5f * s, ChamberType.FUNGUS_GARDEN, 4.4f * s, 4.4f * s, 2.4f * s);
            UUID royal = createNode(nx, ny, nz - 12.0f * s, ChamberType.QUEEN_CHAMBER, 4.8f * s, 4.8f * s, 2.4f * s);
            createEdge(cr1, hub);
            createEdge(cr2, hub);
            createEdge(hub, leafCache);
            createEdge(hub, fungus1);
            createEdge(hub, fungus2);
            createEdge(hub, royal);
        } else if (a.contains("MOUND") || a.contains("SURFACE_MOUND") || a.contains("SOLAR") || a.contains("RUFA")) {
            UUID ent1 = createNode(nx - 2.0f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.4f * s);
            UUID ent2 = createNode(nx + 2.0f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.4f * s);
            UUID solarium = createNode(nx, ny, nz + 3.0f * s, ChamberType.SOLARIUM, 3.8f * s, 3.8f * s, 2.0f * s);
            UUID hub = createNode(nx, ny, nz - 2.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.5f * s);
            UUID wintering = createNode(nx - 3.5f * s, ny, nz - 7.0f * s, ChamberType.HIBERNATION, 3.8f * s, 3.8f * s, 2.0f * s);
            UUID queen = createNode(nx + 2.5f * s, ny, nz - 10.0f * s, ChamberType.QUEEN_CHAMBER, 4.4f * s, 4.4f * s, 2.2f * s);
            createEdge(ent1, solarium);
            createEdge(ent2, solarium);
            createEdge(solarium, hub);
            createEdge(hub, wintering);
            createEdge(hub, queen);
        } else if (a.contains("SILK") || a.contains("LEAF") || a.contains("OECOPHYLLA") || a.contains("ARBOREAL_SILK")) {
            UUID branch = createNode(nx, ny, nz + 5.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID hub = createNode(nx, ny, nz + 7.5f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.4f * s);
            UUID pav1 = createNode(nx - 3.0f * s, ny, nz + 7.0f * s, ChamberType.BROOD_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID pav2 = createNode(nx + 3.0f * s, ny, nz + 7.0f * s, ChamberType.FOOD_STORAGE, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID royalPav = createNode(nx, ny, nz + 8.5f * s, ChamberType.QUEEN_CHAMBER, 4.2f * s, 4.2f * s, 2.0f * s);
            createEdge(branch, hub);
            createEdge(hub, pav1);
            createEdge(hub, pav2);
            createEdge(hub, royalPav);
        } else if (a.contains("CARTON")) {
            UUID ent = createNode(nx, ny, nz + 3.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID core = createNode(nx, ny, nz + 5.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.5f * s);
            UUID brood1 = createNode(nx - 2.5f * s, ny, nz + 6.0f * s, ChamberType.BROOD_CHAMBER, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID food = createNode(nx + 2.5f * s, ny, nz + 6.0f * s, ChamberType.FOOD_STORAGE, 3.2f * s, 3.2f * s, 1.8f * s);
            UUID queen = createNode(nx, ny, nz + 6.8f * s, ChamberType.QUEEN_CHAMBER, 4.0f * s, 4.0f * s, 2.0f * s);
            createEdge(ent, core);
            createEdge(core, brood1);
            createEdge(core, food);
            createEdge(core, queen);
        } else if (a.contains("BIVOUAC") || a.contains("ARMY") || a.contains("ECITON")) {
            UUID anchor = createNode(nx, ny, nz + 2.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
            UUID core = createNode(nx, ny, nz + 0.8f * s, ChamberType.QUEEN_CHAMBER, 4.4f * s, 4.4f * s, 2.2f * s);
            UUID brood = createNode(nx - 2.5f * s, ny, nz - 0.4f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
            UUID food = createNode(nx + 2.5f * s, ny, nz - 0.4f * s, ChamberType.FOOD_STORAGE, 3.4f * s, 3.4f * s, 1.8f * s);
            createEdge(anchor, core);
            createEdge(core, brood);
            createEdge(core, food);
        } else {
            buildYoungBurrowNest(nx, ny, nz, s);
        }
    }

    // ── 3. Mature Climax Architecture Router ───────────────────────────────────
    private void buildMatureNestForArchitecture(float nx, float ny, float nz, String arch, float scale) {
        if (arch.contains("WOOD") || arch.contains("TREE") || arch.contains("TRUNK") || arch.contains("HOLLOW_TRUNK")) {
            buildHollowTrunkNest(nx, ny, nz, scale);
        } else if (arch.contains("WAX_COMB") || arch.contains("BEEHIVE") || arch.contains("HEXAGONAL")) {
            buildHexagonalCombNest(nx, ny, nz, scale);
        } else if (arch.contains("WAX_POTS") || arch.contains("POTS_CLUSTER") || arch.contains("BOMBUS")) {
            buildWaxPotsClusterNest(nx, ny, nz, scale);
        } else if (arch.contains("PAPER") || arch.contains("PEDUNCULATE") || arch.contains("VESPA")) {
            buildPaperPedunculateNest(nx, ny, nz, scale);
        } else if (arch.contains("CATHEDRAL") || arch.contains("TERMITE") || arch.contains("STERCORAL")) {
            buildCathedralMoundNest(nx, ny, nz, scale);
        } else if (arch.contains("FUNGI") || arch.contains("VAULT") || arch.contains("ATTA")) {
            buildSubterraneanFungiVaultNest(nx, ny, nz, scale);
        } else if (arch.contains("MOUND") || arch.contains("SURFACE_MOUND") || arch.contains("SOLAR") || arch.contains("RUFA")) {
            buildSurfaceMoundNest(nx, ny, nz, scale);
        } else if (arch.contains("SILK") || arch.contains("LEAF") || arch.contains("OECOPHYLLA") || arch.contains("ARBOREAL_SILK")) {
            buildArborealSilkLeafNest(nx, ny, nz, scale);
        } else if (arch.contains("CARTON")) {
            buildCartonNest(nx, ny, nz, scale);
        } else if (arch.contains("BAMBOO") || arch.contains("STEM") || arch.contains("GALL")) {
            buildBambooStemNest(nx, ny, nz, scale);
        } else if (arch.contains("BIVOUAC") || arch.contains("ARMY") || arch.contains("ECITON")) {
            buildBivouacLivingNest(nx, ny, nz, scale);
        } else if (arch.contains("SUPERCOLONY") || arch.contains("SUPERCOLONIE")) {
            buildSupercolonyNetworkNest(nx, ny, nz, scale);
        } else {
            buildMatureUndergroundBurrow(nx, ny, nz, scale);
        }
    }

    /**
     * Checks colony growth and seamlessly triggers ontogenetic nest expansion
     * as population and building materials increase over time.
     */
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

            float rx = Math.max(1.2f, node.radiusX() * scaleFactorSetting);
            float ry = Math.max(1.2f, node.radiusY() * scaleFactorSetting);
            float rz = Math.max(1.0f, node.radiusZ() * scaleFactorSetting);

            int irx = (int) Math.ceil(rx);
            int iry = (int) Math.ceil(ry);
            int irz = (int) Math.ceil(rz);

            org.swarmforge.core.domain.TerrariumCell.Material mat = (node.type() == ChamberType.ENTRANCE || node.z() >= 0)
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
        String arch = nestType.toUpperCase();
        float scale = Math.max(0.5f, scaleFactorSetting);

        if (arch.contains("WOOD") || arch.contains("TREE") || arch.contains("TRUNK") || arch.contains("HOLLOW_TRUNK")) {
            buildHollowTrunkNest(nx, ny, nz, scale);
        } else if (arch.contains("WAX_COMB") || arch.contains("BEEHIVE") || arch.contains("HEXAGONAL")) {
            buildHexagonalCombNest(nx, ny, nz, scale);
        } else if (arch.contains("WAX_POTS") || arch.contains("POTS_CLUSTER") || arch.contains("BOMBUS")) {
            buildWaxPotsClusterNest(nx, ny, nz, scale);
        } else if (arch.contains("PAPER") || arch.contains("PEDUNCULATE") || arch.contains("VESPA")) {
            buildPaperPedunculateNest(nx, ny, nz, scale);
        } else if (arch.contains("CATHEDRAL") || arch.contains("TERMITE") || arch.contains("STERCORAL")) {
            buildCathedralMoundNest(nx, ny, nz, scale);
        } else if (arch.contains("FUNGI") || arch.contains("VAULT") || arch.contains("ATTA")) {
            buildSubterraneanFungiVaultNest(nx, ny, nz, scale);
        } else if (arch.contains("MOUND") || arch.contains("SURFACE_MOUND") || arch.contains("SOLAR") || arch.contains("RUFA")) {
            buildSurfaceMoundNest(nx, ny, nz, scale);
        } else if (arch.contains("SILK") || arch.contains("LEAF") || arch.contains("OECOPHYLLA") || arch.contains("ARBOREAL_SILK")) {
            buildArborealSilkLeafNest(nx, ny, nz, scale);
        } else if (arch.contains("CARTON")) {
            buildCartonNest(nx, ny, nz, scale);
        } else if (arch.contains("BAMBOO") || arch.contains("STEM") || arch.contains("GALL")) {
            buildBambooStemNest(nx, ny, nz, scale);
        } else if (arch.contains("BIVOUAC") || arch.contains("ARMY") || arch.contains("ECITON")) {
            buildBivouacLivingNest(nx, ny, nz, scale);
        } else if (arch.contains("SUPERCOLONY") || arch.contains("SUPERCOLONIE")) {
            buildSupercolonyNetworkNest(nx, ny, nz, scale);
        } else if (arch.contains("SIMPLE") || arch.contains("YOUNG") || arch.contains("JEUNE")) {
            buildYoungBurrowNest(nx, ny, nz, scale);
        } else {
            buildMatureUndergroundBurrow(nx, ny, nz, scale);
        }
    }

    // ── 1. Mature Subterranean Burrow (Lasius / Formica) ───────────────────────
    private void buildMatureUndergroundBurrow(float nx, float ny, float nz, float s) {
        UUID ent1 = createNode(nx - 2.5f * s, ny + 1.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID ent2 = createNode(nx + 2.5f * s, ny - 1.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID hub = createNode(nx, ny, nz - 3.0f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.4f * s);
        createEdge(ent1, hub);
        createEdge(ent2, hub);

        UUID shaft1 = createNode(nx + 0.8f * s, ny - 0.5f * s, nz - 6.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.4f * s);
        UUID shaft2 = createNode(nx - 0.8f * s, ny + 0.5f * s, nz - 10.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.4f * s);
        UUID shaft3 = createNode(nx + 0.5f * s, ny + 0.8f * s, nz - 14.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.4f * s);
        createEdge(hub, shaft1);
        createEdge(shaft1, shaft2);
        createEdge(shaft2, shaft3);

        // Food granaries (shallow)
        UUID food1 = createNode(nx + 4.5f * s, ny + 2.0f * s, nz - 4.5f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID food2 = createNode(nx - 4.0f * s, ny - 3.0f * s, nz - 5.5f * s, ChamberType.FOOD_STORAGE, 3.6f * s, 3.6f * s, 1.9f * s);
        createEdge(shaft1, food1);
        createEdge(shaft1, food2);

        // Brood nurseries (mid depth)
        UUID brood1 = createNode(nx - 4.5f * s, ny + 3.5f * s, nz - 8.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood2 = createNode(nx + 4.0f * s, ny - 3.5f * s, nz - 9.5f * s, ChamberType.BROOD_CHAMBER, 3.5f * s, 3.5f * s, 1.8f * s);
        UUID brood3 = createNode(nx, ny + 5.0f * s, nz - 11.5f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        createEdge(shaft2, brood1);
        createEdge(shaft2, brood2);
        createEdge(shaft2, brood3);

        // Queen Royal Chamber (deep, protected, wide lenticular)
        UUID queen = createNode(nx + 3.0f * s, ny + 1.0f * s, nz - 15.5f * s, ChamberType.QUEEN_CHAMBER, 5.2f * s, 5.2f * s, 2.4f * s);
        createEdge(shaft3, queen);

        // Waste dump & Hibernation
        UUID waste = createNode(nx - 5.0f * s, ny - 4.0f * s, nz - 17.0f * s, ChamberType.WASTE_DUMP, 3.4f * s, 3.4f * s, 1.8f * s);
        UUID hiber = createNode(nx + 2.0f * s, ny - 4.5f * s, nz - 18.0f * s, ChamberType.HIBERNATION, 4.2f * s, 4.2f * s, 2.0f * s);
        createEdge(queen, waste);
        createEdge(shaft3, hiber);
    }

    // ── 2. Young Simple Burrow ────────────────────────────────────────────────
    private void buildYoungBurrowNest(float nx, float ny, float nz, float s) {
        UUID ent = createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
        UUID shaft = createNode(nx + 0.5f * s, ny - 0.5f * s, nz - 4.0f * s, ChamberType.TUNNEL, 1.3f * s, 1.3f * s, 1.3f * s);
        createEdge(ent, shaft);

        UUID brood = createNode(nx - 3.0f * s, ny + 1.5f * s, nz - 5.0f * s, ChamberType.BROOD_CHAMBER, 3.2f * s, 3.2f * s, 1.6f * s);
        UUID food = createNode(nx + 3.0f * s, ny - 1.5f * s, nz - 4.5f * s, ChamberType.FOOD_STORAGE, 3.0f * s, 3.0f * s, 1.5f * s);
        UUID queen = createNode(nx, ny, nz - 8.5f * s, ChamberType.QUEEN_CHAMBER, 4.4f * s, 4.4f * s, 2.2f * s);
        UUID waste = createNode(nx - 2.5f * s, ny - 2.0f * s, nz - 9.5f * s, ChamberType.WASTE_DUMP, 2.8f * s, 2.8f * s, 1.4f * s);

        createEdge(shaft, brood);
        createEdge(shaft, food);
        createEdge(shaft, queen);
        createEdge(queen, waste);
    }

    // ── 3. Surface Thatch Mound (Formica rufa) ────────────────────────────────
    private void buildSurfaceMoundNest(float nx, float ny, float nz, float s) {
        // Mound surface entrances & Solarium (elevated above ground z > 0)
        UUID ent1 = createNode(nx - 3.0f * s, ny - 1.0f * s, nz + 0.6f * s, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);
        UUID ent2 = createNode(nx + 3.0f * s, ny + 1.0f * s, nz + 0.6f * s, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);
        UUID solarium = createNode(nx, ny, nz + 4.5f * s, ChamberType.BROOD_CHAMBER, 4.5f * s, 4.5f * s, 2.4f * s);
        UUID moundCenter = createNode(nx, ny, nz + 1.5f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.8f * s);

        createEdge(ent1, moundCenter);
        createEdge(ent2, moundCenter);
        createEdge(moundCenter, solarium);

        UUID deepShaft1 = createNode(nx - 0.5f * s, ny + 0.5f * s, nz - 4.0f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.5f * s);
        UUID deepShaft2 = createNode(nx + 0.5f * s, ny - 0.5f * s, nz - 9.0f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.5f * s);
        createEdge(moundCenter, deepShaft1);
        createEdge(deepShaft1, deepShaft2);

        UUID broodWarm = createNode(nx + 4.0f * s, ny + 2.0f * s, nz - 3.0f * s, ChamberType.BROOD_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID pantry = createNode(nx - 4.0f * s, ny - 2.5f * s, nz - 5.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        createEdge(deepShaft1, broodWarm);
        createEdge(deepShaft1, pantry);

        UUID winterCrypt = createNode(nx, ny, nz - 14.0f * s, ChamberType.HIBERNATION, 4.8f * s, 4.8f * s, 2.4f * s);
        UUID royalVault = createNode(nx + 3.5f * s, ny - 2.0f * s, nz - 13.0f * s, ChamberType.QUEEN_CHAMBER, 5.0f * s, 5.0f * s, 2.5f * s);
        UUID waste = createNode(nx - 4.5f * s, ny + 3.0f * s, nz - 15.0f * s, ChamberType.WASTE_DUMP, 3.4f * s, 3.4f * s, 1.8f * s);

        createEdge(deepShaft2, winterCrypt);
        createEdge(deepShaft2, royalVault);
        createEdge(winterCrypt, waste);
    }

    // ── 4. Subterranean Fungi Vault (Atta leafcutters) ─────────────────────────
    private void buildSubterraneanFungiVaultNest(float nx, float ny, float nz, float s) {
        UUID ent1 = createNode(nx - 4.5f * s, ny - 2.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);
        UUID ent2 = createNode(nx + 4.5f * s, ny + 2.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);
        UUID ent3 = createNode(nx, ny + 5.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);

        UUID upperHub = createNode(nx, ny, nz - 3.5f * s, ChamberType.TUNNEL, 2.2f * s, 2.2f * s, 1.8f * s);
        createEdge(ent1, upperHub);
        createEdge(ent2, upperHub);
        createEdge(ent3, upperHub);

        UUID midShaft = createNode(nx + 0.5f * s, ny - 0.5f * s, nz - 8.0f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.8f * s);
        UUID lowShaft = createNode(nx - 0.5f * s, ny + 0.5f * s, nz - 13.0f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.8f * s);
        createEdge(upperHub, midShaft);
        createEdge(midShaft, lowShaft);

        // Huge fungal chambers (domed/lenticular)
        UUID fungus1 = createNode(nx - 6.0f * s, ny + 3.0f * s, nz - 6.5f * s, ChamberType.FUNGUS_GARDEN, 4.8f * s, 4.8f * s, 2.5f * s);
        UUID fungus2 = createNode(nx + 6.0f * s, ny - 3.0f * s, nz - 7.5f * s, ChamberType.FUNGUS_GARDEN, 5.0f * s, 5.0f * s, 2.6f * s);
        UUID fungus3 = createNode(nx - 5.5f * s, ny - 4.0f * s, nz - 11.5f * s, ChamberType.FUNGUS_GARDEN, 5.2f * s, 5.2f * s, 2.8f * s);
        UUID fungus4 = createNode(nx + 5.5f * s, ny + 4.0f * s, nz - 12.5f * s, ChamberType.FUNGUS_GARDEN, 5.0f * s, 5.0f * s, 2.6f * s);
        createEdge(midShaft, fungus1);
        createEdge(midShaft, fungus2);
        createEdge(lowShaft, fungus3);
        createEdge(lowShaft, fungus4);

        // Brood nestled near fungi
        UUID brood1 = createNode(nx, ny - 5.5f * s, nz - 9.5f * s, ChamberType.BROOD_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID brood2 = createNode(nx, ny + 5.5f * s, nz - 10.5f * s, ChamberType.BROOD_CHAMBER, 3.8f * s, 3.8f * s, 2.0f * s);
        createEdge(midShaft, brood1);
        createEdge(midShaft, brood2);

        // Deep Queen Royal Crypt
        UUID queen = createNode(nx, ny, nz - 16.0f * s, ChamberType.QUEEN_CHAMBER, 5.5f * s, 5.5f * s, 2.6f * s);
        createEdge(lowShaft, queen);

        // Isolated deep waste pits
        UUID waste1 = createNode(nx - 7.0f * s, ny, nz - 18.5f * s, ChamberType.WASTE_DUMP, 4.0f * s, 4.0f * s, 2.0f * s);
        UUID waste2 = createNode(nx + 7.0f * s, ny, nz - 18.5f * s, ChamberType.WASTE_DUMP, 4.0f * s, 4.0f * s, 2.0f * s);
        createEdge(queen, waste1);
        createEdge(queen, waste2);
    }

    // ── 5. Cathedral Termite Mound (Isoptera) ───────────────────────────────────
    private void buildCathedralMoundNest(float nx, float ny, float nz, float s) {
        UUID ent1 = createNode(nx - 2.5f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID ent2 = createNode(nx + 2.5f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID spire = createNode(nx, ny, nz + 10.0f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 3.0f * s);
        UUID subHub = createNode(nx, ny, nz - 2.5f * s, ChamberType.TUNNEL, 2.2f * s, 2.2f * s, 1.8f * s);

        createEdge(ent1, spire);
        createEdge(ent2, spire);
        createEdge(ent1, subHub);
        createEdge(ent2, subHub);

        UUID royalCell = createNode(nx, ny, nz - 9.0f * s, ChamberType.QUEEN_CHAMBER, 5.4f * s, 5.4f * s, 2.5f * s);
        createEdge(subHub, royalCell);

        UUID nursery1 = createNode(nx + 4.5f * s, ny + 2.0f * s, nz - 5.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID nursery2 = createNode(nx - 4.5f * s, ny - 2.0f * s, nz - 5.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        createEdge(subHub, nursery1);
        createEdge(subHub, nursery2);

        UUID fungusComb1 = createNode(nx - 4.0f * s, ny + 3.5f * s, nz - 8.0f * s, ChamberType.FOOD_STORAGE, 4.0f * s, 4.0f * s, 2.2f * s);
        UUID fungusComb2 = createNode(nx + 4.0f * s, ny - 3.5f * s, nz - 8.0f * s, ChamberType.FOOD_STORAGE, 4.0f * s, 4.0f * s, 2.2f * s);
        createEdge(royalCell, fungusComb1);
        createEdge(royalCell, fungusComb2);

        UUID waste = createNode(nx, ny + 4.0f * s, nz - 14.0f * s, ChamberType.WASTE_DUMP, 3.5f * s, 3.5f * s, 1.8f * s);
        createEdge(royalCell, waste);
    }

    // ── 6. Complex Supercolony Network (Linepithema) ───────────────────────────
    private void buildSupercolonyNetworkNest(float nx, float ny, float nz, float s) {
        UUID mainEnt = createNode(nx, ny, nz - 0.1f, ChamberType.ENTRANCE, 2.5f * s, 2.5f * s, 1.8f * s);
        UUID leftEnt = createNode(nx - 9.0f * s, ny + 4.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID rightEnt = createNode(nx + 9.0f * s, ny - 4.0f * s, nz - 0.1f, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);

        UUID centerShaft = createNode(nx, ny, nz - 6.0f * s, ChamberType.TUNNEL, 2.2f * s, 2.2f * s, 1.8f * s);
        UUID leftShaft = createNode(nx - 8.0f * s, ny + 3.5f * s, nz - 5.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.6f * s);
        UUID rightShaft = createNode(nx + 8.0f * s, ny - 3.5f * s, nz - 5.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.6f * s);

        createEdge(mainEnt, centerShaft);
        createEdge(leftEnt, leftShaft);
        createEdge(rightEnt, rightShaft);
        createEdge(centerShaft, leftShaft);
        createEdge(centerShaft, rightShaft);

        // Polygyne queens
        UUID queen1 = createNode(nx, ny, nz - 13.0f * s, ChamberType.QUEEN_CHAMBER, 5.0f * s, 5.0f * s, 2.4f * s);
        UUID queen2 = createNode(nx - 10.0f * s, ny + 5.0f * s, nz - 10.5f * s, ChamberType.QUEEN_CHAMBER, 4.6f * s, 4.6f * s, 2.2f * s);
        UUID queen3 = createNode(nx + 10.0f * s, ny - 5.0f * s, nz - 10.5f * s, ChamberType.QUEEN_CHAMBER, 4.6f * s, 4.6f * s, 2.2f * s);
        createEdge(centerShaft, queen1);
        createEdge(leftShaft, queen2);
        createEdge(rightShaft, queen3);

        // Brood clusters
        UUID brood1 = createNode(nx - 4.0f * s, ny + 2.5f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood2 = createNode(nx + 4.0f * s, ny - 2.5f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood3 = createNode(nx - 12.0f * s, ny + 2.0f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood4 = createNode(nx + 12.0f * s, ny - 2.0f * s, nz - 8.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        createEdge(centerShaft, brood1);
        createEdge(centerShaft, brood2);
        createEdge(leftShaft, brood3);
        createEdge(rightShaft, brood4);

        // Granaries & Storage
        UUID food1 = createNode(nx + 4.5f * s, ny + 4.5f * s, nz - 7.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID food2 = createNode(nx - 4.5f * s, ny - 4.5f * s, nz - 7.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID food3 = createNode(nx, ny, nz - 16.5f * s, ChamberType.FOOD_STORAGE, 4.2f * s, 4.2f * s, 2.2f * s);
        createEdge(centerShaft, food1);
        createEdge(centerShaft, food2);
        createEdge(queen1, food3);

        UUID waste = createNode(nx, ny + 8.0f * s, nz - 19.0f * s, ChamberType.WASTE_DUMP, 3.8f * s, 3.8f * s, 1.8f * s);
        createEdge(food3, waste);
    }

    // ── 7. Hexagonal Wax Comb (Apis mellifera) ──────────────────────────────────
    private void buildHexagonalCombNest(float nx, float ny, float nz, float s) {
        UUID ent1 = createNode(nx - 2.0f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.5f * s);
        UUID ent2 = createNode(nx + 2.0f * s, ny, nz + 0.5f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.5f * s);
        UUID frameTop = createNode(nx, ny, nz + 3.0f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.5f * s);
        createEdge(ent1, frameTop);
        createEdge(ent2, frameTop);

        UUID queenCell = createNode(nx, ny, nz + 1.8f * s, ChamberType.QUEEN_CHAMBER, 4.5f * s, 4.5f * s, 2.2f * s);
        createEdge(frameTop, queenCell);

        UUID broodL = createNode(nx - 3.5f * s, ny, nz + 4.5f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        UUID broodR = createNode(nx + 3.5f * s, ny, nz + 4.5f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        createEdge(frameTop, broodL);
        createEdge(frameTop, broodR);

        UUID honeyVault1 = createNode(nx - 2.5f * s, ny, nz + 8.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        UUID honeyVault2 = createNode(nx + 2.5f * s, ny, nz + 8.0f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        createEdge(broodL, honeyVault1);
        createEdge(broodR, honeyVault2);
    }

    // ── 8. Wax Pots Cluster (Bombus terrestris) ─────────────────────────────────
    private void buildWaxPotsClusterNest(float nx, float ny, float nz, float s) {
        UUID ent = createNode(nx, ny, nz + 0.2f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.5f * s);
        UUID hub = createNode(nx, ny, nz - 0.8f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.4f * s);
        createEdge(ent, hub);

        UUID queen = createNode(nx, ny, nz - 1.8f * s, ChamberType.QUEEN_CHAMBER, 4.6f * s, 4.6f * s, 2.3f * s);
        createEdge(hub, queen);

        UUID brood1 = createNode(nx + 2.2f * s, ny + 1.5f * s, nz - 1.2f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        UUID brood2 = createNode(nx - 2.2f * s, ny - 1.5f * s, nz - 1.2f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        createEdge(hub, brood1);
        createEdge(hub, brood2);

        UUID honeyPot = createNode(nx + 1.8f * s, ny - 2.2f * s, nz - 1.0f * s, ChamberType.FOOD_STORAGE, 3.6f * s, 3.6f * s, 1.9f * s);
        UUID pollenPot = createNode(nx - 1.8f * s, ny + 2.2f * s, nz - 1.0f * s, ChamberType.FOOD_STORAGE, 3.6f * s, 3.6f * s, 1.9f * s);
        createEdge(hub, honeyPot);
        createEdge(hub, pollenPot);

        UUID waste = createNode(nx, ny + 3.0f * s, nz - 2.2f * s, ChamberType.WASTE_DUMP, 3.0f * s, 3.0f * s, 1.5f * s);
        createEdge(queen, waste);
    }

    // ── 9. Hanging Paper Nest (Vespula) ─────────────────────────────────────────
    private void buildPaperPedunculateNest(float nx, float ny, float nz, float s) {
        UUID branchAnchor = createNode(nx - 4.0f * s, ny, nz + 8.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.5f * s);
        UUID peduncle = createNode(nx, ny, nz + 8.0f * s, ChamberType.TUNNEL, 1.4f * s, 1.4f * s, 1.8f * s);
        UUID entrance = createNode(nx, ny, nz + 1.5f * s, ChamberType.ENTRANCE, 2.0f * s, 2.0f * s, 1.5f * s);
        UUID spire = createNode(nx, ny, nz + 4.5f * s, ChamberType.TUNNEL, 1.6f * s, 1.6f * s, 1.5f * s);

        createEdge(branchAnchor, peduncle);
        createEdge(peduncle, spire);
        createEdge(spire, entrance);

        UUID queenCell = createNode(nx, ny, nz + 6.5f * s, ChamberType.QUEEN_CHAMBER, 4.4f * s, 4.4f * s, 2.2f * s);
        createEdge(spire, queenCell);

        UUID brood1 = createNode(nx - 3.0f * s, ny, nz + 4.2f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        UUID brood2 = createNode(nx + 3.0f * s, ny, nz + 4.2f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 3.4f * s, 1.8f * s);
        createEdge(spire, brood1);
        createEdge(spire, brood2);

        UUID foodStorage = createNode(nx, ny + 3.0f * s, nz + 5.0f * s, ChamberType.FOOD_STORAGE, 3.5f * s, 3.5f * s, 1.8f * s);
        createEdge(spire, foodStorage);
    }

    // ── 10. Arboreal Silk Leaf Nest (Oecophylla) ────────────────────────────────
    private void buildArborealSilkLeafNest(float nx, float ny, float nz, float s) {
        UUID branch = createNode(nx, ny, nz + 6.0f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID canopyHub = createNode(nx, ny, nz + 10.0f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 1.5f * s);
        createEdge(branch, canopyHub);

        UUID queenLeaf = createNode(nx, ny, nz + 9.0f * s, ChamberType.QUEEN_CHAMBER, 4.8f * s, 4.8f * s, 2.4f * s);
        createEdge(canopyHub, queenLeaf);

        UUID broodLeaf1 = createNode(nx - 3.5f * s, ny, nz + 8.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID broodLeaf2 = createNode(nx + 3.5f * s, ny, nz + 8.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        createEdge(canopyHub, broodLeaf1);
        createEdge(canopyHub, broodLeaf2);

        UUID foodLeaf = createNode(nx, ny + 3.5f * s, nz + 8.0f * s, ChamberType.FOOD_STORAGE, 3.6f * s, 3.6f * s, 1.8f * s);
        createEdge(canopyHub, foodLeaf);
    }

    // ── 11. Carton Nest (Crematogaster) ─────────────────────────────────────────
    private void buildCartonNest(float nx, float ny, float nz, float s) {
        UUID entrance = createNode(nx, ny, nz + 4.0f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.5f * s);
        UUID core = createNode(nx, ny, nz + 6.5f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.6f * s);
        createEdge(entrance, core);

        UUID queenCell = createNode(nx, ny, nz + 6.0f * s, ChamberType.QUEEN_CHAMBER, 4.6f * s, 4.6f * s, 2.3f * s);
        createEdge(core, queenCell);

        UUID brood = createNode(nx - 3.0f * s, ny, nz + 7.2f * s, ChamberType.BROOD_CHAMBER, 3.5f * s, 3.5f * s, 1.8f * s);
        UUID food = createNode(nx + 3.0f * s, ny, nz + 7.2f * s, ChamberType.FOOD_STORAGE, 3.5f * s, 3.5f * s, 1.8f * s);
        createEdge(core, brood);
        createEdge(core, food);
    }

    // ── 12. Bamboo Stem Nest (Colobopsis / Temnothorax) ─────────────────────────
    private void buildBambooStemNest(float nx, float ny, float nz, float s) {
        UUID entrance = createNode(nx - 8.0f * s, ny, nz + 2.0f * s, ChamberType.ENTRANCE, 1.8f * s, 1.4f * s, 1.4f * s);
        UUID stemHub = createNode(nx, ny, nz + 2.0f * s, ChamberType.TUNNEL, 1.5f * s, 1.3f * s, 1.3f * s);
        createEdge(entrance, stemHub);

        UUID queen = createNode(nx + 3.0f * s, ny, nz + 2.0f * s, ChamberType.QUEEN_CHAMBER, 4.2f * s, 1.8f * s, 1.6f * s);
        createEdge(stemHub, queen);

        UUID brood = createNode(nx + 7.5f * s, ny, nz + 2.0f * s, ChamberType.BROOD_CHAMBER, 3.4f * s, 1.6f * s, 1.4f * s);
        createEdge(queen, brood);

        UUID food = createNode(nx - 3.5f * s, ny, nz + 2.0f * s, ChamberType.FOOD_STORAGE, 3.2f * s, 1.5f * s, 1.4f * s);
        createEdge(stemHub, food);
    }

    // ── 13. Living Bivouac Nest (Eciton Army Ants) ──────────────────────────────
    private void buildBivouacLivingNest(float nx, float ny, float nz, float s) {
        UUID logAnchor = createNode(nx, ny, nz + 3.5f * s, ChamberType.TUNNEL, 2.0f * s, 2.0f * s, 1.5f * s);
        UUID entrance = createNode(nx, ny, nz + 1.5f * s, ChamberType.ENTRANCE, 2.4f * s, 2.4f * s, 1.6f * s);
        createEdge(logAnchor, entrance);

        UUID core = createNode(nx, ny, nz + 0.2f * s, ChamberType.QUEEN_CHAMBER, 5.0f * s, 5.0f * s, 2.5f * s);
        createEdge(entrance, core);

        UUID brood = createNode(nx - 3.0f * s, ny, nz - 0.8f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID food = createNode(nx + 3.0f * s, ny, nz - 0.8f * s, ChamberType.FOOD_STORAGE, 3.6f * s, 3.6f * s, 1.8f * s);
        createEdge(core, brood);
        createEdge(core, food);
    }

    // ── 14. Hollow Trunk Nest (Camponotus Carpenter Ants) ──────────────────────
    private void buildHollowTrunkNest(float nx, float ny, float nz, float s) {
        UUID entrance = createNode(nx, ny - 2.0f * s, nz + 1.5f * s, ChamberType.ENTRANCE, 2.2f * s, 2.2f * s, 1.6f * s);
        UUID shaft1 = createNode(nx, ny, nz + 4.5f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 2.0f * s);
        UUID shaft2 = createNode(nx, ny, nz + 9.0f * s, ChamberType.TUNNEL, 1.8f * s, 1.8f * s, 2.0f * s);
        createEdge(entrance, shaft1);
        createEdge(shaft1, shaft2);

        UUID queen = createNode(nx, ny, nz + 7.0f * s, ChamberType.QUEEN_CHAMBER, 5.0f * s, 5.0f * s, 2.4f * s);
        createEdge(shaft1, queen);

        UUID brood1 = createNode(nx + 3.5f * s, ny, nz + 4.0f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        UUID brood2 = createNode(nx - 3.5f * s, ny, nz + 6.5f * s, ChamberType.BROOD_CHAMBER, 3.6f * s, 3.6f * s, 1.8f * s);
        createEdge(shaft1, brood1);
        createEdge(queen, brood2);

        UUID foodStorage = createNode(nx + 3.0f * s, ny, nz + 10.5f * s, ChamberType.FOOD_STORAGE, 3.8f * s, 3.8f * s, 2.0f * s);
        createEdge(shaft2, foodStorage);

        UUID wasteDump = createNode(nx, ny, nz + 0.5f * s, ChamberType.WASTE_DUMP, 3.2f * s, 3.2f * s, 1.6f * s);
        createEdge(entrance, wasteDump);
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

    private UUID createNode(float x, float y, float z, ChamberType type) {
        return createNode(x, y, z, type,
                TunnelNode.defaultRadius(type, true),
                TunnelNode.defaultRadius(type, true),
                TunnelNode.defaultRadius(type, false));
    }

    private UUID createNode(float x, float y, float z, ChamberType type, float rx, float ry, float rz) {
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

    private void createEdge(UUID from, UUID to) {
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
