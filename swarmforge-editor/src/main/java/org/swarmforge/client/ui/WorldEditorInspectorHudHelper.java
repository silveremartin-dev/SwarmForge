package org.swarmforge.client.ui;

import java.util.Locale;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.swarmforge.client.util.I18nManager;

import static org.swarmforge.client.ui.WorldEditorPane.GRID_SIZE;
import static org.swarmforge.client.ui.WorldEditorPane.SOIL_DEPTH;

/**
 * Helper class responsible for floating mouse inspector card HUD and hover telemetry formatting
 * within the World Editor.
 */
public final class WorldEditorInspectorHudHelper {

    private WorldEditorInspectorHudHelper() {
        // Utility class
    }

    public static VBox createFloatingMouseInspectorCard(WorldEditorPane pane) {
        VBox card = new VBox(4);
        card.setMouseTransparent(true);
        card.setManaged(false);
        card.setVisible(false);
        card.setMinWidth(380);
        card.setPrefWidth(430);
        card.setMaxWidth(540);
        card.setStyle(
            "-fx-background-color: rgba(15, 23, 42, 0.96); " +
            "-fx-background-radius: 8px; " +
            "-fx-border-color: #38bdf8; " +
            "-fx-border-radius: 8px; " +
            "-fx-border-width: 1.5px; " +
            "-fx-padding: 10px 14px; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 0, 0, 0.85), 14, 0, 0, 4);"
        );
        card.getStyleClass().add("card-pane");

        pane.lblFInspTitle = new Label();
        pane.lblFInspTitle.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold; -fx-font-size: 11px;");
        pane.lblFInspTitle.setWrapText(true);
        pane.lblFInspTitle.setMinWidth(Region.USE_PREF_SIZE);

        pane.lblFInspAlt = new Label();
        pane.lblFInspAlt.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 10px;");
        pane.lblFInspAlt.setWrapText(true);
        pane.lblFInspAlt.setMinWidth(Region.USE_PREF_SIZE);

        pane.lblFInspSubstrat = new Label();
        pane.lblFInspSubstrat.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 10px;");
        pane.lblFInspSubstrat.setWrapText(true);
        pane.lblFInspSubstrat.setMinWidth(Region.USE_PREF_SIZE);

        pane.lblFInspClimate = new Label();
        pane.lblFInspClimate.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 10px;");
        pane.lblFInspClimate.setWrapText(true);
        pane.lblFInspClimate.setMinWidth(Region.USE_PREF_SIZE);

        pane.lblFInspChem = new Label();
        pane.lblFInspChem.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 10px;");
        pane.lblFInspChem.setWrapText(true);
        pane.lblFInspChem.setMinWidth(Region.USE_PREF_SIZE);

        pane.lblFInspGas = new Label();
        pane.lblFInspGas.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold; -fx-font-size: 10px;");
        pane.lblFInspGas.setWrapText(true);
        pane.lblFInspGas.setMinWidth(Region.USE_PREF_SIZE);

        pane.lblFInspPhero = new Label();
        pane.lblFInspPhero.setStyle("-fx-text-fill: #a78bfa; -fx-font-size: 9.5px;");
        pane.lblFInspPhero.setWrapText(true);
        pane.lblFInspPhero.setMinWidth(Region.USE_PREF_SIZE);

        pane.lblFInspBio = new Label();
        pane.lblFInspBio.setStyle("-fx-text-fill: #f8fafc; -fx-font-size: 10px;");
        pane.lblFInspBio.setWrapText(true);
        pane.lblFInspBio.setMinWidth(Region.USE_PREF_SIZE);

        card.getChildren().addAll(pane.lblFInspTitle, pane.lblFInspAlt, pane.lblFInspSubstrat, pane.lblFInspClimate, pane.lblFInspChem, pane.lblFInspGas, pane.lblFInspPhero, pane.lblFInspBio);
        return card;
    }

    public static void updateFloatingMouseInspector(WorldEditorPane pane, int gx, int gy, int gd, double screenX, double screenY, String bioOverride) {
        if (!pane.isVoxelInfoVisible) {
            if (pane.floatingMouseInspectorCard != null) pane.floatingMouseInspectorCard.setVisible(false);
            return;
        }
        if (pane.floatingMouseInspectorCard == null) return;
        int maxGX = (pane.activeSimulation != null && pane.activeSimulation.getTerrarium() != null) ? pane.activeSimulation.getTerrarium().getWidth() : GRID_SIZE;
        int maxGY = (pane.activeSimulation != null && pane.activeSimulation.getTerrarium() != null) ? pane.activeSimulation.getTerrarium().getHeight() : GRID_SIZE;
        if (gx < 0 || gx >= maxGX || gy < 0 || gy >= maxGY) {
            pane.floatingMouseInspectorCard.setVisible(false);
            return;
        }
        int safeDepth = Math.max(0, Math.min(SOIL_DEPTH - 1, gd));

        double dM = pane.depthSlider != null ? pane.depthSlider.getValue() : 3.0;
        double surfAltM = pane.heightGrid[gx][gy] * dM;
        double depthM = ((double) safeDepth / (double) (SOIL_DEPTH - 1)) * dM;
        double voxelAltM = surfAltM - depthM;

        int humPct = (int) (pane.humidityGrid[gx][gy][safeDepth] * 100);
        byte mat = pane.soilLayers[gx][gy][safeDepth];
        boolean isVoid = pane.voidGrid[gx][gy][safeDepth] || pane.carvedVoxelGrid[gx][gy];

        String matName;
        if (isVoid) {
            matName = I18nManager.getInstance().get("world.mat.cavity");
        } else {
            matName = switch (mat) {
                case 0 -> I18nManager.getInstance().get("world.mat.humus");
                case 1 -> I18nManager.getInstance().get("world.mat.sand");
                case 2 -> I18nManager.getInstance().get("world.mat.clay");
                case 3 -> I18nManager.getInstance().get("world.mat.bedrock");
                case 4 -> I18nManager.getInstance().get("world.mat.gravel");
                case 5 -> I18nManager.getInstance().get("world.mat.litter");
                case 6 -> I18nManager.getInstance().get("world.mat.gallery");
                case 7 -> I18nManager.getInstance().get("world.mat.brood_chamber");
                case 8 -> I18nManager.getInstance().get("world.mat.royal_chamber");
                default -> I18nManager.getInstance().get("world.mat.earth");
            };
        }

        double tempC = pane.tempGrid[gx][gy][safeDepth];
        if (tempC == 0) {
            tempC = 20.0 - (((double) (SOIL_DEPTH - 1 - safeDepth) / (double) (SOIL_DEPTH - 1)) * 3.5);
        }
        int rootPct = (int) (pane.rootGrid[gx][gy][safeDepth] * 100);
        float phVal = pane.phGrid[gx][gy][safeDepth] > 0 ? pane.phGrid[gx][gy][safeDepth] : 6.5f;

        String humStatus;
        if (humPct >= 55 && humPct <= 90) {
            humStatus = "🟢";
        } else if ((humPct >= 35 && humPct < 55) || (humPct > 90 && humPct <= 95)) {
            humStatus = "🟠";
        } else {
            humStatus = "🔴";
        }

        float co2Ppm = 400.0f;
        float o2Pct = 20.95f;
        String pheromoneInfo = "Aucune trace chimique";
        if (pane.activeSimulation != null && pane.activeSimulation.getTerrarium() != null) {
            var cell = pane.activeSimulation.getTerrarium().getCell(gx, gy, safeDepth);
            if (cell != null) {
                if (cell.co2() > 0) co2Ppm = cell.co2();
                if (cell.o2() > 0) o2Pct = cell.o2();
                if (cell.pheromones() != null) {
                    float[] ph = cell.pheromones();
                    StringBuilder sb = new StringBuilder();
                    if (ph.length > 0 && ph[0] > 0.05f) sb.append(String.format(Locale.US, "🟢 Piste:%.0f%% ", ph[0] * 100));
                    if (ph.length > 1 && ph[1] > 0.05f) sb.append(String.format(Locale.US, "🔴 Alarme:%.0f%% ", ph[1] * 100));
                    if (ph.length > 2 && ph[2] > 0.05f) sb.append(String.format(Locale.US, "🟡 Nourr:%.0f%% ", ph[2] * 100));
                    if (ph.length > 3 && ph[3] > 0.05f) sb.append(String.format(Locale.US, "🔵 Nid:%.0f%% ", ph[3] * 100));
                    if (ph.length > 5 && ph[5] > 0.05f) sb.append(String.format(Locale.US, "🟣 Reine:%.0f%% ", ph[5] * 100));
                    if (sb.length() > 0) pheromoneInfo = sb.toString().trim();
                }
            }
        } else {
            double depthFrac = (double) (SOIL_DEPTH - 1 - safeDepth) / (double) (SOIL_DEPTH - 1);
            co2Ppm = (float) (415.0 + depthFrac * 650.0 + (isVoid ? 450.0 : 0.0));
            o2Pct = (float) Math.max(12.0, 20.95 - (co2Ppm - 400.0) * 0.0006);
        }
        float co2Pct = co2Ppm / 10000.0f;

        String gasStatus;
        String gasColorStyle;
        if (co2Ppm < 1500.0f && o2Pct >= 19.5f) {
            gasStatus = "🟢 Optimal";
            gasColorStyle = "-fx-text-fill: #4ade80;";
        } else if (co2Ppm < 5000.0f && o2Pct >= 18.0f) {
            gasStatus = "🟠 CO₂ Élevé";
            gasColorStyle = "-fx-text-fill: #f59e0b;";
        } else {
            gasStatus = "🔴 Hypoxie Critique";
            gasColorStyle = "-fx-text-fill: #ef4444;";
        }

        float lightLux = (safeDepth == 0) ? (pane.isForestArea(gx, gy) ? 4500.0f : 12000.0f) : (safeDepth == 1 && isVoid ? 120.0f : 0.0f);
        String lightStr = (lightLux > 0) ? String.format(Locale.US, "☀️ %.0flux", lightLux) : "🌑 Aphotique (0 lux)";
        float baroPressure = (float) (1013.25 + (depthM * 0.12));

        String bioContents = bioOverride != null ? bioOverride : "Sol intact (aucun individu)";
        if (bioOverride == null && pane.isSimulationMode && pane.activeSimulation != null) {
            int antCount = 0;
            int broodCount = 0;
            String mainCaste = null;
            int tW = Math.max(1, pane.activeSimulation.getTerrarium() != null ? pane.activeSimulation.getTerrarium().getWidth() : 1);
            int tH = Math.max(1, pane.activeSimulation.getTerrarium() != null ? pane.activeSimulation.getTerrarium().getHeight() : 1);

            for (org.swarmforge.core.domain.Colony colony : pane.activeSimulation.getColonies()) {
                for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                    int igx = (int) Math.max(0, Math.min(GRID_SIZE - 1, (ind.getX() / (double) tW) * GRID_SIZE));
                    int igy = (int) Math.max(0, Math.min(GRID_SIZE - 1, (ind.getY() / (double) tH) * GRID_SIZE));
                    if (igx == gx && igy == gy) {
                        if (ind.getLifeStage() != org.swarmforge.core.domain.Individual.LifeStage.ADULT) {
                            broodCount++;
                        } else {
                            antCount++;
                            if (mainCaste == null) mainCaste = ind.getCaste() != null ? ind.getCaste().name() : "Ouvrière";
                        }
                    }
                }
            }
            if (antCount > 0 || broodCount > 0) {
                if (antCount > 0 && broodCount > 0) {
                    bioContents = String.format("%d fourmi(s) (%s) + %d couvain", antCount, mainCaste, broodCount);
                } else if (antCount > 0) {
                    bioContents = String.format("%d fourmi(s) (%s)", antCount, mainCaste);
                } else {
                    bioContents = String.format("%d couvain(s)", broodCount);
                }
            } else if (isVoid) {
                bioContents = "Galerie vide (accessible)";
            }
        }

        pane.lblFInspTitle.setText(String.format(Locale.US, "📍 Voxel [%d, %d, Prof: %d/%d] | 🌪️ %.1f hPa", gx, gy, safeDepth, SOIL_DEPTH - 1, baroPressure));
        pane.lblFInspAlt.setText(String.format(Locale.US, "📏 Alt: %.2fm | Profondeur: -%.2fm (%dcm)", voxelAltM, depthM, (int) (depthM * 100)));
        pane.lblFInspSubstrat.setText(String.format(Locale.US, "🟤 Substrat: %s | %s", matName, lightStr));
        pane.lblFInspClimate.setText(String.format(Locale.US, "🌡️ Temp: %.1f°C | 💧 Humidité: %d%% %s", tempC, humPct, humStatus));
        String phStatus = phVal < 5.8f ? " (Acide)" : (phVal > 7.4f ? " (Basique)" : " (Neutre)");
        pane.lblFInspChem.setText(String.format(Locale.US, "🧪 pH: %.1f%s | 🌿 Racines: %d%%", phVal, phStatus, rootPct));
        pane.lblFInspGas.setStyle(gasColorStyle + " -fx-font-weight: bold; -fx-font-size: 10px;");
        pane.lblFInspGas.setText(String.format(Locale.US, "💨 CO₂: %.0f ppm (%.3f%%) | O₂: %.1f%% | %s", co2Ppm, co2Pct, o2Pct, gasStatus));
        pane.lblFInspPhero.setText(String.format(Locale.US, "🧪 Phéromones: %s", pheromoneInfo));
        pane.lblFInspBio.setText(String.format(Locale.US, "🐜 Contenu Voxel: %s", bioContents));

        pane.floatingMouseInspectorCard.autosize();
        double parentW = pane.h3d != null && pane.h3d.getWidth() > 0 ? pane.h3d.getWidth() : 800;
        double parentH = pane.h3d != null && pane.h3d.getHeight() > 0 ? pane.h3d.getHeight() : 600;
        double cardW = Math.max(400.0, pane.floatingMouseInspectorCard.getWidth());
        double cardH = Math.max(180.0, pane.floatingMouseInspectorCard.getHeight());
        double hx = Math.max(10, Math.min(parentW - cardW - 10, screenX + 15));
        double hy = Math.max(10, Math.min(parentH - cardH - 10, screenY - 20));

        pane.floatingMouseInspectorCard.relocate(hx, hy);
        pane.floatingMouseInspectorCard.setVisible(true);
        pane.floatingMouseInspectorCard.toFront();
    }

    public static void updateHoverInfo(WorldEditorPane pane, double mx, double my, String viewType) {
        double cw = "3D".equals(viewType) ? pane.canvas3D.getWidth() : ("SIDE".equals(viewType) ? pane.canvasSide.getWidth() : pane.canvasTop.getWidth());
        double ch = "3D".equals(viewType) ? pane.canvas3D.getHeight() : ("SIDE".equals(viewType) ? pane.canvasSide.getHeight() : pane.canvasTop.getHeight());
        if (cw <= 0 || ch <= 0) return;

        int gx, gy;
        int inspectedY;

        if (!pane.isVoxelInfoVisible) {
            if (pane.floatingMouseInspectorCard != null) pane.floatingMouseInspectorCard.setVisible(false);
            if (pane.lblHoverInfo != null) pane.lblHoverInfo.setText("");
            pane.hover3DCell = null;
            return;
        }

        if ("3D".equals(viewType)) {
            double radAz = Math.toRadians(pane.azimuth);
            double radEl = Math.toRadians(pane.elevation);
            double cx = cw / 2 + pane.pan3DX;
            double cy = ch / 2 + pane.pan3DY + 40;
            double scale = pane.zoom * 12.0;

            int[] picked = WorldEditorInteractionAndInspectorHelper.pick3DGridCell(pane, mx, my, cx, cy, scale, radAz, radEl);
            if (picked != null) {
                pane.hover3DCell = picked;
                pane.hoverMX = mx; pane.hoverMY = my;
                gx = picked[0]; gy = picked[1];
                inspectedY = picked[2];
            } else {
                pane.hover3DCell = null;
                gx = (int) Math.max(0, Math.min(GRID_SIZE - 1, (mx / cw) * GRID_SIZE));
                gy = (int) Math.max(0, Math.min(GRID_SIZE - 1, (my / ch) * GRID_SIZE));
                inspectedY = 0;
            }
            pane.repaintAllViews();
        } else if ("SIDE".equals(viewType)) {
            double blockW = (cw - 20.0) / GRID_SIZE;
            double blockH = (ch - 90.0) / SOIL_DEPTH;
            double refSurfaceH = 0.50 * 25.0;
            double cutRatio = pane.slicePlaneSlider != null ? (pane.slicePlaneSlider.getValue() / 100.0) : 1.0;
            int cutX = Math.max(0, Math.min(GRID_SIZE - 1, (int) (GRID_SIZE * cutRatio)));

            int yScreen = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((mx - 10.0) / blockW)));
            gx = cutX;
            gy = GRID_SIZE - 1 - yScreen;
            inspectedY = (int) Math.max(0, Math.min(SOIL_DEPTH - 1, Math.floor((my - (65 - refSurfaceH)) / blockH)));
        } else {
            double gridPx = Math.min(cw - 12, ch - 12) * pane.topZoom;
            double cellW = gridPx / GRID_SIZE;
            double cellH = gridPx / GRID_SIZE;
            double offsetX = (cw - gridPx) / 2.0 + pane.topPanX;
            double offsetY = (ch - gridPx) / 2.0 + pane.topPanY;
            gx = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((mx - offsetX) / cellW)));
            gy = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((my - offsetY) / cellH)));
            if (!pane.isTerrainVisible || (pane.slicePlaneSlider != null && pane.slicePlaneSlider.getValue() < 99.0)) {
                double sliceVal = pane.slicePlaneSlider != null ? pane.slicePlaneSlider.getValue() : 100.0;
                inspectedY = (int) Math.max(0, Math.min(SOIL_DEPTH - 1, Math.round((1.0 - sliceVal / 100.0) * (SOIL_DEPTH - 1))));
            } else {
                inspectedY = (int) Math.max(0, Math.min(SOIL_DEPTH - 1, Math.round(pane.heightGrid[gx][gy] * (SOIL_DEPTH - 1))));
            }
        }

        double dM = pane.depthSlider != null ? pane.depthSlider.getValue() : 3.0;
        double altM = ((double) (SOIL_DEPTH - 1 - inspectedY) / (double) (SOIL_DEPTH - 1)) * dM;

        int humPct = (int) (pane.humidityGrid[gx][gy][inspectedY] * 100);
        byte mat = pane.soilLayers[gx][gy][inspectedY];
        boolean isVoid = pane.voidGrid[gx][gy][inspectedY] || pane.carvedVoxelGrid[gx][gy];
        String matName;
        if (isVoid) {
            matName = I18nManager.getInstance().get("world.mat.cavity");
        } else {
            matName = switch (mat) {
                case 0 -> I18nManager.getInstance().get("world.mat.humus");
                case 1 -> I18nManager.getInstance().get("world.mat.sand");
                case 2 -> I18nManager.getInstance().get("world.mat.clay");
                case 3 -> I18nManager.getInstance().get("world.mat.bedrock");
                case 4 -> I18nManager.getInstance().get("world.mat.gravel");
                case 5 -> I18nManager.getInstance().get("world.mat.litter");
                case 6 -> I18nManager.getInstance().get("world.mat.gallery");
                case 7 -> I18nManager.getInstance().get("world.mat.brood_chamber");
                case 8 -> I18nManager.getInstance().get("world.mat.royal_chamber");
                default -> I18nManager.getInstance().get("world.mat.earth");
            };
        }
        boolean isRiver = WorldEditorGeneratorHelper.isNearRiver(pane, gx, gy, 1);

        double tempC = pane.tempGrid[gx][gy][inspectedY];
        if (tempC == 0) {
            double surfaceTemp = 20.0;
            double depthFactor = (double) (SOIL_DEPTH - 1 - inspectedY) / (double) (SOIL_DEPTH - 1);
            tempC = surfaceTemp - (depthFactor * 3.5);
        }
        float phVal = pane.phGrid[gx][gy][inspectedY] > 0 ? pane.phGrid[gx][gy][inspectedY] : 6.5f;
        int rootPct = (int) (pane.rootGrid[gx][gy][inspectedY] * 100);

        String antHoverStr = "";
        if (pane.activeSimulation != null && ("3D".equals(viewType) || "TOP".equals(viewType))) {
            double radAz = Math.toRadians(pane.azimuth);
            double radEl = Math.toRadians(pane.elevation);
            double cx = cw / 2 + pane.pan3DX;
            double cy = ch / 2 + pane.pan3DY + 40;
            double scale = pane.zoom * 12.0;

            org.swarmforge.core.domain.Individual hoveredAnt = null;
            org.swarmforge.core.domain.Predator hoveredPredator = null;
            org.swarmforge.core.simulation.TunnelNetwork.TunnelNode hoveredChamber = null;
            double minEntityDistSq = 1225.0;
            double minChamberDistSq = 625.0;
            for (org.swarmforge.core.domain.Colony colony : pane.activeSimulation.getColonies()) {
                for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                    double ax = ind.getX();
                    double ay = ind.getY();
                    double az = ind.getZ();

                    double igx = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (ax / (float) Math.max(1, pane.activeSimulation.getTerrarium().getWidth())) * GRID_SIZE));
                    double igy = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (ay / (float) Math.max(1, pane.activeSimulation.getTerrarium().getHeight())) * GRID_SIZE));
                    int iigx = (int) igx;
                    int iigy = (int) igy;
                    double gz = pane.heightGrid[iigx][iigy] * 40.0 + az * 2.0 + 1.5;

                    double[] p = pane.project3DPoint(igx, igy, gz, cx, cy, scale, radAz, radEl);
                    double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                    if (dSq < minEntityDistSq) {
                        minEntityDistSq = dSq;
                        hoveredAnt = ind;
                        hoveredPredator = null;
                    }
                }
            }

            if (pane.activeSimulation.getPredatorManager() != null) {
                for (org.swarmforge.core.domain.Predator pred : pane.activeSimulation.getPredatorManager().getPredators()) {
                    if (!pred.isAlive()) continue;
                    double px = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (pred.getX() / (float) Math.max(1, pane.activeSimulation.getTerrarium().getWidth())) * GRID_SIZE));
                    double py = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (pred.getY() / (float) Math.max(1, pane.activeSimulation.getTerrarium().getHeight())) * GRID_SIZE));
                    int ipx = Math.max(0, Math.min(GRID_SIZE - 1, (int) px));
                    int ipy = Math.max(0, Math.min(GRID_SIZE - 1, (int) py));
                    double pz = pane.heightGrid[ipx][ipy] * 40.0 + Math.max(-40.0, Math.min(60.0, (double) pred.getZ())) * 2.0 + 2.0;

                    double[] p = pane.project3DPoint(px, py, pz, cx, cy, scale, radAz, radEl);
                    double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                    if (dSq < minEntityDistSq) {
                        minEntityDistSq = dSq;
                        hoveredPredator = pred;
                        hoveredAnt = null;
                    }
                }
            }

            if (hoveredAnt == null && hoveredPredator == null) {
                int tWidth = Math.max(1, pane.activeSimulation.getTerrarium().getWidth());
                int tHeight = Math.max(1, pane.activeSimulation.getTerrarium().getHeight());
                int tDepth = Math.max(1, pane.activeSimulation.getTerrarium().getDepth());
                for (org.swarmforge.core.domain.Colony colony : pane.activeSimulation.getColonies()) {
                    if (colony.getTunnelNetwork() != null && colony.getTunnelNetwork().getNodes() != null) {
                        for (org.swarmforge.core.simulation.TunnelNetwork.TunnelNode node : colony.getTunnelNetwork().getNodes()) {
                            double gxNode = (node.x() / (double) tWidth) * GRID_SIZE;
                            double gyNode = (node.y() / (double) tHeight) * GRID_SIZE;
                            int igx = Math.max(0, Math.min(GRID_SIZE - 1, (int) gxNode));
                            int igy = Math.max(0, Math.min(GRID_SIZE - 1, (int) gyNode));
                            float sElev = pane.activeSimulation.getTerrarium().getSurfaceElevation(node.x(), node.y());
                            double gz = pane.heightGrid[igx][igy] * 40.0 + ((node.z() - sElev) / (double) tDepth) * 40.0;

                            double[] p = pane.project3DPoint(gxNode, gyNode, gz, cx, cy, scale, radAz, radEl);
                            double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                            if (dSq < minChamberDistSq) {
                                minChamberDistSq = dSq;
                                hoveredChamber = node;
                            }
                        }
                    }
                }
            }

            if (hoveredAnt != null) {
                antHoverStr = I18nManager.getInstance().get("world.hover.ant",
                    hoveredAnt.getSpecies() != null ? hoveredAnt.getSpecies().getCommonName() : "Formicidae",
                    hoveredAnt.getCaste(), hoveredAnt.getJob(), hoveredAnt.getHealth() * 100, hoveredAnt.getEnergy() * 100,
                    hoveredAnt.getState() != null ? hoveredAnt.getState().name() : "Active");
            } else if (hoveredPredator != null) {
                antHoverStr = String.format(Locale.US, " | 🦅 %s [Santé: %.0f/%.0f | État: %s]",
                    hoveredPredator.getType().getDisplayName(),
                    hoveredPredator.getHealth(), hoveredPredator.getMaxHealth(),
                    hoveredPredator.getState());
            } else if (hoveredChamber != null) {
                antHoverStr = String.format(Locale.US, " | 🏛️ %s [Prof: %.1fm | Cavité: Rx=%.1fm]",
                    hoveredChamber.type().name(), -hoveredChamber.z(), hoveredChamber.radiusX());
            }
        }

        float co2Ppm = 400.0f;
        float o2Pct = 20.95f;
        if (pane.activeSimulation != null && pane.activeSimulation.getTerrarium() != null) {
            org.swarmforge.core.domain.TerrariumCell cell = pane.activeSimulation.getTerrarium().getCell(gx, gy, inspectedY);
            if (cell != null) {
                if (cell.co2() > 0) co2Ppm = cell.co2();
                if (cell.o2() > 0) o2Pct = cell.o2();
            }
        } else {
            double depthFrac = (double) (SOIL_DEPTH - 1 - inspectedY) / (double) (SOIL_DEPTH - 1);
            co2Ppm = (float) (415.0 + depthFrac * 650.0 + (isVoid ? 450.0 : 0.0));
            o2Pct = (float) Math.max(12.0, 20.95 - (co2Ppm - 400.0) * 0.0006);
        }

        float co2Pct = co2Ppm / 10000.0f;
        String gasBadge = (co2Ppm < 1500.0f && o2Pct >= 19.5f) ? "🟢" : ((co2Ppm < 5000.0f && o2Pct >= 18.0f) ? "🟠" : "🔴");
        String humBadge = (humPct >= 55 && humPct <= 90) ? "🟢" : ((humPct >= 35 && humPct < 55) || (humPct > 90 && humPct <= 95) ? "🟠" : "🔴");

        String riverOrTerr = isRiver ? I18nManager.getInstance().get("world.hover.river") : I18nManager.getInstance().get("world.hover.terrestrial");
        if (pane.lblHoverInfo != null) {
            pane.lblHoverInfo.setText(I18nManager.getInstance().get("world.hover.format",
                gx, gy, inspectedY, SOIL_DEPTH - 1, altM, matName, tempC, humPct, humBadge, phVal, rootPct,
                co2Ppm, co2Pct, gasBadge, o2Pct, riverOrTerr, antHoverStr));
        }
        updateFloatingMouseInspector(pane, gx, gy, inspectedY, mx, my, antHoverStr.isEmpty() ? null : antHoverStr);
    }
}
