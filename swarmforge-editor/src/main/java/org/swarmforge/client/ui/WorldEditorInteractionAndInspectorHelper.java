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
        if (chamberId == null || pane.activeSimulation == null) return;
        for (org.swarmforge.core.domain.Colony colony : pane.activeSimulation.getColonies()) {
            if (colony.getTunnelNetwork() != null && colony.getTunnelNetwork().getNodes() != null) {
                for (org.swarmforge.core.simulation.TunnelNetwork.TunnelNode node : colony.getTunnelNetwork().getNodes()) {
                    if (chamberId.equals(String.valueOf(node.id())) || chamberId.equals(node.type().name())) {
                        pane.selectedChamberNode = node;
                        if (pane.chamberInfoPane != null) {
                            pane.chamberInfoPane.updateChamber(node, colony);
                            pane.chamberInfoPane.setVisible(true);
                            if (pane.showChamberOverlayCheck != null) pane.showChamberOverlayCheck.setSelected(true);
                        }
                        return;
                    }
                }
            }
        }
    }

    static void selectNextNearestIndividual(WorldEditorPane pane) {
        if (pane.activeSimulation == null) return;

        List<org.swarmforge.core.domain.Individual> candidates = new ArrayList<>();
        for (org.swarmforge.core.domain.Colony colony : pane.activeSimulation.getColonies()) {
            if (colony != null) {
                for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                    if (ind != null && ind.isAlive() && ind.getHealth() > 0) {
                        candidates.add(ind);
                    }
                }
            }
        }

        if (candidates.isEmpty()) return;

        double refX = 32.0, refY = 32.0, refZ = 0.0;
        UUID currentId = (pane.followedAnt != null) ? pane.followedAnt.getId() : null;

        if (pane.followedAnt != null) {
            refX = pane.followedAnt.getX();
            refY = pane.followedAnt.getY();
            refZ = pane.followedAnt.getZ();
            candidates.removeIf(ind -> ind.getId() != null && ind.getId().equals(currentId));
        }

        if (candidates.isEmpty()) return;

        final double fRefX = refX, fRefY = refY, fRefZ = refZ;
        candidates.sort(Comparator.comparingDouble(ind -> {
            double dx = ind.getX() - fRefX;
            double dy = ind.getY() - fRefY;
            double dz = ind.getZ() - fRefZ;
            return dx * dx + dy * dy + dz * dz;
        }));

        org.swarmforge.core.domain.Individual bestMatch = candidates.get(0);
        if (candidates.size() > 1 && !pane.individualNavigationHistory.isEmpty() && bestMatch.getId() != null && bestMatch.getId().equals(pane.individualNavigationHistory.peek())) {
            bestMatch = candidates.get(1);
        }

        if (currentId != null) {
            pane.individualNavigationHistory.push(currentId);
            if (pane.individualNavigationHistory.size() > 50) {
                pane.individualNavigationHistory.removeLast();
            }
        }

        pane.setFollowedIndividual(bestMatch);
        pane.setFollowAntCameraEnabled(true);
        if (pane.trackedAntPane != null) {
            pane.trackedAntPane.setVisible(true);
            pane.trackedAntPane.updateAnt(bestMatch, true);
        }
    }

    static void selectPreviousNearestIndividual(WorldEditorPane pane) {
        if (pane.activeSimulation == null) return;

        while (!pane.individualNavigationHistory.isEmpty()) {
            UUID prevId = pane.individualNavigationHistory.pop();
            for (org.swarmforge.core.domain.Colony colony : pane.activeSimulation.getColonies()) {
                if (colony != null) {
                    for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                        if (ind != null && ind.isAlive() && ind.getId() != null && ind.getId().equals(prevId)) {
                            pane.setFollowedIndividual(ind);
                            pane.setFollowAntCameraEnabled(true);
                            if (pane.trackedAntPane != null) {
                                pane.trackedAntPane.setVisible(true);
                                pane.trackedAntPane.updateAnt(ind, true);
                            }
                            return;
                        }
                    }
                }
            }
        }

        selectNextNearestIndividual(pane);
    }

    static void setFollowedIndividualById(WorldEditorPane pane, String query) {
        if (query == null || query.trim().isEmpty()) return;
        String q = query.trim().toLowerCase();
        org.swarmforge.core.domain.Individual match = null;

        if (pane.activeSimulation != null) {
            long targetIndividualNumber = -1;
            if (q.startsWith("ant_") || q.startsWith("insect_") || q.startsWith("ind_")) {
                try {
                    int underIdx = q.indexOf('_');
                    targetIndividualNumber = Long.parseLong(q.substring(underIdx + 1));
                } catch (Exception ignored) {}
            } else {
                try { targetIndividualNumber = Long.parseLong(q); } catch (Exception ignored) {}
            }

            for (org.swarmforge.core.domain.Colony colony : pane.activeSimulation.getColonies()) {
                for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                    if (targetIndividualNumber > 0 && ind.getIndividualNumber() == targetIndividualNumber) {
                        match = ind;
                        break;
                    }
                    String fullId = ind.getId() != null ? ind.getId().toString().toLowerCase() : "";
                    if (fullId.equalsIgnoreCase(q) || fullId.startsWith(q)) {
                        match = ind;
                        break;
                    }
                }
                if (match != null) break;
            }

            if (match == null) {
                for (org.swarmforge.core.domain.Colony colony : pane.activeSimulation.getColonies()) {
                    for (org.swarmforge.core.domain.Individual ind : colony.getLivingIndividuals()) {
                        String fullId = ind.getId() != null ? ind.getId().toString().toLowerCase() : "";
                        String caste = ind.getCaste() != null ? ind.getCaste().name().toLowerCase() : "";
                        if (fullId.contains(q) || caste.equalsIgnoreCase(q) || caste.contains(q)) {
                            match = ind;
                            break;
                        }
                    }
                    if (match != null) break;
                }
            }
        }

        if (match != null) {
            pane.setFollowedIndividual(match);
            pane.setFollowAntCameraEnabled(true);
            if (pane.trackedAntPane != null) {
                pane.trackedAntPane.updateAnt(match, true);
            }
        } else if (pane.trackedAntPane != null) {
            pane.trackedAntPane.setSearchStatusError(I18nManager.getInstance().get("tracked_ant.not_found", query));
        }
    }

    static void centerCameraOnSelection(WorldEditorPane pane) {
        if (pane.activeSimulation == null || pane.activeSimulation.getTerrarium() == null) return;
        double targetX = -1, targetY = -1, targetZ = 0;
        int tWidth = Math.max(1, pane.activeSimulation.getTerrarium().getWidth());
        int tHeight = Math.max(1, pane.activeSimulation.getTerrarium().getHeight());

        org.swarmforge.core.domain.Individual ant = (pane.followedAnt != null) ? pane.followedAnt : (pane.trackedAntPane != null ? pane.trackedAntPane.getCurrentAnt() : null);
        if (ant != null) {
            targetX = (ant.getX() / (double) tWidth) * GRID_SIZE;
            targetY = (ant.getY() / (double) tHeight) * GRID_SIZE;
            int igx = (int) Math.max(0, Math.min(GRID_SIZE - 1, targetX));
            int igy = (int) Math.max(0, Math.min(GRID_SIZE - 1, targetY));
            targetZ = pane.heightGrid[igx][igy] * 40.0 + ant.getZ() * 2.0;
        } else if (pane.trackedAntPane != null && pane.trackedAntPane.getCurrentPredator() != null) {
            var pred = pane.trackedAntPane.getCurrentPredator();
            targetX = (pred.getX() / (double) tWidth) * GRID_SIZE;
            targetY = (pred.getY() / (double) tHeight) * GRID_SIZE;
            int igx = (int) Math.max(0, Math.min(GRID_SIZE - 1, targetX));
            int igy = (int) Math.max(0, Math.min(GRID_SIZE - 1, targetY));
            targetZ = pane.heightGrid[igx][igy] * 40.0 + pred.getZ() * 2.0;
        } else {
            org.swarmforge.core.simulation.TunnelNetwork.TunnelNode node = (pane.chamberInfoPane != null && pane.chamberInfoPane.getCurrentNode() != null)
                    ? pane.chamberInfoPane.getCurrentNode() : pane.selectedChamberNode;
            if (node != null) {
                targetX = (node.x() / (double) tWidth) * GRID_SIZE;
                targetY = (node.y() / (double) tHeight) * GRID_SIZE;
                int igx = (int) Math.max(0, Math.min(GRID_SIZE - 1, targetX));
                int igy = (int) Math.max(0, Math.min(GRID_SIZE - 1, targetY));
                targetZ = pane.heightGrid[igx][igy] * 40.0 + node.z() * 2.0;
            }
        }

        if (targetX >= 0 && targetY >= 0) {
            double radAz = Math.toRadians(pane.azimuth);
            double radEl = Math.toRadians(pane.elevation);
            double w = (pane.canvas3D != null && pane.canvas3D.getWidth() > 0) ? pane.canvas3D.getWidth() : 800.0;
            double h = (pane.canvas3D != null && pane.canvas3D.getHeight() > 0) ? pane.canvas3D.getHeight() : 600.0;
            double scale = (Math.min(w, h) / 120.0) * (pane.zoom / 7.5);

            double isoX = (targetX - GRID_SIZE / 2.0) * Math.cos(radAz) - (targetY - GRID_SIZE / 2.0) * Math.sin(radAz);
            double isoY = (targetX - GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (targetY - GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - targetZ * Math.cos(radEl);

            pane.pan3DX = -isoX * (scale / 10.0);
            pane.pan3DY = -isoY * (scale / 10.0);

            pane.topPanX = (GRID_SIZE / 2.0 - targetX) * 2.0;
            pane.topPanY = (GRID_SIZE / 2.0 - targetY) * 2.0;
            pane.sidePanX = (GRID_SIZE / 2.0 - targetY) * 2.0;

            pane.repaintAllViews();
        }
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
        int bestX = -1, bestY = -1, bestD = 0;
        double minDstSq = Double.MAX_VALUE;
        int step = 2;

        double cutRatio = pane.slicePlaneSlider != null ? (pane.slicePlaneSlider.getValue() / 100.0) : 1.0;
        int cutXLimit = (int) (GRID_SIZE * cutRatio);
        double targetDepthVal = pane.depthSlider != null ? pane.depthSlider.getValue() : 1.5;
        double maxDepthPx = targetDepthVal * 22.0;
        double layerDepthPx = maxDepthPx / SOIL_DEPTH;

        boolean isCutPlaneFacingCamera = Math.sin(radAz) >= 0;
        if (cutXLimit > 0 && cutXLimit < GRID_SIZE && (!pane.isTerrainVisible || isCutPlaneFacingCamera)) {
            for (int y = 0; y < GRID_SIZE - step; y += step) {
                double surfZ0 = pane.heightGrid[cutXLimit][y] * 40.0;
                double surfZ1 = pane.heightGrid[cutXLimit][y + step] * 40.0;
                if (pane.carvedVoxelGrid[cutXLimit][y]) surfZ0 -= 15.0;
                if (pane.carvedVoxelGrid[cutXLimit][y + step]) surfZ1 -= 15.0;

                double refZ0 = 32.0;
                double refZ1 = 32.0;

                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double topZ0 = Math.min(surfZ0, refZ0 - d * layerDepthPx);
                    double topZ1 = Math.min(surfZ1, refZ1 - d * layerDepthPx);
                    double botZ0 = Math.min(surfZ0, refZ0 - (d + 1) * layerDepthPx);
                    double botZ1 = Math.min(surfZ1, refZ1 - (d + 1) * layerDepthPx);

                    if (topZ0 <= botZ0 && topZ1 <= botZ1) continue;

                    double[] pTop0 = pane.project3DPoint(cutXLimit, y, topZ0, cx, cy, scale, radAz, radEl);
                    double[] pTop1 = pane.project3DPoint(cutXLimit, y + step, topZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot1 = pane.project3DPoint(cutXLimit, y + step, botZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot0 = pane.project3DPoint(cutXLimit, y, botZ0, cx, cy, scale, radAz, radEl);

                    double midPx = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                    double midPy = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;

                    double dSq = (midPx - mx) * (midPx - mx) + (midPy - my) * (midPy - my);
                    if (dSq < minDstSq) {
                        minDstSq = dSq;
                        bestX = cutXLimit;
                        bestY = y;
                        bestD = d;
                    }
                }
            }
        }

        int[][] skirtEdges = new int[][]{
            {0, 0, GRID_SIZE - 1, 0},
            {GRID_SIZE - 1, 0, GRID_SIZE - 1, GRID_SIZE - 1},
            {0, GRID_SIZE - 1, GRID_SIZE - 1, GRID_SIZE - 1},
            {0, 0, 0, GRID_SIZE - 1}
        };
        for (int[] edge : skirtEdges) {
            int ex0 = edge[0], ey0 = edge[1], ex1 = edge[2], ey1 = edge[3];
            int edx = Integer.compare(ex1, ex0);
            int edy = Integer.compare(ey1, ey0);
            int elen = Math.max(Math.abs(ex1 - ex0), Math.abs(ey1 - ey0));
            for (int i = 0; i < elen; i += step) {
                int wx0 = ex0 + i * edx;
                int wy0 = ey0 + i * edy;
                int wx1 = ex0 + Math.min(elen, i + step) * edx;
                int wy1 = ey0 + Math.min(elen, i + step) * edy;
                double surfZ0 = pane.heightGrid[wx0][wy0] * 40.0;
                double surfZ1 = pane.heightGrid[wx1][wy1] * 40.0;

                for (int d = 0; d < SOIL_DEPTH; d++) {
                    double topZ0 = Math.min(surfZ0, 32.0 - d * layerDepthPx);
                    double topZ1 = Math.min(surfZ1, 32.0 - d * layerDepthPx);
                    double botZ0 = Math.min(surfZ0, 32.0 - (d + 1) * layerDepthPx);
                    double botZ1 = Math.min(surfZ1, 32.0 - (d + 1) * layerDepthPx);
                    if (topZ0 <= botZ0 && topZ1 <= botZ1) continue;

                    double[] pTop0 = pane.project3DPoint(wx0, wy0, topZ0, cx, cy, scale, radAz, radEl);
                    double[] pTop1 = pane.project3DPoint(wx1, wy1, topZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot1 = pane.project3DPoint(wx1, wy1, botZ1, cx, cy, scale, radAz, radEl);
                    double[] pBot0 = pane.project3DPoint(wx0, wy0, botZ0, cx, cy, scale, radAz, radEl);

                    double midPx = (pTop0[0] + pTop1[0] + pBot1[0] + pBot0[0]) / 4.0;
                    double midPy = (pTop0[1] + pTop1[1] + pBot1[1] + pBot0[1]) / 4.0;

                    double dSq = (midPx - mx) * (midPx - mx) + (midPy - my) * (midPy - my);
                    if (dSq < minDstSq) {
                        minDstSq = dSq;
                        bestX = wx0;
                        bestY = wy0;
                        bestD = d;
                    }
                }
            }
        }

        int maxX = Math.min(GRID_SIZE - 1, cutXLimit - 1);
        for (int x = 0; x <= maxX; x += step) {
            for (int y = 0; y < GRID_SIZE; y += step) {
                int x2 = Math.min(maxX, x + step);
                int y2 = Math.min(GRID_SIZE - 1, y + step);
                int[][] quadPts = {{x, y}, {x2, y}, {x, y2}, {x2, y2}, {x, GRID_SIZE - 1}, {x2, GRID_SIZE - 1}, {maxX, y}, {maxX, y2}};
                for (int[] qp : quadPts) {
                    int qx = qp[0];
                    int qy = qp[1];
                    double z = pane.heightGrid[qx][qy] * 40.0;
                    double[] p = pane.project3DPoint(qx, qy, z, cx, cy, scale, radAz, radEl);
                    double dSq = (p[0] - mx) * (p[0] - mx) + (p[1] - my) * (p[1] - my);
                    if (dSq < minDstSq) {
                        minDstSq = dSq;
                        bestX = qx;
                        bestY = qy;
                        bestD = 0;
                    }
                }
            }
        }

        if (minDstSq < 4900.0) {
            return new int[]{bestX, bestY, bestD};
        }
        return null;
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
        if (pane.enableSculptingCheck == null || !pane.enableSculptingCheck.isSelected()) return;
        double cw = "3D".equals(viewType) ? pane.canvas3D.getWidth() : ("SIDE".equals(viewType) ? pane.canvasSide.getWidth() : pane.canvasTop.getWidth());
        double ch = "3D".equals(viewType) ? pane.canvas3D.getHeight() : ("SIDE".equals(viewType) ? pane.canvasSide.getHeight() : pane.canvasTop.getHeight());
        if (cw <= 0 || ch <= 0) return;

        int gx, gy;
        if ("3D".equals(viewType)) {
            double radAz = Math.toRadians(pane.azimuth);
            double radEl = Math.toRadians(pane.elevation);
            double cx = cw / 2 + pane.pan3DX;
            double cy = ch / 2 + pane.pan3DY + 40;
            double scale = pane.zoom * 12.0;
            int[] picked = pick3DGridCell(pane, mx, my, cx, cy, scale, radAz, radEl);
            if (picked != null) {
                gx = picked[0];
                gy = picked[1];
            } else {
                gx = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((mx / cw) * GRID_SIZE)));
                gy = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((my / ch) * GRID_SIZE)));
            }
        } else if ("SIDE".equals(viewType)) {
            double blockW = (cw - 20.0) / GRID_SIZE;
            double cutRatio = pane.slicePlaneSlider != null ? (pane.slicePlaneSlider.getValue() / 100.0) : 1.0;
            gx = Math.max(0, Math.min(GRID_SIZE - 1, (int) (GRID_SIZE * cutRatio)));
            int yScreen = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((mx - 10.0) / blockW)));
            gy = GRID_SIZE - 1 - yScreen;
        } else {
            double gridPx = Math.min(cw - 12, ch - 12) * pane.topZoom;
            double cellW = gridPx / GRID_SIZE;
            double cellH = gridPx / GRID_SIZE;
            double offsetX = (cw - gridPx) / 2.0 + pane.topPanX;
            double offsetY = (ch - gridPx) / 2.0 + pane.topPanY;
            gx = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((mx - offsetX) / cellW)));
            gy = (int) Math.max(0, Math.min(GRID_SIZE - 1, Math.floor((my - offsetY) / cellH)));
        }
        int radius = (int) pane.brushRadiusSlider.getValue();
        double strength = pane.brushStrengthSlider.getValue() / 100.0 * 0.08;
        String mode = pane.brushModeSelect.getValue();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    int cx = Math.max(0, Math.min(GRID_SIZE - 1, gx + dx));
                    int cy2 = Math.max(0, Math.min(GRID_SIZE - 1, gy + dy));
                    double falloff = 1.0 - Math.sqrt(dx * dx + dy * dy) / (double) (radius + 1);
                    if (mode != null && (mode.contains("RAISE") || mode.contains("Raise"))) {
                        pane.heightGrid[cx][cy2] = Math.min(1.0, pane.heightGrid[cx][cy2] + strength * falloff);
                    } else if (mode != null && (mode.contains("LOWER") || mode.contains("Lower"))) {
                        pane.heightGrid[cx][cy2] = Math.max(0.01, pane.heightGrid[cx][cy2] - strength * falloff);
                    } else if (mode != null && (mode.contains("SMOOTH") || mode.contains("Smooth"))) {
                        pane.heightGrid[cx][cy2] = TerrainProceduralGenerator.lrp(pane.heightGrid[cx][cy2], 0.5, strength * 2 * falloff);
                    }
                }
            }
        }
        smoothSlopeStabilization(pane);
        pane.repaintAllViews();
    }

    static void smoothSlopeStabilization(WorldEditorPane pane) {
        double maxDelta = 0.18;
        for (int x = 1; x < GRID_SIZE - 1; x++) {
            for (int y = 1; y < GRID_SIZE - 1; y++) {
                double minN = Math.min(Math.min(pane.heightGrid[x - 1][y], pane.heightGrid[x + 1][y]),
                                       Math.min(pane.heightGrid[x][y - 1], pane.heightGrid[x][y + 1]));
                if (pane.heightGrid[x][y] - minN > maxDelta) pane.heightGrid[x][y] = minN + maxDelta;
            }
        }
    }
}
