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
 *   PAPER_PEDUNCULATE, CATHEDRAL_MOUND, ARBOREAL_SILK_LEAF, SURFACE_MOUND)
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
        int chamberTarget = (int) p.getChamberCount();

        Random rnd = new Random((long)(maxDepth*31 + entrances*7 + queenCnt*3 + broodCnt*17 + arch.hashCode()));

        String archKey = NestRenderer.normalizeArchKey(arch);
        switch (archKey) {
            case "WOODEN_BEEHIVE"             -> generateWoodenBeehive(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, anatomicalScale, rnd);
            case "WAX_COMB_HEXAGONAL"         -> generateHexagonalComb(nest, chamberTarget, queenCnt, broodCnt, foodCnt, anatomicalScale, rnd);
            case "WAX_POTS_CLUSTER"           -> generatePotCluster(nest, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, anatomicalScale, rnd);
            case "PAPER_PEDUNCULATE"          -> generatePaperNest(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, anatomicalScale, rnd);
            case "CATHEDRAL_MOUND"           -> generateCathedralMound(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd);
            case "ARBOREAL_SILK_LEAF"         -> generateArborealLeafNest(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, anatomicalScale, rnd);
            case "SUBTERRANEAN_FUNGI_VAULT"   -> generateFungiVault(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd);
            case "CARTON_NEST"                -> generateCartonNest(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, anatomicalScale, rnd);
            case "BAMBOO_STEM_NEST"           -> generateStemGallNest(nest, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, anatomicalScale, rnd);
            case "BIVOUAC_LIVING_NEST"       -> generateBivouacNest(nest, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, anatomicalScale, rnd);
            case "HOLLOW_TRUNK_NEST"         -> generateHollowTrunkNest(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, anatomicalScale, rnd);
            case "SURFACE_MOUND"              -> generateMoundBurrow(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd, true);
            default                           -> generateMoundBurrow(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd, false);
        }

        return nest;
    }

    // ── 1. Subterranean Burrow & Mound (Ants) ──────────────────────────────────

    private static void generateMoundBurrow(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int chamberTarget, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt,
            int branching, double scale, Random rnd, boolean hasSurfaceMound) {

        int entrances = Math.max(1, nest.nodes.size());
        double groundZ = hasSurfaceMound ? 5.0 : 0.0;

        // Entrances
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < Math.max(1, entrances); i++) {
            double ang = 2*Math.PI*i/Math.max(1, entrances) + rnd.nextDouble()*0.4;
            double d   = entrances > 1 ? 4 + rnd.nextDouble()*8 : 0;
            entNodes.add(node(nest, d*Math.cos(ang), d*Math.sin(ang), groundZ,
                "ENTRANCE", 2.0*scale, Color.LIMEGREEN));
        }

        // Hub
        NestGeneratorPane.NestNode hub = node(nest, 0, 0, groundZ + 2.0, "JUNCTION", 1.1*scale, Color.SLATEGRAY);
        for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, hub, rnd);

        // Main shaft
        List<NestGeneratorPane.NestNode> shaft = new ArrayList<>();
        shaft.add(hub);
        int steps = Math.max(4, (int)(maxDepth/5.0));
        NestGeneratorPane.NestNode prev = hub;
        for (int i = 1; i <= steps; i++) {
            double z   = groundZ + (i/(double)steps)*maxDepth*0.95;
            double ang = i*0.75 + rnd.nextDouble()*0.5;
            double r   = 1.5 + rnd.nextDouble()*2.5;
            NestGeneratorPane.NestNode sn = node(nest, r*Math.cos(ang), r*Math.sin(ang), z, "JUNCTION", 1.1*scale, Color.SLATEGRAY);
            edge(nest, prev, sn, rnd);
            shaft.add(sn);
            prev = sn;
        }

        // Chambers with lenticular anatomical shapes
        List<String[]> queue = new ArrayList<>();
        fill(queue, "QUEEN",  queenCnt,  0.80, 0.15, 4.5*scale, 2.2*scale, Color.GOLD);
        fill(queue, "BROOD",  broodCnt,  0.25, 0.40, 3.2*scale, 1.6*scale, Color.DEEPSKYBLUE);
        fill(queue, "FOOD",   foodCnt,   0.15, 0.35, 3.5*scale, 1.8*scale, Color.ORANGE);
        fill(queue, "FUNGUS", fungusCnt, 0.45, 0.30, 4.0*scale, 2.0*scale, Color.MEDIUMPURPLE);
        fill(queue, "WASTE",  wasteCnt,  0.55, 0.30, 3.0*scale, 1.5*scale, Color.INDIANRED);

        for (String[] q : queue) {
            double targetZ = groundZ + Double.parseDouble(q[1]) * maxDepth;
            NestGeneratorPane.NestNode par = shaft.get(0);
            double best = Double.MAX_VALUE;
            for (NestGeneratorPane.NestNode sn : shaft) {
                double diff = Math.abs(sn.z - targetZ);
                if (diff < best) { best = diff; par = sn; }
            }
            double ba = rnd.nextDouble()*Math.PI*2;
            double bl = 6 + rnd.nextDouble()*(5+branching*2.5);
            Color col = parseColor(q[4]);
            double rx = Double.parseDouble(q[2]);
            double rz = Double.parseDouble(q[3]);
            NestGeneratorPane.NestNode cn = nodeLenticular(nest,
                par.x + bl*Math.cos(ba), par.y + bl*Math.sin(ba),
                Math.min(maxDepth, par.z + (rnd.nextDouble()-0.3)*3),
                q[0], rx, rx, rz, col);
            edge(nest, par, cn, rnd);
        }

        if (branching >= 3 && shaft.size() >= 4) {
            for (int i = 1; i < shaft.size()-2; i += 2)
                edge(nest, shaft.get(i), shaft.get(i+2), rnd);
        }
    }

    // ── 2. Wooden Beehive (Dadant / Langstroth) ──────────────────────────────

    private static void generateWoodenBeehive(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int count, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, double scale, Random rnd) {
        // Entrance flight board at base of hive box
        NestGeneratorPane.NestNode flightBoard = node(nest, 0, 0, 0, "ENTRANCE", 3.0 * scale, Color.LIMEGREEN);

        // Frame box boundaries and parallel vertical frames
        double boxW = 12.0 * scale;
        int frames = 4;
        NestGeneratorPane.NestNode prevFrameTop = flightBoard;

        for (int f = 0; f < frames; f++) {
            double yOff = (f - 1.5) * 3.5 * scale;
            NestGeneratorPane.NestNode frameTop = node(nest, 0, yOff, 2.0 * scale, "JUNCTION", 1.8 * scale, Color.SIENNA);
            edge(nest, flightBoard, frameTop, rnd);
            if (f > 0) {
                edge(nest, prevFrameTop, frameTop, rnd);
            }
            prevFrameTop = frameTop;

            int cellsPerFrame = Math.max(3, count / frames);
            NestGeneratorPane.NestNode prevCell = frameTop;
            for (int i = 0; i < cellsPerFrame; i++) {
                double x = (-boxW / 2.5) + (i % 4) * (boxW / 3.5);
                double z = 3.5 * scale + (i / 4) * 3.2 * scale;

                String type = (z < 6.0 * scale && queenCnt > 0 && f == 1 && i == 0) ? "QUEEN"
                            : (z > 8.0 * scale) ? "FOOD" : "BROOD";
                Color col = type.equals("QUEEN") ? Color.GOLD : type.equals("FOOD") ? Color.ORANGE : Color.DEEPSKYBLUE;

                NestGeneratorPane.NestNode cell = nodeLenticular(nest, x, yOff, z, type, 3.2 * scale, 2.2 * scale, 2.8 * scale, col);
                edge(nest, frameTop, cell, rnd);
                if (i > 0) {
                    edge(nest, prevCell, cell, rnd);
                }
                prevCell = cell;
            }
        }
    }

    // ── 3. Honeybee Hexagonal Comb ─────────────────────────────────────────────

    private static void generateHexagonalComb(NestGeneratorPane.GeneratedNest nest, int chambers,
            int queenCnt, int broodCnt, int foodCnt, double scale, Random rnd) {
        double cellSize = 2.8 * scale;
        int cols = Math.max(3, (int) Math.sqrt(chambers * 1.5));
        int rows = Math.max(3, chambers / cols);

        NestGeneratorPane.NestNode topEntrance = node(nest, 0, 0, 0, "ENTRANCE", 2.5 * scale, Color.LIMEGREEN);
        NestGeneratorPane.NestNode[][] grid = new NestGeneratorPane.NestNode[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double x = (c - cols / 2.0) * cellSize * 1.1;
                double z = (r + 1) * cellSize * 1.2;
                double y = (c % 2 == 0) ? 0 : cellSize * 0.4;

                String type = "BROOD";
                Color color = Color.DEEPSKYBLUE;
                if (r == 0) { type = "FOOD"; color = Color.ORANGE; }
                else if (r == rows - 1 && c == cols / 2 && queenCnt > 0) { type = "QUEEN"; color = Color.GOLD; }

                NestGeneratorPane.NestNode cell = nodeLenticular(nest, x, y, z, type, cellSize, cellSize, cellSize * 0.8, color);
                grid[r][c] = cell;

                // Connect top row cells to top entrance
                if (r == 0) {
                    edge(nest, topEntrance, cell, rnd);
                }
                // Connect to left adjacent cell
                if (c > 0 && grid[r][c - 1] != null) {
                    edge(nest, grid[r][c - 1], cell, rnd);
                }
                // Connect to upper adjacent cell
                if (r > 0 && grid[r - 1][c] != null) {
                    edge(nest, grid[r - 1][c], cell, rnd);
                }
                // Connect diagonal neighbor for tight hexagonal comb lattice
                if (r > 0 && c > 0 && grid[r - 1][c - 1] != null) {
                    edge(nest, grid[r - 1][c - 1], cell, rnd);
                }
            }
        }
    }

    // ── 3. Bumblebee Pot Cluster ────────────────────────────────────────────────

    private static void generatePotCluster(NestGeneratorPane.GeneratedNest nest, int count,
            int queenCnt, int broodCnt, int foodCnt, int wasteCnt, double scale, Random rnd) {
        NestGeneratorPane.NestNode centerBrood = node(nest, 0, 0, 4 * scale, "BROOD", 4.5 * scale, Color.DEEPSKYBLUE);

        if (queenCnt > 0) {
            NestGeneratorPane.NestNode queenCell = node(nest, 0, 0, 1.5 * scale, "QUEEN", 5.5 * scale, Color.GOLD);
            edge(nest, centerBrood, queenCell, rnd);
        }

        NestGeneratorPane.NestNode prevPot = centerBrood;
        for (int i = 0; i < count; i++) {
            double ang = i * (Math.PI * 2 / Math.max(1, count)) + rnd.nextDouble() * 0.3;
            double dist = (5.0 + (i % 3) * 2.0) * scale;
            double z = (3.0 + rnd.nextDouble() * 3.0) * scale;
            String type = (i % 2 == 0) ? "FOOD" : "BROOD";
            Color col = (i % 2 == 0) ? Color.ORANGE : Color.DEEPSKYBLUE;
            if (i == count - 1 && wasteCnt > 0) { type = "WASTE"; col = Color.INDIANRED; }

            NestGeneratorPane.NestNode pot = node(nest, dist * Math.cos(ang), dist * Math.sin(ang), z, type, 3.2 * scale, col);
            edge(nest, centerBrood, pot, rnd);
            if (i > 0) {
                edge(nest, prevPot, pot, rnd); // Interconnect pot cluster
            }
            prevPot = pot;
        }
    }

    // ── 4. Paper Wasp Hanging Nest ──────────────────────────────────────────────

    private static void generatePaperNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int count, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, double scale, Random rnd) {
        // Support Peduncle Anchor attached to aerial ceiling/branch (z = -6 to 0)
        NestGeneratorPane.NestNode supportBranch = node(nest, -8 * scale, 0, -5 * scale, "JUNCTION", 1.8 * scale, Color.SIENNA);
        NestGeneratorPane.NestNode peduncle = node(nest, 0, 0, 0, "ENTRANCE", 1.8 * scale, Color.LIMEGREEN);
        edge(nest, supportBranch, peduncle, rnd);

        int tiers = Math.max(2, (int)(maxDepth / 5.0));
        NestGeneratorPane.NestNode prevTier = peduncle;

        for (int t = 1; t <= tiers; t++) {
            double z = t * 4.5 * scale;
            int cellsInTier = Math.max(2, count / tiers);
            NestGeneratorPane.NestNode tierCenter = node(nest, 0, 0, z, "JUNCTION", 1.5 * scale, Color.DARKGRAY);
            edge(nest, prevTier, tierCenter, rnd);
            prevTier = tierCenter;

            List<NestGeneratorPane.NestNode> tierCells = new ArrayList<>();
            for (int i = 0; i < cellsInTier; i++) {
                double ang = i * (Math.PI * 2 / cellsInTier);
                double rad = (3.0 + (i % 2) * 2.5) * scale;
                String type = "BROOD"; Color col = Color.DEEPSKYBLUE;
                if (t == 1 && i == 0 && queenCnt > 0) { type = "QUEEN"; col = Color.GOLD; }
                else if (i % 3 == 0) { type = "FOOD"; col = Color.ORANGE; }

                NestGeneratorPane.NestNode cell = node(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, type, 2.2 * scale, col);
                edge(nest, tierCenter, cell, rnd);
                tierCells.add(cell);
                if (i > 0) {
                    edge(nest, tierCells.get(i - 1), cell, rnd);
                }
            }
        }
    }

    // ── 5. Termite Cathedral Mound ──────────────────────────────────────────────

    private static void generateCathedralMound(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int count, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, int branching, double scale, Random rnd) {

        // Entrance at base of mound
        NestGeneratorPane.NestNode baseEnt = node(nest, 0, 0, 0, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN);

        // Tower spire above ground (z <= 0) continuously connected
        NestGeneratorPane.NestNode prevTowerNode = baseEnt;
        for (int h = -3; h >= -18; h -= 3) {
            double r = (18 + h) * 0.35 * scale;
            NestGeneratorPane.NestNode tn = node(nest, (rnd.nextDouble() - 0.5) * 1.5, (rnd.nextDouble() - 0.5) * 1.5, h, "JUNCTION", Math.max(1.0, r), Color.CHOCOLATE);
            edge(nest, prevTowerNode, tn, rnd);
            prevTowerNode = tn;
        }

        // Subterranean royal cell at center depth
        NestGeneratorPane.NestNode royalCell = nodeLenticular(nest, 0, 0, maxDepth * 0.65, "QUEEN", 6.0 * scale, 6.0 * scale, 2.5 * scale, Color.GOLD);
        edge(nest, baseEnt, royalCell, rnd);

        // Interconnected subterranean nursery, fungus, and storage chambers
        List<NestGeneratorPane.NestNode> chambersList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double ang = i * (Math.PI * 2 / Math.max(1, count)) + (rnd.nextDouble() - 0.5) * 0.3;
            double dist = (5.0 + (i % 3) * 3.5) * scale;
            double z = 3.0 + (i / (double) Math.max(1, count)) * (maxDepth * 0.75);
            String type = (i % 4 == 0) ? "FUNGUS" : (i % 3 == 0) ? "FOOD" : (i % 5 == 0) ? "WASTE" : "BROOD";
            Color col = (type.equals("FUNGUS")) ? Color.MEDIUMPURPLE : (type.equals("FOOD")) ? Color.ORANGE : (type.equals("WASTE")) ? Color.INDIANRED : Color.DEEPSKYBLUE;

            NestGeneratorPane.NestNode ch = node(nest, dist * Math.cos(ang), dist * Math.sin(ang), z, type, 3.0 * scale, col);
            edge(nest, royalCell, ch, rnd);
            chambersList.add(ch);

            // Connect to previous adjacent chamber to form ring/mesh ventilation network
            if (i > 0) {
                edge(nest, chambersList.get(i - 1), ch, rnd);
            }
        }
    }

    // ── 6. Arboreal Weaver Ant Leaf Nest ───────────────────────────────────────

    private static void generateArborealLeafNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int count, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, double scale, Random rnd) {

        // Host Tree Canopy Support Branch (z = -8 to 0)
        NestGeneratorPane.NestNode mainCanopyBranch = node(nest, -10 * scale, 0, -8 * scale, "JUNCTION", 2.0 * scale, Color.FORESTGREEN);
        NestGeneratorPane.NestNode branchAnchor = node(nest, 0, 0, 0, "ENTRANCE", 2.0 * scale, Color.LIMEGREEN);
        edge(nest, mainCanopyBranch, branchAnchor, rnd);

        List<NestGeneratorPane.NestNode> leafNodes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double ang = i * (Math.PI * 2 / Math.max(1, count)) + rnd.nextDouble() * 0.5;
            double rad = (4.0 + rnd.nextDouble() * 6.0) * scale;
            double z = (-4.0 + rnd.nextDouble() * 10.0) * scale;

            String type = "BROOD"; Color col = Color.DEEPSKYBLUE;
            if (i == 0 && queenCnt > 0) { type = "QUEEN"; col = Color.GOLD; }
            else if (i % 3 == 0) { type = "FOOD"; col = Color.ORANGE; }
            else if (i % 5 == 0 && wasteCnt > 0) { type = "WASTE"; col = Color.INDIANRED; }

            NestGeneratorPane.NestNode leafCh = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, type, 3.8 * scale, 3.8 * scale, 2.0 * scale, col);
            edge(nest, branchAnchor, leafCh, rnd);
            leafNodes.add(leafCh);
            if (i > 0) {
                edge(nest, leafNodes.get(i - 1), leafCh, rnd);
            }
        }
    }

    // ── 7. Leafcutter Subterranean Fungi Vault (Atta) ───────────────────────────

    private static void generateFungiVault(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int count, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, int branching, double scale, Random rnd) {

        // Multiple excavated surface craters
        List<NestGeneratorPane.NestNode> craters = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            double ang = i * (Math.PI * 2 / 3.0) + rnd.nextDouble() * 0.4;
            craters.add(node(nest, (5.0 + i * 2.0) * Math.cos(ang) * scale, (5.0 + i * 2.0) * Math.sin(ang) * scale, 0.0, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN));
        }

        NestGeneratorPane.NestNode centralHub = node(nest, 0, 0, 3.0 * scale, "JUNCTION", 1.5 * scale, Color.SLATEGRAY);
        for (NestGeneratorPane.NestNode c : craters) edge(nest, c, centralHub, rnd);

        // Deep central trunk line
        NestGeneratorPane.NestNode mainShaft = node(nest, 0, 0, maxDepth * 0.4, "JUNCTION", 1.8 * scale, Color.SLATEGRAY);
        edge(nest, centralHub, mainShaft, rnd);

        // Subterranean Fungal Vaults (huge lenticular caverns for Atta gardens)
        int actualFungus = Math.max(1, fungusCnt > 0 ? fungusCnt : count / 2);
        List<NestGeneratorPane.NestNode> gardenNodes = new ArrayList<>();
        for (int i = 0; i < actualFungus; i++) {
            double ang = i * (Math.PI * 2 / Math.max(1, actualFungus));
            double dist = (8.0 + (i % 2) * 5.0) * scale;
            double z = maxDepth * 0.35 + (i * 3.5 * scale);

            NestGeneratorPane.NestNode vault = nodeLenticular(nest, dist * Math.cos(ang), dist * Math.sin(ang), z,
                    "FUNGUS", 5.5 * scale, 5.5 * scale, 3.0 * scale, Color.MEDIUMPURPLE);
            edge(nest, mainShaft, vault, rnd);
            gardenNodes.add(vault);
        }

        // Royal Chamber attached to central vault
        if (queenCnt > 0 && !gardenNodes.isEmpty()) {
            NestGeneratorPane.NestNode royalVault = nodeLenticular(nest, 0, 0, maxDepth * 0.5, "QUEEN", 5.0 * scale, 5.0 * scale, 2.8 * scale, Color.GOLD);
            edge(nest, gardenNodes.get(0), royalVault, rnd);
        }

        // Deep Waste Pits situated strictly BELOW fungus gardens (Atta refuse isolation)
        int actualWaste = Math.max(1, wasteCnt);
        NestGeneratorPane.NestNode wasteShaft = node(nest, 0, 0, maxDepth * 0.8, "JUNCTION", 1.5 * scale, Color.DARKRED);
        edge(nest, mainShaft, wasteShaft, rnd);
        for (int i = 0; i < actualWaste; i++) {
            double ang = i * (Math.PI * 2 / Math.max(1, actualWaste));
            double dist = (6.0 + i * 3.0) * scale;
            double z = maxDepth * 0.85 + (i * 2.0 * scale);

            NestGeneratorPane.NestNode wastePit = nodeLenticular(nest, dist * Math.cos(ang), dist * Math.sin(ang), z,
                    "WASTE", 4.0 * scale, 4.0 * scale, 2.5 * scale, Color.INDIANRED);
            edge(nest, wasteShaft, wastePit, rnd);
        }
    }

    // ── 8. Wood Pulp / Carton Nest (Crematogaster / Lasius fuliginosus) ────────

    private static void generateCartonNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int count, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, double scale, Random rnd) {

        // Host Tree Trunk Support Anchor (z = -10 to 0)
        NestGeneratorPane.NestNode trunkAnchor = node(nest, 0, 0, -10.0 * scale, "JUNCTION", 2.5 * scale, Color.SIENNA);
        NestGeneratorPane.NestNode nestCenter = node(nest, 0, 0, 0, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN);
        edge(nest, trunkAnchor, nestCenter, rnd);

        // Concentric spherical carton layers
        NestGeneratorPane.NestNode royalCarton = nodeLenticular(nest, 0, 0, 2.0 * scale, "QUEEN", 4.0 * scale, 4.0 * scale, 3.0 * scale, Color.GOLD);
        edge(nest, nestCenter, royalCarton, rnd);

        NestGeneratorPane.NestNode prevNode = royalCarton;
        for (int i = 0; i < count; i++) {
            double ang = i * (Math.PI * 2 / Math.max(1, count));
            double rad = (4.0 + (i % 3) * 3.0) * scale;
            double z = (-3.0 + (i % 4) * 2.5) * scale;

            String type = (i % 3 == 0) ? "FOOD" : "BROOD";
            Color col = (i % 3 == 0) ? Color.ORANGE : Color.DEEPSKYBLUE;
            if (i == count - 1 && wasteCnt > 0) { type = "WASTE"; col = Color.INDIANRED; }

            NestGeneratorPane.NestNode cell = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, type, 3.2 * scale, 3.2 * scale, 2.2 * scale, col);
            edge(nest, prevNode, cell, rnd);
            prevNode = cell;
        }
    }

    // ── 9. Bamboo / Plant Stem Gall Nest (Colobopsis / Temnothorax) ────────────

    private static void generateStemGallNest(NestGeneratorPane.GeneratedNest nest,
            int count, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, double scale, Random rnd) {

        // Narrow entrance plug at stem tip (phragmotic soldier head plug)
        NestGeneratorPane.NestNode entrancePlug = node(nest, -12.0 * scale, 0, 0, "ENTRANCE", 1.5 * scale, Color.LIMEGREEN);

        // Linear tubular stem chambers along X axis
        NestGeneratorPane.NestNode prevChamber = entrancePlug;
        for (int i = 0; i < count; i++) {
            double x = (-9.0 + i * 4.5) * scale;
            String type = (i == 0 && queenCnt > 0) ? "QUEEN" : (i % 2 == 0) ? "BROOD" : "FOOD";
            Color col = (type.equals("QUEEN")) ? Color.GOLD : (type.equals("FOOD")) ? Color.ORANGE : Color.DEEPSKYBLUE;
            if (i == count - 1 && wasteCnt > 0) { type = "WASTE"; col = Color.INDIANRED; }

            NestGeneratorPane.NestNode stemCell = nodeLenticular(nest, x, 0, 0, type, 4.0 * scale, 1.4 * scale, 1.2 * scale, col);
            edge(nest, prevChamber, stemCell, rnd);
            prevChamber = stemCell;
        }
    }

    // ── 10. Living Worker Bivouac Nest (Eciton Army Ants) ──────────────────────

    private static void generateBivouacNest(NestGeneratorPane.GeneratedNest nest,
            int count, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, double scale, Random rnd) {

        // Canopy / Overhang Bivouac Anchor
        NestGeneratorPane.NestNode logAnchor = node(nest, 0, 0, -8.0 * scale, "JUNCTION", 2.2 * scale, Color.SIENNA);
        NestGeneratorPane.NestNode bivouacHead = node(nest, 0, 0, -4.0 * scale, "ENTRANCE", 2.5 * scale, Color.LIMEGREEN);
        edge(nest, logAnchor, bivouacHead, rnd);

        // Central protected queen & brood core
        NestGeneratorPane.NestNode protectedCore = node(nest, 0, 0, 0, "QUEEN", 5.0 * scale, Color.GOLD);
        edge(nest, bivouacHead, protectedCore, rnd);

        // Catenary hanging cluster of worker body nodes
        NestGeneratorPane.NestNode prevBody = protectedCore;
        for (int i = 0; i < count; i++) {
            double ang = i * (Math.PI * 2 / Math.max(1, count));
            double rad = (3.5 + (i % 2) * 2.5) * scale;
            double z = (-2.0 + (i / 2.0) * 2.0) * scale;

            String type = (i % 2 == 0) ? "BROOD" : "FOOD";
            Color col = (i % 2 == 0) ? Color.DEEPSKYBLUE : Color.ORANGE;

            NestGeneratorPane.NestNode bodyCluster = node(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, type, 3.5 * scale, col);
            edge(nest, protectedCore, bodyCluster, rnd);
            if (i > 0) {
                edge(nest, prevBody, bodyCluster, rnd);
            }
            prevBody = bodyCluster;
        }
    }

    // ── 11. Hollow Tree Trunk Cavity Nest ─────────────────────────────────────

    private static void generateHollowTrunkNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int count, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, double scale, Random rnd) {
        // Hollow trunk knot entrance hole
        NestGeneratorPane.NestNode knotHole = node(nest, 0, -5.5 * scale, -4.0 * scale, "ENTRANCE", 2.2 * scale, Color.LIMEGREEN);

        // Central vertical cavity shaft
        NestGeneratorPane.NestNode cavityCenter = node(nest, 0, 0, 0, "JUNCTION", 2.0 * scale, Color.SIENNA);
        edge(nest, knotHole, cavityCenter, rnd);

        int levels = Math.max(3, count / 3);
        NestGeneratorPane.NestNode prevLevel = cavityCenter;
        for (int l = 0; l < levels; l++) {
            double z = (-10.0 + l * (20.0 / Math.max(1, levels - 1))) * scale;
            double ang = l * 1.2 + rnd.nextDouble() * 0.4;
            double rad = (2.0 + (l % 2) * 2.5) * scale;

            String type = (l == levels / 2 && queenCnt > 0) ? "QUEEN"
                        : (z < -3.0 * scale) ? "FOOD"
                        : (z > 7.0 * scale && wasteCnt > 0) ? "WASTE" : "BROOD";
            Color col = type.equals("QUEEN") ? Color.GOLD : type.equals("FOOD") ? Color.ORANGE : type.equals("WASTE") ? Color.INDIANRED : Color.DEEPSKYBLUE;

            NestGeneratorPane.NestNode cell = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, type, 3.5 * scale, 3.5 * scale, 2.2 * scale, col);
            edge(nest, cavityCenter, cell, rnd);
            if (l > 0) {
                edge(nest, prevLevel, cell, rnd);
            }
            prevLevel = cell;
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static void fill(List<String[]> q, String type, int count,
            double baseRatio, double spread, double rx, double rz, Color color) {
        for (int i = 0; i < count; i++)
            q.add(new String[]{type, String.valueOf(baseRatio + Math.random()*spread),
                String.valueOf(rx), String.valueOf(rz), color.toString()});
    }

    private static Color parseColor(String s) {
        try { return Color.web(s); } catch (Exception e) { return Color.SANDYBROWN; }
    }

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
        double dx = (to.x-from.x)/seg, dy = (to.y-from.y)/seg, dz = (to.z-from.z)/seg;
        double px = -dy, py = dx, len = Math.hypot(px, py);
        if (len > 0.001) { px/=len; py/=len; }
        for (int i = 1; i < seg; i++) {
            double f = Math.sin((i/(double)seg)*Math.PI);
            double off = (rnd.nextDouble()-0.5)*2.5*f;
            pts.add(new double[]{from.x+dx*i+px*off, from.y+dy*i+py*off,
                from.z+dz*i+(rnd.nextDouble()-0.5)*f});
        }
        pts.add(new double[]{to.x, to.y, to.z});
        nest.edges.add(new NestGeneratorPane.NestEdge(from, to, pts));
    }
}

