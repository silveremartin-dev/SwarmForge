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
import org.swarmforge.client.ui.nest.NestGhostMeshRenderer;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.ThemeManager;

import java.util.*;

/**
 * Static rendering methods for NestGeneratorPane multi-species views (3D isometric, 2D side, and 2D top).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class NestRenderer {

    // ── Shared Chamber Color Constants (synchronized with simulation legend) ──────
    public static final Color CHAMBER_COLOR_ENTRANCE = Color.web("#10b981"); // Emerald/Lime
    public static final Color CHAMBER_COLOR_QUEEN    = Color.web("#d946ef"); // Royal Magenta/Gold
    public static final Color CHAMBER_COLOR_BROOD    = Color.web("#0284c7"); // DeepSkyBlue / Brood Blue
    public static final Color CHAMBER_COLOR_STORAGE  = Color.web("#22c55e"); // Green Storage
    public static final Color CHAMBER_COLOR_FUNGUS   = Color.web("#a855f7"); // Purple Fungus
    public static final Color CHAMBER_COLOR_WASTE    = Color.web("#eab308"); // Amber Waste
    public static final Color CHAMBER_COLOR_TUNNEL   = Color.web("#f59e0b"); // Amber Ochre Tunnel

    private NestRenderer() {}

    // ── 3D Isometric View ────────────────────────────────────────────────────

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
        gc.setFill(ThemeManager.getInstance().getViewportBackgroundColor());
        gc.fillRect(0, 0, w, h);

        double az = Math.toRadians(azimuth), el = Math.toRadians(elevation);
        double cAz = Math.cos(az), sAz = Math.sin(az);
        double cEl = Math.cos(el), sEl = Math.sin(el);
        double cx = w / 2 + panX, cy = h / 2 - 15 + panY;

        java.util.function.Function<double[], double[]> proj = pt -> {
            if (pt == null || pt.length < 3) return new double[]{cx, cy, 0};
            double rx = pt[0] * cAz - pt[1] * sAz;
            double ry = pt[0] * sAz + pt[1] * cAz;
            double rz = pt[2];
            return new double[]{
                cx + rx * zoom,
                cy + (ry * sEl + rz * cEl) * zoom,
                ry * cEl - rz * sEl
            };
        };

        // Render Systematized 3D Ghost Mesh for Architecture Types
        String arch = nest.architecture != null ? nest.architecture.toUpperCase() : "BURROW_UNDERGROUND";
        if (showGhost) {
            NestGhostMeshRenderer.drawGhostMesh(arch, nest, proj, gc, zoom);
        } else {
            // Draw baseline ground grid even if ghost mesh is disabled
            gc.setStroke(Color.rgb(56, 189, 248, 0.25));
            gc.setLineWidth(1.0);
            for (double g = -20; g <= 20; g += 5) {
                double[] a = proj.apply(new double[]{g, -20, 0}), b = proj.apply(new double[]{g, 20, 0});
                double[] c2 = proj.apply(new double[]{-20, g, 0}), d = proj.apply(new double[]{20, g, 0});
                gc.strokeLine(a[0], a[1], b[0], b[1]);
                gc.strokeLine(c2[0], c2[1], d[0], d[1]);
            }
        }

        class Item implements Comparable<Item> {
            final double depth;
            final Runnable draw;
            Item(double d, Runnable r) { depth = d; draw = r; }
            public int compareTo(Item o) { return Double.compare(depth, o.depth); }
        }
        List<Item> items = new ArrayList<>();

        // Edges / Tunnels
        double tw = tunnelW * 2.0;
        Color edgeColor = CHAMBER_COLOR_TUNNEL;
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
                items.add(new Item(fa - 1000, () -> {
                    Color drawEdgeCol = (showGhost && fSubEdge) ? Color.color(edgeColor.getRed(), edgeColor.getGreen(), edgeColor.getBlue(), 0.85) : edgeColor;
                    gc.setStroke(drawEdgeCol);
                    gc.setLineWidth(tw);
                    gc.beginPath();
                    for (int i = 0; i < fsp.size(); i++) {
                        double[] p = fsp.get(i);
                        if (i == 0) gc.moveTo(p[0], p[1]); else gc.lineTo(p[0], p[1]);
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
                double rx = Math.max(3.0, (n.rx > 0 ? n.rx : n.radius) * zoom * 1.5);
                double rz = Math.max(2.0, (n.rz > 0 ? n.rz : n.radius * 0.55) * zoom * 1.5);
                double depth = p[2];
                final boolean isSubterraneanNode = !"ENTRANCE".equals(n.type);
                items.add(new Item(depth, () -> {
                    if ("ENTRANCE".equals(n.type)) {
                        gc.setFill(Color.LIMEGREEN);
                        gc.fillOval(p[0] - 6, p[1] - 4, 12, 8);
                    } else if (!"JUNCTION".equals(n.type)) {
                        Color baseNodeCol = (showGhost && isSubterraneanNode) ? Color.color(nodeColor.getRed(), nodeColor.getGreen(), nodeColor.getBlue(), 0.80) : nodeColor;
                        RadialGradient rg = new RadialGradient(0, 0, p[0] - rx * 0.3, p[1] - rz * 0.3, Math.max(rx, rz) * 1.2, false,
                            CycleMethod.NO_CYCLE,
                            new Stop(0, baseNodeCol.brighter()),
                            new Stop(0.75, baseNodeCol),
                            new Stop(1, baseNodeCol.darker().darker()));
                        gc.setFill(rg);
                        // Render anatomical lenticular dome shape (flattened Z height)
                        gc.fillOval(p[0] - rx, p[1] - rz, rx * 2, rz * 2);
                        gc.setStroke(Color.rgb(255, 255, 255, 0.4));
                        gc.setLineWidth(1);
                        gc.strokeOval(p[0] - rx, p[1] - rz, rx * 2, rz * 2);
                    }
                }));
            }
        }

        Collections.sort(items);
        items.forEach(i -> i.draw.run());

        // HUD & Legend
        gc.setFill(Color.rgb(200, 200, 200, 0.85));
        gc.setFont(Font.font("SansSerif", 11));
        gc.fillText(String.format("3D View (Zoom x%.1f) | Drag: Orbit | Scroll: Zoom", zoom), 8, h - 8);
        legendHeader(gc, nest, 8, 18);
    }

    // ── 2D Side View ─────────────────────────────────────────────────────────

    public static void drawSide(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW) {
        drawSide(nest, gc, w, h, tunnelW, 1.0, 0, 0);
    }

    public static void drawSide(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW, double zoom, double panX, double panY) {
        if (nest == null || gc == null || w < 10 || h < 10) return;
        boolean isDark = ThemeManager.getInstance().isDarkMode();
        gc.clearRect(0, 0, w, h);
        gc.setFill(ThemeManager.getInstance().getViewportBackgroundColor());
        gc.fillRect(0, 0, w, h);

        boolean aerial = isAerialOrArboreal(nest.architecture);

        double skyH = 28 * zoom + panY;
        double groundY = Math.max(10, skyH) + 7 * zoom;

        if (aerial) {
            gc.setFill(isDark ? Color.rgb(25, 35, 48) : Color.rgb(224, 242, 254));
            gc.fillRect(0, 0, w, h);

            gc.setStroke(isDark ? Color.rgb(255, 255, 255, 0.08) : Color.rgb(2, 132, 199, 0.12));
            gc.setLineWidth(1);
            for (double y = 30 * zoom; y < h; y += 35 * zoom) {
                gc.strokeLine(0, y, w, y);
            }

            String key = normalizeArchKey(nest.architecture);
            double cx = w / 2 + panX;
            if ("PAPER_PEDUNCULATE".equals(key) || "WAX_COMB_HEXAGONAL".equals(key) || "WAX_POTS_CLUSTER".equals(key)) {
                gc.setFill(isDark ? Color.rgb(85, 55, 30) : Color.rgb(139, 90, 43));
                gc.fillRect(cx - 60 * zoom, Math.max(5, groundY - 15 * zoom), 120 * zoom, 8 * zoom);
            } else if ("HOLLOW_TRUNK_NEST".equals(key) || "CARTON_NEST".equals(key) || "BAMBOO_STEM_NEST".equals(key)) {
                gc.setStroke(isDark ? Color.rgb(100, 70, 40, 0.4) : Color.rgb(160, 110, 60, 0.4));
                gc.setLineWidth(4 * zoom);
                gc.strokeLine(cx - 45 * zoom, 0, cx - 45 * zoom, h);
            } else if ("WOODEN_BEEHIVE".equals(key)) {
                gc.setStroke(isDark ? Color.rgb(120, 80, 40, 0.5) : Color.rgb(180, 120, 60, 0.5));
                gc.setLineWidth(3 * zoom);
                gc.strokeRect(cx - 50 * zoom, groundY + 40 * zoom, 100 * zoom, 30 * zoom);
            }
        } else {
            gc.setFill(isDark ? Color.rgb(30, 42, 58) : Color.rgb(224, 242, 254));
            gc.fillRect(0, 0, w, Math.max(10, skyH));
            gc.setFill(isDark ? Color.rgb(55, 85, 40) : Color.rgb(134, 239, 172));
            gc.fillRect(0, Math.max(10, skyH), w, 7 * zoom);
            gc.setFill(isDark ? Color.rgb(52, 36, 22) : Color.rgb(217, 119, 6));
            gc.fillRect(0, groundY, w, h);

            gc.setStroke(isDark ? Color.rgb(70, 48, 30, 0.35) : Color.rgb(180, 140, 100, 0.45));
            gc.setLineWidth(1);
            for (double y = groundY + 25 * zoom; y < h; y += 35 * zoom) {
                gc.strokeLine(0, y, w, y);
            }
        }

        double maxD = nest.maxDepth;
        double sY = ((h - 55) / Math.max(1, maxD)) * zoom;
        double sX = 8.5 * zoom;
        double cx = w / 2 + panX;

        Color edgeColor = CHAMBER_COLOR_TUNNEL;
        gc.setStroke(edgeColor);
        gc.setLineWidth(tunnelW * 2.1 * zoom);
        if (nest.edges != null) {
            for (NestGeneratorPane.NestEdge e : nest.edges) {
                if (e == null || e.pts == null || e.pts.isEmpty()) continue;
                gc.beginPath();
                for (int i = 0; i < e.pts.size(); i++) {
                    double[] pt = e.pts.get(i);
                    if (pt == null || pt.length < 3) continue;
                    double px = cx + pt[0] * sX, py = groundY + pt[2] * sY;
                    if (i == 0) gc.moveTo(px, py); else gc.lineTo(px, py);
                }
                gc.stroke();
            }
        }

        if (nest.nodes != null) {
            for (NestGeneratorPane.NestNode n : nest.nodes) {
                if (n == null) continue;
                Color nodeColor = n.color != null ? n.color : Color.GRAY;
                double nx = cx + n.x * sX, ny = groundY + n.z * sY;
                double rx = Math.max(3.0, (n.rx > 0 ? n.rx : n.radius) * sX * 1.2);
                double rz = Math.max(2.0, (n.rz > 0 ? n.rz : n.radius * 0.55) * sY * 1.2);
                if ("ENTRANCE".equals(n.type)) {
                    gc.setFill(Color.LIMEGREEN);
                    gc.fillOval(nx - 7 * zoom, groundY - 5 * zoom, 14 * zoom, 10 * zoom);
                } else if (!"JUNCTION".equals(n.type)) {
                    gc.setFill(nodeColor.darker());
                    gc.fillOval(nx - rx, ny - rz, rx * 2, rz * 2);
                    gc.setStroke(nodeColor);
                    gc.setLineWidth(1.5 * zoom);
                    gc.strokeOval(nx - rx, ny - rz, rx * 2, rz * 2);
                }
            }
        }

        gc.setFill(ThemeManager.getInstance().getViewportTextColor());
        gc.setFont(Font.font("SansSerif", 10));
        gc.fillText(String.format("Side View (x%.1f) - Scroll: Zoom | Drag: Pan", zoom), 4, 12);
    }

    // ── 2D Top View ──────────────────────────────────────────────────────────

    public static void drawTop(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW) {
        drawTop(nest, gc, w, h, tunnelW, 1.0, 0, 0);
    }

    public static void drawTop(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW, double zoom, double panX, double panY) {
        if (nest == null || gc == null || w < 10 || h < 10) return;
        boolean isDark = ThemeManager.getInstance().isDarkMode();
        gc.clearRect(0, 0, w, h);
        gc.setFill(ThemeManager.getInstance().getViewportBackgroundColor());
        gc.fillRect(0, 0, w, h);

        double cx = w / 2 + panX, cy = h / 2 + panY, sc = 8.0 * zoom;

        gc.setStroke(isDark ? Color.rgb(50, 50, 75, 0.4) : Color.rgb(180, 190, 210, 0.5));
        gc.setLineWidth(1);
        for (double r = 35 * zoom; r < Math.max(w, h); r += 35 * zoom) {
            gc.strokeOval(cx - r, cy - r, r * 2, r * 2);
        }

        Color edgeColor = CHAMBER_COLOR_TUNNEL;
        gc.setStroke(edgeColor);
        gc.setLineWidth(tunnelW * 2.0 * zoom);
        if (nest.edges != null) {
            for (NestGeneratorPane.NestEdge e : nest.edges) {
                if (e == null || e.pts == null || e.pts.isEmpty()) continue;
                gc.beginPath();
                for (int i = 0; i < e.pts.size(); i++) {
                    double[] pt = e.pts.get(i);
                    if (pt == null || pt.length < 2) continue;
                    double px = cx + pt[0] * sc, py = cy + pt[1] * sc;
                    if (i == 0) gc.moveTo(px, py); else gc.lineTo(px, py);
                }
                gc.stroke();
            }
        }

        if (nest.nodes != null) {
            for (NestGeneratorPane.NestNode n : nest.nodes) {
                if (n == null) continue;
                Color nodeColor = n.color != null ? n.color : Color.GRAY;
                double nx = cx + n.x * sc, ny = cy + n.y * sc;
                double r = Math.max(3.0, (n.rx > 0 ? n.rx : n.radius) * sc * 1.2);
                double depthRatio = n.z / Math.max(1, nest.maxDepth);
                Color ring = Color.hsb(200 - depthRatio * 150, 0.8, 0.9);
                gc.setFill(nodeColor);
                gc.fillOval(nx - r, ny - r, r * 2, r * 2);
                gc.setStroke(ring);
                gc.setLineWidth(1.5 * zoom);
                gc.strokeOval(nx - r, ny - r, r * 2, r * 2);
            }
        }

        gc.setFill(ThemeManager.getInstance().getViewportTextColor());
        gc.setFont(Font.font("SansSerif", 10));
        gc.fillText(String.format("Top View (x%.1f) - Scroll: Zoom | Drag: Pan", zoom), 4, 12);
    }

    // ── Shared Legend & Color Key ─────────────────────────────────────────────

    public static String normalizeArchKey(String arch) {
        if (arch == null) return "BURROW_UNDERGROUND";
        String s = arch.toUpperCase();
        if (s.contains("CARTON")) return "CARTON_NEST";
        if (s.contains("HEXAGONAL") || s.contains("WAX_COMB") || s.contains("RAYON") || s.contains("ALVÉOLE") || s.contains("ALVEOLE")) return "WAX_COMB_HEXAGONAL";
        if (s.contains("POTS") || s.contains("WAX_POTS") || s.contains("POT")) return "WAX_POTS_CLUSTER";
        if (s.contains("PAPER") || s.contains("PEDUNCULATE") || s.contains("PAPIER") || s.contains("PÉDONCULÉ") || s.contains("PEDONCULE") || s.contains("SUSPENDU")) return "PAPER_PEDUNCULATE";
        if (s.contains("CATHEDRAL") || s.contains("CATHÉDRALE") || s.contains("CATHEDRALE")) return "CATHEDRAL_MOUND";
        if (s.contains("SILK") || s.contains("SOIE") || s.contains("FEUILLE") || s.contains("TISSERANDE")) return "ARBOREAL_SILK_LEAF";
        if (s.contains("FUNGI") || s.contains("VAULT") || s.contains("CHAMPIGNON") || s.contains("CHAMPIGNONNIERE") || s.contains("CHAMPIGNONNIÈRE")) return "SUBTERRANEAN_FUNGI_VAULT";
        if (s.contains("BAMBOO") || s.contains("BAMBOU") || s.contains("STEM") || s.contains("TIGE") || s.contains("GALL") || s.contains("GALLE") || s.contains("GLAND")) return "BAMBOO_STEM_NEST";
        if (s.contains("BIVOUAC") || s.contains("VIVANT") || s.contains("LIVING") || s.contains("LÉGIONNAIRE") || s.contains("LEGIONNAIRE") || s.contains("ECITON")) return "BIVOUAC_LIVING_NEST";
        if (s.contains("TRUNK") || s.contains("TRONC") || s.contains("HOLLOW") || s.contains("CREUX") || s.contains("BOIS")) return "HOLLOW_TRUNK_NEST";
        if (s.contains("SURFACE") || s.contains("DÔME") || s.contains("DOME") || s.contains("MOUND") || s.contains("TÉTRÔME") || s.contains("AIGUILLE")) return "SURFACE_MOUND";
        if (s.contains("BEEHIVE") || s.contains("RUCHE") || s.contains("WOODEN")) return "WOODEN_BEEHIVE";
        if (s.contains("SOUTERRAIN") || s.contains("BURROW") || s.contains("TERRIER") || s.contains("SIMPLE")) return "BURROW_UNDERGROUND";
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
        if (s.contains("BEESWAX") || s.contains("CIRE")) return "BEESWAX";
        if (s.contains("LIVING") || s.contains("BODIES") || s.contains("BIVOUAC") || s.contains("CORPS") || s.contains("INSECTES")) return "LIVING_INSECT_BODIES";
        if (s.contains("PROPOLIS") || s.contains("RÉSINE") || s.contains("RESINE") || s.contains("RESIN")) return "PROPOLIS";
        if (s.contains("SILK") || s.contains("SOIE")) return "SILK_WEAVE";
        if (s.contains("STERCORAL") || s.contains("CEMENT") || s.contains("CIMENT") || s.contains("FECES")) return "STERCORAL_CEMENT";
        if (s.contains("BRANCH") || s.contains("BRANCHE") || s.contains("ECORCE") || s.contains("ÉCORCE") || s.contains("BARK")) return "TREE_BRANCH";
        if (s.contains("LEAF") || s.contains("FEUILLE")) return "TREE_LEAF";
        if (s.contains("TRUNK") || s.contains("TRONC") || s.contains("BOIS_CREUX")) return "TREE_TRUNK";
        if (s.contains("PLANK") || s.contains("PLANCHE")) return "WOOD_PLANK";
        if (s.contains("PAPER") || s.contains("PAPIER") || s.contains("WOOD_PULP_PAPER")) return "WOOD_PULP_PAPER";
        if (s.contains("EARTH") || s.contains("SOIL") || s.contains("CLAY") || s.contains("TERRE") || s.contains("ARGILE") || s.contains("SOL")) return "EARTH";
        return "EARTH";
    }

    public static String formatArchitectureName(String arch) {
        String key = normalizeArchKey(arch);
        I18nManager i18n = I18nManager.getInstance();
        return switch (key) {
            case "CARTON_NEST" -> i18n.get("nest.arch.arboreal_carton", "Arboreal Carton Nest");
            case "WAX_COMB_HEXAGONAL" -> i18n.get("nest.arch.wax_comb", "Hexagonal Wax Comb");
            case "WAX_POTS_CLUSTER" -> i18n.get("nest.arch.wax_pots", "Wax Pots Cluster");
            case "PAPER_PEDUNCULATE" -> i18n.get("nest.arch.hanging_paper", "Hanging Paper Nest");
            case "CATHEDRAL_MOUND" -> i18n.get("nest.arch.cathedral", "Cathedral Mound");
            case "ARBOREAL_SILK_LEAF" -> i18n.get("nest.arch.arboreal_silk", "Arboreal Silk Leaf");
            case "SUBTERRANEAN_FUNGI_VAULT" -> i18n.get("nest.arch.fungi_vault", "Subterranean Fungi Vault");
            case "BAMBOO_STEM_NEST" -> i18n.get("nest.arch.bamboo", "Bamboo Stem & Gall");
            case "BIVOUAC_LIVING_NEST" -> i18n.get("nest.arch.bivouac", "Bivouac Living Nest");
            case "HOLLOW_TRUNK_NEST" -> i18n.get("nest.arch.hollow_trunk", "Hollow Trunk Cavity");
            case "SURFACE_MOUND" -> i18n.get("nest.arch.surface_dome", "Surface Dome Mound");
            case "WOODEN_BEEHIVE" -> i18n.get("nest.arch.wooden_beehive", "Wooden Beehive");
            default -> i18n.get("nest.arch.subterranean", "Subterranean Burrow");
        };
    }

    public static String formatMaterialName(String mat) {
        String key = normalizeMatKey(mat);
        I18nManager i18n = I18nManager.getInstance();
        return switch (key) {
            case "CARTON_PULP" -> i18n.get("nest.mat.carton", "Carton & Wood Pulp");
            case "BEESWAX" -> i18n.get("nest.mat.beeswax", "Beeswax (Apidae)");
            case "LIVING_INSECT_BODIES" -> i18n.get("nest.mat.bivouac", "Living Insect Bodies (Bivouac)");
            case "PROPOLIS" -> i18n.get("nest.mat.propolis", "Propolis & Tree Resin");
            case "SILK_WEAVE" -> i18n.get("nest.mat.silk", "Silk Weave (Oecophylla Larvae)");
            case "STERCORAL_CEMENT" -> i18n.get("nest.mat.stercoral", "Stercoral Cement (Termite Feces/Mud)");
            case "TREE_BRANCH" -> i18n.get("nest.mat.bark", "Tree Branch & Bark");
            case "TREE_LEAF" -> i18n.get("nest.mat.leaf", "Tree Leaf Tissue");
            case "TREE_TRUNK" -> i18n.get("nest.mat.hollow_wood", "Tree Trunk & Hollow Wood");
            case "WOOD_PLANK" -> i18n.get("nest.mat.planks", "Wood Plank Construction");
            case "WOOD_PULP_PAPER" -> i18n.get("nest.mat.paper", "Wood Pulp Paper (Vespidae)");
            default -> i18n.get("nest.mat.earth", "Earth & Clay Soil");
        };
    }

    private static void legendHeader(GraphicsContext gc, NestGeneratorPane.GeneratedNest nest,
            double x, double y) {
        long chambers = nest.nodes.stream()
            .filter(n -> !"JUNCTION".equals(n.type) && !"ENTRANCE".equals(n.type)).count();
        gc.setFill(ThemeManager.getInstance().getViewportSubtextColor());
        gc.setFont(Font.font("SansSerif", 11));
        gc.fillText(String.format("Arch: %s | Mat: %s | Scale: %.1fmm | Chambers: %d",
            formatArchitectureName(nest.architecture), formatMaterialName(nest.material), nest.workerSizeMm, chambers), x, y);
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

        record LegendItem(String label, Color color) {}
        LegendItem[] items = {
            new LegendItem("Entrance", CHAMBER_COLOR_ENTRANCE),
            new LegendItem("Queen Chamber", CHAMBER_COLOR_QUEEN),
            new LegendItem("Brood Nursery", CHAMBER_COLOR_BROOD),
            new LegendItem("Food Storage", CHAMBER_COLOR_STORAGE),
            new LegendItem("Fungus / Pollen", CHAMBER_COLOR_FUNGUS),
            new LegendItem("Waste / Cemetery", CHAMBER_COLOR_WASTE),
            new LegendItem("Tunnels & Galleries", CHAMBER_COLOR_TUNNEL)
        };

        double ly = y + 32;
        gc.setFont(Font.font("SansSerif", 10));
        for (LegendItem it : items) {
            gc.setFill(it.color());
            gc.fillOval(x + 10, ly - 7, 9, 9);
            gc.setStroke(isDark ? Color.WHITE : Color.rgb(100, 116, 139));
            gc.setLineWidth(0.5);
            gc.strokeOval(x + 10, ly - 7, 9, 9);

            gc.setFill(ThemeManager.getInstance().getViewportTextColor());
            gc.fillText(it.label(), x + 24, ly);
            ly += 15;
        }
    }
}
