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

import org.swarmforge.client.util.ThemeManager;

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

        if (nest == null || gc == null || w < 10 || h < 10) return;
        gc.clearRect(0, 0, w, h);
        gc.setFill(ThemeManager.getInstance().getViewportBackgroundColor()); gc.fillRect(0, 0, w, h);

        double az = Math.toRadians(azimuth), el = Math.toRadians(elevation);
        double cAz = Math.cos(az), sAz = Math.sin(az);
        double cEl = Math.cos(el), sEl = Math.sin(el);
        double cx = w/2 + panX, cy = h/2 - 15 + panY;

        java.util.function.Function<double[], double[]> proj = pt -> {
            if (pt == null || pt.length < 3) return new double[]{cx, cy, 0};
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
        if (nest.edges != null) {
            for (NestGeneratorPane.NestEdge edge : nest.edges) {
                if (edge == null || edge.pts == null || edge.pts.isEmpty()) continue;
                List<double[]> sp = new ArrayList<>();
                double avg = 0;
                boolean isSubterraneanEdge = false;
                for (double[] wp : edge.pts) {
                    if (wp == null) continue;
                    double[] p = proj.apply(wp);
                    sp.add(p);
                    avg += p[2];
                    if (wp.length >= 3 && wp[2] > 0) isSubterraneanEdge = true;
                }
                if (sp.isEmpty()) continue;
                avg /= sp.size();
                final List<double[]> fsp = sp;
                final double fa = avg;
                final boolean fSubEdge = isSubterraneanEdge;
                items.add(new Item(fa-1000, () -> {
                    Color drawEdgeCol = (showGhost && fSubEdge) ? Color.color(edgeColor.darker().getRed(), edgeColor.darker().getGreen(), edgeColor.darker().getBlue(), 0.75) : edgeColor.darker();
                    gc.setStroke(drawEdgeCol); gc.setLineWidth(tw);
                    gc.beginPath();
                    for (int i=0;i<fsp.size();i++) {
                        double[] p=fsp.get(i);
                        if(i==0) gc.moveTo(p[0],p[1]); else gc.lineTo(p[0],p[1]);
                    }
                    gc.stroke();
                }));
            }
        }

        // Nodes / Chambers
        if (nest.nodes != null) {
            for (NestGeneratorPane.NestNode n : nest.nodes) {
                if (n == null) continue;
                Color nodeColor = n.color != null ? n.color : Color.GRAY;
                double[] p = proj.apply(new double[]{n.x, n.y, n.z});
                double rx = n.rx * 2.2, rz = n.rz * 2.2, depth = p[2];
                final boolean isSubterraneanNode = n.z > 0;
                items.add(new Item(depth, () -> {
                    if ("ENTRANCE".equals(n.type)) {
                        gc.setFill(Color.LIMEGREEN);
                        gc.fillOval(p[0]-6, p[1]-4, 12, 8);
                    } else if (!"JUNCTION".equals(n.type)) {
                        Color baseNodeCol = (showGhost && isSubterraneanNode) ? Color.color(nodeColor.getRed(), nodeColor.getGreen(), nodeColor.getBlue(), 0.80) : nodeColor;
                        RadialGradient rg = new RadialGradient(0,0,p[0]-rx*0.3,p[1]-rz*0.3,Math.max(rx,rz)*1.2,false,
                            CycleMethod.NO_CYCLE,
                            new Stop(0, baseNodeCol.brighter()),
                            new Stop(0.75, baseNodeCol),
                            new Stop(1, baseNodeCol.darker().darker()));
                        gc.setFill(rg);
                        // Render anatomical lenticular dome shape (flattened Z height)
                        gc.fillOval(p[0]-rx, p[1]-rz, rx*2, rz*2);
                        gc.setStroke(Color.rgb(255,255,255,0.4)); gc.setLineWidth(1);
                        gc.strokeOval(p[0]-rx, p[1]-rz, rx*2, rz*2);
                    }
                }));
            }
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
        if (nest == null || gc == null || w < 10 || h < 10) return;
        boolean isDark = ThemeManager.getInstance().isDarkMode();
        gc.clearRect(0,0,w,h);
        gc.setFill(ThemeManager.getInstance().getViewportBackgroundColor()); gc.fillRect(0,0,w,h);

        boolean aerial = isAerialOrArboreal(nest.architecture);

        double skyH = 28 * zoom + panY;
        double groundY = Math.max(10, skyH) + 7 * zoom;

        if (aerial) {
            // Fill entire view with atmospheric open sky backdrop (no ground or soil layers)
            gc.setFill(isDark ? Color.rgb(25, 35, 48) : Color.rgb(224, 242, 254));
            gc.fillRect(0, 0, w, h);

            // Subtle atmospheric altitude grid lines
            gc.setStroke(isDark ? Color.rgb(255, 255, 255, 0.08) : Color.rgb(2, 132, 199, 0.12));
            gc.setLineWidth(1);
            for (double y = 30 * zoom; y < h; y += 35 * zoom) {
                gc.strokeLine(0, y, w, y);
            }

            // Render structural support cue for arboreal / aerial nest types
            String key = normalizeArchKey(nest.architecture);
            double cx = w / 2 + panX;
            if ("PAPER_PEDUNCULATE".equals(key) || "WAX_COMB_HEXAGONAL".equals(key) || "WAX_POTS_CLUSTER".equals(key)) {
                // Top hanging peduncle / branch beam
                gc.setFill(isDark ? Color.rgb(85, 55, 30) : Color.rgb(139, 90, 43));
                gc.fillRect(cx - 60 * zoom, Math.max(5, groundY - 15 * zoom), 120 * zoom, 8 * zoom);
            } else if ("HOLLOW_TRUNK_NEST".equals(key) || "CARTON_NEST".equals(key) || "BAMBOO_STEM_NEST".equals(key)) {
                // Vertical trunk / stem contour
                gc.setStroke(isDark ? Color.rgb(100, 70, 40, 0.4) : Color.rgb(160, 110, 60, 0.4));
                gc.setLineWidth(4 * zoom);
                gc.strokeLine(cx - 45 * zoom, 0, cx - 45 * zoom, h);
            } else if ("WOODEN_BEEHIVE".equals(key)) {
                // Wooden hive stand frame at bottom
                gc.setStroke(isDark ? Color.rgb(120, 80, 40, 0.5) : Color.rgb(180, 120, 60, 0.5));
                gc.setLineWidth(3 * zoom);
                gc.strokeRect(cx - 50 * zoom, groundY + 40 * zoom, 100 * zoom, 30 * zoom);
            }
        } else {
            // Subterranean & Terrestrial nests: draw sky, vegetation line, and soil stratigraphy
            gc.setFill(isDark ? Color.rgb(30,42,58) : Color.rgb(224,242,254)); gc.fillRect(0,0,w,Math.max(10, skyH));
            gc.setFill(isDark ? Color.rgb(55,85,40) : Color.rgb(134,239,172)); gc.fillRect(0,Math.max(10, skyH),w,7*zoom);
            gc.setFill(isDark ? Color.rgb(52,36,22) : Color.rgb(217,119,6)); gc.fillRect(0,groundY,w,h);

            gc.setStroke(isDark ? Color.rgb(70,48,30,0.35) : Color.rgb(180,140,100,0.45)); gc.setLineWidth(1);
            for (double y=groundY + 25*zoom; y<h; y+=35*zoom) gc.strokeLine(0,y,w,y);
        }

        double maxD = nest.maxDepth;
        double sY = ((h-55)/Math.max(1,maxD)) * zoom;
        double sX = 8.5 * zoom;
        double cx = w/2 + panX;

        Color edgeColor = getMaterialColor(nest.material);
        gc.setStroke(edgeColor); gc.setLineWidth(tunnelW * 2.1 * zoom);
        if (nest.edges != null) {
            for (NestGeneratorPane.NestEdge e : nest.edges) {
                if (e == null || e.pts == null || e.pts.isEmpty()) continue;
                gc.beginPath();
                for (int i=0;i<e.pts.size();i++) {
                    double[] pt=e.pts.get(i);
                    if (pt == null || pt.length < 3) continue;
                    double px=cx+pt[0]*sX, py=groundY+pt[2]*sY;
                    if(i==0) gc.moveTo(px,py); else gc.lineTo(px,py);
                }
                gc.stroke();
            }
        }

        if (nest.nodes != null) {
            for (NestGeneratorPane.NestNode n : nest.nodes) {
                if (n == null) continue;
                Color nodeColor = n.color != null ? n.color : Color.GRAY;
                double nx=cx+n.x*sX, ny=groundY+n.z*sY;
                double rx=n.rx*2.4*zoom, rz=n.rz*1.5*zoom;
                if ("ENTRANCE".equals(n.type)) {
                    gc.setFill(Color.LIMEGREEN);
                    gc.fillOval(nx-7*zoom, groundY-5*zoom, 14*zoom, 10*zoom);
                }
                else if (!"JUNCTION".equals(n.type)) {
                    gc.setFill(nodeColor.darker()); gc.fillOval(nx-rx, ny-rz, rx*2, rz*2);
                    gc.setStroke(nodeColor); gc.setLineWidth(1.5*zoom);
                    gc.strokeOval(nx-rx, ny-rz, rx*2, rz*2);
                }
            }
        }

        gc.setFill(ThemeManager.getInstance().getViewportTextColor()); gc.setFont(Font.font("SansSerif",10));
        gc.fillText(String.format("Side View (x%.1f) - Scroll: Zoom | Drag: Pan", zoom), 4, 12);
    }

    // ── 2D top view ──────────────────────────────────────────────────────────

    public static void drawTop(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW) {
        drawTop(nest, gc, w, h, tunnelW, 1.0, 0, 0);
    }

    public static void drawTop(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW, double zoom, double panX, double panY) {
        if (nest == null || gc == null || w < 10 || h < 10) return;
        boolean isDark = ThemeManager.getInstance().isDarkMode();
        gc.clearRect(0,0,w,h);
        gc.setFill(ThemeManager.getInstance().getViewportBackgroundColor()); gc.fillRect(0,0,w,h);

        double cx = w/2 + panX, cy = h/2 + panY, sc = 8.0 * zoom;

        gc.setStroke(isDark ? Color.rgb(50,50,75,0.4) : Color.rgb(180,190,210,0.5)); gc.setLineWidth(1);
        for (double r = 35 * zoom; r < Math.max(w, h); r += 35 * zoom) {
            gc.strokeOval(cx-r, cy-r, r*2, r*2);
        }

        Color edgeColor = getMaterialColor(nest.material);
        gc.setStroke(edgeColor); gc.setLineWidth(tunnelW * 2.0 * zoom);
        if (nest.edges != null) {
            for (NestGeneratorPane.NestEdge e : nest.edges) {
                if (e == null || e.pts == null || e.pts.isEmpty()) continue;
                gc.beginPath();
                for (int i=0;i<e.pts.size();i++) {
                    double[] pt=e.pts.get(i);
                    if (pt == null || pt.length < 2) continue;
                    double px=cx+pt[0]*sc, py=cy+pt[1]*sc;
                    if(i==0) gc.moveTo(px,py); else gc.lineTo(px,py);
                }
                gc.stroke();
            }
        }

        if (nest.nodes != null) {
            for (NestGeneratorPane.NestNode n : nest.nodes) {
                if (n == null) continue;
                Color nodeColor = n.color != null ? n.color : Color.GRAY;
                double nx=cx+n.x*sc, ny=cy+n.y*sc, r=n.radius*2.4*zoom;
                double depthRatio = n.z/Math.max(1,nest.maxDepth);
                Color ring = Color.hsb(200-depthRatio*150, 0.8, 0.9);
                gc.setFill(nodeColor); gc.fillOval(nx-r,ny-r,r*2,r*2);
                gc.setStroke(ring); gc.setLineWidth(1.5*zoom);
                gc.strokeOval(nx-r,ny-r,r*2,r*2);
            }
        }

        gc.setFill(ThemeManager.getInstance().getViewportTextColor()); gc.setFont(Font.font("SansSerif",10));
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

    public static String normalizeArchKey(String arch) {
        if (arch == null) return "BURROW_UNDERGROUND";
        String s = arch.toUpperCase();
        if (s.contains("CARTON")) return "CARTON_NEST";
        if (s.contains("HEXAGONAL") || s.contains("WAX_COMB")) return "WAX_COMB_HEXAGONAL";
        if (s.contains("POTS") || s.contains("WAX_POTS")) return "WAX_POTS_CLUSTER";
        if (s.contains("PAPER") || s.contains("PEDUNCULATE")) return "PAPER_PEDUNCULATE";
        if (s.contains("CATHEDRAL")) return "CATHEDRAL_MOUND";
        if (s.contains("SILK") || s.contains("LEAF")) return "ARBOREAL_SILK_LEAF";
        if (s.contains("FUNGI") || s.contains("VAULT")) return "SUBTERRANEAN_FUNGI_VAULT";
        if (s.contains("BAMBOO") || s.contains("STEM")) return "BAMBOO_STEM_NEST";
        if (s.contains("BIVOUAC")) return "BIVOUAC_LIVING_NEST";
        if (s.contains("HOLLOW") || s.contains("TRUNK")) return "HOLLOW_TRUNK_NEST";
        if (s.contains("SURFACE") || s.contains("MOUND")) return "SURFACE_MOUND";
        if (s.contains("BEEHIVE") || s.contains("WOODEN")) return "WOODEN_BEEHIVE";
        return "BURROW_UNDERGROUND";
    }

    public static boolean isAerialOrArboreal(String arch) {
        String key = normalizeArchKey(arch);
        return switch (key) {
            case "PAPER_PEDUNCULATE",
                 "ARBOREAL_SILK_LEAF",
                 "CARTON_NEST",
                 "BAMBOO_STEM_NEST",
                 "WOODEN_BEEHIVE",
                 "WAX_COMB_HEXAGONAL",
                 "WAX_POTS_CLUSTER",
                 "HOLLOW_TRUNK_NEST",
                 "BIVOUAC_LIVING_NEST" -> true;
            default -> false;
        };
    }

    public static String normalizeMatKey(String mat) {
        if (mat == null) return "EARTH";
        String s = mat.toUpperCase();
        if (s.contains("CARTON")) return "CARTON_PULP";
        if (s.contains("BEESWAX")) return "BEESWAX";
        if (s.contains("LIVING") || s.contains("BODIES") || s.contains("BIVOUAC")) return "LIVING_INSECT_BODIES";
        if (s.contains("PROPOLIS")) return "PROPOLIS";
        if (s.contains("SILK")) return "SILK_WEAVE";
        if (s.contains("STERCORAL") || s.contains("CEMENT")) return "STERCORAL_CEMENT";
        if (s.contains("BRANCH")) return "TREE_BRANCH";
        if (s.contains("LEAF")) return "TREE_LEAF";
        if (s.contains("TRUNK")) return "TREE_TRUNK";
        if (s.contains("PLANK")) return "WOOD_PLANK";
        if (s.contains("PAPER") || s.contains("WOOD_PULP_PAPER")) return "WOOD_PULP_PAPER";
        if (s.contains("EARTH") || s.contains("SOIL") || s.contains("CLAY")) return "EARTH";
        return "EARTH";
    }

    public static String formatArchitectureName(String arch) {
        String key = normalizeArchKey(arch);
        return switch (key) {
            case "CARTON_NEST" -> "Arboreal Carton Nest";
            case "WAX_COMB_HEXAGONAL" -> "Hexagonal Wax Comb";
            case "WAX_POTS_CLUSTER" -> "Wax Pots Cluster";
            case "PAPER_PEDUNCULATE" -> "Hanging Paper Nest";
            case "CATHEDRAL_MOUND" -> "Cathedral Mound";
            case "ARBOREAL_SILK_LEAF" -> "Arboreal Silk Leaf";
            case "SUBTERRANEAN_FUNGI_VAULT" -> "Subterranean Fungi Vault";
            case "BAMBOO_STEM_NEST" -> "Bamboo Stem & Gall";
            case "BIVOUAC_LIVING_NEST" -> "Bivouac Living Nest";
            case "HOLLOW_TRUNK_NEST" -> "Hollow Trunk Cavity";
            case "SURFACE_MOUND" -> "Surface Dome Mound";
            case "WOODEN_BEEHIVE" -> "Wooden Beehive";
            default -> "Subterranean Burrow";
        };
    }

    public static String formatMaterialName(String mat) {
        String key = normalizeMatKey(mat);
        return switch (key) {
            case "CARTON_PULP" -> "Carton & Wood Pulp";
            case "BEESWAX" -> "Beeswax (Apidae)";
            case "LIVING_INSECT_BODIES" -> "Living Insect Bodies (Bivouac)";
            case "PROPOLIS" -> "Propolis & Tree Resin";
            case "SILK_WEAVE" -> "Silk Weave (Oecophylla Larvae)";
            case "STERCORAL_CEMENT" -> "Stercoral Cement (Termite Feces/Mud)";
            case "TREE_BRANCH" -> "Tree Branch & Bark";
            case "TREE_LEAF" -> "Tree Leaf Tissue";
            case "TREE_TRUNK" -> "Tree Trunk & Hollow Wood";
            case "WOOD_PLANK" -> "Wood Plank Construction";
            case "WOOD_PULP_PAPER" -> "Wood Pulp Paper (Vespidae)";
            default -> "Earth & Clay Soil";
        };
    }

    private static void legendHeader(GraphicsContext gc, NestGeneratorPane.GeneratedNest nest,
            double x, double y) {
        long chambers = nest.nodes.stream()
            .filter(n -> !"JUNCTION".equals(n.type) && !"ENTRANCE".equals(n.type)).count();
        gc.setFill(ThemeManager.getInstance().getViewportSubtextColor()); gc.setFont(Font.font("SansSerif",11));
        gc.fillText(String.format("Arch: %s | Mat: %s | Scale: %.1fmm | Chambers: %d",
            formatArchitectureName(nest.architecture), formatMaterialName(nest.material), nest.workerSizeMm, chambers), x, y);
    }

    /** Draws 3D Ghost Mesh contours for all 13 biological architectures. */
    private static void drawGhostMesh(String arch, NestGeneratorPane.GeneratedNest nest,
            java.util.function.Function<double[], double[]> proj, GraphicsContext gc, double zoom) {

        String archKey = normalizeArchKey(arch);

        // 1. Surface Ground Grid Mesh (z = 0)
        gc.setStroke(Color.rgb(56, 189, 248, 0.30)); gc.setLineWidth(1.0);
        for (double g = -25; g <= 25; g += 5) {
            double[] a = proj.apply(new double[]{g, -25, 0}), b = proj.apply(new double[]{g, 25, 0});
            double[] c2 = proj.apply(new double[]{-25, g, 0}), d = proj.apply(new double[]{25, g, 0});
            gc.strokeLine(a[0], a[1], b[0], b[1]); gc.strokeLine(c2[0], c2[1], d[0], d[1]);
        }

        double maxD = nest != null ? nest.maxDepth : 30.0;
        double scale = nest != null ? nest.workerSizeMm / 5.0 : 1.0;
        double rx0 = nest != null ? nest.getRootX() : 0.0;
        double ry0 = nest != null ? nest.getRootY() : 0.0;

        switch (archKey) {
            case "WOODEN_BEEHIVE" -> {
                // 3D Translucent Wooden Hive Box enclosing multiple parallel frames
                gc.setStroke(Color.web("#d97706", 0.75)); gc.setLineWidth(2.0 * zoom);
                double boxW = 9.0 * scale, boxD = 9.0 * scale, boxH = 15.0 * scale;
                double zMin = -2.0 * scale, zMax = zMin + boxH;
                double[][] corners = {{rx0 - boxW, ry0 - boxD}, {rx0 + boxW, ry0 - boxD}, {rx0 + boxW, ry0 + boxD}, {rx0 - boxW, ry0 + boxD}};
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
                // Draw 5 parallel hanging wax combs inside the wooden hive frame box
                for (double offsetY = -6.0 * scale; offsetY <= 6.0 * scale; offsetY += 3.0 * scale) {
                    double cW = 7.5 * scale, cH = 11.0 * scale;
                    double[] topA = proj.apply(new double[]{rx0 - cW, ry0 + offsetY, zMin + 1.5 * scale});
                    double[] topB = proj.apply(new double[]{rx0 + cW, ry0 + offsetY, zMin + 1.5 * scale});
                    double[] botA = proj.apply(new double[]{rx0 - cW, ry0 + offsetY, zMin + 1.5 * scale + cH});
                    double[] botB = proj.apply(new double[]{rx0 + cW, ry0 + offsetY, zMin + 1.5 * scale + cH});

                    gc.setStroke(Color.web("#fbbf24", 0.65)); gc.setLineWidth(1.4 * zoom);
                    gc.strokeLine(topA[0], topA[1], topB[0], topB[1]);
                    gc.strokeLine(botA[0], botA[1], botB[0], botB[1]);
                    gc.strokeLine(topA[0], topA[1], botA[0], botA[1]);
                    gc.strokeLine(topB[0], topB[1], botB[0], botB[1]);
                }

                // Flight slot entrance
                double[] ent1 = proj.apply(new double[]{rx0 - 4 * scale, ry0 + boxD, zMax - 1.5 * scale});
                double[] ent2 = proj.apply(new double[]{rx0 + 4 * scale, ry0 + boxD, zMax - 1.5 * scale});
                gc.setStroke(Color.web("#ef4444", 0.95)); gc.setLineWidth(3.5 * zoom);
                gc.strokeLine(ent1[0], ent1[1], ent2[0], ent2[1]);
            }
            case "WAX_COMB_HEXAGONAL" -> {
                // Multiple parallel vertical hexagonal wax comb frames (7 combs)
                double cW = 11.0 * scale, cH = 14.0 * scale;
                int combCount = 7;
                double spacing = 2.4 * scale;
                double startY = ry0 - ((combCount - 1) * spacing) / 2.0;

                for (int i = 0; i < combCount; i++) {
                    double yPos = startY + i * spacing;
                    double[] topA = proj.apply(new double[]{rx0 - cW, yPos, 0});
                    double[] topB = proj.apply(new double[]{rx0 + cW, yPos, 0});
                    double[] botA = proj.apply(new double[]{rx0 - cW, yPos, cH});
                    double[] botB = proj.apply(new double[]{rx0 + cW, yPos, cH});

                    gc.setStroke(Color.web("#f59e0b", 0.75)); gc.setLineWidth(1.8 * zoom);
                    gc.strokeLine(topA[0], topA[1], topB[0], topB[1]);
                    gc.strokeLine(botA[0], botA[1], botB[0], botB[1]);
                    gc.strokeLine(topA[0], topA[1], botA[0], botA[1]);
                    gc.strokeLine(topB[0], topB[1], botB[0], botB[1]);

                    // Honey storage top band highlight
                    double[] hTopA = proj.apply(new double[]{rx0 - cW, yPos, cH * 0.25});
                    double[] hTopB = proj.apply(new double[]{rx0 + cW, yPos, cH * 0.25});
                    gc.setStroke(Color.web("#fef08a", 0.60)); gc.setLineWidth(1.2 * zoom);
                    gc.strokeLine(hTopA[0], hTopA[1], hTopB[0], hTopB[1]);
                }
            }
            case "WAX_POTS_CLUSTER" -> {
                // Organic moss/wax protective dome envelope
                double[] cp = proj.apply(new double[]{rx0, ry0, 4.0 * scale});
                double rx = 12.0 * scale * zoom * 0.85, ry = 12.0 * scale * zoom * 0.42;

                gc.setFill(Color.web("#65a30d", 0.22));
                gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                gc.setStroke(Color.web("#84cc16", 0.65)); gc.setLineWidth(1.5 * zoom);
                gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
            }
            case "PAPER_PEDUNCULATE" -> {
                // Tree branch & peduncle petiole stem
                gc.setStroke(Color.web("#92400e", 0.75)); gc.setLineWidth(3.5 * zoom);
                double[] brA = proj.apply(new double[]{rx0 - 16, ry0, -5 * scale}), brB = proj.apply(new double[]{rx0 + 16, ry0, -5 * scale});
                gc.strokeLine(brA[0], brA[1], brB[0], brB[1]);

                gc.setStroke(Color.web("#78350f", 0.90)); gc.setLineWidth(2.0 * zoom);
                double[] stemTop = proj.apply(new double[]{rx0, ry0, -5 * scale}), stemBot = proj.apply(new double[]{rx0, ry0, 0});
                gc.strokeLine(stemTop[0], stemTop[1], stemBot[0], stemBot[1]);

                // Hanging paper envelope tiers
                double nestH = Math.max(10.0, maxD);
                for (double zLevel = 0; zLevel <= nestH; zLevel += 3.0 * scale) {
                    double norm = zLevel / nestH;
                    double rad = (Math.sin(norm * Math.PI) * 10.0 + 2.5) * scale;
                    double[] cp = proj.apply(new double[]{rx0, ry0, zLevel});
                    double rx = rad * zoom * 0.85, ry = rad * zoom * 0.42;

                    gc.setFill(Color.web("#e2e8f0", 0.22 + (1 - norm) * 0.20));
                    gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                    gc.setStroke(Color.web("#cbd5e1", 0.65)); gc.setLineWidth(1.2 * zoom);
                    gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                }
            }
            case "CATHEDRAL_MOUND" -> {
                // Conical cathedral mound spire precisely aligned at (rx0, ry0)
                double moundH = 18.0;
                int baseRad = 14;
                for (double hZ = 0; hZ <= moundH; hZ += 1.5) {
                    double radius = baseRad * (1.0 - Math.pow(hZ / moundH, 0.75));
                    double[] centerP = proj.apply(new double[]{rx0, ry0, -hZ});
                    double rx = radius * zoom * 0.85, ry = radius * zoom * 0.42;

                    gc.setFill(Color.web("#854d0e", 0.30 + (hZ / moundH) * 0.35));
                    gc.fillOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);
                    gc.setStroke(Color.web("#a16207", 0.75)); gc.setLineWidth(1.2);
                    gc.strokeOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);
                }
                // Ventilation buttresses
                double[] angles = {0, Math.PI / 3, 2 * Math.PI / 3, Math.PI, 4 * Math.PI / 3, 5 * Math.PI / 3};
                for (double ang : angles) {
                    double[] botP = proj.apply(new double[]{rx0 + baseRad * Math.cos(ang), ry0 + baseRad * Math.sin(ang), 0});
                    double[] topP = proj.apply(new double[]{rx0 + 3 * Math.cos(ang), ry0 + 3 * Math.sin(ang), -moundH});
                    gc.setStroke(Color.web("#ca8a04", 0.85)); gc.setLineWidth(2.0);
                    gc.strokeLine(botP[0], botP[1], topP[0], topP[1]);
                }
                // Subterranean cellar box below ground
                gc.setStroke(Color.web("#713f12", 0.35)); gc.setLineWidth(1.0);
                for (double g = -14; g <= 14; g += 7) {
                    double[] a = proj.apply(new double[]{rx0 + g, ry0 - 14, maxD}), b = proj.apply(new double[]{rx0 + g, ry0 + 14, maxD});
                    gc.strokeLine(a[0], a[1], b[0], b[1]);
                }
            }
            case "ARBOREAL_SILK_LEAF" -> {
                // Support tree branch
                gc.setStroke(Color.web("#92400e", 0.75)); gc.setLineWidth(3.0 * zoom);
                double[] b1 = proj.apply(new double[]{rx0 - 16, ry0, -8 * scale}), b2 = proj.apply(new double[]{rx0 + 16, ry0, -8 * scale});
                gc.strokeLine(b1[0], b1[1], b2[0], b2[1]);

                // Translucent foliage sphere & silk weave threads
                double[] centerP = proj.apply(new double[]{rx0, ry0, 3 * scale});
                double rx = 13.0 * scale * zoom * 0.85, ry = 13.0 * scale * zoom * 0.42;

                gc.setFill(Color.web("#15803d", 0.22));
                gc.fillOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);
                gc.setStroke(Color.web("#22c55e", 0.65)); gc.setLineWidth(1.5 * zoom);
                gc.strokeOval(centerP[0] - rx, centerP[1] - ry, rx * 2, ry * 2);

                // Silk criss-cross threads
                gc.setStroke(Color.web("#f8fafc", 0.50)); gc.setLineWidth(1.0 * zoom);
                for (int i = 0; i < 6; i++) {
                    double a1 = i * Math.PI / 3;
                    double[] s1 = proj.apply(new double[]{rx0 + 12 * scale * Math.cos(a1), ry0 + 12 * scale * Math.sin(a1), (i % 2 == 0 ? -2 : 8) * scale});
                    double[] s2 = proj.apply(new double[]{rx0 - 12 * scale * Math.cos(a1), ry0 - 12 * scale * Math.sin(a1), (i % 2 == 0 ? 8 : -2) * scale});
                    gc.strokeLine(s1[0], s1[1], s2[0], s2[1]);
                }
            }
            case "SUBTERRANEAN_FUNGI_VAULT" -> {
                // Surface craters aligned at root
                for (int i = 0; i < 3; i++) {
                    double ang = i * (Math.PI * 2 / 3.0);
                    double[] crP = proj.apply(new double[]{rx0 + (5.0 + i * 2.0) * Math.cos(ang) * scale, ry0 + (5.0 + i * 2.0) * Math.sin(ang) * scale, 0});
                    gc.setFill(Color.web("#854d0e", 0.40));
                    gc.fillOval(crP[0] - 6 * zoom, crP[1] - 3 * zoom, 12 * zoom, 6 * zoom);
                }

                // Fungi vaults translucent purple contour
                double zVault = maxD * 0.45;
                double[] vaultP = proj.apply(new double[]{rx0, ry0, zVault});
                double rx = 14.0 * scale * zoom * 0.85, ry = 14.0 * scale * zoom * 0.42;
                gc.setFill(Color.web("#a855f7", 0.25));
                gc.fillOval(vaultP[0] - rx, vaultP[1] - ry, rx * 2, ry * 2);
                gc.setStroke(Color.web("#c084fc", 0.65)); gc.setLineWidth(1.4 * zoom);
                gc.strokeOval(vaultP[0] - rx, vaultP[1] - ry, rx * 2, ry * 2);

                // Refuse waste pit contour
                double zWaste = maxD * 0.85;
                double[] wasteP = proj.apply(new double[]{rx0, ry0, zWaste});
                double rxW = 10.0 * scale * zoom * 0.85, ryW = 10.0 * scale * zoom * 0.42;
                gc.setFill(Color.web("#ef4444", 0.25));
                gc.fillOval(wasteP[0] - rxW, wasteP[1] - ryW, rxW * 2, ryW * 2);
                gc.setStroke(Color.web("#f87171", 0.65)); gc.setLineWidth(1.4 * zoom);
                gc.strokeOval(wasteP[0] - rxW, wasteP[1] - ryW, rxW * 2, ryW * 2);
            }
            case "CARTON_NEST" -> {
                // Host tree trunk anchor
                gc.setStroke(Color.web("#78350f", 0.70)); gc.setLineWidth(4.0 * zoom);
                double[] trunkB = proj.apply(new double[]{rx0, ry0 - 10 * scale, 16 * scale}), trunkT = proj.apply(new double[]{rx0, ry0 - 10 * scale, -14 * scale});
                gc.strokeLine(trunkB[0], trunkB[1], trunkT[0], trunkT[1]);

                // Concentric spherical carton globe
                double[] cp = proj.apply(new double[]{rx0, ry0, 2 * scale});
                double rx = 12.0 * scale * zoom * 0.85, ry = 12.0 * scale * zoom * 0.42;

                gc.setFill(Color.web("#a16207", 0.28));
                gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                gc.setStroke(Color.web("#78350f", 0.75)); gc.setLineWidth(1.6 * zoom);
                gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
            }
            case "BAMBOO_STEM_NEST" -> {
                // Bamboo cylinder stem
                double stemMinX = rx0 - 15.0 * scale, stemMaxX = rx0 + 18.0 * scale;
                double rStem = 3.6 * scale;

                gc.setStroke(Color.web("#84cc16", 0.60)); gc.setLineWidth(1.6 * zoom);
                for (double x = stemMinX; x <= stemMaxX; x += 6 * scale) {
                    double[] topP = proj.apply(new double[]{x, ry0, -rStem});
                    double[] botP = proj.apply(new double[]{x, ry0, rStem});
                    gc.strokeLine(topP[0], topP[1], botP[0], botP[1]);
                }

                double[] leftTop = proj.apply(new double[]{stemMinX, ry0, -rStem});
                double[] rightTop = proj.apply(new double[]{stemMaxX, ry0, -rStem});
                double[] leftBot = proj.apply(new double[]{stemMinX, ry0, rStem});
                double[] rightBot = proj.apply(new double[]{stemMaxX, ry0, rStem});

                gc.strokeLine(leftTop[0], leftTop[1], rightTop[0], rightTop[1]);
                gc.strokeLine(leftBot[0], leftBot[1], rightBot[0], rightBot[1]);

                // Entrance plug
                double[] plug = proj.apply(new double[]{rx0 - 12 * scale, ry0, 0});
                gc.setFill(Color.web("#ef4444", 0.85));
                gc.fillOval(plug[0] - 3 * zoom, plug[1] - 3 * zoom, 6 * zoom, 6 * zoom);
            }
            case "BIVOUAC_LIVING_NEST" -> {
                // Overhead fallen log shelter
                gc.setStroke(Color.web("#78350f", 0.80)); gc.setLineWidth(4.0 * zoom);
                double[] log1 = proj.apply(new double[]{rx0 - 15, ry0, -8 * scale}), log2 = proj.apply(new double[]{rx0 + 16, ry0, -8 * scale});
                gc.strokeLine(log1[0], log1[1], log2[0], log2[1]);

                // Living ant catenary body curtain
                double bivouacH = 12.0 * scale;
                for (double zLevel = -8 * scale; zLevel <= bivouacH; zLevel += 2.0 * scale) {
                    double norm = (zLevel - (-8 * scale)) / (bivouacH - (-8 * scale));
                    double rad = (Math.sin(norm * Math.PI) * 10.0 + 2.0) * scale;
                    double[] cp = proj.apply(new double[]{rx0, ry0, zLevel});
                    double rx = rad * zoom * 0.85, ry = rad * zoom * 0.42;

                    gc.setFill(Color.web("#dc2626", 0.22 + (1 - norm) * 0.15));
                    gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                    gc.setStroke(Color.web("#ef4444", 0.60)); gc.setLineWidth(1.1 * zoom);
                    gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                }
            }
            case "SURFACE_MOUND" -> {
                // Earth dome mound precisely aligned at (rx0, ry0)
                double moundH = 8.0 * scale;
                for (double hZ = 0; hZ <= moundH; hZ += 1.5 * scale) {
                    double radius = 14.0 * scale * (1.0 - Math.pow(hZ / moundH, 0.65));
                    double[] cp = proj.apply(new double[]{rx0, ry0, -hZ});
                    double rx = radius * zoom * 0.85, ry = radius * zoom * 0.42;

                    gc.setFill(Color.web("#ca8a04", 0.25 + (hZ / moundH) * 0.25));
                    gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                    gc.setStroke(Color.web("#eab308", 0.65)); gc.setLineWidth(1.2 * zoom);
                    gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                }
                // Subterranean soil volume below
                gc.setStroke(Color.web("#78350f", 0.35)); gc.setLineWidth(1.0);
                for (double g = -14; g <= 14; g += 7) {
                    double[] a = proj.apply(new double[]{rx0 + g, ry0 - 14, maxD}), b = proj.apply(new double[]{rx0 + g, ry0 + 14, maxD});
                    gc.strokeLine(a[0], a[1], b[0], b[1]);
                }
            }
            case "HOLLOW_TRUNK_NEST" -> {
                // Hollow trunk cylinder
                gc.setStroke(Color.web("#78350f", 0.65)); gc.setLineWidth(1.8 * zoom);
                for (double z = -14 * scale; z <= 14 * scale; z += 4 * scale) {
                    double[] cp = proj.apply(new double[]{rx0, ry0, z});
                    double rx = 8.5 * scale * zoom * 0.85, ry = 8.5 * scale * zoom * 0.42;
                    gc.setFill(Color.web("#78350f", 0.22));
                    gc.fillOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                    gc.strokeOval(cp[0] - rx, cp[1] - ry, rx * 2, ry * 2);
                }
                // Knot entrance hole
                double[] knot = proj.apply(new double[]{rx0, ry0 - 5.5 * scale, -4.0 * scale});
                gc.setFill(Color.web("#22c55e", 0.85));
                gc.fillOval(knot[0] - 4 * zoom, knot[1] - 3 * zoom, 8 * zoom, 6 * zoom);
            }
            default -> {
                // Classic Subterranean Soil Volume (BURROW_UNDERGROUND)
                gc.setStroke(Color.rgb(148, 163, 184, 0.25)); gc.setLineWidth(0.8);
                double bw = 22, bd = 22;
                double[][] botCorners = {
                    {rx0 - bw, ry0 - bd, maxD}, {rx0 + bw, ry0 - bd, maxD}, {rx0 + bw, ry0 + bd, maxD}, {rx0 - bw, ry0 + bd, maxD}
                };
                double[][] topCorners = {
                    {rx0 - bw, ry0 - bd, 0}, {rx0 + bw, ry0 - bd, 0}, {rx0 + bw, ry0 + bd, 0}, {rx0 - bw, ry0 + bd, 0}
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
        boolean isDark = ThemeManager.getInstance().isDarkMode();
        gc.setFill(isDark ? Color.rgb(20, 24, 38, 0.85) : Color.rgb(255, 255, 255, 0.92));
        gc.fillRoundRect(x, y, 165, 140, 8, 8);
        gc.setStroke(isDark ? Color.rgb(80, 100, 140, 0.6) : Color.rgb(203, 213, 225, 0.9));
        gc.setLineWidth(1);
        gc.strokeRoundRect(x, y, 165, 140, 8, 8);

        gc.setFill(isDark ? Color.rgb(0, 212, 255) : Color.rgb(2, 132, 199));
        gc.setFont(Font.font("SansSerif", 11));
        gc.fillText("Color Key", x + 10, y + 16);

        String[][] items = {
            {"Entrance", "#32CD32"},
            {"Queen Chamber", "#FFD700"},
            {"Brood Nursery", "#00BFFF"},
            {"Food Storage", "#FFA500"},
            {"Fungus / Pollen", "#9370DB"},
            {"Waste / Cemetery", "#CD5C5C"},
            {"Tunnels & Galleries", "#708090"}
        };

        double ly = y + 32;
        gc.setFont(Font.font("SansSerif", 10));
        for (String[] it : items) {
            gc.setFill(Color.web(it[1]));
            gc.fillOval(x + 10, ly - 7, 9, 9);
            gc.setStroke(isDark ? Color.WHITE : Color.rgb(100, 116, 139));
            gc.setLineWidth(0.5);
            gc.strokeOval(x + 10, ly - 7, 9, 9);

            gc.setFill(ThemeManager.getInstance().getViewportTextColor());
            gc.fillText(it[0], x + 24, ly);
            ly += 15;
        }
    }
}

