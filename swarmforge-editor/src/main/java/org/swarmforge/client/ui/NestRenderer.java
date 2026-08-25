/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import javafx.scene.text.Font;

import java.util.*;

/** Static rendering methods for NestGeneratorPane multi-species views. */
public final class NestRenderer {

    private NestRenderer() {}

    // ── 3D isometric view ────────────────────────────────────────────────────

    public static void draw3D(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double azimuth, double elevation, double zoom, double tunnelW) {
        draw3D(nest, gc, w, h, azimuth, elevation, zoom, tunnelW, 0, 0, true);
    }

    public static void draw3D(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double azimuth, double elevation, double zoom, double tunnelW, double panX, double panY) {
        draw3D(nest, gc, w, h, azimuth, elevation, zoom, tunnelW, panX, panY, true);
    }

    public static void draw3D(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double azimuth, double elevation, double zoom, double tunnelW,
            double panX, double panY, boolean showGhost) {

        if (nest == null) return;
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.rgb(12, 14, 25)); gc.fillRect(0, 0, w, h);

        double az = Math.toRadians(azimuth), el = Math.toRadians(elevation);
        double cAz = Math.cos(az), sAz = Math.sin(az);
        double cEl = Math.cos(el), sEl = Math.sin(el);
        double cx = w/2 + panX, cy = h/2 - 15 + panY;

        java.util.function.Function<double[], double[]> proj = pt -> {
            double rx = pt[0]*cAz - pt[1]*sAz;
            double ry = pt[0]*sAz + pt[1]*cAz;
            double rz = pt[2];
            return new double[]{
                cx + rx*zoom,
                cy + (ry*sEl + rz*cEl)*zoom,
                ry*cEl - rz*sEl
            };
        };

        // Render Systematized 3D Ghost Mesh for Architecture Types
        String arch = nest.architecture != null ? nest.architecture.toUpperCase() : "BURROW_UNDERGROUND";
        if (showGhost) {
            drawGhostMesh(arch, nest, proj, gc, zoom);
        } else {
            // Draw baseline ground grid even if ghost mesh is disabled
            gc.setStroke(Color.rgb(56, 189, 248, 0.25)); gc.setLineWidth(1.0);
            for (double g = -20; g <= 20; g += 5) {
                double[] a = proj.apply(new double[]{g, -20, 0}), b = proj.apply(new double[]{g, 20, 0});
                double[] c2 = proj.apply(new double[]{-20, g, 0}), d = proj.apply(new double[]{20, g, 0});
                gc.strokeLine(a[0], a[1], b[0], b[1]); gc.strokeLine(c2[0], c2[1], d[0], d[1]);
            }
        }

        class Item implements Comparable<Item> {
            double depth; Runnable draw;
            Item(double d, Runnable r) { depth=d; draw=r; }
            public int compareTo(Item o) { return Double.compare(depth,o.depth); }
        }
        List<Item> items = new ArrayList<>();

        // Edges / Tunnels
        double tw = tunnelW * 2.0;
        Color edgeColor = getMaterialColor(nest.material);
        for (NestGeneratorPane.NestEdge edge : nest.edges) {
            List<double[]> sp = new ArrayList<>();
            double avg = 0;
            for (double[] wp : edge.pts) { double[] p=proj.apply(wp); sp.add(p); avg+=p[2]; }
            avg /= sp.size();
            final List<double[]> fsp = sp; final double fa = avg;
            items.add(new Item(fa-1000, () -> {
                gc.setStroke(edgeColor.darker()); gc.setLineWidth(tw);
                gc.beginPath();
                for (int i=0;i<fsp.size();i++) {
                    double[] p=fsp.get(i);
                    if(i==0) gc.moveTo(p[0],p[1]); else gc.lineTo(p[0],p[1]);
                }
                gc.stroke();
            }));
        }

        // Nodes / Chambers
        for (NestGeneratorPane.NestNode n : nest.nodes) {
            double[] p = proj.apply(new double[]{n.x, n.y, n.z});
            double rx = n.rx * 2.2, rz = n.rz * 2.2, depth = p[2];
            items.add(new Item(depth, () -> {
                if ("ENTRANCE".equals(n.type)) {
                    gc.setFill(Color.LIMEGREEN);
                    gc.fillOval(p[0]-6, p[1]-4, 12, 8);
                } else if (!"JUNCTION".equals(n.type)) {
                    RadialGradient rg = new RadialGradient(0,0,p[0]-rx*0.3,p[1]-rz*0.3,Math.max(rx,rz)*1.2,false,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, n.color.brighter()),
                        new Stop(0.75, n.color),
                        new Stop(1, n.color.darker().darker()));
                    gc.setFill(rg);
                    // Render anatomical lenticular dome shape (flattened Z height)
                    gc.fillOval(p[0]-rx, p[1]-rz, rx*2, rz*2);
                    gc.setStroke(Color.rgb(255,255,255,0.4)); gc.setLineWidth(1);
                    gc.strokeOval(p[0]-rx, p[1]-rz, rx*2, rz*2);
                }
            }));
        }

        Collections.sort(items);
        items.forEach(i -> i.draw.run());

        // HUD & Legend
        gc.setFill(Color.rgb(200,200,200,0.85));
        gc.setFont(Font.font("SansSerif",11));
        gc.fillText(String.format("3D View (Zoom x%.1f) | Drag: Orbit | Scroll: Zoom", zoom), 8, h-8);
        legendHeader(gc, nest, 8, 18);
    }

    // ── 2D side view ─────────────────────────────────────────────────────────

    public static void drawSide(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW) {
        drawSide(nest, gc, w, h, tunnelW, 1.0, 0, 0);
    }

    public static void drawSide(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW, double zoom, double panX, double panY) {
        if (nest == null) return;
        gc.clearRect(0,0,w,h);
        gc.setFill(Color.rgb(18,18,30)); gc.fillRect(0,0,w,h);

        double skyH = 28 * zoom + panY;
        double groundY = Math.max(10, skyH) + 7 * zoom;
        gc.setFill(Color.rgb(30,42,58)); gc.fillRect(0,0,w,Math.max(10, skyH));
        gc.setFill(Color.rgb(55,85,40)); gc.fillRect(0,Math.max(10, skyH),w,7*zoom);
        gc.setFill(Color.rgb(52,36,22)); gc.fillRect(0,groundY,w,h);

        gc.setStroke(Color.rgb(70,48,30,0.35)); gc.setLineWidth(1);
        for (double y=groundY + 25*zoom; y<h; y+=35*zoom) gc.strokeLine(0,y,w,y);

        double maxD = nest.maxDepth;
        double sY = ((h-55)/Math.max(1,maxD)) * zoom;
        double sX = 8.5 * zoom;
        double cx = w/2 + panX;

        Color edgeColor = getMaterialColor(nest.material);
        gc.setStroke(edgeColor); gc.setLineWidth(tunnelW * 2.1 * zoom);
        for (NestGeneratorPane.NestEdge e : nest.edges) {
            gc.beginPath();
            for (int i=0;i<e.pts.size();i++) {
                double[] pt=e.pts.get(i);
                double px=cx+pt[0]*sX, py=groundY+pt[2]*sY;
                if(i==0) gc.moveTo(px,py); else gc.lineTo(px,py);
            }
            gc.stroke();
        }

        for (NestGeneratorPane.NestNode n : nest.nodes) {
            double nx=cx+n.x*sX, ny=groundY+n.z*sY;
            double rx=n.rx*2.4*zoom, rz=n.rz*1.5*zoom;
            if ("ENTRANCE".equals(n.type)) {
                gc.setFill(Color.LIMEGREEN);
                gc.fillOval(nx-7*zoom, groundY-5*zoom, 14*zoom, 10*zoom);
            }
            else if (!"JUNCTION".equals(n.type)) {
                gc.setFill(n.color.darker()); gc.fillOval(nx-rx, ny-rz, rx*2, rz*2);
                gc.setStroke(n.color); gc.setLineWidth(1.5*zoom);
                gc.strokeOval(nx-rx, ny-rz, rx*2, rz*2);
            }
        }

        gc.setFill(Color.rgb(220,220,220)); gc.setFont(Font.font("SansSerif",10));
        gc.fillText(String.format("Side View (x%.1f) - Scroll: Zoom | Drag: Pan", zoom), 4, 12);
    }

    // ── 2D top view ──────────────────────────────────────────────────────────

    public static void drawTop(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW) {
        drawTop(nest, gc, w, h, tunnelW, 1.0, 0, 0);
    }

    public static void drawTop(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW, double zoom, double panX, double panY) {
        if (nest == null) return;
        gc.clearRect(0,0,w,h);
        gc.setFill(Color.rgb(18,20,32)); gc.fillRect(0,0,w,h);

        double cx = w/2 + panX, cy = h/2 + panY, sc = 8.0 * zoom;

        gc.setStroke(Color.rgb(50,50,75,0.4)); gc.setLineWidth(1);
        for (double r = 35 * zoom; r < Math.max(w, h); r += 35 * zoom) {
            gc.strokeOval(cx-r, cy-r, r*2, r*2);
        }

        Color edgeColor = getMaterialColor(nest.material);
        gc.setStroke(edgeColor); gc.setLineWidth(tunnelW * 2.0 * zoom);
        for (NestGeneratorPane.NestEdge e : nest.edges) {
            gc.beginPath();
            for (int i=0;i<e.pts.size();i++) {
                double[] pt=e.pts.get(i);
                double px=cx+pt[0]*sc, py=cy+pt[1]*sc;
                if(i==0) gc.moveTo(px,py); else gc.lineTo(px,py);
            }
            gc.stroke();
        }

        for (NestGeneratorPane.NestNode n : nest.nodes) {
            double nx=cx+n.x*sc, ny=cy+n.y*sc, r=n.radius*2.4*zoom;
            double depthRatio = n.z/Math.max(1,nest.maxDepth);
            Color ring = Color.hsb(200-depthRatio*150, 0.8, 0.9);
            gc.setFill(n.color); gc.fillOval(nx-r,ny-r,r*2,r*2);
            gc.setStroke(ring); gc.setLineWidth(1.5*zoom);
            gc.strokeOval(nx-r,ny-r,r*2,r*2);
        }

        gc.setFill(Color.rgb(220,220,220)); gc.setFont(Font.font("SansSerif",10));
        gc.fillText(String.format("Top View (x%.1f) - Scroll: Zoom | Drag: Pan", zoom), 4, 12);
    }

    // ── Shared Legend & Color Key ─────────────────────────────────────────────

    private static Color getMaterialColor(String mat) {
        if (mat == null) return Color.rgb(130,90,55);
        return switch (mat) {
            case "WOOD_PULP_PAPER" -> Color.rgb(180, 160, 130);
            case "BEESWAX"          -> Color.rgb(230, 190, 60);
            case "STERCORAL_CEMENT"-> Color.rgb(160, 110, 70);
            case "SILK_WEAVE"      -> Color.rgb(200, 220, 180);
            case "PROPOLIS"        -> Color.rgb(150, 90, 40);
            case "CARTON_PULP"     -> Color.rgb(110, 75, 45);
            case "LIVING_INSECT_BODIES" -> Color.rgb(180, 50, 40);
            default                -> Color.rgb(130, 90, 55);
        };
    }

    private static void legendHeader(GraphicsContext gc, NestGeneratorPane.GeneratedNest nest,
            double x, double y) {
        long chambers = nest.nodes.stream()
            .filter(n -> !"JUNCTION".equals(n.type) && !"ENTRANCE".equals(n.type)).count();
        gc.setFill(Color.rgb(180,180,180,0.85)); gc.setFont(Font.font("SansSerif",11));
        gc.fillText(String.format("Arch: %s | Mat: %s | Scale: %.1fmm | Chambers: %d",
            nest.architecture, nest.material, nest.workerSizeMm, chambers), x, y);
    }

    /** Draws 3D Ghost Mesh contours for all 13 biological architectures. */
    private static void drawGhostMesh(String arch, NestGeneratorPane.GeneratedNest nest,
            java.util.function.Function<double[], double[]> proj, GraphicsContext gc, double zoom) {

        if (arch == null) arch = "BURROW_UNDERGROUND";

        // 1. Surface Ground Grid Mesh (z = 0)
        gc.setStroke(Color.rgb(56, 189, 248, 0.30)); gc.setLineWidth(1.0);
        for (double g = -25; g <= 25; g += 5) {
            double[] a = proj.apply(new double[]{g, -25, 0}), b = proj.apply(new double[]{g, 25, 0});
            double[] c2 = proj.apply(new double[]{-25, g, 0}), d = proj.apply(new double[]{25, g, 0});
            gc.strokeLine(a[0], a[1], b[0], b[1]); gc.strokeLine(c2[0], c2[1], d[0], d[1]);
        }

        double maxD = nest != null ? nest.maxDepth : 30.0;
        double scale = nest != null ? nest.workerSizeMm / 5.0 : 1.0;

        switch (arch) {
            case "WOODEN_BEEHIVE" -> {
                // 3D Translucent Wooden Hive Box enclosing frames & brood cells
                gc.setStroke(Color.web("#d97706", 0.65)); gc.setLineWidth(1.8 * zoom);
                double boxW = 8.0 * scale, boxH = 13.5 * scale;
                double zMin = -1.0 * scale, zMax = zMin + boxH;
                double[][] corners = {{-boxW, -boxW}, {boxW, -boxW}, {boxW, boxW}, {-boxW, boxW}};
                for (int i = 0; i < 4; i++) {
                    int next = (i + 1) % 4;
                    double[] b1 = proj.apply(new double[]{corners[i][0], corners[i][1], zMin});
                    double[] b2 = proj.apply(new double[]{corners[next][0], corners[next][1], zMin});
                    double[] t1 = proj.apply(new double[]{corners[i][0], corners[i][1], zMax});
                    double[] t2 = proj.apply(new double[]{corners[next][0], corners[next][1], zMax});

                    gc.strokeLine(b1[0], b1[1], b2[0], b2[1]);
                    gc.strokeLine(t1[0], t1[1], t2[0], t2[1]);
                    gc.strokeLine(b1[0], b1[1], t1[0], t1[1]);
                }
                // Flight slot entrance
                double[] ent1 = proj.apply(new double[]{-4 * scale, boxW, 0});
                double[] ent2 = proj.apply(new double[]{4 * scale, boxW, 0});
                gc.setStroke(Color.web("#ef4444", 0.90)); gc.setLineWidth(3.0 * zoom);
                gc.strokeLine(ent1[0], ent1[1], ent2[0], ent2[1]);
            }
            case "WAX_COMB_HEXAGONAL" -> {
                // Hanging vertical wax comb frame outline
                double combW = 12.0 * scale, combH = 14.0 * scale;
                double[] topA = proj.apply(new double[]{-combW, 0, 0}), topB = proj.apply(new double[]{combW, 0, 0});
                double[] botA = proj.apply(new double[]{-combW, 0, combH}), botB = proj.apply(new double[]{combW, 0, combH});

                gc.setStroke(Color.web("#f59e0b", 0.70)); gc.setLineWidth(2.0 * zoom);
                gc.strokeLine(topA[0], topA[1], topB[0], topB[1]);
                gc.strokeLine(botA[0], botA[1], botB[0], botB[1]);
                gc.strokeLine(topA[0], topA[1], botA[0], botA[1]);
                gc.strokeLine(topB[0], topB[1], botB[0], botB[1]);
            }
            case "WAX_POTS_CLUSTER" -> {
                // Organic moss/wax protective dome envelope
                double[] cp = proj.apply(new double[]{0, 0, 4.0 * scale});
                double rx = 12.0 * scale * zoom * 0.85, ry = 12.0 * scale * zoom * 0.42;

                gc.setFill(Color.web("#65a30d", 0.22));
                gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                gc.setStroke(Color.web("#84cc16", 0.65)); gc.setLineWidth(1.5 * zoom);
                gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
            }
            case "PAPER_PEDUNCULATE" -> {
                // Tree branch & peduncle petiole stem
                gc.setStroke(Color.web("#92400e", 0.75)); gc.setLineWidth(3.5 * zoom);
                double[] brA = proj.apply(new double[]{-16, 0, -5 * scale}), brB = proj.apply(new double[]{16, 0, -5 * scale});
                gc.strokeLine(brA[0], brA[1], brB[0], brB[1]);

                gc.setStroke(Color.web("#78350f", 0.90)); gc.setLineWidth(2.0 * zoom);
                double[] stemTop = proj.apply(new double[]{0, 0, -5 * scale}), stemBot = proj.apply(new double[]{0, 0, 0});
                gc.strokeLine(stemTop[0], stemTop[1], stemBot[0], stemBot[1]);

                // Hanging paper envelope tiers
                double nestH = Math.max(10.0, maxD);
                for (double zLevel = 0; zLevel <= nestH; zLevel += 3.0 * scale) {
                    double norm = zLevel / nestH;
                    double rad = (Math.sin(norm * Math.PI) * 10.0 + 2.5) * scale;
                    double[] cp = proj.apply(new double[]{0, 0, zLevel});
                    double rx = rad * zoom * 0.85, ry = rad * zoom * 0.42;

                    gc.setFill(Color.web("#e2e8f0", 0.22 + (1 - norm) * 0.20));
                    gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                    gc.setStroke(Color.web("#cbd5e1", 0.65)); gc.setLineWidth(1.2 * zoom);
                    gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                }
            }
            case "CATHEDRAL_MOUND" -> {
                // Conical cathedral mound spire above ground
                double moundH = 18.0;
                int baseRad = 14;
                for (double hZ = 0; hZ <= moundH; hZ += 1.5) {
                    double radius = baseRad * (1.0 - Math.pow(hZ / moundH, 0.75));
                    double[] centerP = proj.apply(new double[]{0, 0, -hZ});
                    double rx = radius * zoom * 0.85, ry = radius * zoom * 0.42;

                    gc.setFill(Color.web("#854d0e", 0.30 + (hZ / moundH) * 0.35));
                    gc.fillOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);
                    gc.setStroke(Color.web("#a16207", 0.75)); gc.setLineWidth(1.2);
                    gc.strokeOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);
                }
                // Ventilation buttresses
                double[] angles = {0, Math.PI / 3, 2 * Math.PI / 3, Math.PI, 4 * Math.PI / 3, 5 * Math.PI / 3};
                for (double ang : angles) {
                    double[] botP = proj.apply(new double[]{baseRad * Math.cos(ang), baseRad * Math.sin(ang), 0});
                    double[] topP = proj.apply(new double[]{3 * Math.cos(ang), 3 * Math.sin(ang), -moundH});
                    gc.setStroke(Color.web("#ca8a04", 0.85)); gc.setLineWidth(2.0);
                    gc.strokeLine(botP[0], botP[1], topP[0], topP[1]);
                }
                // Subterranean cellar box below ground
                gc.setStroke(Color.web("#713f12", 0.35)); gc.setLineWidth(1.0);
                for (double g = -14; g <= 14; g += 7) {
                    double[] a = proj.apply(new double[]{g, -14, maxD}), b = proj.apply(new double[]{g, 14, maxD});
                    gc.strokeLine(a[0], a[1], b[0], b[1]);
                }
            }
            case "ARBOREAL_SILK_LEAF" -> {
                // Support tree branch
                gc.setStroke(Color.web("#92400e", 0.75)); gc.setLineWidth(3.0 * zoom);
                double[] b1 = proj.apply(new double[]{-16, 0, -8 * scale}), b2 = proj.apply(new double[]{16, 0, -8 * scale});
                gc.strokeLine(b1[0], b1[1], b2[0], b2[1]);

                // Translucent foliage sphere & silk weave threads
                double[] centerP = proj.apply(new double[]{0, 0, 3 * scale});
                double rx = 13.0 * scale * zoom * 0.85, ry = 13.0 * scale * zoom * 0.42;

                gc.setFill(Color.web("#15803d", 0.22));
                gc.fillOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);
                gc.setStroke(Color.web("#22c55e", 0.65)); gc.setLineWidth(1.5 * zoom);
                gc.strokeOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);

                // Silk criss-cross threads
                gc.setStroke(Color.web("#f8fafc", 0.50)); gc.setLineWidth(1.0 * zoom);
                for (int i = 0; i < 6; i++) {
                    double a1 = i * Math.PI / 3;
                    double[] s1 = proj.apply(new double[]{12 * scale * Math.cos(a1), 12 * scale * Math.sin(a1), (i % 2 == 0 ? -2 : 8) * scale});
                    double[] s2 = proj.apply(new double[]{-12 * scale * Math.cos(a1), -12 * scale * Math.sin(a1), (i % 2 == 0 ? 8 : -2) * scale});
                    gc.strokeLine(s1[0], s1[1], s2[0], s2[1]);
                }
            }
            case "SUBTERRANEAN_FUNGI_VAULT" -> {
                // Surface craters
                for (int i = 0; i < 3; i++) {
                    double ang = i * (Math.PI * 2 / 3.0);
                    double[] crP = proj.apply(new double[]{(5.0 + i * 2.0) * Math.cos(ang) * scale, (5.0 + i * 2.0) * Math.sin(ang) * scale, 0});
                    gc.setFill(Color.web("#854d0e", 0.40));
                    gc.fillOval(crP[0] - 6 * zoom, crP[1] - 3 * zoom, 12 * zoom, 6 * zoom);
                }

                // Fungi vaults translucent purple contour
                double zVault = maxD * 0.45;
                double[] vaultP = proj.apply(new double[]{0, 0, zVault});
                double rx = 14.0 * scale * zoom * 0.85, ry = 14.0 * scale * zoom * 0.42;
                gc.setFill(Color.web("#a855f7", 0.25));
                gc.fillOval(vaultP[0] - rx, vaultP[1] - ry, rx * 2, ry * 2);
                gc.setStroke(Color.web("#c084fc", 0.65)); gc.setLineWidth(1.4 * zoom);
                gc.strokeOval(vaultP[0] - rx, vaultP[1] - ry, rx * 2, ry * 2);

                // Refuse waste pit contour
                double zWaste = maxD * 0.85;
                double[] wasteP = proj.apply(new double[]{0, 0, zWaste});
                double rxW = 10.0 * scale * zoom * 0.85, ryW = 10.0 * scale * zoom * 0.42;
                gc.setFill(Color.web("#ef4444", 0.25));
                gc.fillOval(wasteP[0] - rxW, wasteP[1] - ryW, rxW * 2, ryW * 2);
                gc.setStroke(Color.web("#f87171", 0.65)); gc.setLineWidth(1.4 * zoom);
                gc.strokeOval(wasteP[0] - rxW, wasteP[1] - ryW, rxW * 2, ryW * 2);
            }
            case "CARTON_NEST" -> {
                // Host tree trunk anchor
                gc.setStroke(Color.web("#78350f", 0.70)); gc.setLineWidth(4.0 * zoom);
                double[] trunkB = proj.apply(new double[]{0, -10 * scale, 16 * scale}), trunkT = proj.apply(new double[]{0, -10 * scale, -14 * scale});
                gc.strokeLine(trunkB[0], trunkB[1], trunkT[0], trunkT[1]);

                // Concentric spherical carton globe
                double[] cp = proj.apply(new double[]{0, 0, 2 * scale});
                double rx = 12.0 * scale * zoom * 0.85, ry = 12.0 * scale * zoom * 0.42;

                gc.setFill(Color.web("#a16207", 0.28));
                gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                gc.setStroke(Color.web("#78350f", 0.75)); gc.setLineWidth(1.6 * zoom);
                gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
            }
            case "BAMBOO_STEM_NEST" -> {
                // Bamboo cylinder stem
                double stemMinX = -15.0 * scale, stemMaxX = 18.0 * scale;
                double rStem = 3.6 * scale;

                gc.setStroke(Color.web("#84cc16", 0.60)); gc.setLineWidth(1.6 * zoom);
                for (double x = stemMinX; x <= stemMaxX; x += 6 * scale) {
                    double[] topP = proj.apply(new double[]{x, 0, -rStem});
                    double[] botP = proj.apply(new double[]{x, 0, rStem});
                    gc.strokeLine(topP[0], topP[1], botP[0], botP[1]);
                }

                double[] leftTop = proj.apply(new double[]{stemMinX, 0, -rStem});
                double[] rightTop = proj.apply(new double[]{stemMaxX, 0, -rStem});
                double[] leftBot = proj.apply(new double[]{stemMinX, 0, rStem});
                double[] rightBot = proj.apply(new double[]{stemMaxX, 0, rStem});

                gc.strokeLine(leftTop[0], leftTop[1], rightTop[0], rightTop[1]);
                gc.strokeLine(leftBot[0], leftBot[1], rightBot[0], rightBot[1]);

                // Entrance plug
                double[] plug = proj.apply(new double[]{-12 * scale, 0, 0});
                gc.setFill(Color.web("#ef4444", 0.85));
                gc.fillOval(plug[0] - 3 * zoom, plug[1] - 3 * zoom, 6 * zoom, 6 * zoom);
            }
            case "BIVOUAC_LIVING_NEST" -> {
                // Overhead fallen log shelter
                gc.setStroke(Color.web("#78350f", 0.80)); gc.setLineWidth(4.0 * zoom);
                double[] log1 = proj.apply(new double[]{-15, 0, -8 * scale}), log2 = proj.apply(new double[]{16, 0, -8 * scale});
                gc.strokeLine(log1[0], log1[1], log2[0], log2[1]);

                // Living ant catenary body curtain
                double bivouacH = 12.0 * scale;
                for (double zLevel = -8 * scale; zLevel <= bivouacH; zLevel += 2.0 * scale) {
                    double norm = (zLevel - (-8 * scale)) / (bivouacH - (-8 * scale));
                    double rad = (Math.sin(norm * Math.PI) * 10.0 + 2.0) * scale;
                    double[] cp = proj.apply(new double[]{0, 0, zLevel});
                    double rx = rad * zoom * 0.85, ry = rad * zoom * 0.42;

                    gc.setFill(Color.web("#dc2626", 0.22 + (1 - norm) * 0.15));
                    gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                    gc.setStroke(Color.web("#ef4444", 0.60)); gc.setLineWidth(1.1 * zoom);
                    gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                }
            }
            case "SURFACE_MOUND" -> {
                // Earth dome mound above ground
                double moundH = 8.0 * scale;
                for (double hZ = 0; hZ <= moundH; hZ += 1.5 * scale) {
                    double radius = 14.0 * scale * (1.0 - Math.pow(hZ / moundH, 0.65));
                    double[] cp = proj.apply(new double[]{0, 0, -hZ});
                    double rx = radius * zoom * 0.85, ry = radius * zoom * 0.42;

                    gc.setFill(Color.web("#ca8a04", 0.25 + (hZ / moundH) * 0.25));
                    gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                    gc.setStroke(Color.web("#eab308", 0.65)); gc.setLineWidth(1.2 * zoom);
                    gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                }
                // Subterranean soil volume below
                gc.setStroke(Color.web("#78350f", 0.35)); gc.setLineWidth(1.0);
                for (double g = -14; g <= 14; g += 7) {
                    double[] a = proj.apply(new double[]{g, -14, maxD}), b = proj.apply(new double[]{g, 14, maxD});
                    gc.strokeLine(a[0], a[1], b[0], b[1]);
                }
            }
            case "HOLLOW_TRUNK_NEST" -> {
                // Hollow trunk cylinder
                gc.setStroke(Color.web("#78350f", 0.65)); gc.setLineWidth(1.8 * zoom);
                for (double z = -14 * scale; z <= 14 * scale; z += 4 * scale) {
                    double[] cp = proj.apply(new double[]{0, 0, z});
                    double rx = 8.5 * scale * zoom * 0.85, ry = 8.5 * scale * zoom * 0.42;
                    gc.setFill(Color.web("#78350f", 0.22));
                    gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                    gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                }
                // Knot entrance hole
                double[] knot = proj.apply(new double[]{0, -5.5 * scale, -4.0 * scale});
                gc.setFill(Color.web("#22c55e", 0.85));
                gc.fillOval(knot[0] - 4 * zoom, knot[1] - 3 * zoom, 8 * zoom, 6 * zoom);
            }
            default -> {
                // Classic Subterranean Soil Volume (BURROW_UNDERGROUND)
                gc.setStroke(Color.rgb(148, 163, 184, 0.25)); gc.setLineWidth(0.8);
                double bw = 22, bd = 22;
                double[][] botCorners = {
                    {-bw, -bd, maxD}, {bw, -bd, maxD}, {bw, bd, maxD}, {-bw, bd, maxD}
                };
                double[][] topCorners = {
                    {-bw, -bd, 0}, {bw, -bd, 0}, {bw, bd, 0}, {-bw, bd, 0}
                };
                for (int i = 0; i < 4; i++) {
                    double[] p1 = proj.apply(botCorners[i]), p2 = proj.apply(botCorners[(i + 1) % 4]);
                    gc.strokeLine(p1[0], p1[1], p2[0], p2[1]);

                    double[] pT = proj.apply(topCorners[i]), pB = proj.apply(botCorners[i]);
                    gc.strokeLine(pT[0], pT[1], pB[0], pB[1]);
                }
            }
        }
    }

    /** Draws a visual key box mapping node colors to chamber function. */
    public static void drawColorKeyLegend(GraphicsContext gc, double x, double y) {
        gc.setFill(Color.rgb(20, 24, 38, 0.85));
        gc.fillRoundRect(x, y, 165, 140, 8, 8);
        gc.setStroke(Color.rgb(80, 100, 140, 0.6));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, 165, 140, 8, 8);

        gc.setFill(Color.rgb(0, 212, 255));
        gc.setFont(Font.font("SansSerif", 11));
        gc.fillText("Légende / Color Key", x + 10, y + 16);

        String[][] items = {
            {"Entrée / Entrance", "#32CD32"},
            {"Loge Royale / Queen", "#FFD700"},
            {"Couvain / Brood", "#00BFFF"},
            {"Réserve / Food", "#FFA500"},
            {"Champignon / Pollen", "#9370DB"},
            {"Dépotoir / Waste", "#CD5C5C"},
            {"Tunnel / Galeries", "#708090"}
        };

        double ly = y + 32;
        gc.setFont(Font.font("SansSerif", 10));
        for (String[] it : items) {
            gc.setFill(Color.web(it[1]));
            gc.fillOval(x + 10, ly - 7, 9, 9);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(0.5);
            gc.strokeOval(x + 10, ly - 7, 9, 9);

            gc.setFill(Color.rgb(220, 220, 230));
            gc.fillText(it[0], x + 24, ly);
            ly += 15;
        }
    }
}

