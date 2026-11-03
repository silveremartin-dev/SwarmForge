/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.swarmforge.client.audio.SimulationAudioManager;
import org.swarmforge.client.ui.WorldEditorPane;
import org.swarmforge.client.util.I18nManager;

import java.util.Locale;

/**
 * Builds the render layer checkboxes/toggles (realistic, scientific, gamified, isolines,
 * UV/ommatidial vision, slice plane, nest, pheromones) and the audio mixer section
 * for {@link ClientViewportSidebarBuilder}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class ClientViewportLayersPanelBuilder {

    private final SwarmForgeClient client;
    private final I18nManager i18n = I18nManager.getInstance();

    ClientViewportLayersPanelBuilder(SwarmForgeClient client) {
        this.client = client;
    }

    VBox buildRenderLayersSection(CheckBox chkMinimap) {
        VBox renderSection = new VBox(6);
        renderSection.getStyleClass().add("sidebar-section-card");
        Label lblRenderMode = new Label();
        lblRenderMode.textProperty().bind(i18n.createStringBinding("sidebar.render_layers.title"));
        lblRenderMode.getStyleClass().add("sidebar-section-label");

        ComboBox<String> comboRenderMode = new ComboBox<>();
        comboRenderMode.getItems().addAll(
            "🌿 Mode Réaliste (Naturaliste Photoréaliste)",
            "🔬 Mode Scientifique (Minimaliste Structural)",
            "🎮 Mode Gamifié (Voxel / Minecraft)"
        );
        comboRenderMode.setMaxWidth(Double.MAX_VALUE);
        comboRenderMode.getStyleClass().add("compact-combo");
        comboRenderMode.getSelectionModel().selectedItemProperty().addListener((o, oldV, newV) -> {
            if (newV == null || client.simWorldViewer == null) return;
            if (newV.contains("Scientifique")) {
                client.simWorldViewer.setRenderMode(WorldEditorPane.RenderMode.SCIENTIFIC);
            } else if (newV.contains("Gamifié")) {
                client.simWorldViewer.setRenderMode(WorldEditorPane.RenderMode.GAMIFIED);
            } else {
                client.simWorldViewer.setRenderMode(WorldEditorPane.RenderMode.REALISTIC);
            }
        });
        comboRenderMode.getSelectionModel().select(0);

        chkMinimap.textProperty().bind(i18n.createStringBinding("sidebar.chk.minimap"));
        chkMinimap.setSelected(true);
        chkMinimap.getStyleClass().add("compact-checkbox");
        Tooltip ttMinimap = new Tooltip();
        ttMinimap.textProperty().bind(i18n.createStringBinding("sidebar.chk.minimap.tt"));
        chkMinimap.setTooltip(ttMinimap);
        chkMinimap.selectedProperty().addListener((o, a, b) -> client.simWorldViewer.setDualMinimapVisible(b));

        CheckBox chkSyncMinimap = new CheckBox();
        chkSyncMinimap.textProperty().bind(i18n.createStringBinding("minimap.sync"));
        chkSyncMinimap.setSelected(true);
        chkSyncMinimap.getStyleClass().add("compact-checkbox");
        VBox.setMargin(chkSyncMinimap, new Insets(0, 0, 0, 16));
        chkSyncMinimap.selectedProperty().addListener((o, a, b) -> {
            if (client.simWorldViewer != null) client.simWorldViewer.setSyncViews(b);
        });

        CheckBox chkShowLegend = new CheckBox();
        chkShowLegend.textProperty().bind(i18n.createStringBinding("legend.show"));
        chkShowLegend.setSelected(true);
        chkShowLegend.getStyleClass().add("compact-checkbox");
        chkShowLegend.selectedProperty().addListener((o, a, b) -> {
            if (client.simWorldViewer != null) client.simWorldViewer.setShowLegend(b);
        });

        CheckBox chkTerrain = new CheckBox();
        chkTerrain.textProperty().bind(i18n.createStringBinding("sidebar.chk.terrain"));
        chkTerrain.setSelected(true);
        chkTerrain.getStyleClass().add("compact-checkbox");
        Tooltip ttTerrain = new Tooltip();
        ttTerrain.textProperty().bind(i18n.createStringBinding("world.render.terrain.tt"));
        chkTerrain.setTooltip(ttTerrain);
        chkTerrain.selectedProperty().addListener((o, a, b) -> {
            if (client.gameView != null && client.gameView.getGameApp() != null) client.gameView.getGameApp().setTerrainVisible(b);
            client.simWorldViewer.setTerrainVisible(b);
        });

        CheckBox chkTrees = new CheckBox();
        chkTrees.textProperty().bind(i18n.createStringBinding("sidebar.chk.trees"));
        chkTrees.setSelected(true);
        chkTrees.getStyleClass().add("compact-checkbox");
        Tooltip ttTrees = new Tooltip();
        ttTrees.textProperty().bind(i18n.createStringBinding("sidebar.chk.trees.tt"));
        chkTrees.setTooltip(ttTrees);
        chkTrees.selectedProperty().addListener((o, a, b) -> client.simWorldViewer.setShowTrees(b));

        CheckBox chkSkirt = new CheckBox();
        chkSkirt.textProperty().bind(i18n.createStringBinding("sidebar.chk.skirt"));
        chkSkirt.setSelected(true);
        chkSkirt.getStyleClass().add("compact-checkbox");
        Tooltip ttSkirt = new Tooltip();
        ttSkirt.textProperty().bind(i18n.createStringBinding("world.render.bezel.tt"));
        chkSkirt.setTooltip(ttSkirt);
        chkSkirt.selectedProperty().addListener((o, a, b) -> client.simWorldViewer.setShow3DSkirt(b));

        client.sliceSlider = new Slider(0, 100, 100);
        client.sliceSlider.setPrefWidth(90);
        Label sliceLbl = new Label();
        sliceLbl.textProperty().bind(i18n.createStringBinding("sidebar.lbl.slice"));
        sliceLbl.getStyleClass().add("card-caption-title");
        Tooltip ttSlice = new Tooltip();
        ttSlice.textProperty().bind(i18n.createStringBinding("sidebar.chk.scanner.tt"));
        sliceLbl.setTooltip(ttSlice);
        client.sliceSlider.setTooltip(ttSlice);

        Label sliceValLbl = new Label("100 %");
        sliceValLbl.getStyleClass().add("slider-val-accent");

        client.sliceSlider.valueProperty().addListener((o, a, b) -> {
            sliceValLbl.setText(String.format(Locale.US, "%.0f %%", b.doubleValue()));
            if (client.simWorldViewer != null) client.simWorldViewer.setSlicePlane(b.doubleValue());
        });
        if (client.simWorldViewer != null) {
            client.simWorldViewer.setOnSlicePlaneChanged(val -> {
                if (client.sliceSlider != null && Math.abs(client.sliceSlider.getValue() - val) > 0.01) {
                    client.sliceSlider.setValue(val);
                    sliceValLbl.setText(String.format(Locale.US, "%.0f %%", val));
                }
            });
        }

        HBox sliceBox = new HBox(6, sliceLbl, client.sliceSlider, sliceValLbl);
        sliceBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox chkNid = new CheckBox();
        chkNid.textProperty().bind(i18n.createStringBinding("sidebar.chk.nest"));
        chkNid.setSelected(true);
        chkNid.getStyleClass().add("compact-checkbox");
        Tooltip ttNid = new Tooltip();
        ttNid.textProperty().bind(i18n.createStringBinding("sidebar.chk.nest.tt"));
        chkNid.setTooltip(ttNid);
        chkNid.selectedProperty().addListener((o, a, b) -> {
            if (client.gameView != null && client.gameView.getGameApp() != null) client.gameView.getGameApp().setTunnelsVisible(b);
            client.simWorldViewer.setGalleriesVisible(b);
        });

        CheckBox chkPheromonesLayer = new CheckBox();
        chkPheromonesLayer.textProperty().bind(i18n.createStringBinding("sidebar.chk.pheromones"));
        chkPheromonesLayer.setSelected(true);
        chkPheromonesLayer.getStyleClass().add("compact-checkbox");
        Tooltip ttPhero = new Tooltip();
        ttPhero.textProperty().bind(i18n.createStringBinding("sidebar.chk.pheromones.tt"));
        chkPheromonesLayer.setTooltip(ttPhero);
        chkPheromonesLayer.selectedProperty().addListener((o, a, b) -> {
            if (client.gameView != null && client.gameView.getGameApp() != null) client.gameView.getGameApp().setPheromonesVisible(b);
            client.simWorldViewer.setPheromonesVisible(b);
        });

        ComboBox<String> comboPheromoneType = new ComboBox<>();
        comboPheromoneType.getItems().addAll(
            "🌐 Toutes les Phéromones",
            "🚨 Phéromone d'Alarme",
            "🍃 Phéromone de Nourriture",
            "🏠 Phéromone de Nid / Homing",
            "📢 Phéromone de Recrutement",
            "👑 Phéromone de Reine",
            "💀 Phéromone de Cadavre (Nécrophorèse)"
        );
        comboPheromoneType.getSelectionModel().selectFirst();
        comboPheromoneType.setMaxWidth(Double.MAX_VALUE);
        comboPheromoneType.getStyleClass().add("compact-combo");
        comboPheromoneType.getSelectionModel().selectedIndexProperty().addListener((o, oldV, newV) -> {
            if (newV == null) return;
            int idx = newV.intValue();
            if (client.simWorldViewer != null) {
                client.simWorldViewer.setPheromoneChannelFilter(idx);
                client.simWorldViewer.setPheromoneRenderMode(WorldEditorPane.PheromoneRenderMode.HEATMAP_GRADIENT);
            }
        });

        CheckBox chkAntsLayer = new CheckBox();
        chkAntsLayer.textProperty().bind(i18n.createStringBinding("sidebar.chk.ants"));
        chkAntsLayer.setSelected(true);
        chkAntsLayer.getStyleClass().add("compact-checkbox");
        Tooltip ttAnts = new Tooltip();
        ttAnts.textProperty().bind(i18n.createStringBinding("sidebar.chk.ants.tt"));
        chkAntsLayer.setTooltip(ttAnts);
        chkAntsLayer.selectedProperty().addListener((o, a, b) -> {
            if (client.gameView != null && client.gameView.getGameApp() != null) client.gameView.getGameApp().setAntsVisible(b);
            client.simWorldViewer.setColonyVisible(b);
        });

        CheckBox chkWeatherLayer = new CheckBox();
        chkWeatherLayer.textProperty().bind(i18n.createStringBinding("sidebar.chk.weather"));
        chkWeatherLayer.setSelected(true);
        chkWeatherLayer.getStyleClass().add("compact-checkbox");
        Tooltip ttWeather = new Tooltip();
        ttWeather.textProperty().bind(i18n.createStringBinding("sidebar.chk.weather.tt"));
        chkWeatherLayer.setTooltip(ttWeather);
        chkWeatherLayer.selectedProperty().addListener((o, a, b) -> {
            if (client.gameView != null && client.gameView.getGameApp() != null) client.gameView.getGameApp().setWeatherVisible(b);
            client.simWorldViewer.setWeatherVisible(b);
        });

        CheckBox chkVoxelInfo = new CheckBox();
        chkVoxelInfo.textProperty().bind(i18n.createStringBinding("world.render.voxel_info"));
        chkVoxelInfo.setSelected(true);
        chkVoxelInfo.getStyleClass().add("compact-checkbox");
        Tooltip ttVoxelInfo = new Tooltip();
        ttVoxelInfo.textProperty().bind(i18n.createStringBinding("world.render.voxel_info.tt"));
        chkVoxelInfo.setTooltip(ttVoxelInfo);
        chkVoxelInfo.selectedProperty().addListener((o, a, b) -> client.simWorldViewer.setVoxelInfoVisible(b));

        client.chkAntTracking = new CheckBox();
        client.chkAntTracking.textProperty().bind(i18n.createStringBinding("world.render.ant_tracking"));
        client.chkAntTracking.setSelected(true);
        client.chkAntTracking.getStyleClass().add("compact-checkbox");
        Tooltip ttAntTracking = new Tooltip();
        ttAntTracking.textProperty().bind(i18n.createStringBinding("world.render.ant_tracking.tt"));
        client.chkAntTracking.setTooltip(ttAntTracking);
        client.chkAntTracking.selectedProperty().addListener((o, a, b) -> client.simWorldViewer.setAntTrackingEnabled(b));

        CheckBox chkChamberOverlay = new CheckBox();
        chkChamberOverlay.textProperty().bind(i18n.createStringBinding("world.render.chamber_overlay"));
        chkChamberOverlay.setSelected(true);
        chkChamberOverlay.getStyleClass().add("compact-checkbox");
        Tooltip ttChamberOverlay = new Tooltip();
        ttChamberOverlay.textProperty().bind(i18n.createStringBinding("world.render.chamber_overlay.tt"));
        chkChamberOverlay.setTooltip(ttChamberOverlay);
        chkChamberOverlay.selectedProperty().addListener((o, a, b) -> client.simWorldViewer.setChamberOverlayVisible(b));

        CheckBox chkWeatherOverlay = new CheckBox();
        chkWeatherOverlay.textProperty().bind(i18n.createStringBinding("world.render.weather_overlay"));
        chkWeatherOverlay.setSelected(true);
        chkWeatherOverlay.getStyleClass().add("compact-checkbox");
        Tooltip ttWeatherOverlay = new Tooltip();
        ttWeatherOverlay.textProperty().bind(i18n.createStringBinding("world.render.weather_overlay.tt"));
        chkWeatherOverlay.setTooltip(ttWeatherOverlay);
        chkWeatherOverlay.selectedProperty().addListener((o, a, b) -> client.simWorldViewer.setWeatherOverlayVisible(b));

        CheckBox chkElevationIsolines = new CheckBox();
        chkElevationIsolines.textProperty().bind(i18n.createStringBinding("world.render.isolines_elevation"));
        chkElevationIsolines.tooltipProperty().bind(i18n.createTooltipBinding("world.render.isolines_elevation.tt"));
        chkElevationIsolines.setSelected(false);
        chkElevationIsolines.getStyleClass().add("compact-checkbox");
        chkElevationIsolines.selectedProperty().addListener((o, a, b) -> {
            if (client.simWorldViewer != null) client.simWorldViewer.setShowElevationIsolines(b);
        });

        CheckBox chkClimateIsolines = new CheckBox();
        chkClimateIsolines.textProperty().bind(i18n.createStringBinding("world.render.isolines_climate"));
        chkClimateIsolines.tooltipProperty().bind(i18n.createTooltipBinding("world.render.isolines_climate.tt"));
        chkClimateIsolines.setSelected(false);
        chkClimateIsolines.getStyleClass().add("compact-checkbox");
        chkClimateIsolines.selectedProperty().addListener((o, a, b) -> {
            if (client.simWorldViewer != null) client.simWorldViewer.setShowClimateIsolines(b);
        });

        CheckBox chkPheromoneIsolines = new CheckBox();
        chkPheromoneIsolines.textProperty().bind(i18n.createStringBinding("world.render.isolines_pheromone"));
        chkPheromoneIsolines.tooltipProperty().bind(i18n.createTooltipBinding("world.render.isolines_pheromone.tt"));
        chkPheromoneIsolines.setSelected(false);
        chkPheromoneIsolines.getStyleClass().add("compact-checkbox");
        chkPheromoneIsolines.selectedProperty().addListener((o, a, b) -> {
            if (client.simWorldViewer != null) client.simWorldViewer.setShowPheromoneIsolines(b);
        });

        CheckBox chkUVVision = new CheckBox();
        chkUVVision.textProperty().bind(i18n.createStringBinding("world.render.uv_vision"));
        chkUVVision.tooltipProperty().bind(i18n.createTooltipBinding("world.render.uv_vision.tt"));
        chkUVVision.setSelected(false);
        chkUVVision.getStyleClass().add("compact-checkbox");
        chkUVVision.selectedProperty().addListener((o, a, b) -> {
            if (client.simWorldViewer != null) client.simWorldViewer.setUVVisionMode(b);
        });

        CheckBox chkOmmatidialVision = new CheckBox();
        chkOmmatidialVision.textProperty().bind(i18n.createStringBinding("world.render.ommatidial_vision"));
        chkOmmatidialVision.tooltipProperty().bind(i18n.createTooltipBinding("world.render.ommatidial_vision.tt"));
        chkOmmatidialVision.setSelected(false);
        chkOmmatidialVision.getStyleClass().add("compact-checkbox");
        chkOmmatidialVision.selectedProperty().addListener((o, a, b) -> {
            if (client.simWorldViewer != null) client.simWorldViewer.setOmmatidialVisionMode(b);
        });

        renderSection.getChildren().addAll(
            lblRenderMode, comboRenderMode,
            chkMinimap, chkSyncMinimap, chkShowLegend,
            chkVoxelInfo, client.chkAntTracking, chkChamberOverlay, chkWeatherOverlay,
            new Separator(),
            chkTerrain, chkTrees, chkSkirt, sliceBox, chkNid, chkPheromonesLayer, comboPheromoneType, chkAntsLayer, chkWeatherLayer,
            new Separator(),
            chkElevationIsolines, chkClimateIsolines, chkPheromoneIsolines, chkUVVision, chkOmmatidialVision
        );
        return renderSection;
    }

    VBox buildAudioControlsSection() {
        VBox audioSection = new VBox(6);
        audioSection.getStyleClass().add("sidebar-section-card");
        Label lblAudio = new Label();
        lblAudio.textProperty().bind(i18n.createStringBinding("sidebar.audio.title"));
        lblAudio.getStyleClass().add("sidebar-section-label");

        SimulationAudioManager audioMgr = SimulationAudioManager.getInstance();

        Slider masterVolSlider = new Slider(0, 100, 70);
        masterVolSlider.getStyleClass().add("compact-combo");
        masterVolSlider.valueProperty().addListener((o, oldV, newV) -> audioMgr.setMasterVolume(newV.doubleValue() / 100.0));
        Label volLbl = new Label();
        volLbl.textProperty().bind(i18n.createStringBinding("sidebar.audio.volume"));
        volLbl.getStyleClass().add("card-caption-title");
        HBox volBox = new HBox(6, volLbl, masterVolSlider);

        CheckBox chkAmbientSound = new CheckBox();
        chkAmbientSound.textProperty().bind(i18n.createStringBinding("sidebar.audio.ambient"));
        chkAmbientSound.setSelected(audioMgr.isAmbientEnabled());
        chkAmbientSound.getStyleClass().add("compact-checkbox");
        Tooltip ttAmb = new Tooltip();
        ttAmb.textProperty().bind(i18n.createStringBinding("sidebar.audio.ambient.tt"));
        chkAmbientSound.setTooltip(ttAmb);
        chkAmbientSound.selectedProperty().addListener((o, a, b) -> audioMgr.setAmbientEnabled(b));

        CheckBox chkRiverSound = new CheckBox();
        chkRiverSound.textProperty().bind(i18n.createStringBinding("sidebar.audio.river"));
        chkRiverSound.setSelected(audioMgr.isRiverEnabled());
        chkRiverSound.getStyleClass().add("compact-checkbox");
        Tooltip ttRiv = new Tooltip();
        ttRiv.textProperty().bind(i18n.createStringBinding("sidebar.audio.river.tt"));
        chkRiverSound.setTooltip(ttRiv);
        chkRiverSound.selectedProperty().addListener((o, a, b) -> audioMgr.setRiverEnabled(b));

        CheckBox chkWeatherSound = new CheckBox();
        chkWeatherSound.textProperty().bind(i18n.createStringBinding("sidebar.audio.weather"));
        chkWeatherSound.setSelected(audioMgr.isWeatherEnabled());
        chkWeatherSound.getStyleClass().add("compact-checkbox");
        Tooltip ttWea = new Tooltip();
        ttWea.textProperty().bind(i18n.createStringBinding("sidebar.audio.weather.tt"));
        chkWeatherSound.setTooltip(ttWea);
        chkWeatherSound.selectedProperty().addListener((o, a, b) -> audioMgr.setWeatherEnabled(b));

        CheckBox chkInsectSound = new CheckBox();
        chkInsectSound.textProperty().bind(i18n.createStringBinding("sidebar.audio.insect"));
        chkInsectSound.setSelected(audioMgr.isInsectEnabled());
        chkInsectSound.getStyleClass().add("compact-checkbox");
        Tooltip ttIns = new Tooltip();
        ttIns.textProperty().bind(i18n.createStringBinding("sidebar.audio.insect.tt"));
        chkInsectSound.setTooltip(ttIns);
        chkInsectSound.selectedProperty().addListener((o, a, b) -> audioMgr.setInsectEnabled(b));

        audioSection.getChildren().addAll(lblAudio, volBox, chkAmbientSound, chkRiverSound, chkWeatherSound, chkInsectSound);
        return audioSection;
    }
}
