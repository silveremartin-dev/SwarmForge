/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.simulation.TunnelNetwork.ChamberType;
import org.swarmforge.core.simulation.TunnelNetwork.TunnelEdge;
import org.swarmforge.core.simulation.TunnelNetwork.TunnelNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Carves 3D lenticular chambers and connecting sinuous tunnels directly into the physical Terrarium voxel grid.
 */
public final class TunnelVoxelCarver {

    private TunnelVoxelCarver() {}

    public static void carveIntoTerrarium(
            Terrarium terrarium,
            Map<UUID, TunnelNode> nodes,
            List<TunnelEdge> edges,
            float tunnelWidthSetting,
            float scaleFactorSetting
    ) {
        if (terrarium == null || nodes == null) return;

        int tunnelRadius = Math.max(1, Math.round(tunnelWidthSetting / 2.0f));

        // 1. Carve node chambers as 3D lenticular ellipsoids
        for (TunnelNode node : nodes.values()) {
            int cx = Math.round(node.x());
            int cy = Math.round(node.y());
            int cz = Math.round(node.z());

            float rx = Math.max(0.6f, node.radiusX() * scaleFactorSetting);
            float ry = Math.max(0.6f, node.radiusY() * scaleFactorSetting);
            float rz = Math.max(0.5f, node.radiusZ() * scaleFactorSetting);

            int irx = (int) Math.ceil(rx);
            int iry = (int) Math.ceil(ry);
            int irz = (int) Math.ceil(rz);

            float surfElev = terrarium.getSurfaceElevation(cx, cy);
            TerrariumCell.Material mat = (node.type() == ChamberType.ENTRANCE || node.z() >= surfElev)
                    ? TerrariumCell.Material.AIR
                    : TerrariumCell.Material.CHAMBER;

            for (int dx = -irx; dx <= irx; dx++) {
                for (int dy = -iry; dy <= iry; dy++) {
                    for (int dz = -irz; dz <= irz; dz++) {
                        float normDist = (dx * dx) / (rx * rx) + (dy * dy) / (ry * ry) + (dz * dz) / (rz * rz);
                        if (normDist <= 1.05f) {
                            int px = cx + dx;
                            int py = cy + dy;
                            int pz = cz + dz;
                            if (terrarium.inBounds(px, py, pz)) {
                                var existing = terrarium.getCell(px, py, pz);
                                if (existing == null || existing.material() != TerrariumCell.Material.ROCK) {
                                    terrarium.setCell(new TerrariumCell(px, py, pz, mat,
                                            new float[TerrariumCell.PHEROMONE_TYPES], 20.0f, 60.0f));
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Carve connecting tunnel edges along curved pathPoints
        if (edges == null) return;
        for (TunnelEdge edge : edges) {
            List<float[]> pts = (edge.pathPoints() != null && !edge.pathPoints().isEmpty())
                    ? edge.pathPoints()
                    : defaultSegmentPoints(nodes.get(edge.fromNode()), nodes.get(edge.toNode()));

            for (int i = 0; i < pts.size() - 1; i++) {
                float[] pA = pts.get(i);
                float[] pB = pts.get(i + 1);
                carveSegment(terrarium, pA[0], pA[1], pA[2], pB[0], pB[1], pB[2], tunnelRadius);
            }
        }
    }

    private static List<float[]> defaultSegmentPoints(TunnelNode n1, TunnelNode n2) {
        List<float[]> res = new ArrayList<>();
        if (n1 != null) res.add(new float[]{n1.x(), n1.y(), n1.z()});
        if (n2 != null) res.add(new float[]{n2.x(), n2.y(), n2.z()});
        return res;
    }

    private static void carveSegment(
            Terrarium terrarium,
            float x1, float y1, float z1, float x2, float y2, float z2, int tunnelRadius
    ) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float dist = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = Math.max(1, (int) (dist * 2.5f));

        for (int step = 0; step <= steps; step++) {
            float t = (float) step / steps;
            int tx = Math.round(x1 + dx * t);
            int ty = Math.round(y1 + dy * t);
            int tz = Math.round(z1 + dz * t);

            for (int rx = tx - tunnelRadius; rx <= tx + tunnelRadius; rx++) {
                for (int ry = ty - tunnelRadius; ry <= ty + tunnelRadius; ry++) {
                    for (int rz = tz - tunnelRadius; rz <= tz + tunnelRadius; rz++) {
                        if ((rx - tx) * (rx - tx) + (ry - ty) * (ry - ty) + (rz - tz) * (rz - tz) <= tunnelRadius * tunnelRadius + 1) {
                            if (terrarium.inBounds(rx, ry, rz)) {
                                var existing = terrarium.getCell(rx, ry, rz);
                                if (existing == null || existing.material() == TerrariumCell.Material.EARTH
                                        || existing.material() == TerrariumCell.Material.ROCK) {
                                    terrarium.setCell(new TerrariumCell(rx, ry, rz,
                                            TerrariumCell.Material.AIR,
                                            new float[TerrariumCell.PHEROMONE_TYPES], 20.0f, 60.0f));
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
