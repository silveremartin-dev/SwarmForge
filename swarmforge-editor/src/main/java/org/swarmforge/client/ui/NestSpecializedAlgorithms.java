/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Specialized physical architecture generation algorithms for diverse eusocial insect societies:
 * - Honeybees (Hexagonal wax combs & wooden beehives)
 * - Bumblebees (Wax & propolis pot clusters)
 * - Paper Wasps (Pedunculate hanging cells)
 * - Termites (Cathedral ventilation chimney mounds)
 * - Weaver Ants (Arboreal silk-stitched leaf chambers)
 * - Leafcutter Ants (Subterranean multi-tier fungi vaults)
 * - Carton Ants (Wood-pulp chewed carton galleries)
 * - Stem Ants (Hollow bamboo & gall internodes)
 * - Army Ants (Living worker bivouac clusters)
 * - Carpenter Ants (Hollow tree trunk heartwood galleries)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class NestSpecializedAlgorithms {

    private NestSpecializedAlgorithms() {}

    public static class ChamberSpec {
        public String type;
        public double targetDepthRel;
        public double rx, rz;
        public Color color;

        public ChamberSpec(String type, double targetDepthRel, double rx, double rz, Color color) {
            this.type = type;
            this.targetDepthRel = targetDepthRel;
            this.rx = rx;
            this.rz = rz;
            this.color = color;
        }
    }

    public static List<ChamberSpec> buildChamberQueue(int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        List<ChamberSpec> q = new ArrayList<>();
        for (int i = 0; i < queenCnt; i++)  q.add(new ChamberSpec("QUEEN",  0.40, 0.12 * scale, 0.05 * scale, NestRenderer.CHAMBER_COLOR_QUEEN));
        for (int i = 0; i < broodCnt; i++)  q.add(new ChamberSpec("BROOD",  0.30, 0.08 * scale, 0.04 * scale, NestRenderer.CHAMBER_COLOR_BROOD));
        for (int i = 0; i < foodCnt; i++)   q.add(new ChamberSpec("FOOD",   0.20, 0.08 * scale, 0.04 * scale, NestRenderer.CHAMBER_COLOR_STORAGE));
        for (int i = 0; i < fungusCnt; i++) q.add(new ChamberSpec("FUNGUS", 0.50, 0.12 * scale, 0.06 * scale, NestRenderer.CHAMBER_COLOR_FUNGUS));
        for (int i = 0; i < wasteCnt; i++)  q.add(new ChamberSpec("WASTE",  0.65, 0.06 * scale, 0.03 * scale, NestRenderer.CHAMBER_COLOR_WASTE));
        return q;
    }

    public static NestGeneratorPane.NestNode node(NestGeneratorPane.GeneratedNest nest,
                                                 double x, double y, double z, String type, double r, Color c) {
        NestGeneratorPane.NestNode n = new NestGeneratorPane.NestNode(x, y, z, type, r, c);
        nest.nodes.add(n);
        return n;
    }

    public static NestGeneratorPane.NestNode nodeLenticular(NestGeneratorPane.GeneratedNest nest,
                                                           double x, double y, double z, String type, double rx, double ry, double rz, Color c) {
        NestGeneratorPane.NestNode n = new NestGeneratorPane.NestNode(x, y, z, type, rx, ry, rz, c);
        nest.nodes.add(n);
        return n;
    }

    public static void edge(NestGeneratorPane.GeneratedNest nest,
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

    public static void generateWoodenBeehive(NestGeneratorPane.GeneratedNest nest, double maxDepth,
                                             int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        double boxW = 1.6 * scale;
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double x = (i - (entrances - 1) / 2.0) * 0.3 * scale;
            entNodes.add(node(nest, x, -0.6 * scale, 0, "ENTRANCE", 0.045 * scale, Color.LIMEGREEN));
        }

        int frames = 4;
        List<NestGeneratorPane.NestNode> frameTops = new ArrayList<>();
        for (int f = 0; f < frames; f++) {
            double yOff = (f - 1.5) * 0.22 * scale;
            NestGeneratorPane.NestNode frameTop = node(nest, 0, yOff, 0.15 * scale, "JUNCTION", 0.04 * scale, Color.SIENNA);
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
            double z = 0.25 * scale + (idx / (frames * 4.0)) * 0.35 * scale;
            double yOff = fTop.y;

            NestGeneratorPane.NestNode cell = nodeLenticular(nest, x, yOff, z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, fTop, cell, rnd);
        }
    }

    public static void generateHexagonalComb(NestGeneratorPane.GeneratedNest nest, int entrances,
                                             int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double x = (i - (entrances - 1) / 2.0) * 0.25 * scale;
            entNodes.add(node(nest, x, 0, 0, "ENTRANCE", 0.045 * scale, Color.LIMEGREEN));
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        if (total == 0) return;

        double cellSize = 0.16 * scale;
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

    public static void generatePotCluster(NestGeneratorPane.GeneratedNest nest, int entrances,
                                          int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances);
            entNodes.add(node(nest, 0.35 * scale * Math.cos(ang), 0.35 * scale * Math.sin(ang), 0, "ENTRANCE", 0.045 * scale, Color.LIMEGREEN));
        }

        NestGeneratorPane.NestNode centerHub = node(nest, 0, 0, 0.15 * scale, "JUNCTION", 0.04 * scale, Color.SIENNA);
        for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, centerHub, rnd);

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevPot = centerHub;

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total)) + rnd.nextDouble() * 0.3;
            double dist = (0.35 + (i % 3) * 0.15) * scale;
            double z = (0.15 + rnd.nextDouble() * 0.25) * scale;

            NestGeneratorPane.NestNode pot = nodeLenticular(nest, dist * Math.cos(ang), dist * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, centerHub, pot, rnd);
            if (i > 0) edge(nest, prevPot, pot, rnd);
            prevPot = pot;
        }
    }

    public static void generatePaperNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
                                         int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        NestGeneratorPane.NestNode peduncle = node(nest, 0, 0, -0.60 * scale, "JUNCTION", 0.035 * scale, Color.LIGHTSLATEGRAY);
        NestGeneratorPane.NestNode umbrellaCenter = node(nest, 0, 0, 0, "JUNCTION", 0.04 * scale, Color.LIGHTSLATEGRAY);
        edge(nest, peduncle, umbrellaCenter, rnd);

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances);
            NestGeneratorPane.NestNode en = node(nest, 0.40 * scale * Math.cos(ang), 0.40 * scale * Math.sin(ang), 0.15 * scale, "ENTRANCE", 0.045 * scale, Color.LIMEGREEN);
            edge(nest, umbrellaCenter, en, rnd);
            entNodes.add(en);
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevNode = umbrellaCenter;

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total));
            double rad = (0.20 + (i % 3) * 0.18) * scale;
            double z = (0.10 + (i / 3) * 0.12) * scale;

            NestGeneratorPane.NestNode cell = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, umbrellaCenter, cell, rnd);
            if (i > 0) edge(nest, prevNode, cell, rnd);
            prevNode = cell;
        }
    }

    public static void generateCathedralMound(NestGeneratorPane.GeneratedNest nest, double maxDepth,
                                              int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, int branching, double scale, Random rnd) {
        NestGeneratorPane.NestNode spireApex = node(nest, 0, 0, -1.20 * scale, "ENTRANCE", 0.05 * scale, Color.LIMEGREEN);
        NestGeneratorPane.NestNode coreSpire = node(nest, 0, 0, -0.60 * scale, "JUNCTION", 0.045 * scale, Color.SADDLEBROWN);
        edge(nest, spireApex, coreSpire, rnd);

        NestGeneratorPane.NestNode basalHub = node(nest, 0, 0, 0.10 * scale, "JUNCTION", 0.045 * scale, Color.SLATEGRAY);
        edge(nest, coreSpire, basalHub, rnd);

        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances);
            NestGeneratorPane.NestNode en = node(nest, 0.50 * scale * Math.cos(ang), 0.50 * scale * Math.sin(ang), 0.0, "ENTRANCE", 0.045 * scale, Color.LIMEGREEN);
            edge(nest, en, basalHub, rnd);
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevCell = basalHub;

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total));
            double rad = (0.35 + (i % 3) * 0.25) * scale;
            double z = (-0.40 + (i / (double) Math.max(1, total)) * (maxDepth * 0.8)) * scale;

            NestGeneratorPane.NestNode vault = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, basalHub, vault, rnd);
            if (i > 0) edge(nest, prevCell, vault, rnd);
            prevCell = vault;
        }
    }

    public static void generateArborealLeafNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
                                                int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        NestGeneratorPane.NestNode branchAnchor = node(nest, 0, 0, -0.75 * scale, "JUNCTION", 0.045 * scale, Color.SIENNA);

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances);
            NestGeneratorPane.NestNode leafEn = node(nest, 0.35 * scale * Math.cos(ang), 0.35 * scale * Math.sin(ang), 0, "ENTRANCE", 0.045 * scale, Color.LIMEGREEN);
            edge(nest, branchAnchor, leafEn, rnd);
            entNodes.add(leafEn);
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevLeaf = entNodes.get(0);

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total)) + rnd.nextDouble() * 0.5;
            double rad = (0.35 + rnd.nextDouble() * 0.45) * scale;
            double z = (-0.35 + rnd.nextDouble() * 0.75) * scale;

            NestGeneratorPane.NestNode leafCh = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, entNodes.get(0), leafCh, rnd);
            if (i > 0) edge(nest, prevLeaf, leafCh, rnd);
            prevLeaf = leafCh;
        }
    }

    public static void generateFungiVault(NestGeneratorPane.GeneratedNest nest, double maxDepth,
                                          int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, int branching, double scale, Random rnd) {
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances) + rnd.nextDouble() * 0.4;
            entNodes.add(node(nest, (0.45 + i * 0.18) * Math.cos(ang) * scale, (0.45 + i * 0.18) * Math.sin(ang) * scale, 0.0, "ENTRANCE", 0.05 * scale, Color.LIMEGREEN));
        }

        NestGeneratorPane.NestNode centralHub = node(nest, 0, 0, 0.25 * scale, "JUNCTION", 0.04 * scale, Color.SLATEGRAY);
        for (NestGeneratorPane.NestNode c : entNodes) edge(nest, c, centralHub, rnd);

        NestGeneratorPane.NestNode mainShaft = node(nest, 0, 0, maxDepth * 0.4, "JUNCTION", 0.04 * scale, Color.SLATEGRAY);
        edge(nest, centralHub, mainShaft, rnd);

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevV = mainShaft;

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total));
            double dist = (0.50 + (i % 3) * 0.35) * scale;
            double z = maxDepth * 0.30 + (i / (double) Math.max(1, total)) * (maxDepth * 0.55);

            NestGeneratorPane.NestNode vault = nodeLenticular(nest, dist * Math.cos(ang), dist * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, mainShaft, vault, rnd);
            if (i > 0) edge(nest, prevV, vault, rnd);
            prevV = vault;
        }
    }

    public static void generateCartonNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
                                          int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        NestGeneratorPane.NestNode trunkAnchor = node(nest, 0, 0, -0.80 * scale, "JUNCTION", 0.045 * scale, Color.SIENNA);

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances);
            NestGeneratorPane.NestNode nestCenter = node(nest, 0.25 * scale * Math.cos(ang), 0.25 * scale * Math.sin(ang), 0, "ENTRANCE", 0.045 * scale, Color.LIMEGREEN);
            edge(nest, trunkAnchor, nestCenter, rnd);
            entNodes.add(nestCenter);
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevNode = entNodes.get(0);

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total));
            double rad = (0.30 + (i % 3) * 0.22) * scale;
            double z = (-0.25 + (i % 4) * 0.20) * scale;

            NestGeneratorPane.NestNode cell = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, entNodes.get(0), cell, rnd);
            if (i > 0) edge(nest, prevNode, cell, rnd);
            prevNode = cell;
        }
    }

    public static void generateStemGallNest(NestGeneratorPane.GeneratedNest nest,
                                            int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double x = (-0.80 + i * 0.20) * scale;
            entNodes.add(node(nest, x, 0, 0, "ENTRANCE", 0.035 * scale, Color.LIMEGREEN));
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevChamber = entNodes.get(0);

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double x = (-0.60 + i * 0.35) * scale;
            NestGeneratorPane.NestNode stemCell = nodeLenticular(nest, x, 0, 0, cs.type, cs.rx, 0.12 * scale, 0.10 * scale, cs.color);
            edge(nest, prevChamber, stemCell, rnd);
            prevChamber = stemCell;
        }
    }

    public static void generateBivouacNest(NestGeneratorPane.GeneratedNest nest,
                                           int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        NestGeneratorPane.NestNode logAnchor = node(nest, 0, 0, -0.65 * scale, "JUNCTION", 0.045 * scale, Color.SIENNA);

        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = i * (Math.PI * 2 / entrances);
            NestGeneratorPane.NestNode bivouacHead = node(nest, 0.20 * scale * Math.cos(ang), 0.20 * scale * Math.sin(ang), -0.30 * scale, "ENTRANCE", 0.045 * scale, Color.LIMEGREEN);
            edge(nest, logAnchor, bivouacHead, rnd);
            entNodes.add(bivouacHead);
        }

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevBody = entNodes.get(0);

        for (int i = 0; i < total; i++) {
            ChamberSpec cs = queue.get(i);
            double ang = i * (Math.PI * 2 / Math.max(1, total));
            double rad = (0.28 + (i % 2) * 0.20) * scale;
            double z = (-0.15 + (i / 2.0) * 0.15) * scale;

            NestGeneratorPane.NestNode bodyCluster = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, entNodes.get(0), bodyCluster, rnd);
            if (i > 0) edge(nest, prevBody, bodyCluster, rnd);
            prevBody = bodyCluster;
        }
    }

    public static void generateHollowTrunkNest(NestGeneratorPane.GeneratedNest nest, double maxDepth,
                                               int entrances, int queenCnt, int broodCnt, int foodCnt, int wasteCnt, int fungusCnt, double scale, Random rnd) {
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double y = (-0.45 + i * 0.15) * scale;
            entNodes.add(node(nest, 0, y, -0.30 * scale, "ENTRANCE", 0.045 * scale, Color.LIMEGREEN));
        }

        NestGeneratorPane.NestNode cavityCenter = node(nest, 0, 0, 0, "JUNCTION", 0.04 * scale, Color.SIENNA);
        for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, cavityCenter, rnd);

        List<ChamberSpec> queue = buildChamberQueue(queenCnt, broodCnt, foodCnt, wasteCnt, fungusCnt, scale, rnd);
        int total = queue.size();
        NestGeneratorPane.NestNode prevLevel = cavityCenter;

        for (int l = 0; l < total; l++) {
            ChamberSpec cs = queue.get(l);
            double z = (-0.80 + l * (1.60 / Math.max(1, total - 1))) * scale;
            double ang = l * 1.2 + rnd.nextDouble() * 0.4;
            double rad = (0.16 + (l % 2) * 0.20) * scale;

            NestGeneratorPane.NestNode cell = nodeLenticular(nest, rad * Math.cos(ang), rad * Math.sin(ang), z, cs.type, cs.rx, cs.rx, cs.rz, cs.color);
            edge(nest, cavityCenter, cell, rnd);
            if (l > 0) edge(nest, prevLevel, cell, rnd);
            prevLevel = cell;
        }
    }
}
