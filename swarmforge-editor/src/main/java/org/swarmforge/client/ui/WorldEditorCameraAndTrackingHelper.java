package org.swarmforge.client.ui;

import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.TunnelNetwork.TunnelNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Handles camera focus, individual tracking, selection transitions,
 * and view centration for {@link WorldEditorPane}.
 */
public final class WorldEditorCameraAndTrackingHelper {

    private WorldEditorCameraAndTrackingHelper() {}

    public static void setFollowedIndividual(WorldEditorPane pane, Individual individual) {
        pane.followedAnt = individual;
        pane.antTrailHistory.clear();
        if (pane.gameView != null && pane.gameView.getGameApp() != null) {
            pane.gameView.getGameApp().followIndividual(individual != null && individual.getId() != null ? individual.getId().toString() : null);
        }
        if (pane.trackedAntPane != null) pane.trackedAntPane.updateAnt(individual, individual != null);
        if (pane.onIndividualSelectedListener != null && individual != null) pane.onIndividualSelectedListener.accept(individual);
        pane.repaintAllViews();
    }

    public static void selectNextNearestIndividual(WorldEditorPane pane) {
        if (pane.activeSimulation == null) return;
        List<Individual> candidates = new ArrayList<>();
        for (Colony colony : pane.activeSimulation.getColonies()) {
            if (colony != null) {
                for (Individual ind : colony.getLivingIndividuals()) {
                    if (ind != null && ind.isAlive() && ind.getHealth() > 0) candidates.add(ind);
                }
            }
        }
        if (candidates.isEmpty()) return;

        double refX = 32.0, refY = 32.0, refZ = 0.0;
        UUID currentId = (pane.followedAnt != null) ? pane.followedAnt.getId() : null;
        if (pane.followedAnt != null) {
            refX = pane.followedAnt.getX(); refY = pane.followedAnt.getY(); refZ = pane.followedAnt.getZ();
            candidates.removeIf(ind -> ind.getId() != null && ind.getId().equals(currentId));
        }
        if (candidates.isEmpty()) return;

        final double fRefX = refX, fRefY = refY, fRefZ = refZ;
        candidates.sort(Comparator.comparingDouble(ind -> {
            double dx = ind.getX() - fRefX, dy = ind.getY() - fRefY, dz = ind.getZ() - fRefZ;
            return dx * dx + dy * dy + dz * dz;
        }));

        Individual bestMatch = candidates.get(0);
        if (candidates.size() > 1 && !pane.individualNavigationHistory.isEmpty() && bestMatch.getId() != null && bestMatch.getId().equals(pane.individualNavigationHistory.peek())) {
            bestMatch = candidates.get(1);
        }
        if (currentId != null) {
            pane.individualNavigationHistory.push(currentId);
            if (pane.individualNavigationHistory.size() > 50) pane.individualNavigationHistory.removeLast();
        }
        pane.setFollowedIndividual(bestMatch);
        pane.setFollowAntCameraEnabled(true);
        if (pane.trackedAntPane != null) {
            pane.trackedAntPane.setVisible(true);
            pane.trackedAntPane.updateAnt(bestMatch, true);
        }
    }

    public static void selectPreviousNearestIndividual(WorldEditorPane pane) {
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

    public static void resetIndividualTracking(WorldEditorPane pane) {
        if (!javafx.application.Platform.isFxApplicationThread()) {
            javafx.application.Platform.runLater(pane::resetIndividualTracking);
            return;
        }
        pane.followedAnt = null;
        pane.antTrailHistory.clear();
        if (pane.trackedAntPane != null) pane.trackedAntPane.updateAnt(null, false);
        pane.repaintAllViews();
    }

    public static void setFollowedIndividualById(WorldEditorPane pane, String query) {
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
                        match = ind; break;
                    }
                    String fullId = ind.getId() != null ? ind.getId().toString().toLowerCase() : "";
                    if (fullId.equalsIgnoreCase(q) || fullId.startsWith(q)) {
                        match = ind; break;
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
                            match = ind; break;
                        }
                    }
                    if (match != null) break;
                }
            }
        }

        if (match != null) {
            pane.setFollowedIndividual(match);
            pane.setFollowAntCameraEnabled(true);
            if (pane.trackedAntPane != null) pane.trackedAntPane.updateAnt(match, true);
        } else if (pane.trackedAntPane != null) {
            pane.trackedAntPane.setSearchStatusError(I18nManager.getInstance().get("tracked_ant.not_found", query));
        }
    }

    public static void centerCameraOnSelection(WorldEditorPane pane) {
        if (pane.activeSimulation == null || pane.activeSimulation.getTerrarium() == null) return;
        double targetX = -1, targetY = -1, targetZ = 0;
        int tWidth = Math.max(1, pane.activeSimulation.getTerrarium().getWidth());
        int tHeight = Math.max(1, pane.activeSimulation.getTerrarium().getHeight());

        Individual ant = (pane.followedAnt != null) ? pane.followedAnt : (pane.trackedAntPane != null ? pane.trackedAntPane.getCurrentAnt() : null);
        if (ant != null) {
            targetX = (ant.getX() / (double) tWidth) * WorldEditorPane.GRID_SIZE;
            targetY = (ant.getY() / (double) tHeight) * WorldEditorPane.GRID_SIZE;
            int igx = (int) Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, targetX));
            int igy = (int) Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, targetY));
            targetZ = pane.heightGrid[igx][igy] * 40.0 + ant.getZ() * 2.0;
        } else {
            TunnelNode node = (pane.chamberInfoPane != null && pane.chamberInfoPane.getCurrentNode() != null)
                    ? pane.chamberInfoPane.getCurrentNode() : pane.selectedChamberNode;
            if (node != null) {
                targetX = (node.x() / (double) tWidth) * WorldEditorPane.GRID_SIZE;
                targetY = (node.y() / (double) tHeight) * WorldEditorPane.GRID_SIZE;
                int igx = (int) Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, targetX));
                int igy = (int) Math.max(0, Math.min(WorldEditorPane.GRID_SIZE - 1, targetY));
                targetZ = pane.heightGrid[igx][igy] * 40.0 + node.z() * 2.0;
            }
        }

        if (targetX >= 0 && targetY >= 0) {
            double radAz = Math.toRadians(pane.azimuth);
            double radEl = Math.toRadians(pane.elevation);
            double w = (pane.canvas3D != null && pane.canvas3D.getWidth() > 0) ? pane.canvas3D.getWidth() : 800.0;
            double h = (pane.canvas3D != null && pane.canvas3D.getHeight() > 0) ? pane.canvas3D.getHeight() : 600.0;
            double scale = (Math.min(w, h) / 120.0) * (pane.zoom / 7.5);

            double isoX = (targetX - WorldEditorPane.GRID_SIZE / 2.0) * Math.cos(radAz) - (targetY - WorldEditorPane.GRID_SIZE / 2.0) * Math.sin(radAz);
            double isoY = (targetX - WorldEditorPane.GRID_SIZE / 2.0) * Math.sin(radAz) * Math.sin(radEl) + (targetY - WorldEditorPane.GRID_SIZE / 2.0) * Math.cos(radAz) * Math.sin(radEl) - targetZ * Math.cos(radEl);

            pane.pan3DX = -isoX * (scale / 10.0);
            pane.pan3DY = -isoY * (scale / 10.0);
            pane.topPanX = (WorldEditorPane.GRID_SIZE / 2.0 - targetX) * 2.0;
            pane.topPanY = (WorldEditorPane.GRID_SIZE / 2.0 - targetY) * 2.0;
            pane.sidePanX = (WorldEditorPane.GRID_SIZE / 2.0 - targetY) * 2.0;
            pane.repaintAllViews();
        }
    }
}
