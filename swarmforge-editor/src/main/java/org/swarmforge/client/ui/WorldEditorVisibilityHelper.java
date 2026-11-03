/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.layout.Pane;
import org.swarmforge.client.view.GameViewPane;

/**
 * Handles view mode switches, fullscreen state toggling, and panel visibility
 * synchronization for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorVisibilityHelper {

    private WorldEditorVisibilityHelper() {
    }

    static void setSimulationMode(WorldEditorPane pane, boolean simMode) {
        pane.isSimulationMode = simMode;
        pane.setHideHeaderPresets(simMode);
        pane.setHideConfigPanel(simMode);
        pane.setHideRightRenderOptions(simMode);
        pane.setSlicePlane(100.0);
        if (pane.showAntTrackingCheck != null) {
            pane.showAntTrackingCheck.setVisible(simMode);
            pane.showAntTrackingCheck.setManaged(simMode);
        }
        if (pane.showChamberOverlayCheck != null) {
            pane.showChamberOverlayCheck.setVisible(simMode);
            pane.showChamberOverlayCheck.setManaged(simMode);
        }
        boolean show = (pane.showLegendCheckBox == null || pane.showLegendCheckBox.isSelected());
        if (pane.nestLegendBox != null) { pane.nestLegendBox.setVisible(simMode && show); pane.nestLegendBox.setManaged(simMode && show); }
        if (pane.pheroLegendBox != null) { pane.pheroLegendBox.setVisible(simMode && show); pane.pheroLegendBox.setManaged(simMode && show); }
        if (pane.castesLegendBox != null) { pane.castesLegendBox.setVisible(simMode && show); pane.castesLegendBox.setManaged(simMode && show); }
        if (pane.weatherOverlayPane != null) {
            pane.weatherOverlayPane.setVisible(simMode && pane.isWeatherOverlayVisible);
            pane.weatherOverlayPane.setManaged(simMode && pane.isWeatherOverlayVisible);
        }
        if (pane.gameView != null && pane.gameView.getGameApp() != null) {
            pane.gameView.getGameApp().setSimulationPaused(!simMode);
        }
        updateRightScrollVisibility(pane);
        pane.repaintAllViews();
    }

    static void setRenderMode(WorldEditorPane pane, WorldEditorPane.RenderMode mode) {
        pane.currentRenderMode = mode != null ? mode : WorldEditorPane.RenderMode.REALISTIC;
        boolean isSci = (pane.currentRenderMode == WorldEditorPane.RenderMode.SCIENTIFIC);
        if (pane.canvas3D != null) {
            pane.canvas3D.setVisible(isSci);
            pane.canvas3D.setManaged(isSci);
            if (isSci) pane.canvas3D.toFront();
        }
        if (!isSci) pane.attachGameView();
        else if (pane.gameView != null) {
            pane.gameView.setVisible(false);
            pane.gameView.setManaged(false);
        }
        if (pane.floatingMouseInspectorCard != null) pane.floatingMouseInspectorCard.toFront();
        if (pane.sideMinimapsBox != null) pane.sideMinimapsBox.toFront();
        if (pane.trackedAntPane != null) pane.trackedAntPane.toFront();
        if (pane.chamberInfoPane != null) pane.chamberInfoPane.toFront();
        if (pane.weatherOverlayPane != null) pane.weatherOverlayPane.toFront();
        if (isSci) pane.isActive = true;
        pane.repaintAllViews();
    }

    static void attachGameView(WorldEditorPane pane) {
        if (pane.gameView == null) pane.gameView = GameViewPane.getSharedInstance(1024, 768);
        if (pane.h3d != null && pane.gameView != null) {
            boolean reparented = false;
            if (pane.gameView.getParent() != pane.h3d) {
                if (pane.gameView.getParent() instanceof Pane oldParent) oldParent.getChildren().remove(pane.gameView);
                int idx = pane.h3d.getChildren().indexOf(pane.canvas3D);
                pane.h3d.getChildren().add(Math.max(0, idx + 1), pane.gameView);
                reparented = true;
            }
            boolean isSci = (pane.currentRenderMode == WorldEditorPane.RenderMode.SCIENTIFIC);
            pane.gameView.setVisible(!isSci);
            pane.gameView.setManaged(!isSci);
            if (reparented) {
                pane.bindGameViewListeners();
                if (pane.gameView.getGameApp() != null) {
                    pane.gameView.getGameApp().setSimulationPaused(!pane.isSimulationMode);
                    if (pane.activeSimulation != null) {
                        pane.gameView.getGameApp().setSimulation(pane.activeSimulation);
                        if (pane.activeSimulation.getTerrarium() != null) {
                            pane.gameView.getGameApp().renderTerrarium(pane.activeSimulation.getTerrarium());
                        }
                    }
                }
            }
            if (!isSci) pane.gameView.setRenderMode(pane.currentRenderMode);
        }
    }

    static void setFullscreenMode(WorldEditorPane pane, boolean fs) {
        pane.setDualMinimapVisible(!fs);
        pane.setHeaderVisible(!fs);
        if (fs) {
            pane.previousZoom = pane.zoom;
            pane.zoom = Math.min(80.0, pane.zoom * 1.25);
            if (pane.isSync()) {
                pane.sideZoom = Math.max(0.3, Math.min(20.0, pane.zoom / 7.5));
                pane.topZoom = pane.sideZoom;
            }
            pane.setHideHeaderPresets(true);
            pane.setHideConfigPanel(true);
            pane.setHideRightRenderOptions(true);
            pane.setRightLegendVisible(false);
        } else {
            pane.zoom = pane.previousZoom;
            if (pane.isSync()) {
                pane.sideZoom = Math.max(0.3, Math.min(6.0, pane.zoom / 7.5));
                pane.topZoom = pane.sideZoom;
            }
            if (!pane.isSimulationMode) {
                pane.setHideHeaderPresets(false);
                pane.setHideConfigPanel(false);
                pane.setHideRightRenderOptions(false);
            }
            updateRightScrollVisibility(pane);
        }
        pane.repaintAllViews();
    }

    static void updateRightScrollVisibility(WorldEditorPane pane) {
        if (pane.rightScroll != null) {
            boolean hasLegend = (pane.legendPanel != null && pane.legendPanel.isVisible()
                    && (pane.showLegendCheckBox == null || pane.showLegendCheckBox.isSelected()));
            boolean hasOptions = (pane.rightRenderOptions != null && pane.rightRenderOptions.isVisible());
            boolean hasVisibleContent = hasLegend || hasOptions;
            pane.rightScroll.setVisible(hasVisibleContent);
            pane.rightScroll.setManaged(hasVisibleContent);
        }
    }
}
