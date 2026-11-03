/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.paint.Color;

/**
 * Procedural voxel tree rendering for Gamified display mode in World Editor 3D Viewport.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorGamifiedTreeRenderer {

    private WorldEditorGamifiedTreeRenderer() {
    }

    static void renderGamifiedTree(WorldEditorPane pane, int speciesIdx, int seasonIdx,
                                   double worldX, double worldY, double worldZ,
                                   double treeHeightM, double canopyRadiusM, double sideM, double step,
                                   double cx, double cy, double scale, double radAz, double radEl) {
        double vSize = step * 1.5;

        switch (speciesIdx) {
            case 5 -> { // Bamboo
                for (double[] offset : new double[][]{{-0.4, -0.4}, {0.3, -0.2}, {-0.1, 0.4}}) {
                    double bx = worldX + offset[0] * vSize;
                    double by = worldY + offset[1] * vSize;
                    double bH = Math.max(vSize * 3.0, (treeHeightM / sideM) * WorldEditorPane.GRID_SIZE * 0.50);
                    int bC = Math.max(3, (int) (bH / vSize));
                    double dz = bH / bC;
                    for (int i = 0; i < bC; i++) {
                        pane.drawGamifiedVoxelCube3D(bx - vSize * 0.25, by - vSize * 0.25, worldZ + i * dz,
                                vSize * 0.5, vSize * 0.5, dz, Color.web("#84cc16"), Color.web("#4d7c0f"),
                                cx, cy, scale, radAz, radEl);
                    }
                    pane.drawGamifiedVoxelCube3D(bx - vSize * 0.5, by - vSize * 0.5, worldZ + bH,
                            vSize, vSize, vSize * 0.8, Color.web("#4ade80"), Color.web("#15803d"),
                            cx, cy, scale, radAz, radEl);
                }
            }
            case 6 -> { // Stump
                pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.6, worldY - vSize * 0.6, worldZ,
                        vSize * 1.2, vSize * 1.2, vSize * 0.8, Color.web("#92400e"), Color.web("#451a03"),
                        cx, cy, scale, radAz, radEl);
                pane.drawGamifiedVoxelCube3D(worldX + vSize * 0.2, worldY - vSize * 0.4, worldZ + vSize * 0.8,
                        vSize * 0.5, vSize * 0.5, vSize * 0.3, Color.web("#f59e0b"), Color.web("#b45309"),
                        cx, cy, scale, radAz, radEl);
            }
            case 3 -> { // Cactus Saguaro
                double cH = Math.max(vSize * 4.0, (treeHeightM / sideM) * WorldEditorPane.GRID_SIZE * 0.60);
                int cCount = Math.max(4, (int) (cH / vSize));
                double dz = cH / cCount;
                for (int i = 0; i < cCount; i++) {
                    pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.4, worldY - vSize * 0.4, worldZ + i * dz,
                            vSize * 0.8, vSize * 0.8, dz, Color.web("#15803d"), Color.web("#14532d"),
                            cx, cy, scale, radAz, radEl);
                }
                pane.drawGamifiedVoxelCube3D(worldX - vSize * 1.2, worldY - vSize * 0.3, worldZ + cH * 0.5,
                        vSize * 0.8, vSize * 0.6, vSize * 0.6, Color.web("#15803d"), Color.web("#14532d"),
                        cx, cy, scale, radAz, radEl);
                pane.drawGamifiedVoxelCube3D(worldX - vSize * 1.2, worldY - vSize * 0.3, worldZ + cH * 0.5 + vSize * 0.6,
                        vSize * 0.6, vSize * 0.6, vSize * 1.0, Color.web("#15803d"), Color.web("#14532d"),
                        cx, cy, scale, radAz, radEl);
                pane.drawGamifiedVoxelCube3D(worldX + vSize * 0.4, worldY - vSize * 0.3, worldZ + cH * 0.65,
                        vSize * 0.8, vSize * 0.6, vSize * 0.6, Color.web("#15803d"), Color.web("#14532d"),
                        cx, cy, scale, radAz, radEl);
                pane.drawGamifiedVoxelCube3D(worldX + vSize * 0.6, worldY - vSize * 0.3, worldZ + cH * 0.65 + vSize * 0.6,
                        vSize * 0.6, vSize * 0.6, vSize * 0.8, Color.web("#15803d"), Color.web("#14532d"),
                        cx, cy, scale, radAz, radEl);
            }
            case 1 -> { // Pine
                double vTrunkH = Math.max(step * 4.0, (treeHeightM / sideM) * WorldEditorPane.GRID_SIZE * 0.65);
                double trunkTopZ = worldZ + vTrunkH;
                int trunkCount = Math.max(3, (int) (vTrunkH / vSize));
                double dz = vTrunkH / trunkCount;
                for (int i = 0; i < trunkCount; i++) {
                    pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.4, worldY - vSize * 0.4, worldZ + i * dz,
                            vSize * 0.8, vSize * 0.8, dz, Color.web("#78350f"), Color.web("#451a03"),
                            cx, cy, scale, radAz, radEl);
                }
                Color pineCol = seasonIdx == 3 ? Color.web("#1e293b") : Color.web("#14532d");
                for (int bx = -2; bx <= 2; bx++) {
                    for (int by = -2; by <= 2; by++) {
                        if (Math.abs(bx) == 2 && Math.abs(by) == 2) continue;
                        pane.drawGamifiedVoxelCube3D(worldX + bx * vSize * 0.7 - vSize * 0.4, worldY + by * vSize * 0.7 - vSize * 0.4,
                                trunkTopZ - vSize * 0.5, vSize * 0.9, vSize * 0.9, vSize * 0.9,
                                pineCol, pineCol.darker(), cx, cy, scale, radAz, radEl);
                    }
                }
                for (int bx = -1; bx <= 1; bx++) {
                    for (int by = -1; by <= 1; by++) {
                        pane.drawGamifiedVoxelCube3D(worldX + bx * vSize * 0.7 - vSize * 0.4, worldY + by * vSize * 0.7 - vSize * 0.4,
                                trunkTopZ + vSize * 0.4, vSize * 0.9, vSize * 0.9, vSize * 0.9,
                                pineCol, pineCol.darker(), cx, cy, scale, radAz, radEl);
                    }
                }
                pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.4, worldY - vSize * 0.4, trunkTopZ + vSize * 1.3,
                        vSize * 0.9, vSize * 0.9, vSize * 1.1, pineCol, pineCol.darker(), cx, cy, scale, radAz, radEl);
            }
            case 2 -> { // Acacia
                double vTrunkH = Math.max(step * 4.5, (treeHeightM / sideM) * WorldEditorPane.GRID_SIZE * 0.70);
                double trunkTopZ = worldZ + vTrunkH;
                int trunkCount = Math.max(3, (int) (vTrunkH / vSize));
                double dz = vTrunkH / trunkCount;
                for (int i = 0; i < trunkCount; i++) {
                    pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.45, worldY - vSize * 0.45, worldZ + i * dz,
                            vSize * 0.9, vSize * 0.9, dz, Color.web("#78350f"), Color.web("#451a03"),
                            cx, cy, scale, radAz, radEl);
                }
                Color acaciaCol = seasonIdx == 2 ? Color.web("#eab308") : Color.web("#a16207");
                for (int bx = -3; bx <= 3; bx++) {
                    for (int by = -3; by <= 3; by++) {
                        if (Math.abs(bx) == 3 && Math.abs(by) == 3) continue;
                        pane.drawGamifiedVoxelCube3D(worldX + bx * vSize * 0.7 - vSize * 0.4, worldY + by * vSize * 0.7 - vSize * 0.4,
                                trunkTopZ, vSize * 0.9, vSize * 0.9, vSize * 0.6, acaciaCol, acaciaCol.darker(),
                                cx, cy, scale, radAz, radEl);
                    }
                }
            }
            default -> { // Deciduous / Oak / Birch
                double vTrunkH = Math.max(step * 4.0, (treeHeightM / sideM) * WorldEditorPane.GRID_SIZE * 0.65);
                double trunkTopZ = worldZ + vTrunkH;
                int trunkCount = Math.max(3, (int) (vTrunkH / vSize));
                double dz = vTrunkH / trunkCount;

                Color barkCol = (speciesIdx == 4) ? Color.web("#f8fafc") : Color.web("#78350f");
                Color barkSideCol = (speciesIdx == 4) ? Color.web("#cbd5e1") : Color.web("#451a03");

                for (int i = 0; i < trunkCount; i++) {
                    pane.drawGamifiedVoxelCube3D(worldX - vSize * 0.5, worldY - vSize * 0.5, worldZ + i * dz,
                            vSize, vSize, dz, barkCol, barkSideCol, cx, cy, scale, radAz, radEl);
                }

                Color leafBaseCol = switch (speciesIdx) {
                    case 4 -> (seasonIdx == 2 ? Color.web("#fde047") : (seasonIdx == 0 ? Color.web("#86efac") : (seasonIdx == 3 ? Color.web("#e2e8f0") : Color.web("#4ade80"))));
                    default -> (seasonIdx == 2 ? Color.web("#d97706") : (seasonIdx == 0 ? Color.web("#22c55e") : (seasonIdx == 3 ? Color.web("#cbd5e1") : Color.web("#15803d"))));
                };
                Color leafSideCol = leafBaseCol.darker();

                double cSize = Math.max(vSize * 1.2, (canopyRadiusM / sideM) * WorldEditorPane.GRID_SIZE * 0.40);
                int rVox = 2;
                int hVox = 3;

                int startX = Math.cos(radAz) >= 0 ? -rVox : rVox;
                int endX = Math.cos(radAz) >= 0 ? rVox : -rVox;
                int stepX = Math.cos(radAz) >= 0 ? 1 : -1;

                int startY = Math.sin(radAz) >= 0 ? -rVox : rVox;
                int endY = Math.sin(radAz) >= 0 ? rVox : -rVox;
                int stepY = Math.sin(radAz) >= 0 ? 1 : -1;

                for (int bz = 0; bz < hVox; bz++) {
                    for (int bx = startX; bx != endX + stepX; bx += stepX) {
                        for (int by = startY; by != endY + stepY; by += stepY) {
                            if (Math.abs(bx) == rVox && Math.abs(by) == rVox && (bz == 0 || bz == hVox - 1)) continue;
                            double cxWorld = worldX + bx * cSize * 0.70 - cSize * 0.5;
                            double cyWorld = worldY + by * cSize * 0.70 - cSize * 0.5;
                            double czWorld = trunkTopZ + bz * cSize * 0.70;

                            pane.drawGamifiedVoxelCube3D(cxWorld, cyWorld, czWorld, cSize, cSize, cSize,
                                    leafBaseCol, leafSideCol, cx, cy, scale, radAz, radEl);
                        }
                    }
                }
            }
        }
    }
}
