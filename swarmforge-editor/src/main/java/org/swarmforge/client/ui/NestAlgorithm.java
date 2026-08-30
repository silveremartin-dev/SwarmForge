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
        NestGeneratorPane.GeneratedNest nest = new NestGeneratorPane.GeneratedNest();
        double maxDepth = p.getDepth();
        String arch = p.getArchitecture();
        String mat = p.getMaterial();
        double workerMm = p.getWorkerSizeMm();

        nest.maxDepth = maxDepth;
        nest.architecture = arch;
        nest.material = mat;
        nest.workerSizeMm = workerMm;

        // Scale factor relative to 5mm baseline
        double anatomicalScale = workerMm / 5.0;

        int entrances = Math.max(1, p.sp("🚪 Entrances"));
        int queenCnt  = p.sp("👑 Queen Chamber");
        int broodCnt  = p.sp("🥚 Brood Chambers");
        int foodCnt   = p.sp("🍖 Food Storage");
        int wasteCnt  = p.sp("🗑 Waste Dumps");
        int fungusCnt = p.sp("🍄 Fungus Gardens");
        int branching = (int) p.getBranching();

        long seed = p.getSeed();
        Random rnd = new Random(seed);

        String archKey = NestRenderer.normalizeArchKey(arch);
        switch (archKey) {
            case "WOODEN_BEEHIVE"             -> generateWoodenBeehive(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "WAX_COMB_HEXAGONAL"         -> generateHexagonalComb(nest, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "WAX_POTS_CLUSTER"           -> generatePotCluster(nest, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "PAPER_PEDUNCULATE"          -> generatePaperNest(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "CATHEDRAL_MOUND"           -> generateCathedralMound(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd);
            case "ARBOREAL_SILK_LEAF"         -> generateArborealLeafNest(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "SUBTERRANEAN_FUNGI_VAULT"   -> generateFungiVault(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd);
            case "CARTON_NEST"                -> generateCartonNest(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "BAMBOO_STEM_NEST"           -> generateStemGallNest(nest, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "BIVOUAC_LIVING_NEST"       -> generateBivouacNest(nest, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "HOLLOW_TRUNK_NEST"         -> generateHollowTrunkNest(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, anatomicalScale, rnd);
            case "SURFACE_MOUND"              -> generateMoundBurrow(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd, true);
            default                           -> generateMoundBurrow(nest, maxDepth, entrances, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd, false);
        }

        return nest;
    }

    // ── Helper to build queued chamber specifications ────────────────────────
    private static List<ChamberSpec> buildChamberQueue(int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        List<ChamberSpec> q = new ArrayList<>();
        for (int i = 0; i < queenCnt; i++)  q.add(new ChamberSpec("QUEEN",  0.80, 4.8 * scale, 2.4 * scale, Color.GOLD));
        for (int i = 0; i < broodCnt; i++)  q.add(new ChamberSpec("BROOD",  0.30, 3.4 * scale, 1.8 * scale, Color.DEEPSKYBLUE));
        for (int i = 0; i < foodCnt; i++)   q.add(new ChamberSpec("FOOD",   0.20, 3.6 * scale, 1.9 * scale, Color.ORANGE));
        for (int i = 0; i < fungusCnt; i++) q.add(new ChamberSpec("FUNGUS", 0.50, 4.2 * scale, 2.2 * scale, Color.MEDIUMPURPLE));
        for (int i = 0; i < wasteCnt; i++)  q.add(new ChamberSpec("WASTE",  0.65, 3.2 * scale, 1.6 * scale, Color.INDIANRED));
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

        double groundZ = hasSurfaceMound ? 5.0 : 0.0;

        // Entrances
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = 2 * Math.PI * i / entrances + rnd.nextDouble() * 0.4;
            double d   = entrances > 1 ? 4 + rnd.nextDouble() * 8 : 0;
            entNodes.add(node(nest, d * Math.cos(ang), d * Math.sin(ang), groundZ,
                "ENTRANCE", 2.0 * scale, Color.LIMEGREEN));
        }

        // Hub
        NestGeneratorPane.NestNode hub = node(nest, 0, 0, groundZ + 2.0, "JUNCTION", 1.1 * scale, Color.SLATEGRAY);
        for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, hub, rnd);

        // Main shaft
        List<NestGeneratorPane.NestNode> shaft = new ArrayList<>();
        shaft.add(hub);
        int steps = Math.max(4, (int)(maxDepth / 5.0));
        NestGeneratorPane.NestNode prev = hub;
        for (int i = 1; i <= steps; i++) {
            double z   = groundZ + (i / (double) steps) * maxDepth * 0.95;
            double ang = i * 0.75 + rnd.nextDouble() * 0.5;
            double r   = 1.5 + rnd.nextDouble() * 2.5;
            NestGeneratorPane.NestNode sn = node(nest, r * Math.cos(ang), r * Math.sin(ang), z, "JUNCTION", 1.1 * scale, Color.SLATEGRAY);
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
            double bl = 6 + rnd.nextDouble() * (5 + branching * 2.5);
            NestGeneratorPane.NestNode cn = nodeLenticular(nest,
                par.x + bl * Math.cos(ba), par.y + bl * Math.sin(ba),
                Math.min(maxDepth, par.z + (rnd.nextDouble() - 0.3) * 3),
                cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, par, cn, rnd);
        }

        if (branching >= 3 && shaft.size() >= 4) {
            for (int i = 1; i < shaft.size() - 2; i += 2)
                edge(nest, shaft.get(i), shaft.get(i + 2), rnd);
        }
    }

    // ── 2. Wooden Beehive (Dadant / Langstroth) ──────────────────────────────

    private static void generateWoodenBeehive(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double x = (i - (entrances - 1) / 2.0) * 4.0 * scale;
            entNodes.add(node(nest, x, 0, 0, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN));
        }

        double boxW = 12.0 * scale;
        int frames = 4;
        List<NestGeneratorPane.NestNode> frameTops = new ArrayList<>();

        for (int f = 0; f < frames; f++) {
            double yOff = (f - 1.5) * 3.5 * scale;
            NestGeneratorPane.NestNode frameTop = node(nest, 0, yOff, 2.0 * scale, "JUNCTION", 1.8 * scale, Color.SIENNA);
            for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, frameTop, rnd);
            if (f > 0 && !frameTops.isEmpty()) {
                edge(nest, frameTops.get(f - 1), frameTop, rnd);
            }
            frameTops.add(frameTop);
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int totalChambers = queue.size();
        for (int idx = 0; idx < totalChambers; idx++) {
            ChamberSpec cs = queue.get(idx);
            int frameIdx = idx % frames;
            NestGeneratorPane.NestNode fTop = frameTops.get(frameIdx);

            double x = (-boxW / 2.5) + ((idx / frames) % 4) * (boxW / 3.5);
            double z = 3.5 * scale + (idx / (frames * 4.0)) * 4.0 * scale;
            double yOff = fTop.y;

            NestGeneratorPane.NestNode cell = nodeLenticular(nest, x, yOff, z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, fTop, cell, rnd);
        }
    }

    // ── 3. Honeybee Hexagonal Comb ─────────────────────────────────────────────

    private static void generateHexagonalComb(NestGeneratorPane.GeneratedNest nest, int entrances,
            int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double x = (i - (entrances - 1) / 2.0) * 3.5 * scale;
            entNodes.add(node(nest, x, 0, 0, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN));
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        if (total == 0) return;

        double cellSize = 2.8 * scale;
        int cols = Math.max(3, (int) Math.ceil(Math.sqrt(total * 1.3)));
        int rows = (int) Math.ceil((double) total / cols);

        NestGeneratorPane.NestNode[][] grid = new NestGeneratorPane.NestNode[rows][cols];
        int qIdx = 0;

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (qIdx >= total) break;
                ChamberSpec cs = queue.get(qIdx++);
                double x = (c - cols / 2.0) * cellSize * 1.1;
                double z = (r + 1) * cellSize * 1.2;
                double y = (c % 2 == 0) ? 0 : cellSize * 0.4;

                NestGeneratorPane.NestNode cell = nodeLenticular(nest, x, y, z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
                grid[r][c] = cell;

                if (r == 0) {
                    for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, cell, rnd);
                }
                if (c > 0 && grid[r][c - 1] != null) edge(nest, grid[r][c - 1], cell, rnd);
                if (r > 0 && grid[r - 1][c] != null) edge(nest, grid[r - 1][c], cell, rnd);
            }
        }
    }

    // ── 4. Bumblebee Pot Cluster ────────────────────────────────────────────────

    private static void generatePotCluster(NestGeneratorPane.GeneratedNest nest, int entrances,
            int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances);
            entNodes.add(node(nest, 4.0 * scale * Math.cos(ang), 4.0 * scale * Math.sin(ang), 0, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN));
        }

        NestGeneratorPane.NestNode centerHub = node(nest, 0, 0, 2.0 * scale, "JUNCTION", 1.8 * scale, Color.SIENNA);
        for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, centerHub, rnd);

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevPot = centerHub;

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total)) + rnd.nextDouble() * 0.3;
            double dist = (4.5 + (i % 3) * 2.0) * scale;
            double z = (2.5 + rnd.nextDouble() * 3.0) * scale;

            NestGeneratorPane.NestNode pot = nodeLenticular(nest, dist * Math.cos(ang), dist * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, centerHub, pot, rnd);
            if (i > 0) edge(nest, prevPot, pot, rnd);
            prevPot = pot;
        }
    }

    // ── 5. Paper Wasp Hanging Nest ──────────────────────────────────────────────

    private static void generatePaperNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {

        NestGeneratorPane.NestNode supportBranch = node(nest, -8 * scale, 0, -5 * scale, "JUNCTION", 1.8 * scale, Color.SIENNA);

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double offset = (i - (entrances - 1) / 2.0) * 2.0 * scale;
            NestGeneratorPane.NestNode peduncle = node(nest, offset, 0, 0, "ENTRANCE", 1.8 * scale, Color.LIMEGREEN);
            edge(nest, supportBranch, peduncle, rnd);
            entNodes.add(peduncle);
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        if (total == 0) return;

        int tiers = Math.max(1, (int) Math.ceil(total / 6.0));
        NestGeneratorPane.NestNode prevTier = entNodes.get(0);

        int qIdx = 0;
        for (int t = 1; t <= tiers; t++) {
            double z = t * 4.5 * scale;
            NestGeneratorPane.NestNode tierCenter = node(nest, 0, 0, z, "JUNCTION", 1.5 * scale, Color.DARKGRAY);
            edge(nest, prevTier, tierCenter, rnd);
            prevTier = tierCenter;

            List<NestGeneratorPane.NestNode> tierCells = new ArrayList<>();
            int cellsInTier = Math.min(total - qIdx, 6);

            for (int i = 0; i < cellsInTier; i++) {
                ChamberSpec cs = queue.get(qIdx++);
                double ang = i * (Math.PI * 2 / Math.max(1, cellsInTier));
                double rad = (3.0 + (i % 2) * 2.5) * scale;

                NestGeneratorPane.NestNode cell = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
                edge(nest, tierCenter, cell, rnd);
                tierCells.add(cell);
                if (i > 0) edge(nest, tierCells.get(i - 1), cell, rnd);
            }
        }
    }

    // ── 6. Termite Cathedral Mound ──────────────────────────────────────────────

    private static void generateCathedralMound(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, int branching, double scale, Random rnd) {

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances);
            entNodes.add(node(nest, 3.5 * scale * Math.cos(ang), 3.5 * scale * Math.sin(ang), 0, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN));
        }

        NestGeneratorPane.NestNode prevTowerNode = entNodes.get(0);
        for (int h = -3; h >= -18; h -= 3) {
            double r = (18 + h) * 0.35 * scale;
            NestGeneratorPane.NestNode tn = node(nest, (rnd.nextDouble() - 0.5) * 1.5, (rnd.nextDouble() - 0.5) * 1.5, h, "JUNCTION", Math.max(1.0, r), Color.CHOCOLATE);
            edge(nest, prevTowerNode, tn, rnd);
            prevTowerNode = tn;
        }

        NestGeneratorPane.NestNode centralHub = node(nest, 0, 0, 3.0 * scale, "JUNCTION", 1.8 * scale, Color.CHOCOLATE);
        for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, centralHub, rnd);

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevCh = centralHub;

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total)) + (rnd.nextDouble() - 0.5) * 0.3;
            double dist = (4.5 + (i % 3) * 3.5) * scale;
            double z = 3.0 + (i / (double) Math.max(1, total)) * (maxDepth * 0.75);

            NestGeneratorPane.NestNode ch = nodeLenticular(nest, dist * Math.cos(ang), dist * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, centralHub, ch, rnd);
            if (i > 0) edge(nest, prevCh, ch, rnd);
            prevCh = ch;
        }
    }

    // ── 7. Arboreal Weaver Ant Leaf Nest ───────────────────────────────────────

    private static void generateArborealLeafNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {

        NestGeneratorPane.NestNode mainCanopyBranch = node(nest, -10 * scale, 0, -8 * scale, "JUNCTION", 2.0 * scale, Color.FORESTGREEN);

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double offset = (i - (entrances - 1) / 2.0) * 3.0 * scale;
            NestGeneratorPane.NestNode branchAnchor = node(nest, offset, 0, 0, "ENTRANCE", 2.0 * scale, Color.LIMEGREEN);
            edge(nest, mainCanopyBranch, branchAnchor, rnd);
            entNodes.add(branchAnchor);
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevLeaf = entNodes.get(0);

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total)) + rnd.nextDouble() * 0.5;
            double rad = (4.0 + rnd.nextDouble() * 6.0) * scale;
            double z = (-4.0 + rnd.nextDouble() * 10.0) * scale;

            NestGeneratorPane.NestNode leafCh = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, entNodes.get(0), leafCh, rnd);
            if (i > 0) edge(nest, prevLeaf, leafCh, rnd);
            prevLeaf = leafCh;
        }
    }

    // ── 8. Leafcutter Subterranean Fungi Vault (Atta) ───────────────────────────

    private static void generateFungiVault(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, int branching, double scale, Random rnd) {

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances) + rnd.nextDouble() * 0.4;
            entNodes.add(node(nest, (5.0 + i * 2.0) * Math.cos(ang) * scale, (5.0 + i * 2.0) * Math.sin(ang) * scale, 0.0, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN));
        }

        NestGeneratorPane.NestNode centralHub = node(nest, 0, 0, 3.0 * scale, "JUNCTION", 1.5 * scale, Color.SLATEGRAY);
        for (NestGeneratorPane.NestNode c : entNodes) edge(nest, c, centralHub, rnd);

        NestGeneratorPane.NestNode mainShaft = node(nest, 0, 0, maxDepth * 0.4, "JUNCTION", 1.8 * scale, Color.SLATEGRAY);
        edge(nest, centralHub, mainShaft, rnd);

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevV = mainShaft;

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total));
            double dist = (6.0 + (i % 3) * 4.0) * scale;
            double z = maxDepth * 0.30 + (i / (double) Math.max(1, total)) * (maxDepth * 0.55);

            NestGeneratorPane.NestNode vault = nodeLenticular(nest, dist * Math.cos(ang), dist * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, mainShaft, vault, rnd);
            if (i > 0) edge(nest, prevV, vault, rnd);
            prevV = vault;
        }
    }

    // ── 9. Wood Pulp / Carton Nest (Crematogaster / Lasius fuliginosus) ────────

    private static void generateCartonNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {

        NestGeneratorPane.NestNode trunkAnchor = node(nest, 0, 0, -10.0 * scale, "JUNCTION", 2.5 * scale, Color.SIENNA);

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances);
            NestGeneratorPane.NestNode nestCenter = node(nest, 3.0 * scale * Math.cos(ang), 3.0 * scale * Math.sin(ang), 0, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN);
            edge(nest, trunkAnchor, nestCenter, rnd);
            entNodes.add(nestCenter);
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevNode = entNodes.get(0);

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total));
            double rad = (4.0 + (i % 3) * 3.0) * scale;
            double z = (-3.0 + (i % 4) * 2.5) * scale;

            NestGeneratorPane.NestNode cell = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, entNodes.get(0), cell, rnd);
            if (i > 0) edge(nest, prevNode, cell, rnd);
            prevNode = cell;
        }
    }

    // ── 10. Bamboo / Plant Stem Gall Nest (Colobopsis / Temnothorax) ────────────

    private static void generateStemGallNest(NestGeneratorPane.GeneratedNest nest,
            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double x = (-12.0 + i * 2.5) * scale;
            entNodes.add(node(nest, x, 0, 0, "ENTRANCE", 1.5 * scale, Color.LIMEGREEN));
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevChamber = entNodes.get(0);

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double x = (-9.0 + i * 4.5) * scale;
            NestGeneratorPane.NestNode stemCell = nodeLenticular(nest, x, 0, 0, cs.type, cs.rx, 1.4 * scale, 1.2 * scale, cs.color);
            edge(nest, prevChamber, stemCell, rnd);
            prevChamber = stemCell;
        }
    }

    // ── 11. Living Worker Bivouac Nest (Eciton Army Ants) ──────────────────────

    private static void generateBivouacNest(NestGeneratorPane.GeneratedNest nest,
            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {

        NestGeneratorPane.NestNode logAnchor = node(nest, 0, 0, -8.0 * scale, "JUNCTION", 2.2 * scale, Color.SIENNA);

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances);
            NestGeneratorPane.NestNode bivouacHead = node(nest, 2.5 * scale * Math.cos(ang), 2.5 * scale * Math.sin(ang), -4.0 * scale, "ENTRANCE", 2.5 * scale, Color.LIMEGREEN);
            edge(nest, logAnchor, bivouacHead, rnd);
            entNodes.add(bivouacHead);
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevBody = entNodes.get(0);

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total));
            double rad = (3.5 + (i % 2) * 2.5) * scale;
            double z = (-2.0 + (i / 2.0) * 2.0) * scale;

            NestGeneratorPane.NestNode bodyCluster = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, entNodes.get(0), bodyCluster, rnd);
            if (i > 0) edge(nest, prevBody, bodyCluster, rnd);
            prevBody = bodyCluster;
        }
    }

    // ── 12. Hollow Tree Trunk Cavity Nest ─────────────────────────────────────

    private static void generateHollowTrunkNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double y = (-5.5 + i * 2.0) * scale;
            entNodes.add(node(nest, 0, y, -4.0 * scale, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN));
        }

        NestGeneratorPane.NestNode cavityCenter = node(nest, 0, 0, 0, "JUNCTION", 2.0 * scale, Color.SIENNA);
        for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, cavityCenter, rnd);

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevLevel = cavityCenter;

        for (int l = 0; l < total; l++) {
            ChamberSpec cs = queue.get(l);
            double z = (-10.0 + l * (20.0 / Math.max(1, total - 1))) * scale;
            double ang = l * 1.2 + rnd.nextDouble() * 0.4;
            double rad = (2.0 + (l % 2) * 2.5) * scale;

            NestGeneratorPane.NestNode cell = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, cavityCenter, cell, rnd);
            if (l > 0) edge(nest, prevLevel, cell, rnd);
            prevLevel = cell;
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
            double off = (rnd.nextDouble() - 0.5) * 2.5 * f;
            pts.add(new double[]{from.x + dx * i + px * off, from.y + dy * i + py * off,
                from.z + dz * i + (rnd.nextDouble() - 0.5) * f});
        }
        pts.add(new double[]{to.x, to.y, to.z});
        nest.edges.add(new NestGeneratorPane.NestEdge(from, to, pts));
    }
}
