/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.swarmforge.core.simulation.TunnelNetwork;
import org.swarmforge.core.structure.Chamber;
import org.swarmforge.core.structure.Nest;

import java.util.List;
import java.util.Random;

/**
 * Calculates subterranean chamber or surface entrance spawn positions for individual castes.
 */
public final class ColonySpawnPositionLocator {

    private ColonySpawnPositionLocator() {
    }

    public static float[] computeInitialPositionForCaste(
            Colony colony,
            Individual.Caste caste,
            int index,
            int dayOfYear,
            double hourOfDay,
            float ambientTemp,
            double surfaceActivityRatio
    ) {
        float nestX = colony.getNestX();
        float nestY = colony.getNestY();
        float nestZ = colony.getNestZ();
        float mapWidth = colony.getMapWidth();
        float mapHeight = colony.getMapHeight();

        Nest nest = colony.getNest();
        TunnelNetwork tunnelNetwork = colony.getTunnelNetwork();
        Random rng = colony.getRandom();

        float sx = nestX;
        float sy = nestY;
        float sz = nestZ;

        List<Chamber> nestChambers = (nest != null && nest.getChambers() != null) ? nest.getChambers() : List.of();
        List<Chamber> entranceChambers = (nest != null) ? nest.getChambersOfType(Chamber.Type.ENTRANCE) : List.of();
        List<Chamber> nurseryChambers = (nest != null) ? nest.getChambersOfType(Chamber.Type.NURSERY) : List.of();
        List<TunnelNetwork.TunnelNode> simNodes = (tunnelNetwork != null && tunnelNetwork.getNodes() != null) ? tunnelNetwork.getNodes() : List.of();

        if (caste == Individual.Caste.QUEEN) {
            var qChamber = tunnelNetwork != null ? tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.QUEEN_CHAMBER, nestX, nestY, nestZ) : null;
            if (qChamber != null) {
                sx = qChamber.x(); sy = qChamber.y(); sz = qChamber.z();
            } else if (nest != null) {
                List<Chamber> queenChambers = nest.getChambersOfType(Chamber.Type.QUEEN_QUARTERS);
                if (!queenChambers.isEmpty()) {
                    var ch = queenChambers.get(index % queenChambers.size());
                    sx = (float) ch.getX(); sy = (float) ch.getY(); sz = (float) ch.getZ();
                } else if (!nestChambers.isEmpty()) {
                    var ch = nestChambers.get(index % nestChambers.size());
                    sx = (float) ch.getX(); sy = (float) ch.getY(); sz = (float) ch.getZ();
                } else if (!simNodes.isEmpty()) {
                    var node = simNodes.stream()
                            .filter(n -> n.type() != TunnelNetwork.ChamberType.ENTRANCE)
                            .findFirst().orElse(simNodes.get(0));
                    sx = node.x(); sy = node.y(); sz = node.z();
                } else {
                    sz = colony.getDynamicQueenChamberDepth();
                }
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.stream()
                        .filter(n -> n.type() != TunnelNetwork.ChamberType.ENTRANCE)
                        .findFirst().orElse(simNodes.get(0));
                sx = node.x(); sy = node.y(); sz = node.z();
            } else {
                sz = colony.getDynamicQueenChamberDepth();
            }

            float rAngle = rng.nextFloat(0f, (float) (Math.PI * 2));
            float rDist = rng.nextFloat(0.05f, 0.2f);
            sx += (float) Math.cos(rAngle) * rDist;
            sy += (float) Math.sin(rAngle) * rDist;

        } else if (caste == Individual.Caste.NURSE) {
            if (!nurseryChambers.isEmpty()) {
                var ch = nurseryChambers.get(index % nurseryChambers.size());
                float maxR = Math.min(1.2f, Math.max(0.3f, (float) Math.cbrt(ch.getCapacity()) * 0.3f));
                double rAngle = rng.nextDouble(0, Math.PI * 2);
                double rDist = rng.nextDouble(0.05, maxR * 0.7);
                sx = (float) (ch.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (ch.getY() + Math.sin(rAngle) * rDist);
                sz = (float) ch.getZ();
            } else if (tunnelNetwork != null && tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.BROOD_CHAMBER, nestX, nestY, nestZ) != null) {
                var ch = tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.BROOD_CHAMBER, nestX, nestY, nestZ);
                float maxR = Math.min(1.0f, Math.max(0.2f, ch.radiusX() * 0.7f));
                double rAngle = rng.nextDouble(0, Math.PI * 2);
                double rDist = rng.nextDouble(0.05, maxR);
                sx = (float) (ch.x() + Math.cos(rAngle) * rDist);
                sy = (float) (ch.y() + Math.sin(rAngle) * rDist);
                sz = ch.z();
            } else if (!nestChambers.isEmpty()) {
                var ch = nestChambers.get(index % nestChambers.size());
                sx = (float) ch.getX(); sy = (float) ch.getY(); sz = (float) ch.getZ();
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.get(index % simNodes.size());
                float maxR = Math.min(1.0f, Math.max(0.2f, node.radiusX() * 0.7f));
                double rAngle = rng.nextDouble(0, Math.PI * 2);
                double rDist = rng.nextDouble(0.05, maxR);
                sx = (float) (node.x() + Math.cos(rAngle) * rDist);
                sy = (float) (node.y() + Math.sin(rAngle) * rDist);
                sz = node.z();
            } else {
                sz = nestZ - 3.0f;
            }

        } else if (caste == Individual.Caste.SOLDIER) {
            boolean spawnSurfaceGuard = rng.nextDouble() < 0.35;
            if (spawnSurfaceGuard) {
                float refX = nestX;
                float refY = nestY;
                if (!entranceChambers.isEmpty()) {
                    var ent = entranceChambers.get(index % entranceChambers.size());
                    refX = (float) ent.getX();
                    refY = (float) ent.getY();
                } else if (tunnelNetwork != null && tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ) != null) {
                    var ent = tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ);
                    refX = ent.x();
                    refY = ent.y();
                }
                float rAngle = rng.nextFloat(0f, (float) (Math.PI * 2));
                float rDist = rng.nextFloat(0.2f, 1.5f);
                sx = refX + (float) Math.cos(rAngle) * rDist;
                sy = refY + (float) Math.sin(rAngle) * rDist;
                sz = colony.getSurfaceZAt(sx, sy);
            } else if (!nestChambers.isEmpty()) {
                var ch = nestChambers.get(index % nestChambers.size());
                float maxR = Math.min(1.2f, Math.max(0.3f, (float) Math.cbrt(ch.getCapacity()) * 0.3f));
                double rAngle = rng.nextDouble(0, Math.PI * 2);
                double rDist = rng.nextDouble(0.05, maxR * 0.7);
                sx = (float) (ch.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (ch.getY() + Math.sin(rAngle) * rDist);
                sz = (float) ch.getZ();
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.get(index % simNodes.size());
                float maxR = Math.min(1.0f, Math.max(0.2f, node.radiusX() * 0.7f));
                double rAngle = rng.nextDouble(0, Math.PI * 2);
                double rDist = rng.nextDouble(0.05, maxR);
                sx = (float) (node.x() + Math.cos(rAngle) * rDist);
                sy = (float) (node.y() + Math.sin(rAngle) * rDist);
                sz = node.z();
            } else {
                sz = nestZ - 1.0f;
            }

        } else if (caste == Individual.Caste.WORKER) {
            boolean spawnSurface = rng.nextDouble() < surfaceActivityRatio;
            if (spawnSurface) {
                float refX = nestX;
                float refY = nestY;
                if (!entranceChambers.isEmpty()) {
                    var ent = entranceChambers.get(index % entranceChambers.size());
                    refX = (float) ent.getX();
                    refY = (float) ent.getY();
                } else if (tunnelNetwork != null && tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ) != null) {
                    var ent = tunnelNetwork.getNearestChamber(TunnelNetwork.ChamberType.ENTRANCE, nestX, nestY, nestZ);
                    refX = ent.x();
                    refY = ent.y();
                }
                float rAngle = rng.nextFloat(0f, (float) (Math.PI * 2));
                float rDist = rng.nextFloat(0.3f, 2.5f);
                sx = refX + (float) Math.cos(rAngle) * rDist;
                sy = refY + (float) Math.sin(rAngle) * rDist;
                sz = colony.getSurfaceZAt(sx, sy);
            } else if (!nestChambers.isEmpty()) {
                var ch = nestChambers.get(index % nestChambers.size());
                float maxR = Math.min(1.2f, Math.max(0.3f, (float) Math.cbrt(ch.getCapacity()) * 0.3f));
                double rAngle = rng.nextDouble(0, Math.PI * 2);
                double rDist = rng.nextDouble(0.05, maxR * 0.7);
                sx = (float) (ch.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (ch.getY() + Math.sin(rAngle) * rDist);
                sz = (float) ch.getZ();
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.get(index % simNodes.size());
                float maxR = Math.min(1.0f, Math.max(0.2f, node.radiusX() * 0.7f));
                double rAngle = rng.nextDouble(0, Math.PI * 2);
                double rDist = rng.nextDouble(0.05, maxR);
                sx = (float) (node.x() + Math.cos(rAngle) * rDist);
                sy = (float) (node.y() + Math.sin(rAngle) * rDist);
                sz = node.z();
            } else {
                sz = nestZ - 2.0f;
            }
        } else if (caste == Individual.Caste.MALE) {
            if (!nestChambers.isEmpty()) {
                var ch = nestChambers.get(index % nestChambers.size());
                float maxR = Math.min(1.0f, Math.max(0.3f, (float) Math.cbrt(ch.getCapacity()) * 0.3f));
                double rAngle = rng.nextDouble(0, Math.PI * 2);
                double rDist = rng.nextDouble(0.05, maxR * 0.7);
                sx = (float) (ch.getX() + Math.cos(rAngle) * rDist);
                sy = (float) (ch.getY() + Math.sin(rAngle) * rDist);
                sz = (float) ch.getZ();
            } else if (!simNodes.isEmpty()) {
                var node = simNodes.get(index % simNodes.size());
                float maxR = Math.min(1.0f, Math.max(0.2f, node.radiusX() * 0.7f));
                double rAngle = rng.nextDouble(0, Math.PI * 2);
                double rDist = rng.nextDouble(0.05, maxR);
                sx = (float) (node.x() + Math.cos(rAngle) * rDist);
                sy = (float) (node.y() + Math.sin(rAngle) * rDist);
                sz = node.z();
            } else {
                sz = nestZ - 4.0f;
            }
        }

        sx = Math.max(1.0f, Math.min(mapWidth - 1.0f, sx));
        sy = Math.max(1.0f, Math.min(mapHeight - 1.0f, sy));

        return ensureValidWalkablePos(colony, sx, sy, sz);
    }

    public static float[] ensureValidWalkablePos(Colony colony, float sx, float sy, float sz) {
        Terrarium terrarium = colony.getTerrarium();
        if (terrarium == null) return new float[]{sx, sy, sz};

        int cx = Math.round(sx);
        int cy = Math.round(sy);
        int cz = Math.round(sz);

        if (!terrarium.inBounds(cx, cy, cz)) {
            cx = Math.max(0, Math.min(terrarium.getWidth() - 1, cx));
            cy = Math.max(0, Math.min(terrarium.getHeight() - 1, cy));
            cz = Math.max(0, Math.min(terrarium.getDepth() - 1, cz));
        }

        var cell = terrarium.getCell(cx, cy, cz);
        if (cell == null || cell.isPassable()) {
            return new float[]{sx, sy, sz};
        }

        for (int r = 1; r <= 5; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) == r || Math.abs(dy) == r || Math.abs(dz) == r) {
                            int tx = cx + dx;
                            int ty = cy + dy;
                            int tz = cz + dz;
                            if (terrarium.inBounds(tx, ty, tz)) {
                                var tCell = terrarium.getCell(tx, ty, tz);
                                if (tCell != null && tCell.isPassable()) {
                                    return new float[]{(float) tx, (float) ty, (float) tz};
                                }
                            }
                        }
                    }
                }
            }
        }

        float surfZ = colony.getSurfaceZAt(sx, sy);
        return new float[]{sx, sy, surfZ};
    }
}
