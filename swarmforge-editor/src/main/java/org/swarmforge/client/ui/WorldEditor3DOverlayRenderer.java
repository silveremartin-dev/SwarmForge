package org.swarmforge.client.ui;

import javafx.scene.paint.Color;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Predator;
import org.swarmforge.core.domain.PredatorType;
import org.swarmforge.core.gpu.SparsePheromoneGrid;
import org.swarmforge.core.spatial.Morton3D;
import org.swarmforge.core.world.WeatherMarkovChain;
import org.swarmforge.core.world.WeatherSystem;

import java.util.Locale;
import java.util.Map;
import java.util.Random;

/**
 * Renders 3D dynamic overlays including colonies, tracked individuals,
 * multi-channel pheromones, and meteorological/atmospheric phenomena.
 */
public final class WorldEditor3DOverlayRenderer {

    private WorldEditor3DOverlayRenderer() {}

    public static void drawColonyOverlay3D(WorldEditorPane pane, double cx, double cy, double scale, double radAz, double radEl) {
        if (!pane.isSimulationMode) return;
        try {
            if (pane.activeSimulation != null && !pane.activeSimulation.getColonies().isEmpty()) {
                for (Colony colony : pane.activeSimulation.getColonies()) {
                    for (Individual ind : colony.getLivingIndividuals()) {
                        try {
                            double ax = ind.getX();
                            double ay = ind.getY();
                            double az = ind.getZ();

                            double gx = Math.max(0.0, Math.min(WorldEditorPane.GRID_SIZE - 1.0, (ax / (float) Math.max(1, pane.activeSimulation.getTerrarium().getWidth())) * WorldEditorPane.GRID_SIZE));
                            double gy = Math.max(0.0, Math.min(WorldEditorPane.GRID_SIZE - 1.0, (ay / (float) Math.max(1, pane.activeSimulation.getTerrarium().getHeight())) * WorldEditorPane.GRID_SIZE));
                            int igx = (int) gx;
                            int igy = (int) gy;
                            float surfaceZ = pane.activeSimulation.getTerrarium().getSurfaceElevation((float) ax, (float) ay);
                            boolean isSubterranean = az < surfaceZ;

                            // Occlusion check: render subterranean ants (az < surfaceZ) when inside galleries or cut plane
                            double cutRatio = pane.slicePlaneSlider != null ? (pane.slicePlaneSlider.getValue() / 100.0) : 1.0;
                            int cutXLimit = (int) (WorldEditorPane.GRID_SIZE * cutRatio);
                            boolean isTranslucent = pane.showTranslucentVolumetricModeCheck != null && pane.showTranslucentVolumetricModeCheck.isSelected();
                            boolean isAntExposed = !pane.isTerrainVisible || !isSubterranean || igx >= cutXLimit || isTranslucent || pane.isGalleriesVisible;
                            if (!isAntExposed) continue;

                            double gz = pane.heightGrid[igx][igy] * 40.0 + Math.max(-40.0, Math.min(60.0, az - surfaceZ)) * 2.0 + 1.5;

                            double[] p = pane.project3DPoint(gx, gy, gz, cx, cy, scale, radAz, radEl);
                            double sideM = pane.surfaceSizeSlider != null ? pane.surfaceSizeSlider.getValue() : 25.0;
                            double canvasW = pane.canvas3D != null && pane.canvas3D.getWidth() > 0 ? pane.canvas3D.getWidth() : 800.0;
                            double pixelsPerMeter = (canvasW * 0.45) / sideM;
                            double antPhysPx = (6.0 / 1000.0) * pixelsPerMeter;
                            double antR = Math.max(1.0, (antPhysPx / 3.9) * (pane.zoom / 7.5));

                            // Brood rendering (Eggs, Larvae, Pupae): Never appear on surface, render as biological brood clusters
                            if (ind.getLifeStage() != null && ind.getLifeStage() != Individual.LifeStage.ADULT) {
                                if (isSubterranean) {
                                    pane.morphologyRenderer.drawRealisticBroodItem(pane.gc3D, p[0], p[1], ind.getLifeStage(), antR);
                                }
                                continue;
                            }

                            Color casteColor;
                            if (pane.currentRenderMode == WorldEditorPane.RenderMode.REALISTIC) {
                                casteColor = switch (ind.getCaste()) {
                                    case QUEEN -> Color.web("#1c120c");
                                    case SOLDIER -> Color.web("#422212");
                                    case WORKER, FORAGER, NURSE -> Color.web("#2b1d14");
                                    case MALE -> Color.web("#150f0a");
                                };
                            } else {
                                casteColor = switch (ind.getCaste()) {
                                    case QUEEN -> Color.web("#d946ef");
                                    case SOLDIER -> Color.web("#ef4444");
                                    case WORKER, FORAGER, NURSE -> Color.web("#f59e0b");
                                    case MALE -> Color.web("#06b6d4");
                                };
                            }

                            // If subterranean ant in solid terrain, render with slight opacity
                            if (isSubterranean && pane.isTerrainVisible && !isTranslucent && igx < cutXLimit && pane.isGalleriesVisible) {
                                casteColor = Color.color(casteColor.getRed(), casteColor.getGreen(), casteColor.getBlue(), 0.80);
                            }

                            pane.morphologyRenderer.drawRealisticAnt(pane.gc3D, p[0], p[1], ind.getHeading(), ind.getCaste(), casteColor, antR, ind.isCarryingFood());

                            if (pane.isAntTrackingEnabled && pane.followedAnt == null) {
                                pane.followedAnt = ind; // Select first ant as default tracked ant only when tracking is enabled
                            }

                            if (pane.isAntTrackingEnabled && pane.followedAnt != null && pane.followedAnt.getId().equals(ind.getId())) {
                                if (pane.antTrailHistory.isEmpty() || Math.hypot(pane.antTrailHistory.get(pane.antTrailHistory.size() - 1)[0] - gx, pane.antTrailHistory.get(pane.antTrailHistory.size() - 1)[1] - gy) > 0.5) {
                                    pane.antTrailHistory.add(new double[]{gx, gy, gz});
                                    if (pane.antTrailHistory.size() > WorldEditorPane.MAX_TRAIL_LENGTH) {
                                        pane.antTrailHistory.remove(0);
                                    }
                                }

                                if (pane.isFollowAntCameraEnabled && pane.cameraFollowMode != CameraFollowMode.FREE) {
                                    double targetPanX = (WorldEditorPane.GRID_SIZE / 2.0 - gx) * 8.0;
                                    double targetPanY = (WorldEditorPane.GRID_SIZE / 2.0 - gy) * 8.0;
                                    double headingDeg = Math.toDegrees(ind.getHeading());
                                    double targetAz = ((-headingDeg + 90.0) % 360.0 + 360.0) % 360.0;
                                    if (pane.cameraFollowMode == CameraFollowMode.TPS) {
                                        double diff = ((targetAz - pane.azimuth + 540.0) % 360.0) - 180.0;
                                        pane.azimuth = (pane.azimuth + diff * 0.08) % 360.0;
                                        pane.elevation += (38.0 - pane.elevation) * 0.10;
                                        pane.zoom += (14.0 - pane.zoom) * 0.10;
                                    } else if (pane.cameraFollowMode == CameraFollowMode.FPS) {
                                        double diff = ((targetAz - pane.azimuth + 540.0) % 360.0) - 180.0;
                                        pane.azimuth = (pane.azimuth + diff * 0.20) % 360.0;
                                        pane.elevation += (2.0 - pane.elevation) * 0.15;
                                        pane.zoom += (28.0 - pane.zoom) * 0.12;
                                        targetPanY = (WorldEditorPane.GRID_SIZE / 2.0 - gy) * 8.0 - 25.0;
                                    }
                                    pane.pan3DX += (targetPanX - pane.pan3DX) * 0.15;
                                    pane.pan3DY += (targetPanY - pane.pan3DY) * 0.15;
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }

                // Render Predators / Prey
                if (pane.activeSimulation.getPredatorManager() != null) {
                    for (Predator pred : pane.activeSimulation.getPredatorManager().getPredators()) {
                        try {
                            if (!pred.isAlive()) continue;
                            double px = Math.max(0.0, Math.min(WorldEditorPane.GRID_SIZE - 1.0, (pred.getX() / (float) Math.max(1, pane.activeSimulation.getTerrarium().getWidth())) * WorldEditorPane.GRID_SIZE));
                            double py = Math.max(0.0, Math.min(WorldEditorPane.GRID_SIZE - 1.0, (pred.getY() / (float) Math.max(1, pane.activeSimulation.getTerrarium().getHeight())) * WorldEditorPane.GRID_SIZE));
                            int ipx = Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, (int) px));
                            int ipy = Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, (int) py));
                            double pz = pane.heightGrid[ipx][ipy] * 40.0 + Math.max(-40.0, Math.min(60.0, (double) pred.getZ())) * 2.0 + 2.0;

                            double[] p = pane.project3DPoint(px, py, pz, cx, cy, scale, radAz, radEl);
                            double predR = Math.max(2.5, 4.0 * (pane.zoom / 7.5));

                            pane.morphologyRenderer.drawRealisticPredator(pane.gc3D, p[0], p[1], pred.getHeading(), pred.getType(), (float)(pred.getHealth() / Math.max(1.0, pred.getMaxHealth())), predR);
                        } catch (Exception ignored) {}
                    }
                }
            } else {
                Random rand = new Random(4321);
                int antCount = 35;
                for (int i = 0; i < antCount; i++) {
                    int gx = 12 + (int)(rand.nextDouble() * (WorldEditorPane.GRID_SIZE - 24));
                    int gy = 12 + (int)(rand.nextDouble() * (WorldEditorPane.GRID_SIZE - 24));
                    double z = pane.heightGrid[gx][gy] * 40.0 + 1.5;
                    double[] p = pane.project3DPoint(gx, gy, z, cx, cy, scale, radAz, radEl);
                    double antR = Math.max(1.0, 1.2 * (pane.zoom / 7.5));
                    double heading = rand.nextDouble() * Math.PI * 2;
                    Individual.Caste caste = (i % 10 == 0) ? Individual.Caste.QUEEN : ((i % 6 == 0) ? Individual.Caste.SOLDIER : Individual.Caste.WORKER);
                    Color casteColor;
                    if (pane.currentRenderMode == WorldEditorPane.RenderMode.REALISTIC) {
                        casteColor = (caste == Individual.Caste.QUEEN) ? Color.web("#1c120c") : ((caste == Individual.Caste.SOLDIER) ? Color.web("#422212") : Color.web("#2b1d14"));
                    } else {
                        casteColor = (caste == Individual.Caste.QUEEN) ? Color.web("#d946ef") : ((caste == Individual.Caste.SOLDIER) ? Color.web("#ef4444") : Color.web("#f59e0b"));
                    }

                    pane.morphologyRenderer.drawRealisticAnt(pane.gc3D, p[0], p[1], heading, caste, casteColor, antR, i % 4 == 0);
                }
            }
        } catch (Exception ignored) {}

        drawTrackedAntFX(pane, cx, cy, scale, radAz, radEl);
    }

    public static void drawTrackedAntFX(WorldEditorPane pane, double cx, double cy, double scale, double radAz, double radEl) {
        if (!pane.isAntTrackingEnabled || pane.followedAnt == null) return;

        if (pane.activeSimulation != null) {
            boolean stillExists = false;
            for (Colony c : pane.activeSimulation.getColonies()) {
                for (Individual ind : c.getLivingIndividuals()) {
                    if (ind.getId().equals(pane.followedAnt.getId())) {
                        pane.followedAnt = ind; // Rebind to live instance in case of snapshot restore
                        stillExists = true;
                        break;
                    }
                }
                if (stillExists) break;
            }
            if (!stillExists) {
                pane.followedAnt = null;
                pane.antTrailHistory.clear();
                if (pane.trackedAntPane != null) {
                    pane.trackedAntPane.updateAnt(null, false);
                }
                return;
            }
        }

        // 1. Draw Trajectory Trail (Fil d'Ariane)
        if (pane.antTrailHistory.size() > 1) {
            pane.gc3D.setLineWidth(2.0);
            for (int i = 0; i < pane.antTrailHistory.size() - 1; i++) {
                double[] pt1 = pane.antTrailHistory.get(i);
                double[] pt2 = pane.antTrailHistory.get(i + 1);
                double[] s1 = pane.project3DPoint(pt1[0], pt1[1], pt1[2], cx, cy, scale, radAz, radEl);
                double[] s2 = pane.project3DPoint(pt2[0], pt2[1], pt2[2], cx, cy, scale, radAz, radEl);
                double alpha = (double) (i + 1) / pane.antTrailHistory.size();
                pane.gc3D.setStroke(Color.web("#f59e0b", alpha * 0.85));
                pane.gc3D.strokeLine(s1[0], s1[1], s2[0], s2[1]);
            }
        }

        // 2. Target Spotlight Reticle
        double gx = pane.followedAnt.getX();
        double gy = pane.followedAnt.getY();
        double gz = pane.followedAnt.getZ();
        if (pane.activeSimulation != null) {
            gx = (gx / (float) Math.max(1, pane.activeSimulation.getTerrarium().getWidth())) * WorldEditorPane.GRID_SIZE;
            gy = (gy / (float) Math.max(1, pane.activeSimulation.getTerrarium().getHeight())) * WorldEditorPane.GRID_SIZE;
            int igx = Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, (int) gx));
            int igy = Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, (int) gy));
            gz = pane.heightGrid[igx][igy] * 40.0 + gz * 2.0 + 1.5;
        } else {
            gx = WorldEditorPane.GRID_SIZE / 2.0; gy = WorldEditorPane.GRID_SIZE / 2.0; gz = 20.0;
        }

        double[] p = pane.project3DPoint(gx, gy, gz, cx, cy, scale, radAz, radEl);
        double timeMs = (System.currentTimeMillis() % 2000) / 2000.0;
        double pulseR = 14.0 + Math.sin(timeMs * Math.PI * 2) * 4.0;

        pane.gc3D.setStroke(Color.web("#38bdf8"));
        pane.gc3D.setLineWidth(2.0);
        pane.gc3D.strokeOval(p[0] - pulseR, p[1] - pulseR, pulseR * 2, pulseR * 2);
        pane.gc3D.setStroke(Color.web("#f59e0b"));
        pane.gc3D.setLineWidth(1.2);
        pane.gc3D.strokeOval(p[0] - pulseR * 0.6, p[1] - pulseR * 0.6, pulseR * 1.2, pulseR * 1.2);

        // Crosshairs
        pane.gc3D.strokeLine(p[0] - pulseR - 6, p[1], p[0] - pulseR + 2, p[1]);
        pane.gc3D.strokeLine(p[0] + pulseR - 2, p[1], p[0] + pulseR + 6, p[1]);
        pane.gc3D.strokeLine(p[0], p[1] - pulseR - 6, p[0], p[1] - pulseR + 2);
        pane.gc3D.strokeLine(p[0], p[1] + pulseR - 2, p[0], p[1] + pulseR + 6);

        // 3. Render Live Ant Constants & Metrics HUD Card
        pane.updateTrackedAntTelemetry();
        pane.updateWeatherOverlay();
    }

    public static void drawPheromoneOverlay3D(WorldEditorPane pane, double cx, double cy, double scale, double radAz, double radEl) {
        if (!pane.isSimulationMode || pane.activeSimulation == null) return;
        SparsePheromoneGrid grid = pane.activeSimulation.getPheromoneGrid();
        if (grid == null) return;

        Map<Long, float[]> entries = grid.getAllEntries();
        if (entries.isEmpty()) return;

        double baseDotR = Math.max(2.0, 4.0 * (pane.zoom / 7.5));

        int terrW = (pane.activeSimulation.getTerrarium() != null) ? Math.max(1, pane.activeSimulation.getTerrarium().getWidth()) : WorldEditorPane.GRID_SIZE;
        int terrH = (pane.activeSimulation.getTerrarium() != null) ? Math.max(1, pane.activeSimulation.getTerrarium().getHeight()) : WorldEditorPane.GRID_SIZE;

        for (Map.Entry<Long, float[]> e : entries.entrySet()) {
            long key = e.getKey();
            float[] vals = e.getValue();
            if (vals == null) continue;

            int[] coords = Morton3D.decode(key);
            int x = coords[0];
            int y = coords[1];
            int zVox = coords[2];

            // Normalize terrarium cell coordinates (0..terrW, 0..terrH) to editor grid (0..GRID_SIZE)
            double gx = Math.max(0.0, Math.min(WorldEditorPane.GRID_SIZE - 1.0, (x / (double) terrW) * WorldEditorPane.GRID_SIZE));
            double gy = Math.max(0.0, Math.min(WorldEditorPane.GRID_SIZE - 1.0, (y / (double) terrH) * WorldEditorPane.GRID_SIZE));
            int igx = Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, (int) gx));
            int igy = Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, (int) gy));

            // Extract multi-channel pheromone intensities mapped to PheromoneType indices
            float homeVal = (vals.length > 0 && pane.showHomePheromone) ? vals[0] : 0f;
            float foodVal = (vals.length > 1 && pane.showFoodPheromone) ? vals[1] : 0f;
            float alarmVal = (vals.length > 2 && pane.showAlarmPheromone) ? vals[2] : 0f;
            float recruitVal = (vals.length > 3 && pane.showRecruitmentPheromone) ? vals[3] : 0f;
            float queenVal = (vals.length > 4 && pane.showQueenPheromone) ? vals[4] : 0f;
            float deathVal = (vals.length > 5 && pane.showDeathPheromone) ? vals[5] : 0f;

            float maxVal = Math.max(homeVal, Math.max(foodVal, Math.max(alarmVal, Math.max(recruitVal, Math.max(queenVal, deathVal)))));
            if (maxVal < 0.02f) continue;

            // Chemical channel color mapping
            String colorHex;
            if (alarmVal == maxVal) colorHex = "#ef4444";       // Alarm/Danger: Red
            else if (foodVal == maxVal) colorHex = "#a855f7";   // Food Trail: Purple
            else if (homeVal == maxVal) colorHex = "#0284c7";   // Nest/Home: Blue
            else if (recruitVal == maxVal) colorHex = "#f59e0b";// Recruitment: Amber
            else if (queenVal == maxVal) colorHex = "#ec4899";  // Queen Scent: Pink
            else colorHex = "#64748b";                           // Death/Necrophoric: Slate

            boolean isTranslucent = pane.showTranslucentVolumetricModeCheck != null && pane.showTranslucentVolumetricModeCheck.isSelected();
            // Cut-plane & subterranean occlusion check for pheromones
            double cutRatio = pane.slicePlaneSlider != null ? (pane.slicePlaneSlider.getValue() / 100.0) : 1.0;
            int cutXLimit = (int) (WorldEditorPane.GRID_SIZE * cutRatio);
            // Subterranean vs Surface 3D Z depth detection (nest chambers, tunnels, galleries)
            boolean isSubterraneanPheromone = (zVox != 0);
            if (isSubterraneanPheromone) {
                boolean isPheromoneExposed = !pane.isTerrainVisible || (igx >= cutXLimit) || isTranslucent;
                if (!isPheromoneExposed) continue;
            }

            double depthZOffset = isSubterraneanPheromone
                ? - (Math.abs(zVox > 15 ? zVox - 32 : zVox) * 2.0)
                : 1.5;
            double terrainZ = (pane.heightGrid[igx][igy] * 40.0) + depthZOffset;
            double[] p = pane.project3DPoint(gx, gy, terrainZ, cx, cy, scale, radAz, radEl);
            double alpha = Math.min(0.9, maxVal * 0.75);

            // Forest Canopy Depth & Transparency Masking: Dim/occlude ground pheromones under tree canopy
            if (!isSubterraneanPheromone && pane.isForestArea(igx, igy)) {
                if (pane.showVegetationCheck != null && pane.showVegetationCheck.isSelected() && !isTranslucent) {
                    alpha *= 0.18; // Occlude pheromones under dense tree foliage
                }
            }
            if (alpha < 0.01) continue;

            switch (pane.pheromoneRenderMode) {
                case HEATMAP_GRADIENT -> {
                    // Mode 1: Continuous Density Surface / Fading Heatmap Gradient
                    double outerHaloR = baseDotR * (2.2 + maxVal * 3.2);
                    double innerCoreR = baseDotR * (0.9 + maxVal * 1.5);
                    pane.gc3D.setFill(Color.web(colorHex, alpha * 0.28));
                    pane.gc3D.fillOval(p[0] - outerHaloR, p[1] - outerHaloR * 0.7, outerHaloR * 2, outerHaloR * 1.4);
                    pane.gc3D.setFill(Color.web(colorHex, Math.min(1.0, alpha * 0.85 + 0.15)));
                    pane.gc3D.fillOval(p[0] - innerCoreR, p[1] - innerCoreR * 0.7, innerCoreR * 2, innerCoreR * 1.4);
                }
                case VOXEL_PARTICLES -> {
                    // Mode 2: Discrete Points / Voxels with Dynamic Radius & Core Glow
                    double particleR = baseDotR * (0.8 + maxVal * 1.3);
                    pane.gc3D.setFill(Color.web(colorHex, Math.min(1.0, alpha + 0.15)));
                    pane.gc3D.fillOval(p[0] - particleR, p[1] - particleR, particleR * 2, particleR * 2);
                    pane.gc3D.setFill(Color.web("#ffffff", Math.min(1.0, alpha + 0.35)));
                    pane.gc3D.fillOval(p[0] - particleR * 0.3, p[1] - particleR * 0.3, particleR * 0.6, particleR * 0.6);
                }
                case HYBRID_GIS -> {
                    // Mode 3: Multi-Layer SIG Hybrid (Gradient Surface Halo + Voxel Core Glow)
                    double outerHaloR = baseDotR * (1.4 + maxVal * 1.8);
                    double coreR = baseDotR * (0.7 + maxVal * 0.8);

                    pane.gc3D.setFill(Color.web(colorHex, alpha * 0.35));
                    pane.gc3D.fillOval(p[0] - outerHaloR, p[1] - outerHaloR * 0.65, outerHaloR * 2, outerHaloR * 1.3);

                    pane.gc3D.setFill(Color.web(colorHex, Math.min(1.0, alpha + 0.25)));
                    pane.gc3D.fillOval(p[0] - coreR, p[1] - coreR, coreR * 2, coreR * 2);
                    pane.gc3D.setFill(Color.web("#fef08a", Math.min(1.0, alpha + 0.4)));
                    pane.gc3D.fillOval(p[0] - coreR * 0.4, p[1] - coreR * 0.4, coreR * 0.8, coreR * 0.8);
                }
            }
        }
    }

    public static void drawWeatherOverlay3D(WorldEditorPane pane, double w, double h) {
        if (pane.activeSimulation == null || pane.activeSimulation.getWeather() == null) {
            return;
        }

        WeatherSystem weather = pane.activeSimulation.getWeather();
        WeatherMarkovChain.WeatherState state = weather.getWeatherState();
        if (state == null) state = WeatherMarkovChain.WeatherState.SUNNY;

        long timeMs = System.currentTimeMillis();
        double timeSec = timeMs / 1000.0;

        // Miss Météo Polling logic: Update photoperiod & forecast report every 10 seconds (10,000ms)
        if (timeMs - pane.lastMeteoPollMs > 10000 || pane.cachedMeteoReportText.isEmpty()) {
            pane.lastMeteoPollMs = timeMs;

            int simHour = 14;
            int simMinute = 33;
            if (pane.activeSimulation != null && pane.activeSimulation.getDayNightCycle() != null) {
                simHour = pane.activeSimulation.getDayNightCycle().getHour();
                simMinute = (int) (pane.activeSimulation.getDayNightCycle().getPhase() * 24.0 * 60.0) % 60;
            }

            double latRad = Math.toRadians(48.8);
            int dayOfYear = 80;
            double declinationRad = Math.toRadians(23.45 * Math.sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))));
            double tanProduct = -Math.tan(latRad) * Math.tan(declinationRad);
            double dayHours = 12.0;
            if (tanProduct < 1.0 && tanProduct > -1.0) {
                dayHours = Math.toDegrees(Math.acos(tanProduct)) / 15.0 * 2.0;
            } else if (tanProduct <= -1.0) {
                dayHours = 24.0;
            } else {
                dayHours = 0.0;
            }

            int dayH = (int) dayHours;
            int dayM = (int) Math.round((dayHours - dayH) * 60);
            double halfDay = dayHours / 2.0;
            int sunriseH = Math.max(0, (int) (12.0 - halfDay));
            int sunriseM = Math.max(0, (int) Math.round(((12.0 - halfDay) - sunriseH) * 60));
            int sunsetH = Math.min(23, (int) (12.0 + halfDay));
            int sunsetM = Math.max(0, (int) Math.round(((12.0 + halfDay) - sunsetH) * 60));

            pane.cachedPhotoperiodText = String.format(Locale.US, "⏱ %02dh%02d | 🌅 Lever: %02dh%02d | 🌇 Coucher: %02dh%02d | Durée: %dh%02dm",
                simHour, simMinute, sunriseH, sunriseM, sunsetH, sunsetM, dayH, dayM);

            String weatherLabel = switch (state) {
                case SUNNY -> "☀️ Ensoleillé, ciel dégagé";
                case PARTLY_CLOUDY -> "⛅ Passages nuageux";
                case OVERCAST -> "☁️ Temps couvert";
                case LIGHT_RAIN -> "🌧️ Pluie fine continue";
                case HEAVY_RAIN -> "🌧️ Fortes précipitations";
                case THUNDERSTORM -> "⛈️ Orageux avec éclairs";
                case SANDSTORM -> "🏜️ Tempête de sable";
                case HAIL -> "🌨️ Averses de grêle";
                case BLIZZARD -> "❄️ Blizzard et vents violents";
                case TEMPEST -> "🌪️ Tempête et bourrasques";
                case HEATWAVE -> "🔥 Vague de chaleur";
                case DROUGHT -> "🌵 Sécheresse";
                case SNOW -> "❄️ Chutes de neige";
                case FOG -> "🌫️ Brouillard matinal dense";
                default -> state.label != null ? state.label : "🌤️ " + state.name();
            };
            pane.cachedMeteoReportText = weatherLabel + String.format(Locale.US, " (%.1f°C, Vent: %.0f km/h %s)",
                weather.getTemperature(), weather.getWindSpeed(), weather.getWindDirection());
        }

        // 1. Atmosphere & Lighting Vignette
        float lightLevel = weather.getLightLevel();
        if (lightLevel < 0.85f || state != WeatherMarkovChain.WeatherState.SUNNY) {
            double opacity = Math.max(0.0, Math.min(0.08, (0.85 - lightLevel) * 0.12));
            if (opacity > 0.005) {
                pane.gc3D.setFill(Color.web("#0f172a", opacity));
                pane.gc3D.fillRect(0, 0, w, h);
            }
        } else {
            pane.gc3D.setFill(Color.web("#fef08a", 0.035));
            pane.gc3D.fillRect(0, 0, w, h);
        }

        // 2. Rolling Cloud Shadows
        if (state == WeatherMarkovChain.WeatherState.OVERCAST ||
            state == WeatherMarkovChain.WeatherState.LIGHT_RAIN ||
            state == WeatherMarkovChain.WeatherState.HEAVY_RAIN ||
            state == WeatherMarkovChain.WeatherState.THUNDERSTORM ||
            state == WeatherMarkovChain.WeatherState.FOG) {

            pane.gc3D.setFill(Color.web("#0f172a", 0.15));
            double cloudX1 = (timeMs * 0.02) % (w + 300) - 150;
            double cloudX2 = (timeMs * 0.035) % (w + 400) - 200;
            pane.gc3D.fillOval(cloudX1, 10, 260, 60);
            pane.gc3D.fillOval(cloudX2, 30, 320, 70);
        }

        // 3. Fog Effect Overlay
        if (state == WeatherMarkovChain.WeatherState.FOG) {
            pane.gc3D.setFill(Color.web("#cbd5e1", 0.22));
            pane.gc3D.fillRect(0, 0, w, h);
        }

        // 4. Precipitation Effects
        float windSpeed = weather.getWindSpeed();
        double effectiveWindAngleRad = Math.toRadians(weather.getWindDirectionAngle()) - Math.toRadians(pane.azimuth);
        double windDx = Math.cos(effectiveWindAngleRad) * (windSpeed * 0.35);
        double windDy = 14.0 + Math.sin(effectiveWindAngleRad) * (windSpeed * 0.15);

        Random r = new Random(54321);

        if (state == WeatherMarkovChain.WeatherState.LIGHT_RAIN ||
            state == WeatherMarkovChain.WeatherState.HEAVY_RAIN ||
            state == WeatherMarkovChain.WeatherState.THUNDERSTORM) {

            int particleCount = (state == WeatherMarkovChain.WeatherState.HEAVY_RAIN ||
                                 state == WeatherMarkovChain.WeatherState.THUNDERSTORM) ? 80 : 35;

            pane.gc3D.setStroke(Color.web("#38bdf8", 0.50));
            pane.gc3D.setLineWidth(1.1);
            for (int i = 0; i < particleCount; i++) {
                double rx = (r.nextDouble() * w + timeSec * (120.0 + windDx * 10.0) + i * 47) % w;
                double ry = (r.nextDouble() * h + timeSec * (350.0 + windDy * 10.0) + i * 61) % h;
                pane.gc3D.strokeLine(rx, ry, rx + windDx, ry + windDy);
            }
        }

        // 5. Snow Flakes
        if (state == WeatherMarkovChain.WeatherState.SNOW ||
            state == WeatherMarkovChain.WeatherState.BLIZZARD) {

            int snowCount = (state == WeatherMarkovChain.WeatherState.BLIZZARD) ? 90 : 40;
            pane.gc3D.setFill(Color.web("#f8fafc", 0.80));
            for (int i = 0; i < snowCount; i++) {
                double sx = (r.nextDouble() * w + timeSec * (40.0 + windDx * 5.0) + Math.sin(timeSec + i) * 15.0 + i * 31) % w;
                double sy = (r.nextDouble() * h + timeSec * (80.0 + windDy * 2.0) + i * 53) % h;
                double size = 1.5 + r.nextDouble() * 2.0;
                pane.gc3D.fillOval(sx, sy, size, size);
            }
        }

        // 6. Hail Impact Particles
        if (state == WeatherMarkovChain.WeatherState.HAIL) {
            pane.gc3D.setFill(Color.web("#f8fafc", 0.90));
            for (int i = 0; i < 30; i++) {
                double hx = (r.nextDouble() * w + timeSec * 60.0 + i * 67) % w;
                double hy = (r.nextDouble() * h + timeSec * 420.0 + i * 89) % h;
                pane.gc3D.fillOval(hx, hy, 3.0, 3.0);
                if (hy > h * 0.6) {
                    pane.gc3D.setStroke(Color.web("#ffffff", 0.50));
                    pane.gc3D.setLineWidth(0.9);
                    pane.gc3D.strokeOval(hx - 3, hy - 1, 6, 2);
                }
            }
        }

        // 7. Thunderstorm Strobe Flash
        if (state == WeatherMarkovChain.WeatherState.THUNDERSTORM) {
            if (r.nextDouble() < 0.02) {
                pane.gc3D.setFill(Color.web("#ffffff", 0.35));
                pane.gc3D.fillRect(0, 0, w, h);

                double strikeDistMeters = 350.0 + r.nextDouble() * 1050.0;
                org.swarmforge.client.audio.SimulationAudioManager.getInstance().triggerLightningThunderStrike(strikeDistMeters);
            }
        }
    }
}
