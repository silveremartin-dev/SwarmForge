/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.paint.Color;

import java.util.*;

/**
 * Universal, biologically-inspired social insect nest generation algorithm.
 * Supports Ants, Honeybees, Bumblebees, Wasps, Termites, and Weaver Ants with:
 * - Anatomical scaling (workerSizeMm -> lenticular chamber volume & tunnel gauge)
 * - Diverse architectures (BURROW_UNDERGROUND, WAX_COMB_HEXAGONAL, WAX_POTS_CLUSTER,
 *   PAPER_PEDUNCULATE, CATHEDRAL_MOUND, ARBOREAL_SILK_LEAF, SURFACE_MOUND, etc.)
 * - Strict adherence to user-defined chamber distributions (Queen, Brood, Food, Waste, Fungus, Entrances)
 * - Realistic material colorations and depth-zoned microclimates
 */
public final class NestAlgorithm {

    private NestAlgorithm() {}

    public static NestGeneratorPane.GeneratedNest generate(NestGeneratorPane p) {
        return generate(p.getConfiguration(), p.getSeed());
    }

    @SuppressWarnings("unchecked")
    public static NestGeneratorPane.GeneratedNest generate(Map<String, Object> config, long seed) {
        NestGeneratorPane.GeneratedNest nest = new NestGeneratorPane.GeneratedNest();
        if (config == null) config = Collections.emptyMap();

        double maxDepth = getDouble(config, "depth", 25.0);
        String arch = getString(config, "architecture", "BURROW_UNDERGROUND");
        String mat = getString(config, "material", "EARTH");
        double workerMm = getDouble(config, "workerSizeMm", 5.0);

        nest.maxDepth = maxDepth;
        nest.architecture = arch;
        nest.material = mat;
        nest.workerSizeMm = workerMm;

        // Scale factor relative to 5mm baseline
        double anatomicalScale = workerMm / 5.0;

        Map<String, Integer> dist = config.get("chamberDistribution") instanceof Map
                ? (Map<String, Integer>) config.get("chamberDistribution")
                : Collections.emptyMap();

        int entrances = Math.max(1, getDist(dist, "🚪 Entrances", 2));
        int queenCnt  = getDist(dist, "👑 Queen Chamber", 1);
        int broodCnt  = getDist(dist, "🥚 Brood Chambers", 3);
        int foodCnt   = getDist(dist, "🍖 Food Storage", 2);
        int wasteCnt  = getDist(dist, "🗑 Waste Dumps", 1);
        int fungusCnt = getDist(dist, "🍄 Fungus Gardens", 0);
        int branching = getInt(config, "branching", 2);

        Random rnd = new Random(seed);

        String archKey = NestRenderer.normalizeArchKey(arch);
        switch (archKey) {
            case "WOODEN_BEEHIVE"             -> NestSpecializedAlgorithms.generateWoodenBeehive(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "WAX_COMB_HEXAGONAL"         -> NestSpecializedAlgorithms.generateHexagonalComb(nest, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "WAX_POTS_CLUSTER"           -> NestSpecializedAlgorithms.generatePotCluster(nest, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "PAPER_PEDUNCULATE"          -> NestSpecializedAlgorithms.generatePaperNest(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "CATHEDRAL_MOUND"           -> NestSpecializedAlgorithms.generateCathedralMound(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd);
            case "ARBOREAL_SILK_LEAF"         -> NestSpecializedAlgorithms.generateArborealLeafNest(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "SUBTERRANEAN_FUNGI_VAULT"   -> NestSpecializedAlgorithms.generateFungiVault(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd);
            case "CARTON_NEST"                -> NestSpecializedAlgorithms.generateCartonNest(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "BAMBOO_STEM_NEST"           -> NestSpecializedAlgorithms.generateStemGallNest(nest, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "BIVOUAC_LIVING_NEST"       -> NestSpecializedAlgorithms.generateBivouacNest(nest, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "HOLLOW_TRUNK_NEST"         -> NestSpecializedAlgorithms.generateHollowTrunkNest(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "SURFACE_MOUND"              -> generateMoundBurrow(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd, true);
            default                           -> generateMoundBurrow(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd, false);
        }

        return nest;
    }

    public static void applyToTunnelNetwork(NestGeneratorPane.GeneratedNest genNest, float nx, float ny, float nz, org.swarmforge.core.simulation.TunnelNetwork tn, org.swarmforge.core.domain.Colony colony) {
        if (genNest == null || tn == null) return;

        // Determine ground elevation if nz is not set
        if (nz <= 0.5f && colony != null && colony.getTerrarium() != null) {
            nz = colony.getTerrarium().getSurfaceElevation(nx, ny);
        }
        if (nz <= 0.5f) {
            nz = 10.0f; // Standard baseline ground elevation
        }

        // Measure maximum vertical subterranean extent designed in the nest preset
        float maxNestDepth = 0.0f;
        for (NestGeneratorPane.NestNode n : genNest.nodes) {
            if (n.z > maxNestDepth) {
                maxNestDepth = (float) n.z;
            }
        }

        // Available subterranean soil depth between ground surface and bedrock floor (1.0m)
        float availableSoil = Math.max(1.0f, nz - 1.0f);
        float depthScale = (maxNestDepth > availableSoil && maxNestDepth > 0.001f)
                ? (availableSoil / maxNestDepth)
                : 1.0f;

        Map<NestGeneratorPane.NestNode, UUID> nodeMap = new HashMap<>();
        Map<UUID, org.swarmforge.core.simulation.TunnelNetwork.TunnelNode> simNodeMap = new HashMap<>();
        List<org.swarmforge.core.simulation.TunnelNetwork.TunnelNode> simNodes = new ArrayList<>();
        List<org.swarmforge.core.simulation.TunnelNetwork.TunnelEdge> simEdges = new ArrayList<>();

        for (NestGeneratorPane.NestNode n : genNest.nodes) {
            UUID id = UUID.randomUUID();
            nodeMap.put(n, id);
            float wx = nx + (float) n.x;
            float wy = ny + (float) n.y;
            float wz;

            if (n.z >= 0) {
                // Subterranean excavation: descends smoothly into the soil column
                wz = nz - (float) n.z * depthScale;
            } else {
                // Epigeic / mound / arboreal above ground: rises above ground surface
                wz = nz + (float) (-n.z);
            }

            org.swarmforge.core.simulation.TunnelNetwork.ChamberType cType = mapChamberType(n.type);
            float rx = (float) (n.rx > 0 ? n.rx : n.radius);
            float ry = (float) (n.ry > 0 ? n.ry : n.radius);
            float rz = (float) (n.rz > 0 ? n.rz : n.radius * 0.55);

            org.swarmforge.core.simulation.TunnelNetwork.TunnelNode tNode =
                    new org.swarmforge.core.simulation.TunnelNetwork.TunnelNode(id, wx, wy, wz, cType, rx, ry, rz);
            simNodes.add(tNode);
            simNodeMap.put(id, tNode);
        }

        for (NestGeneratorPane.NestEdge e : genNest.edges) {
            UUID fromId = nodeMap.get(e.from);
            UUID toId = nodeMap.get(e.to);
            if (fromId != null && toId != null) {
                org.swarmforge.core.simulation.TunnelNetwork.TunnelNode nFrom = simNodeMap.get(fromId);
                org.swarmforge.core.simulation.TunnelNetwork.TunnelNode nTo = simNodeMap.get(toId);
                List<float[]> pathPoints = new ArrayList<>();
                if (e.pts != null && !e.pts.isEmpty() && nFrom != null && nTo != null) {
                    int ptCount = e.pts.size();
                    for (int pIdx = 0; pIdx < ptCount; pIdx++) {
                        float t = (pIdx + 1.0f) / (ptCount + 1.0f);
                        float px = nFrom.x() + t * (nTo.x() - nFrom.x());
                        float py = nFrom.y() + t * (nTo.y() - nFrom.y());
                        float pz = nFrom.z() + t * (nTo.z() - nFrom.z());
                        pathPoints.add(new float[]{px, py, pz});
                    }
                }
                float dist = (nFrom != null && nTo != null)
                        ? (float) Math.sqrt(Math.pow(nTo.x() - nFrom.x(), 2) + Math.pow(nTo.y() - nFrom.y(), 2) + Math.pow(nTo.z() - nFrom.z(), 2))
                        : (float) Math.sqrt(Math.pow(e.from.x - e.to.x, 2) + Math.pow(e.from.y - e.to.y, 2) + Math.pow(e.from.z - e.to.z, 2));
                simEdges.add(new org.swarmforge.core.simulation.TunnelNetwork.TunnelEdge(fromId, toId, dist, pathPoints));
            }
        }

        tn.populate(simNodes, simEdges, colony);
    }

    private static org.swarmforge.core.simulation.TunnelNetwork.ChamberType mapChamberType(String type) {
        if (type == null) return org.swarmforge.core.simulation.TunnelNetwork.ChamberType.TUNNEL;
        String t = type.toUpperCase().trim();
        return switch (t) {
            case "ENTRANCE", "ENTRY" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.ENTRANCE;
            case "QUEEN", "QUEEN_CHAMBER", "ROYAL_CELL" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.QUEEN_CHAMBER;
            case "BROOD", "BROOD_CHAMBER", "NURSERY" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.BROOD_CHAMBER;
            case "FOOD", "FOOD_STORAGE", "STORAGE", "GRANARY" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.FOOD_STORAGE;
            case "WASTE", "WASTE_DUMP", "MIDDEN" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.WASTE_DUMP;
            case "FUNGUS", "FUNGUS_GARDEN" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.FUNGUS_GARDEN;
            case "HIBERNATION" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.HIBERNATION;
            case "SOLARIUM" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.SOLARIUM;
            case "HONEYCOMB", "HONEY_HONEYCOMB" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.HONEYCOMB;
            case "POLLEN_POT" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.POLLEN_POT;
            case "VENTILATION", "VENTILATION_CHIMNEY" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.VENTILATION_CHIMNEY;
            case "LEAF_CACHE" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.LEAF_CACHE;
            case "GALL_NURSERY" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.GALL_NURSERY;
            case "BIVOUAC_CORE" -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.BIVOUAC_CORE;
            default -> org.swarmforge.core.simulation.TunnelNetwork.ChamberType.TUNNEL;
        };
    }

    private static double getDouble(Map<String, Object> m, String key, double def) {
        if (m != null && m.containsKey(key)) {
            Object v = m.get(key);
            if (v instanceof Number n) return n.doubleValue();
        }
        return def;
    }

    private static int getInt(Map<String, Object> m, String key, int def) {
        if (m != null && m.containsKey(key)) {
            Object v = m.get(key);
            if (v instanceof Number n) return n.intValue();
        }
        return def;
    }

    private static String getString(Map<String, Object> m, String key, String def) {
        if (m != null && m.containsKey(key)) {
            Object v = m.get(key);
            if (v != null) return v.toString();
        }
        return def;
    }

    private static int getDist(Map<String, Integer> dist, String key, int def) {
        if (dist == null || dist.isEmpty()) return def;
        if (dist.containsKey(key)) {
            Object v = dist.get(key);
            if (v instanceof Number n) return n.intValue();
        }
        String cleanKey = key.replaceAll("[^a-zA-Z]", "").toLowerCase();
        for (Map.Entry<String, Integer> e : dist.entrySet()) {
            if (e.getKey() != null && e.getKey().replaceAll("[^a-zA-Z]", "").toLowerCase().contains(cleanKey)) {
                if (e.getValue() != null) return e.getValue();
            }
        }
        return def;
    }

    // ── Helper to build queued chamber specifications ────────────────────────
    private static List<ChamberSpec> buildChamberQueue(int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        List<ChamberSpec> q = new ArrayList<>();
        for (int i = 0; i < queenCnt; i++)  q.add(new ChamberSpec("QUEEN",  0.80, 0.15 * scale, 0.08 * scale, NestRenderer.CHAMBER_COLOR_QUEEN));
        for (int i = 0; i < broodCnt; i++)  q.add(new ChamberSpec("BROOD",  0.30, 0.08 * scale, 0.04 * scale, NestRenderer.CHAMBER_COLOR_BROOD));
        for (int i = 0; i < foodCnt; i++)   q.add(new ChamberSpec("FOOD",   0.20, 0.08 * scale, 0.04 * scale, NestRenderer.CHAMBER_COLOR_STORAGE));
        for (int i = 0; i < fungusCnt; i++) q.add(new ChamberSpec("FUNGUS", 0.50, 0.12 * scale, 0.06 * scale, NestRenderer.CHAMBER_COLOR_FUNGUS));
        for (int i = 0; i < wasteCnt; i++)  q.add(new ChamberSpec("WASTE",  0.65, 0.06 * scale, 0.03 * scale, NestRenderer.CHAMBER_COLOR_WASTE));
        return q;
    }

    private static class ChamberSpec {
        String type;
        double targetDepthRel;
        double rx, rz;
        Color color;
        ChamberSpec(String type, double targetDepthRel, double rx, double rz, Color color) {
            this.type = type; this.targetDepthRel = targetDepthRel; this.rx = rx; this.rz = rz; this.color = color;
        }
    }

    // ── 1. Subterranean Burrow & Mound (Ants) ──────────────────────────────────

    private static void generateMoundBurrow(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt,
            int branching, double scale, Random rnd, boolean hasSurfaceMound) {

        double groundZ = 0.0;

        // Entrances
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        if (hasSurfaceMound) {
            // Apex chimney entrance on top of the pine-needle thatch dome
            entNodes.add(node(nest, 0, 0, -0.65 * scale, "ENTRANCE", 0.05 * scale, Color.LIMEGREEN));
            for (int i = 1; i < entrances; i++) {
                double ang = 2 * Math.PI * (i - 1) / Math.max(1, entrances - 1) + rnd.nextDouble() * 0.4;
                double d = (0.35 + rnd.nextDouble() * 0.25) * scale;
                entNodes.add(node(nest, d * Math.cos(ang), d * Math.sin(ang), -0.20 * scale,
                    "ENTRANCE", 0.045 * scale, Color.LIMEGREEN));
            }
        } else {
            for (int i = 0; i < entrances; i++) {
                double ang = 2 * Math.PI * i / entrances + rnd.nextDouble() * 0.4;
                double d   = entrances > 1 ? (0.20 + rnd.nextDouble() * 0.35) * scale : 0;
                entNodes.add(node(nest, d * Math.cos(ang), d * Math.sin(ang), groundZ,
                    "ENTRANCE", 0.045 * scale, Color.LIMEGREEN));
            }
        }

        // Hub
        NestGeneratorPane.NestNode hub = node(nest, 0, 0, groundZ + 0.20 * scale, "JUNCTION", 0.035 * scale, Color.SLATEGRAY);
        for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, hub, rnd);

        // Epigeic Solarium Incubation Chambers inside the warm above-ground thatch mound (Formica rufa)
        if (hasSurfaceMound) {
            int solariumBrood = Math.min(broodCnt, 2);
            for (int s = 0; s < solariumBrood; s++) {
                double ang = s * Math.PI + (rnd.nextDouble() - 0.5) * 0.5;
                double rad = (0.25 + rnd.nextDouble() * 0.20) * scale;
                double z = (-0.40 + s * 0.15) * scale;
                NestGeneratorPane.NestNode solarium = nodeLenticular(nest,
                    rad * Math.cos(ang), rad * Math.sin(ang), z,
                    "BROOD", 0.08 * scale, 0.08 * scale, 0.045 * scale, NestRenderer.CHAMBER_COLOR_BROOD);
                edge(nest, entNodes.get(0), solarium, rnd);
                edge(nest, hub, solarium, rnd);
            }
            broodCnt = Math.max(0, broodCnt - solariumBrood);
        }

        // Main subterranean shaft
        List<NestGeneratorPane.NestNode> shaft = new ArrayList<>();
        shaft.add(hub);
        int steps = Math.max(4, (int)(maxDepth / 0.50));
        NestGeneratorPane.NestNode prev = hub;
        for (int i = 1; i <= steps; i++) {
            double z   = groundZ + (i / (double) steps) * maxDepth * 0.95;
            double ang = i * 0.75 + rnd.nextDouble() * 0.5;
            double r   = (0.04 + rnd.nextDouble() * 0.12) * scale;
            NestGeneratorPane.NestNode sn = node(nest, r * Math.cos(ang), r * Math.sin(ang), z, "JUNCTION", 0.035 * scale, Color.SLATEGRAY);
            edge(nest, prev, sn, rnd);
            shaft.add(sn);
            prev = sn;
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        for (ChamberSpec cs : queue) {
            double targetZ = groundZ + cs.targetDepthRel * maxDepth;
            NestGeneratorPane.NestNode par = shaft.get(0);
            double best = Double.MAX_VALUE;
            for (NestGeneratorPane.NestNode sn : shaft) {
                double diff = Math.abs(sn.z - targetZ);
                if (diff < best) { best = diff; par = sn; }
            }
            double ba = rnd.nextDouble() * Math.PI * 2;
            double bl = (0.35 + rnd.nextDouble() * (0.35 + branching * 0.12)) * scale;
            NestGeneratorPane.NestNode cn = nodeLenticular(nest,
                par.x + bl * Math.cos(ba), par.y + bl * Math.sin(ba),
                Math.min(maxDepth, par.z + (rnd.nextDouble() - 0.3) * 0.35),
                cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, par, cn, rnd);
        }

        if (branching >= 3 && shaft.size() >= 4) {
            for (int i = 1; i < shaft.size() - 2; i += 2)
                edge(nest, shaft.get(i), shaft.get(i + 2), rnd);
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static NestGeneratorPane.NestNode node(NestGeneratorPane.GeneratedNest nest,
            double x, double y, double z, String type, double r, Color c) {
        NestGeneratorPane.NestNode n = new NestGeneratorPane.NestNode(x, y, z, type, r, c);
        nest.nodes.add(n);
        return n;
    }

    private static NestGeneratorPane.NestNode nodeLenticular(NestGeneratorPane.GeneratedNest nest,
            double x, double y, double z, String type, double rx, double ry, double rz, Color c) {
        NestGeneratorPane.NestNode n = new NestGeneratorPane.NestNode(x, y, z, type, rx, ry, rz, c);
        nest.nodes.add(n);
        return n;
    }

    private static void edge(NestGeneratorPane.GeneratedNest nest,
            NestGeneratorPane.NestNode from, NestGeneratorPane.NestNode to, Random rnd) {
        List<double[]> pts = new ArrayList<>();
        pts.add(new double[]{from.x, from.y, from.z});
        int seg = 5;
        double dx = (to.x - from.x) / seg, dy = (to.y - from.y) / seg, dz = (to.z - from.z) / seg;
        double px = -dy, py = dx, len = Math.hypot(px, py);
        if (len > 0.001) { px /= len; py /= len; }
        for (int i = 1; i < seg; i++) {
            double f = Math.sin((i / (double) seg) * Math.PI);
            double off = (rnd.nextDouble() - 0.5) * 0.08 * f;
            pts.add(new double[]{from.x + dx * i + px * off, from.y + dy * i + py * off,
                from.z + dz * i + (rnd.nextDouble() - 0.5) * 0.04 * f});
        }
        pts.add(new double[]{to.x, to.y, to.z});
        nest.edges.add(new NestGeneratorPane.NestEdge(from, to, pts));
    }
}
