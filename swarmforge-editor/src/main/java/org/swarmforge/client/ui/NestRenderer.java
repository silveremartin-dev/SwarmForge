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

/** Static rendering methods for NestGeneratorPane views. */
public final class NestRenderer {

    private NestRenderer() {}

    // ── 3D isometric view ────────────────────────────────────────────────────

    public static void draw3D(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double azimuth, double elevation, double zoom, double tunnelW) {

        if (nest == null) return;
        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.rgb(12, 14, 25)); gc.fillRect(0, 0, w, h);

        double az = Math.toRadians(azimuth), el = Math.toRadians(elevation);
        double cAz = Math.cos(az), sAz = Math.sin(az);
        double cEl = Math.cos(el), sEl = Math.sin(el);
        double cx = w/2, cy = h/2 - 15;

        // project: world (x,y,z) -> screen (sx,sy,depth)
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

        // ground grid
        gc.setStroke(Color.rgb(40,70,50,0.45)); gc.setLineWidth(0.8);
        for (double g = -20; g <= 20; g += 5) {
            double[] a = proj.apply(new double[]{g,-20,0}), b = proj.apply(new double[]{g,20,0});
            double[] c2= proj.apply(new double[]{-20,g,0}), d = proj.apply(new double[]{20,g,0});
            gc.strokeLine(a[0],a[1],b[0],b[1]); gc.strokeLine(c2[0],c2[1],d[0],d[1]);
        }

        // depth-sort items
        class Item implements Comparable<Item> {
            double depth; Runnable draw;
            Item(double d, Runnable r) { depth=d; draw=r; }
            public int compareTo(Item o) { return Double.compare(depth,o.depth); }
        }
        List<Item> items = new ArrayList<>();

        // edges
        double tw = tunnelW*2.0;
        for (NestGeneratorPane.NestEdge edge : nest.edges) {
            List<double[]> sp = new ArrayList<>();
            double avg = 0;
            for (double[] wp : edge.pts) { double[] p=proj.apply(wp); sp.add(p); avg+=p[2]; }
            avg /= sp.size();
            final List<double[]> fsp = sp; final double fa = avg;
            items.add(new Item(fa-1000, () -> {
                gc.setStroke(Color.rgb(155,110,70,0.9)); gc.setLineWidth(tw);
                gc.beginPath();
                for (int i=0;i<fsp.size();i++) {
                    double[] p=fsp.get(i);
                    if(i==0) gc.moveTo(p[0],p[1]); else gc.lineTo(p[0],p[1]);
                }
                gc.stroke();
            }));
        }

        // nodes
        for (NestGeneratorPane.NestNode n : nest.nodes) {
            double[] p = proj.apply(new double[]{n.x, n.y, n.z});
            double r = n.radius*2.5, depth = p[2];
            items.add(new Item(depth, () -> {
                if ("ENTRANCE".equals(n.type)) {
                    gc.setFill(Color.LIMEGREEN);
                    gc.fillOval(p[0]-6, p[1]-4, 12, 8);
                } else if (!"JUNCTION".equals(n.type)) {
                    RadialGradient rg = new RadialGradient(0,0,p[0]-r*0.3,p[1]-r*0.3,r*1.2,false,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, n.color.brighter()),
                        new Stop(0.7, n.color),
                        new Stop(1, n.color.darker().darker()));
                    gc.setFill(rg);
                    gc.fillOval(p[0]-r, p[1]-r, r*2, r*2);
                    gc.setStroke(Color.rgb(255,255,255,0.35)); gc.setLineWidth(1);
                    gc.strokeOval(p[0]-r, p[1]-r, r*2, r*2);
                }
            }));
        }

        Collections.sort(items);
        items.forEach(i -> i.draw.run());

        // HUD
        gc.setFill(Color.rgb(200,200,200,0.8));
        gc.setFont(Font.font("SansSerif",11));
        gc.fillText("Drag: orbit  |  Scroll: zoom", 8, h-8);
        legend(gc, nest, 8, 18);
    }

    // ── 2D side view ─────────────────────────────────────────────────────────

    public static void drawSide(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW) {
        if (nest == null) return;
        gc.clearRect(0,0,w,h);
        gc.setFill(Color.rgb(18,18,30)); gc.fillRect(0,0,w,h);

        // sky + grass + soil
        gc.setFill(Color.rgb(30,42,58)); gc.fillRect(0,0,w,28);
        gc.setFill(Color.rgb(55,85,40)); gc.fillRect(0,28,w,7);
        gc.setFill(Color.rgb(52,36,22)); gc.fillRect(0,35,w,h-35);

        // soil lines
        gc.setStroke(Color.rgb(70,48,30,0.35)); gc.setLineWidth(1);
        for (double y=60;y<h;y+=35) gc.strokeLine(0,y,w,y);

        double maxD = nest.maxDepth;
        double sY = (h-55)/Math.max(1,maxD), sX = 8.5, cx = w/2;

        // edges
        gc.setStroke(Color.rgb(130,90,55)); gc.setLineWidth(tunnelW*2.1);
        for (NestGeneratorPane.NestEdge e : nest.edges) {
            gc.beginPath();
            for (int i=0;i<e.pts.size();i++) {
                double[] pt=e.pts.get(i);
                double px=cx+pt[0]*sX, py=35+pt[2]*sY;
                if(i==0) gc.moveTo(px,py); else gc.lineTo(px,py);
            }
            gc.stroke();
        }

        // nodes
        for (NestGeneratorPane.NestNode n : nest.nodes) {
            double nx=cx+n.x*sX, ny=35+n.z*sY, r=n.radius*2.6;
            if ("ENTRANCE".equals(n.type)) { gc.setFill(Color.DARKGREEN); gc.fillOval(nx-7,28,14,11); }
            else if (!"JUNCTION".equals(n.type)) {
                gc.setFill(n.color.darker()); gc.fillOval(nx-r, ny-r*0.6, r*2, r*1.2);
                gc.setStroke(n.color); gc.setLineWidth(1.5);
                gc.strokeOval(nx-r, ny-r*0.6, r*2, r*1.2);
            }
        }

        gc.setFill(Color.rgb(200,200,200)); gc.setFont(Font.font("SansSerif",10));
        gc.fillText("Side View", 4, 12);
    }

    // ── 2D top view ──────────────────────────────────────────────────────────

    public static void drawTop(NestGeneratorPane.GeneratedNest nest, GraphicsContext gc,
            double w, double h, double tunnelW) {
        if (nest == null) return;
        gc.clearRect(0,0,w,h);
        gc.setFill(Color.rgb(18,20,32)); gc.fillRect(0,0,w,h);

        double cx=w/2, cy=h/2, sc=8.0;

        // depth rings
        gc.setStroke(Color.rgb(50,50,75,0.4)); gc.setLineWidth(1);
        for (double r=35;r<w/2;r+=35) gc.strokeOval(cx-r,cy-r,r*2,r*2);

        // edges
        gc.setStroke(Color.rgb(125,95,60)); gc.setLineWidth(tunnelW*2.0);
        for (NestGeneratorPane.NestEdge e : nest.edges) {
            gc.beginPath();
            for (int i=0;i<e.pts.size();i++) {
                double[] pt=e.pts.get(i);
                double px=cx+pt[0]*sc, py=cy+pt[1]*sc;
                if(i==0) gc.moveTo(px,py); else gc.lineTo(px,py);
            }
            gc.stroke();
        }

        // nodes
        for (NestGeneratorPane.NestNode n : nest.nodes) {
            double nx=cx+n.x*sc, ny=cy+n.y*sc, r=n.radius*2.4;
            double depthRatio = n.z/Math.max(1,nest.maxDepth);
            Color ring = Color.hsb(200-depthRatio*150, 0.8, 0.9);
            gc.setFill(n.color); gc.fillOval(nx-r,ny-r,r*2,r*2);
            gc.setStroke(ring); gc.setLineWidth(1.5);
            gc.strokeOval(nx-r,ny-r,r*2,r*2);
        }

        gc.setFill(Color.rgb(200,200,200)); gc.setFont(Font.font("SansSerif",10));
        gc.fillText("Top View", 4, 12);
    }

    // ── shared legend ─────────────────────────────────────────────────────────

    private static void legend(GraphicsContext gc, NestGeneratorPane.GeneratedNest nest,
            double x, double y) {
        long chambers = nest.nodes.stream()
            .filter(n -> !"JUNCTION".equals(n.type) && !"ENTRANCE".equals(n.type)).count();
        gc.setFill(Color.rgb(180,180,180,0.85)); gc.setFont(Font.font("SansSerif",11));
        gc.fillText(String.format("Depth: %.0f blk | Chambers: %d | Nodes: %d",
            nest.maxDepth, chambers, nest.nodes.size()), x, y);
    }
}
