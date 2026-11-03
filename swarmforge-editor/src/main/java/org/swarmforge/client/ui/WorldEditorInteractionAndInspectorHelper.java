/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.swarmforge.client.ui.world.TerrainProceduralGenerator;
import org.swarmforge.client.util.CanvasInteractionHandler;
import org.swarmforge.client.event.UiEvent;
import org.swarmforge.client.event.UiEventBus;
import org.swarmforge.client.util.I18nManager;

import static org.swarmforge.client.ui.WorldEditorPane.GRID_SIZE;
import static org.swarmforge.client.ui.WorldEditorPane.SOIL_DEPTH;

/**
 * Mouse/scroll interaction controller, 3D ray/cell picking, sculpting brush
 * execution, floating voxel inspector, and entity/chamber camera tracking
 * helper for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorInteractionAndInspectorHelper {

    private WorldEditorInteractionAndInspectorHelper() {
    }

    static void bindGameViewListeners(WorldEditorPane pane) {
        if (pane.gameView == null || pane.gameView.getGameApp() == null) return;

        pane.gameView.getGameApp().setSelectionListener(new org.swarmforge.client.view.JmeGameApp.ObjectSelectionListener() {
            @Override
            public void onVoxelSelected(int x, int y, int z, String material, float moisture, float temp, float compaction) {
                pane.updateFloatingMouseInspector(x, y, z, pane.lastGameViewMouseX, pane.lastGameViewMouseY, null);
                UiEventBus.getInstance().publishFx(new UiEvent.VoxelSelectedUiEvent(x, y, z, 0, moisture, temp, 0.0));
            }

            @Override
            public void onIndividualSelected(String id, String caste, String stage, float health, float energy, float hunger, float age, String job) {
                pane.setFollowedIndividualById(id);
                if (pane.trackedAntPane != null) {
                    pane.trackedAntPane.setVisible(true);
                }
                String bioText = "🐜 Individu #" + id + " [" + (caste != null ? caste : "Ouvrière") + "] | Santé: " + (int) (health * 100) + "% | Énergie: " + (int) (energy * 100) + "%";
                pane.updateFloatingMouseInspector(0, 0, 0, pane.lastGameViewMouseX, pane.lastGameViewMouseY, bioText);
                java.util.UUID uid = null;
                try {
                    uid = java.util.UUID.fromString(id);
                } catch (Exception ignored) {}
                UiEventBus.getInstance().publishFx(new UiEvent.IndividualFocusedUiEvent(uid, "Formicidae", caste, health, energy, 0, 0, 0));
            }

            @Override
            public void onChamberSelected(String chamberId) {
                pane.selectChamberById(chamberId);
                if (pane.chamberInfoPane != null) {
                    pane.chamberInfoPane.setVisible(true);
                }
                pane.updateFloatingMouseInspector(0, 0, 0, pane.lastGameViewMouseX, pane.lastGameViewMouseY, "🏛️ Cavité: " + chamberId);
            }

            @Override
            public void onHoverInfo(String text) {
                if (text != null && text.contains("📍 Voxel")) {
                    // Handled by onVoxelSelected
                } else if (text != null && pane.isVoxelInfoVisible) {
                    pane.lblFInspTitle.setText("🔍 Inspection Ciblée");
                    pane.lblFInspAlt.setText("");
                    pane.lblFInspSubstrat.setText("");
                    pane.lblFInspClimate.setText("");
                    pane.lblFInspChem.setText("");
                    pane.lblFInspGas.setText("");
                    pane.lblFInspPhero.setText("");
                    pane.lblFInspBio.setText(text);
                    pane.floatingMouseInspectorCard.autosize();
                    if (pane.h3d != null) {
                        double cardW = Math.max(380.0, pane.floatingMouseInspectorCard.getWidth());
                        double cardH = Math.max(160.0, pane.floatingMouseInspectorCard.getHeight());
                        double hx = Math.max(10, Math.min(pane.h3d.getWidth() - cardW - 10, pane.lastGameViewMouseX + 15));
                        double hy = Math.max(10, Math.min(pane.h3d.getHeight() - cardH - 10, pane.lastGameViewMouseY - 20));
                        pane.floatingMouseInspectorCard.relocate(hx, hy);
                        pane.floatingMouseInspectorCard.toFront();
                    }
                    pane.floatingMouseInspectorCard.setVisible(true);
                }
            }
        });

        pane.gameView.setOnMouseMoved(e -> {
            pane.lastGameViewMouseX = e.getX();
            pane.lastGameViewMouseY = e.getY();
            if (pane.gameView.getGameApp() != null) {
                pane.gameView.getGameApp().hover(e.getX(), e.getY(), pane.gameView.getWidth(), pane.gameView.getHeight());
            }
        });

        pane.gameView.setOnMouseExited(e -> {
            if (pane.floatingMouseInspectorCard != null) {
                pane.floatingMouseInspectorCard.setVisible(false);
            }
        });

        final double[] lastJmeMouse = new double[2];
        pane.gameView.setOnMousePressed(e -> {
            lastJmeMouse[0] = e.getSceneX();
            lastJmeMouse[1] = e.getSceneY();
        });
        pane.gameView.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastJmeMouse[0];
            double dy = e.getSceneY() - lastJmeMouse[1];
            if (pane.gameView.getGameApp() != null) {
                if (e.isPrimaryButtonDown() && !e.isShiftDown()) {
                    pane.gameView.getGameApp().rotateCamera((float) dx, (float) dy);
                } else if (e.isSecondaryButtonDown() || e.isMiddleButtonDown() || e.isShiftDown()) {
                    pane.gameView.getGameApp().panCamera((float) dx, (float) dy);
                }
            }
            lastJmeMouse[0] = e.getSceneX();
            lastJmeMouse[1] = e.getSceneY();
            pane.lastGameViewMouseX = e.getX();
            pane.lastGameViewMouseY = e.getY();
            if (pane.gameView.getGameApp() != null) {
                pane.gameView.getGameApp().hover(e.getX(), e.getY(), pane.gameView.getWidth(), pane.gameView.getHeight());
            }
            pane.repaintAllViews();
        });
        pane.gameView.setOnScroll(e -> {
            if (pane.gameView.getGameApp() != null) {
                pane.gameView.getGameApp().zoomCamera((float) e.getDeltaY() * 0.05f, e.getX(), e.getY(), pane.gameView.getWidth(), pane.gameView.getHeight());
            }
            pane.repaintAllViews();
        });
        pane.gameView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                if (pane.gameView.getGameApp() != null) {
                    pane.gameView.getGameApp().resetCamera();
                }
                pane.resetAllCameras();
                return;
            }
            if (pane.gameView.getGameApp() != null) {
                pane.gameView.getGameApp().pick(e.getX(), e.getY(), pane.gameView.getWidth(), pane.gameView.getHeight());
            }
        });
    }

    static void selectChamberById(WorldEditorPane pane, String chamberId) {
        WorldEditorEntityTrackingHelper.selectChamberById(pane, chamberId);
    }

    static void selectNextNearestIndividual(WorldEditorPane pane) {
        WorldEditorEntityTrackingHelper.selectNextNearestIndividual(pane);
    }

    static void selectPreviousNearestIndividual(WorldEditorPane pane) {
        WorldEditorEntityTrackingHelper.selectPreviousNearestIndividual(pane);
    }

    static void setFollowedIndividualById(WorldEditorPane pane, String query) {
        WorldEditorEntityTrackingHelper.setFollowedIndividualById(pane, query);
    }

    static void centerCameraOnSelection(WorldEditorPane pane) {
        WorldEditorEntityTrackingHelper.centerCameraOnSelection(pane);
    }

    static void setupMouseControls(WorldEditorPane pane) {
        pane.canvas3D.setOnMouseMoved(e -> updateHoverInfo(pane, e.getX(), e.getY(), "3D"));
        pane.canvas3D.setOnMousePressed(e -> {
            pane.lastMX = e.getX(); pane.lastMY = e.getY();
            if (pane.enableSculptingCheck != null && pane.enableSculptingCheck.isSelected()) {
                handleSculptClick(pane, e.getX(), e.getY(), "3D");
                pane.canvas3D.setCursor(Cursor.CROSSHAIR);
            } else {
                pane.canvas3D.setCursor(Cursor.CLOSED_HAND);
            }
        });
        pane.canvas3D.setOnMouseReleased(e -> {
            if (pane.enableSculptingCheck != null && pane.enableSculptingCheck.isSelected()) {
                pane.canvas3D.setCursor(Cursor.CROSSHAIR);
            } else {
                pane.canvas3D.setCursor(Cursor.OPEN_HAND);
            }
        });
        pane.canvas3D.setOnMouseEntered(e -> {
            if (pane.enableSculptingCheck != null && pane.enableSculptingCheck.isSelected()) {
                pane.canvas3D.setCursor(Cursor.CROSSHAIR);
            } else {
                pane.canvas3D.setCursor(Cursor.OPEN_HAND);
            }
        });
        pane.canvas3D.setOnMouseDragged(e -> {
            updateHoverInfo(pane, e.getX(), e.getY(), "3D");
            if (pane.enableSculptingCheck != null && pane.enableSculptingCheck.isSelected()) {
                handleSculptClick(pane, e.getX(), e.getY(), "3D");
            } else {
                double dx = e.getX() - pane.lastMX;
                double dy = e.getY() - pane.lastMY;
                if (e.isSecondaryButtonDown() || e.isMiddleButtonDown() || e.isShiftDown() || e.isControlDown()) {
                    pane.pan3DX += dx; pane.pan3DY += dy;
                    if (pane.isSync()) {
                        double radAz = Math.toRadians(pane.azimuth);
                        double sinAz = Math.sin(radAz);
                        double cosAz = Math.cos(radAz);
                        double sinEl = Math.max(0.15, Math.sin(Math.toRadians(pane.elevation)));
                        double topDx = (dx * cosAz + (dy / sinEl) * sinAz) * 0.45;
                        double topDy = (-dx * sinAz + (dy / sinEl) * cosAz) * 0.45;
                        pane.topPanX += topDx;
                        pane.topPanY += topDy;
                        pane.sidePanX = pane.topPanX;
                        pane.sidePanY = pane.topPanY;
                    }
                } else {
                    pane.azimuth = (pane.azimuth + dx * 0.55) % 360;
                    if (pane.azimuth < 0) pane.azimuth += 360;
                    pane.elevation = Math.max(5, Math.min(85, pane.elevation - dy * 0.35));
                }
                pane.lastMX = e.getX(); pane.lastMY = e.getY();
            }
            pane.repaintAllViews();
        });
        pane.canvas3D.setOnScroll(e -> {
            double oldZoom = pane.zoom;
            pane.zoom = Math.max(1.5, Math.min(600.0, pane.zoom + e.getDeltaY() * 0.05 * (pane.zoom / 15.0 + 0.5)));
            if (pane.zoom != oldZoom) {
                double scaleRatio = pane.zoom / oldZoom;
                double cx = pane.canvas3D.getWidth() / 2.0;
                double cy = pane.canvas3D.getHeight() / 2.0;
                pane.pan3DX = CanvasInteractionHandler.calculatePointerAnchoredPan(pane.pan3DX, e.getX(), cx, scaleRatio);
                pane.pan3DY = CanvasInteractionHandler.calculatePointerAnchoredPan(pane.pan3DY, e.getY(), cy, scaleRatio);
                if (pane.isSync()) {
                    pane.sideZoom = Math.max(0.1, Math.min(100.0, pane.zoom / 7.5));
                    pane.topZoom = pane.sideZoom;
                }
            }
            pane.repaintAllViews();
        });
        pane.canvas3D.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) { pane.resetAllCameras(); return; }
            if (pane.enableSculptingCheck != null && pane.enableSculptingCheck.isSelected()) return;

            double mx = e.getX();
            double my = e.getY();
            double w = pane.canvas3D.getWidth();
            double h = pane.canvas3D.getHeight();
            double radAz = Math.toRadians(pane.azimuth);
            double radEl = Math.toRadians(pane.elevation);
            double cx = w / 2 + pane.pan3DX;
            double cy = h / 2 + pane.pan3DY + 40;
            double scale = pane.zoom * 12.0;

            org.swarmforge.core.domain.Individual clickedAnt = null;
            org.swarmforge.core.domain.Predator clickedPredator = null;
            org.swarmforge.core.simulation.TunnelNetwork.TunnelNode clickedChamber = null;
            org.swarmforge.core.domain.Colony chamberColony = null;

            double minEntityDistSq = 900.0;
            double minChamberDistSq = 625.0;

            if (pane.activeSimulation != null) {
                for (org.swarmforge.core.domain.Colony colony : pane.activeSimulation.getColonies()) {
                    for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                        double ax = ind.getX();
                        double ay = ind.getY();
                        double az = ind.getZ();

                        double gx = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (ax / (float) Math.max(1, pane.activeSimulation.getTerrarium().getWidth())) * GRID_SIZE));
                        double gy = Math.max(0.0, Math.min(GRID_SIZE - 1.0, (ay / (float) Math.max(1, pane.activeSimulation.getTerrarium().getHeight())) * GRID_SIZE));
                        int igx = (int) gx;
                        int igy = (int) gy;
                        double gz = pane.heightGrid[igx][igy] * 40.0 + az * 2.0 + 1.5;

                        double[] p = pane.project3DPoint(gx, gy, gz, cx, cy, scale, radAz, radEl);
                        double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                        if (dSq < minEntityDistSq) {
                            minEntityDistSq = dSq;
                            clickedAnt = ind;
                            clickedPredator = null;
                            clickedChamber = null;
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
                            clickedPredator = pred;
                            clickedAnt = null;
                            clickedChamber = null;
                        }
                    }
                }

                if (clickedAnt == null && clickedPredator == null) {
                    int tWidth = Math.max(1, pane.activeSimulation.getTerrarium().getWidth());
                    int tHeight = Math.max(1, pane.activeSimulation.getTerrarium().getHeight());
                    int tDepth = Math.max(1, pane.activeSimulation.getTerrarium().getDepth());
                    for (org.swarmforge.core.domain.Colony colony : pane.activeSimulation.getColonies()) {
                        if (colony.getTunnelNetwork() != null && colony.getTunnelNetwork().getNodes() != null) {
                            for (org.swarmforge.core.simulation.TunnelNetwork.TunnelNode node : colony.getTunnelNetwork().getNodes()) {
                                double gx = (node.x() / (double) tWidth) * GRID_SIZE;
                                double gy = (node.y() / (double) tHeight) * GRID_SIZE;
                                int igx = Math.max(0, Math.min(GRID_SIZE - 1, (int) gx));
                                int igy = Math.max(0, Math.min(GRID_SIZE - 1, (int) gy));
                                float sElev = pane.activeSimulation.getTerrarium().getSurfaceElevation(node.x(), node.y());
                                double gz = pane.heightGrid[igx][igy] * 40.0 + ((node.z() - sElev) / (double) tDepth) * 40.0;

                                double[] p = pane.project3DPoint(gx, gy, gz, cx, cy, scale, radAz, radEl);
                                double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                                if (dSq < minChamberDistSq) {
                                    minChamberDistSq = dSq;
                                    clickedChamber = node;
                                    chamberColony = colony;
                                }
                            }
                        }
                    }
                }
            }

            if (clickedAnt != null && pane.isAntTrackingEnabled) {
                pane.setFollowedIndividual(clickedAnt);
                if (pane.trackedAntPane != null) {
                    pane.trackedAntPane.updateAnt(clickedAnt, true);
                    pane.trackedAntPane.setVisible(true);
                    if (pane.showAntTrackingCheck != null) pane.showAntTrackingCheck.setSelected(true);
                }
            } else if (clickedPredator != null && pane.isAntTrackingEnabled) {
                if (pane.trackedAntPane != null) {
                    pane.trackedAntPane.updatePredator(clickedPredator, true);
                    pane.trackedAntPane.setVisible(true);
                    if (pane.showAntTrackingCheck != null) pane.showAntTrackingCheck.setSelected(true);
                }
            } else if (clickedChamber != null) {
                pane.selectedChamberNode = clickedChamber;
                if (pane.chamberInfoPane != null) {
                    pane.chamberInfoPane.updateChamber(clickedChamber, chamberColony);
                    pane.chamberInfoPane.setVisible(pane.isChamberOverlayVisible());
                    if (pane.showChamberOverlayCheck != null) pane.showChamberOverlayCheck.setSelected(true);
                }
            } else if (pane.trackedAntPane != null) {
                pane.trackedAntPane.clearSearchStatus();
            }
        });

        pane.canvasSide.setOnMouseMoved(e -> updateHoverInfo(pane, e.getX(), e.getY(), "SIDE"));
        pane.canvasSide.setOnMousePressed(e -> { pane.lastSideMX = e.getX(); pane.lastSideMY = e.getY(); handleSculptClick(pane, e.getX(), e.getY(), "SIDE"); });
        pane.canvasSide.setOnMouseDragged(e -> {
            updateHoverInfo(pane, e.getX(), e.getY(), "SIDE");
            if (pane.enableSculptingCheck != null && pane.enableSculptingCheck.isSelected()) {
                handleSculptClick(pane, e.getX(), e.getY(), "SIDE");
            } else {
                double dx = e.getX() - pane.lastSideMX;
                double dy = e.getY() - pane.lastSideMY;
                pane.sidePanX += dx; pane.sidePanY += dy;
                if (pane.isSync()) {
                    pane.topPanX = pane.sidePanX;
                    pane.topPanY = pane.sidePanY;
                }
                pane.lastSideMX = e.getX(); pane.lastSideMY = e.getY();
            }
            pane.repaintAllViews();
        });
        pane.canvasSide.setOnScroll(e -> {
            double oldZoom = pane.sideZoom;
            pane.sideZoom = Math.max(0.1, Math.min(100.0, pane.sideZoom + e.getDeltaY() * 0.008 * (pane.sideZoom / 5.0 + 0.2)));
            if (pane.sideZoom != oldZoom) {
                double scaleRatio = pane.sideZoom / oldZoom;
                double cx = pane.canvasSide.getWidth() / 2.0;
                double cy = pane.canvasSide.getHeight() / 2.0;
                pane.sidePanX = CanvasInteractionHandler.calculatePointerAnchoredPan(pane.sidePanX, e.getX(), cx, scaleRatio);
                pane.sidePanY = CanvasInteractionHandler.calculatePointerAnchoredPan(pane.sidePanY, e.getY(), cy, scaleRatio);
                if (pane.isSync()) {
                    pane.topZoom = pane.sideZoom;
                    pane.zoom = Math.max(1.5, Math.min(600.0, pane.sideZoom * 7.5));
                }
            }
            pane.repaintAllViews();
        });
        pane.canvasSide.setOnMouseClicked(e -> { if (e.getClickCount() == 2) pane.resetAllCameras(); });

        pane.canvasTop.setOnMouseMoved(e -> updateHoverInfo(pane, e.getX(), e.getY(), "TOP"));
        pane.canvasTop.setOnMousePressed(e -> { pane.lastTopMX = e.getX(); pane.lastTopMY = e.getY(); handleSculptClick(pane, e.getX(), e.getY(), "TOP"); });
        pane.canvasTop.setOnMouseDragged(e -> {
            updateHoverInfo(pane, e.getX(), e.getY(), "TOP");
            if (pane.enableSculptingCheck != null && pane.enableSculptingCheck.isSelected()) {
                handleSculptClick(pane, e.getX(), e.getY(), "TOP");
            } else {
                double dx = e.getX() - pane.lastTopMX;
                double dy = e.getY() - pane.lastTopMY;
                pane.topPanX += dx; pane.topPanY += dy;
                if (pane.isSync()) {
                    pane.sidePanX = pane.topPanX;
                    pane.sidePanY = pane.topPanY;
                    double radAz = Math.toRadians(pane.azimuth);
                    double sinAz = Math.sin(radAz);
                    double cosAz = Math.cos(radAz);
                    double sinEl = Math.max(0.15, Math.sin(Math.toRadians(pane.elevation)));
                    pane.pan3DX += (dx * cosAz - dy * sinAz) * 1.5;
                    pane.pan3DY += (dx * sinAz + dy * cosAz) * sinEl * 1.5;
                }
                pane.lastTopMX = e.getX(); pane.lastTopMY = e.getY();
            }
            pane.repaintAllViews();
        });
        pane.canvasTop.setOnScroll(e -> {
            double oldZoom = pane.topZoom;
            pane.topZoom = Math.max(0.1, Math.min(100.0, pane.topZoom + e.getDeltaY() * 0.008 * (pane.topZoom / 5.0 + 0.2)));
            if (pane.topZoom != oldZoom) {
                double scaleRatio = pane.topZoom / oldZoom;
                double cx = pane.canvasTop.getWidth() / 2.0;
                double cy = pane.canvasTop.getHeight() / 2.0;
                pane.topPanX = CanvasInteractionHandler.calculatePointerAnchoredPan(pane.topPanX, e.getX(), cx, scaleRatio);
                pane.topPanY = CanvasInteractionHandler.calculatePointerAnchoredPan(pane.topPanY, e.getY(), cy, scaleRatio);
                if (pane.isSync()) {
                    pane.sideZoom = pane.topZoom;
                    pane.zoom = Math.max(1.5, Math.min(600.0, pane.topZoom * 7.5));
                }
            }
            pane.repaintAllViews();
        });
        pane.canvasTop.setOnMouseClicked(e -> { if (e.getClickCount() == 2) pane.resetAllCameras(); });
    }

    static int[] pick3DGridCell(WorldEditorPane pane, double mx, double my, double cx, double cy, double scale, double radAz, double radEl) {
        return WorldEditorSculptingHelper.pick3DGridCell(pane, mx, my, cx, cy, scale, radAz, radEl);
    }

    static VBox createFloatingMouseInspectorCard(WorldEditorPane pane) {
        return WorldEditorInspectorHudHelper.createFloatingMouseInspectorCard(pane);
    }

    static void updateFloatingMouseInspector(WorldEditorPane pane, int gx, int gy, int gd, double screenX, double screenY, String bioOverride) {
        WorldEditorInspectorHudHelper.updateFloatingMouseInspector(pane, gx, gy, gd, screenX, screenY, bioOverride);
    }

    static void updateHoverInfo(WorldEditorPane pane, double mx, double my, String viewType) {
        WorldEditorInspectorHudHelper.updateHoverInfo(pane, mx, my, viewType);
    }

    static void handleSculptClick(WorldEditorPane pane, double mx, double my, String viewType) {
        WorldEditorSculptingHelper.handleSculptClick(pane, mx, my, viewType);
    }

    static void smoothSlopeStabilization(WorldEditorPane pane) {
        WorldEditorSculptingHelper.smoothSlopeStabilization(pane);
    }
}
