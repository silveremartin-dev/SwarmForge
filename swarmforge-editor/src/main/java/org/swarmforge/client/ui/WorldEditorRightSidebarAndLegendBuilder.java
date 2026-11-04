/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.swarmforge.client.util.I18nManager;

/**
 * Builds the right render options sidebar, interactive geological/chamber/pheromone/caste
 * legends, and live meteorological overlay HUD for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorRightSidebarAndLegendBuilder {

    private WorldEditorRightSidebarAndLegendBuilder() {
    }

    static VBox createStandaloneLegendPanel() {
        return WorldEditorLegendPanelBuilder.createStandaloneLegendPanel();
    }

    static VBox createWeatherOverlayPane(WorldEditorPane pane) {
        return WorldEditorWeatherOverlayHelper.createWeatherOverlayPane(pane);
    }

    static void updateWeatherOverlay(WorldEditorPane pane) {
        WorldEditorWeatherOverlayHelper.updateWeatherOverlay(pane);
    }

    static VBox buildRightRenderOptionsPanel(WorldEditorPane pane) {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(8));
        panel.getStyleClass().add("render-options-panel");

        Label title = new Label();
        title.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render_layers.title"));
        title.setStyle("-fx-font-size: 11.5px; -fx-font-weight: bold;");
        title.getStyleClass().add("legend-title");

        pane.showMinimapCheck = new CheckBox("🗺️ Minimap 2D");
        pane.showMinimapCheck.setSelected(pane.isDualMinimapVisible());
        pane.showMinimapCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.chk.minimap"));
        pane.showMinimapCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("sidebar.chk.minimap.tt"));
        pane.showMinimapCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.setDualMinimapVisible(newV));
        pane.syncViewsCheckBox = new CheckBox("🔄 Synchroniser");
        pane.syncViewsCheckBox.textProperty().bind(I18nManager.getInstance().createStringBinding("minimap.sync"));
        pane.syncViewsCheckBox.setSelected(true);
        pane.syncViewsCheckBox.getStyleClass().add("legend-checkbox");
        VBox.setMargin(pane.syncViewsCheckBox, new Insets(0, 0, 0, 16));

        pane.showLegendCheckBox = new CheckBox("👁️ Afficher les légendes");
        pane.showLegendCheckBox.textProperty().bind(I18nManager.getInstance().createStringBinding("legend.show"));
        pane.showLegendCheckBox.setSelected(true);
        pane.showLegendCheckBox.getStyleClass().add("legend-checkbox");
        pane.showLegendCheckBox.setOnAction(e -> pane.setShowLegend(pane.showLegendCheckBox.isSelected()));

        pane.showTerrainCheck = new CheckBox("🏞️ Render 3D Terrain");
        pane.showTerrainCheck.setSelected(true);
        pane.showTerrainCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.chk.terrain"));
        pane.showTerrainCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.terrain.tt"));
        pane.showTerrainCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.setTerrainVisible(newV));

        pane.showChamferedBezelCheck = new CheckBox("📐 Substrate Chamfered Bezel");
        pane.showChamferedBezelCheck.setSelected(true);
        pane.showChamferedBezelCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.chk.skirt"));
        pane.showChamferedBezelCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.bezel.tt"));
        pane.showChamferedBezelCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.setShow3DSkirt(newV));

        pane.showGravelInclusionsCheck = new CheckBox();
        pane.showGravelInclusionsCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.gravel_inclusions"));
        pane.showGravelInclusionsCheck.setSelected(true);
        pane.showGravelInclusionsCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.gravel.tt"));

        pane.showSubstrateStratigraphyCheck = new CheckBox();
        pane.showSubstrateStratigraphyCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.stratigraphy"));
        pane.showSubstrateStratigraphyCheck.setSelected(true);
        pane.showSubstrateStratigraphyCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.stratigraphy.tt"));

        pane.showTranslucentVolumetricModeCheck = new CheckBox();
        pane.showTranslucentVolumetricModeCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.translucency"));
        pane.showTranslucentVolumetricModeCheck.setSelected(false);
        pane.showTranslucentVolumetricModeCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.translucent.tt"));

        pane.showHumidityCheck = new CheckBox();
        pane.showHumidityCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.humidity_map"));
        pane.showHumidityCheck.setSelected(false);
        pane.showHumidityCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.humidity.tt"));

        pane.showOrganicCheck = new CheckBox(); pane.showOrganicCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.organic")); pane.showOrganicCheck.setSelected(true);
        pane.showEarthCheck = new CheckBox(); pane.showEarthCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.earth")); pane.showEarthCheck.setSelected(true);
        pane.showSandCheck = new CheckBox(); pane.showSandCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.sand")); pane.showSandCheck.setSelected(true);
        pane.showClayCheck = new CheckBox(); pane.showClayCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.clay")); pane.showClayCheck.setSelected(true);
        pane.showSiltCheck = new CheckBox(); pane.showSiltCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.silt")); pane.showSiltCheck.setSelected(true);
        pane.showPeatCheck = new CheckBox(); pane.showPeatCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.peat")); pane.showPeatCheck.setSelected(true);
        pane.showGravelCheck = new CheckBox(); pane.showGravelCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.gravel")); pane.showGravelCheck.setSelected(true);
        pane.showStoneCheck = new CheckBox(); pane.showStoneCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.stone")); pane.showStoneCheck.setSelected(true);

        pane.showGalleriesCheck = new CheckBox("🕳️ Natural Cavities & Voids"); pane.showGalleriesCheck.setSelected(true);
        pane.showGalleriesCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.chk.nest"));
        pane.showGalleriesCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.galleries.tt"));
        pane.showGalleriesCheck.selectedProperty().addListener((o, a, b) -> {
            pane.isGalleriesVisible = b;
            pane.repaintAllViews();
        });

        pane.showVegetationCheck = new CheckBox("🌿 Vegetation & Trees"); pane.showVegetationCheck.setSelected(true);
        pane.showVegetationCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.chk.trees"));
        pane.showRootsCheck = new CheckBox();
        pane.showRootsCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.roots"));
        pane.showRootsCheck.setSelected(true);
        pane.showRootsCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.roots.tt"));
        pane.showPhCheck = new CheckBox();
        pane.showPhCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer.ph_map"));
        pane.showPhCheck.setSelected(false);
        pane.showPhCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.ph.tt"));

        pane.slicePlaneSlider = pane.mkSlider(0.0, 100.0, 100.0);
        pane.addLsn(pane.slicePlaneSlider);
        pane.slicePlaneSlider.valueProperty().addListener((o, a, n) -> {
            if (pane.slicePlaneChangeListener != null) {
                pane.slicePlaneChangeListener.accept(n.doubleValue());
            }
        });

        pane.showAntTrackingCheck = new CheckBox();
        pane.showAntTrackingCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.ant_tracking"));
        pane.showAntTrackingCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.ant_tracking.tt"));
        pane.showAntTrackingCheck.setSelected(pane.isSimulationMode);
        pane.showAntTrackingCheck.setVisible(pane.isSimulationMode);
        pane.showAntTrackingCheck.setManaged(pane.isSimulationMode);
        pane.showAntTrackingCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.setAntTrackingEnabled(newV));

        pane.showChamberOverlayCheck = new CheckBox();
        pane.showChamberOverlayCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.chamber_overlay"));
        pane.showChamberOverlayCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.chamber_overlay.tt"));
        pane.showChamberOverlayCheck.setSelected(false);
        pane.showChamberOverlayCheck.setVisible(pane.isSimulationMode);
        pane.showChamberOverlayCheck.setManaged(pane.isSimulationMode);
        pane.showChamberOverlayCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.setChamberOverlayVisible(newV));

        pane.showElevationIsolinesCheck = new CheckBox();
        pane.showElevationIsolinesCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.isolines_elevation"));
        pane.showElevationIsolinesCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.isolines_elevation.tt"));
        pane.showElevationIsolinesCheck.setSelected(false);
        pane.showElevationIsolinesCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.repaintAllViews());

        pane.showClimateIsolinesCheck = new CheckBox();
        pane.showClimateIsolinesCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.isolines_climate"));
        pane.showClimateIsolinesCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.isolines_climate.tt"));
        pane.showClimateIsolinesCheck.setSelected(false);
        pane.showClimateIsolinesCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.repaintAllViews());

        pane.showPheromoneIsolinesCheck = new CheckBox();
        pane.showPheromoneIsolinesCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.isolines_pheromone"));
        pane.showPheromoneIsolinesCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.isolines_pheromone.tt"));
        pane.showPheromoneIsolinesCheck.setSelected(false);
        pane.showPheromoneIsolinesCheck.selectedProperty().addListener((obs, oldV, newV) -> pane.repaintAllViews());

        pane.showUVVisionModeCheck = new CheckBox();
        pane.showUVVisionModeCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.uv_vision"));
        pane.showUVVisionModeCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.uv_vision.tt"));
        pane.showUVVisionModeCheck.setSelected(false);
        pane.showUVVisionModeCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            pane.isUVVisionMode = newV;
            if (pane.gameView != null && pane.gameView.getGameApp() != null) {
                pane.gameView.getGameApp().setUVVisionMode(newV);
            }
            pane.repaintAllViews();
        });

        pane.showOmmatidialVisionModeCheck = new CheckBox();
        pane.showOmmatidialVisionModeCheck.textProperty().bind(I18nManager.getInstance().createStringBinding("world.render.ommatidial_vision"));
        pane.showOmmatidialVisionModeCheck.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.render.ommatidial_vision.tt"));
        pane.showOmmatidialVisionModeCheck.setSelected(false);
        pane.showOmmatidialVisionModeCheck.selectedProperty().addListener((obs, oldV, newV) -> {
            pane.isOmmatidialVisionMode = newV;
            if (pane.gameView != null && pane.gameView.getGameApp() != null) {
                pane.gameView.getGameApp().setOmmatidialVisionMode(newV);
            }
            pane.repaintAllViews();
        });

        pane.addBoolLsn(pane.showTerrainCheck, pane.showChamferedBezelCheck, pane.showGravelInclusionsCheck, pane.showSubstrateStratigraphyCheck,
                   pane.showTranslucentVolumetricModeCheck, pane.showHumidityCheck,
                   pane.showOrganicCheck, pane.showEarthCheck, pane.showSandCheck, pane.showClayCheck, pane.showSiltCheck, pane.showPeatCheck, pane.showGravelCheck, pane.showStoneCheck, pane.showGalleriesCheck,
                   pane.showVegetationCheck, pane.showRootsCheck, pane.showPhCheck,
                   pane.showAntTrackingCheck, pane.showChamberOverlayCheck,
                   pane.showElevationIsolinesCheck, pane.showClimateIsolinesCheck, pane.showPheromoneIsolinesCheck, pane.showUVVisionModeCheck, pane.showOmmatidialVisionModeCheck);

        Label visHeader = new Label();
        visHeader.textProperty().bind(I18nManager.getInstance().createStringBinding("world.layer_visibility.title"));
        visHeader.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
        visHeader.getStyleClass().add("purple-title");

        VBox visBox = new VBox(4,
            visHeader,
            pane.showMinimapCheck,
            pane.syncViewsCheckBox,
            pane.showLegendCheckBox,
            new Separator(),
            pane.showAntTrackingCheck,
            pane.showChamberOverlayCheck,
            new Separator(),
            pane.showElevationIsolinesCheck,
            new Separator(),
            pane.showTerrainCheck, pane.showOrganicCheck, pane.showEarthCheck, pane.showSandCheck, pane.showClayCheck, pane.showSiltCheck, pane.showPeatCheck, pane.showGravelCheck, pane.showStoneCheck, pane.showGalleriesCheck,
            new Separator(),
            pane.showVegetationCheck, pane.showRootsCheck, pane.showPhCheck, pane.showSubstrateStratigraphyCheck,
            pane.showHumidityCheck
        );

        Label slicePlaneLabel = new Label();
        slicePlaneLabel.textProperty().bind(I18nManager.getInstance().createStringBinding("sidebar.lbl.slice"));

        panel.getChildren().addAll(title, new Separator(), pane.showTerrainCheck, pane.showChamferedBezelCheck, pane.showSubstrateStratigraphyCheck,
                                   slicePlaneLabel, pane.sv(pane.slicePlaneSlider, "%"),
                                   pane.showTranslucentVolumetricModeCheck, new Separator(), visBox);
        return panel;
    }

    static VBox buildLegendPanel(WorldEditorPane pane) {
        return WorldEditorLegendPanelBuilder.buildLegendPanel(pane);
    }
}
