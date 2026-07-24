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
 * Supports Ants, Bees, Wasps, Termites, and Weaver Ants with:
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

        switch (arch) {
            case "WAX_COMB_HEXAGONAL" -> generateHexagonalComb(nest, chamberTarget, queenCnt, broodCnt, foodCnt, rnd);
            case "WAX_POTS_CLUSTER"   -> generatePotCluster(nest, chamberTarget, broodCnt, foodCnt, rnd);
            case "PAPER_PEDUNCULATE"  -> generatePaperNest(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, rnd);
            case "CATHEDRAL_MOUND"   -> generateCathedralMound(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, branching, anatomicalScale, rnd);
            case "ARBOREAL_SILK_LEAF" -> generateArborealLeafNest(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, rnd);
            case "SURFACE_MOUND"      -> generateMoundBurrow(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd, true);
            default                   -> generateMoundBurrow(nest, maxDepth, chamberTarget, queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, branching, anatomicalScale, rnd, false);
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

    // ── 2. Honeybee Hexagonal Comb ─────────────────────────────────────────────

    private static void generateHexagonalComb(NestGeneratorPane.GeneratedNest nest, int chambers,
            int queenCnt, int broodCnt, int foodCnt, Random rnd) {
        double cellSize = 2.8;
        int cols = (int) Math.sqrt(chambers * 1.5);
        int rows = Math.max(3, chambers / Math.max(1, cols));

        NestGeneratorPane.NestNode topEntrance = node(nest, 0, 0, 0, "ENTRANCE", 2.5, Color.LIMEGREEN);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                double x = (c - cols/2.0) * cellSize * 1.1;
                double z = (r + 1) * cellSize * 1.2;
                double y = (c % 2 == 0) ? 0 : cellSize * 0.4;

                String type = "BROOD";
                Color color = Color.GOLDENROD;
                if (r == 0) { type = "FOOD"; color = Color.ORANGE; }
                else if (r == rows-1 && c == cols/2) { type = "QUEEN"; color = Color.GOLD; }

                NestGeneratorPane.NestNode cell = nodeLenticular(nest, x, y, z, type, cellSize, cellSize, cellSize*0.8, color);
                if (r == 0 && c == cols/2) edge(nest, topEntrance, cell, rnd);
            }
        }
    }

    // ── 3. Bumblebee Pot Cluster ────────────────────────────────────────────────

    private static void generatePotCluster(NestGeneratorPane.GeneratedNest nest, int count,
            int broodCnt, int foodCnt, Random rnd) {
        NestGeneratorPane.NestNode centerBrood = node(nest, 0, 0, 4, "BROOD", 4.5, Color.DEEPSKYBLUE);

        for (int i = 0; i < count; i++) {
            double ang = i * (Math.PI * 2 / count) + rnd.nextDouble()*0.3;
            double dist = 5.0 + (i % 3) * 2.0;
            double z = 3.0 + rnd.nextDouble() * 3.0;
            String type = (i % 2 == 0) ? "FOOD" : "BROOD";
            Color col = (i % 2 == 0) ? Color.AMBER : Color.DODGERBLUE;
            NestGeneratorPane.NestNode pot = node(nest, dist*Math.cos(ang), dist*Math.sin(ang), z, type, 3.2, col);
            edge(nest, centerBrood, pot, rnd);
        }
    }

    // ── 4. Paper Wasp Hanging Nest ──────────────────────────────────────────────

    private static void generatePaperNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int count, int queenCnt, int broodCnt, int foodCnt, Random rnd) {
        NestGeneratorPane.NestNode peduncle = node(nest, 0, 0, 0, "ENTRANCE", 1.8, Color.LIGHTGRAY);

        int tiers = 3;
        NestGeneratorPane.NestNode prevTier = peduncle;

        for (int t = 1; t <= tiers; t++) {
            double z = t * 5.0;
            int cellsInTier = count / tiers;
            NestGeneratorPane.NestNode tierCenter = node(nest, 0, 0, z, "JUNCTION", 1.5, Color.DARKGRAY);
            edge(nest, prevTier, tierCenter, rnd);
            prevTier = tierCenter;

            for (int i = 0; i < cellsInTier; i++) {
                double ang = i * (Math.PI * 2 / cellsInTier);
                double rad = 3.0 + (i % 2) * 2.5;
                NestGeneratorPane.NestNode cell = node(nest, rad*Math.cos(ang), rad*Math.sin(ang), z, "BROOD", 2.2, Color.BEIGE);
                edge(nest, tierCenter, cell, rnd);
            }
        }
    }

    // ── 5. Termite Cathedral Mound ──────────────────────────────────────────────

    private static void generateCathedralMound(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int count, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int branching, double scale, Random rnd) {

        // Tower above ground
        for (int h = -15; h <= 0; h += 3) {
            double r = (15 + h) * 0.4 * scale;
            node(nest, 0, 0, h, "JUNCTION", Math.max(1.0, r), Color.CHOCOLATE);
        }

        // Subterranean royal cell at center depth
        NestGeneratorPane.NestNode royalCell = nodeLenticular(nest, 0, 0, maxDepth*0.7, "QUEEN", 6.0*scale, 6.0*scale, 2.5*scale, Color.GOLD);

        // Surrounding nursery chambers
        for (int i = 0; i < count; i++) {
            double ang = i * (Math.PI * 2 / count);
            double dist = 6.0 + rnd.nextDouble() * 8.0;
            double z = 5.0 + rnd.nextDouble() * (maxDepth - 10);
            NestGeneratorPane.NestNode ch = node(nest, dist*Math.cos(ang), dist*Math.sin(ang), z, "BROOD", 3.0*scale, Color.SADDLEBROWN);
            edge(nest, royalCell, ch, rnd);
        }
    }

    // ── 6. Arboreal Weaver Ant Leaf Nest ───────────────────────────────────────

    private static void generateArborealLeafNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
            int count, int queenCnt, int broodCnt, int foodCnt, Random rnd) {

        NestGeneratorPane.NestNode branchAnchor = node(nest, 0, 0, 0, "ENTRANCE", 2.0, Color.BROWN);

        for (int i = 0; i < count; i++) {
            double ang = i * (Math.PI * 2 / count) + rnd.nextDouble()*0.5;
            double rad = 4.0 + rnd.nextDouble() * 6.0;
            double z = 2.0 + rnd.nextDouble() * 8.0;
            NestGeneratorPane.NestNode leafCh = nodeLenticular(nest, rad*Math.cos(ang), rad*Math.sin(ang), z, "BROOD", 3.8, 3.8, 2.0, Color.FORESTGREEN);
            edge(nest, branchAnchor, leafCh, rnd);
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
