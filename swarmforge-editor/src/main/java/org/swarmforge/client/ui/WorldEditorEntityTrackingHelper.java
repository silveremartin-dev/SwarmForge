/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Predator;
import org.swarmforge.core.simulation.TunnelNetwork.TunnelNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.swarmforge.client.ui.WorldEditorPane.GRID_SIZE;

/**
 * Entity tracking, navigation history, and camera focusing helper for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorEntityTrackingHelper {

    private WorldEditorEntityTrackingHelper() {}

    static void selectChamberById(WorldEditorPane pane, String chamberId) {
        if (chamberId == null || pane.activeSimulation == null) return;
        for (Colony colony : pane.activeSimulation.getColonies()) {
            if (colony.getTunnelNetwork() != null && colony.getTunnelNetwork().getNodes() != null) {
                for (TunnelNode node : colony.getTunnelNetwork().getNodes()) {
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

        List<Individual> candidates = new ArrayList<>();
        for (Colony colony : pane.activeSimulation.getColonies()) {
            if (colony != null) {
                for (Individual ind : colony.getLivingIndividuals()) {
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

        Individual bestMatch = candidates.get(0);
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
            for (Colony colony : pane.activeSimulation.getColonies()) {
                if (colony != null) {
                    for (Individual ind : colony.getLivingIndividuals()) {
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
        Individual match = null;

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

            for (Colony colony : pane.activeSimulation.getColonies()) {
                for (Individual ind : colony.getLivingIndividuals()) {
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
                for (Colony colony : pane.activeSimulation.getColonies()) {
                    for (Individual ind : colony.getLivingIndividuals()) {
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

        Individual ant = (pane.followedAnt != null) ? pane.followedAnt : (pane.trackedAntPane != null ? pane.trackedAntPane.getCurrentAnt() : null);
        if (ant != null) {
            targetX = (ant.getX() / (double) tWidth) * GRID_SIZE;
            targetY = (ant.getY() / (double) tHeight) * GRID_SIZE;
            int igx = (int) Math.max(0, Math.min(GRID_SIZE - 1, targetX));
            int igy = (int) Math.max(0, Math.min(GRID_SIZE - 1, targetY));
            targetZ = pane.heightGrid[igx][igy] * 40.0 + ant.getZ() * 2.0;
        } else if (pane.trackedAntPane != null && pane.trackedAntPane.getCurrentPredator() != null) {
            Predator pred = pane.trackedAntPane.getCurrentPredator();
            targetX = (pred.getX() / (double) tWidth) * GRID_SIZE;
            targetY = (pred.getY() / (double) tHeight) * GRID_SIZE;
            int igx = (int) Math.max(0, Math.min(GRID_SIZE - 1, targetX));
            int igy = (int) Math.max(0, Math.min(GRID_SIZE - 1, targetY));
            targetZ = pane.heightGrid[igx][igy] * 40.0 + pred.getZ() * 2.0;
        } else {
            TunnelNode node = (pane.chamberInfoPane != null && pane.chamberInfoPane.getCurrentNode() != null)
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
}
