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
 * Biologically-inspired ant nest generation algorithm.
 * Produces a 3-D graph of tunnels and chambers modelling actual ant excavation strategies:
 * - Helical main shaft with organic curvature
 * - Depth-zoned chambers (Queen deepest, Food near surface, etc.)
 * - Cross-connecting tunnels for realistic traffic loops
 */
public final class NestAlgorithm {

    private NestAlgorithm() {}

    public static NestGeneratorPane.GeneratedNest generate(NestGeneratorPane p) {
        NestGeneratorPane.GeneratedNest nest = new NestGeneratorPane.GeneratedNest();
        double maxDepth = p.getDepth();
        nest.maxDepth = maxDepth;

        int entrances = Math.max(1, p.sp("🚪 Entrances"));
        int queenCnt  = p.sp("👑 Queen Chamber");
        int broodCnt  = p.sp("🥚 Brood Chambers");
        int foodCnt   = p.sp("🍖 Food Storage");
        int wasteCnt  = p.sp("🗑 Waste Dumps");
        int fungusCnt = p.sp("🍄 Fungus Gardens");
        int branching = (int) p.getBranching();
        int extraJunctions = (int)(p.getChamberCount() / 8.0);

        Random rnd = new Random((long)(maxDepth*31 + entrances*7 + queenCnt*3 + broodCnt*17));

        // 1. Entrance nodes at Z=0
        List<NestGeneratorPane.NestNode> entNodes = new ArrayList<>();
        for (int i = 0; i < entrances; i++) {
            double ang = 2*Math.PI*i/entrances + rnd.nextDouble()*0.4;
            double d   = entrances>1 ? 4+rnd.nextDouble()*8 : 0;
            entNodes.add(node(nest, d*Math.cos(ang), d*Math.sin(ang), 0,
                "ENTRANCE", 2.0, Color.LIMEGREEN));
        }

        // 2. Main hub
        NestGeneratorPane.NestNode hub = node(nest, 0, 0, 1.5+rnd.nextDouble()*2, "JUNCTION", 1.1, Color.SLATEGRAY);
        for (NestGeneratorPane.NestNode en : entNodes) edge(nest, en, hub, rnd);

        // 3. Main helical shaft
        List<NestGeneratorPane.NestNode> shaft = new ArrayList<>();
        shaft.add(hub);
        int steps = 4 + (int)(maxDepth/5.5) + extraJunctions;
        NestGeneratorPane.NestNode prev = hub;
        for (int i = 1; i <= steps; i++) {
            double z   = (i/(double)steps)*maxDepth*0.95;
            double ang = i*0.75 + rnd.nextDouble()*0.5;
            double r   = 1.5 + rnd.nextDouble()*2.5;
            NestGeneratorPane.NestNode sn = node(nest, r*Math.cos(ang), r*Math.sin(ang), z, "JUNCTION", 1.1, Color.SLATEGRAY);
            edge(nest, prev, sn, rnd);
            shaft.add(sn);
            prev = sn;
        }

        // 4. Chamber queue
        List<String[]> queue = new ArrayList<>();
        fill(queue, "QUEEN",  queenCnt,  0.80, 0.15, 4.5, Color.GOLD);
        fill(queue, "BROOD",  broodCnt,  0.25, 0.40, 3.2, Color.DEEPSKYBLUE);
        fill(queue, "FOOD",   foodCnt,   0.15, 0.35, 3.5, Color.ORANGE);
        fill(queue, "FUNGUS", fungusCnt, 0.45, 0.30, 4.0, Color.MEDIUMPURPLE);
        fill(queue, "WASTE",  wasteCnt,  0.55, 0.30, 3.0, Color.INDIANRED);

        for (String[] q : queue) {
            double targetZ = Double.parseDouble(q[1]) * maxDepth;
            NestGeneratorPane.NestNode par = shaft.get(0);
            double best = Double.MAX_VALUE;
            for (NestGeneratorPane.NestNode sn : shaft) {
                double diff = Math.abs(sn.z - targetZ);
                if (diff < best) { best = diff; par = sn; }
            }
            double ba = rnd.nextDouble()*Math.PI*2;
            double bl = 6 + rnd.nextDouble()*(5+branching*2.5);
            Color col = (Color) parseColor(q[3]);
            NestGeneratorPane.NestNode cn = node(nest,
                par.x + bl*Math.cos(ba), par.y + bl*Math.sin(ba),
                Math.min(maxDepth, par.z + (rnd.nextDouble()-0.3)*3),
                q[0], Double.parseDouble(q[2]), col);
            edge(nest, par, cn, rnd);
        }

        // 5. Cross-connect loops
        if (branching >= 3 && shaft.size() >= 4) {
            for (int i = 1; i < shaft.size()-2; i += 2)
                edge(nest, shaft.get(i), shaft.get(i+2), rnd);
        }

        return nest;
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static void fill(List<String[]> q, String type, int count,
            double baseRatio, double spread, double radius, Color color) {
        for (int i = 0; i < count; i++)
            q.add(new String[]{type, String.valueOf(baseRatio + Math.random()*spread),
                String.valueOf(radius), color.toString()});
    }

    private static Object parseColor(String s) {
        // Color.toString() produces web color; use it directly via Color.web()
        try { return Color.web(s); } catch (Exception e) { return Color.SANDYBROWN; }
    }

    private static NestGeneratorPane.NestNode node(NestGeneratorPane.GeneratedNest nest,
            double x, double y, double z, String type, double r, Color c) {
        NestGeneratorPane.NestNode n = new NestGeneratorPane.NestNode(x, y, z, type, r, c);
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
