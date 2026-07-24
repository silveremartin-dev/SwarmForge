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
        draw3D(nest, gc, w, h, azimuth, elevation, zoom, tunnelW, 0, 0);
    }

    public static void draw3D(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double azimuth, double elevation, double zoom, double tunnelW, double panX, double panY) {

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

        // Ground grid
        gc.setStroke(Color.rgb(40,70,50,0.45)); gc.setLineWidth(0.8);
        for (double g = -20; g <= 20; g += 5) {
            double[] a = proj.apply(new double[]{g,-20,0}), b = proj.apply(new double[]{g,20,0});
            double[] c2= proj.apply(new double[]{-20,g,0}), d = proj.apply(new double[]{20,g,0});
            gc.strokeLine(a[0],a[1],b[0],b[1]); gc.strokeLine(c2[0],c2[1],d[0],d[1]);
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
        drawColorKeyLegend(gc, w - 175, 12);
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

        double skyH = 28 * zoom;
        gc.setFill(Color.rgb(30,42,58)); gc.fillRect(0,0,w,Math.max(10, skyH));
        gc.setFill(Color.rgb(55,85,40)); gc.fillRect(0,Math.max(10, skyH),w,7*zoom);
        gc.setFill(Color.rgb(52,36,22)); gc.fillRect(0,Math.max(10, skyH)+7*zoom,w,h);

        gc.setStroke(Color.rgb(70,48,30,0.35)); gc.setLineWidth(1);
        for (double y=60*zoom; y<h; y+=35*zoom) gc.strokeLine(0,y,w,y);

        double maxD = nest.maxDepth;
        double sY = ((h-55)/Math.max(1,maxD)) * zoom;
        double sX = 8.5 * zoom;
        double cx = w/2 + panX;
        double groundY = 35*zoom + panY;

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

