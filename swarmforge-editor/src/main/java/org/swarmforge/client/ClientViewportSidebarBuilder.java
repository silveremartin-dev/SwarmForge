/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.swarmforge.client.audio.SimulationAudioManager;
import org.swarmforge.client.ui.MultiplayerScoreboardOverlay;
import org.swarmforge.client.ui.WorldEditorPane;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.MediaCaptureUtil;
import org.swarmforge.client.view.GameViewPane;
import org.swarmforge.client.view.JmeGameApp;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Helper class constructing the 3D visual simulation viewport, right-side
 * control sidebar (render modes, 3D layers, audio mixer, HD photo & MP4/GIF
 * video capture), fullscreen stage listener, and mouse camera controls for
 * {@link SwarmForgeClient}.
 */
class ClientViewportSidebarBuilder {

    private static final Logger LOG = Logger.getLogger(ClientViewportSidebarBuilder.class.getName());
    private final SwarmForgeClient client;
    private final I18nManager i18n = I18nManager.getInstance();
    private final ClientMediaCaptureController mediaCaptureController;
    private double lastX, lastY;

    ClientViewportSidebarBuilder(SwarmForgeClient client) {
        this.client = client;
        this.mediaCaptureController = new ClientMediaCaptureController(client);
    }

    Node createVisualSimulationViewport() {
        BorderPane rootPane = new BorderPane();

        client.simWorldViewer = new WorldEditorPane();
        client.simWorldViewer.setSimulationMode(true);
        client.simWorldViewer.setOnIndividualSelected(ant -> {
            if (client.statisticsDashboard != null && ant != null) {
                client.statisticsDashboard.setTrackedIndividual(ant);
            }
        });
        if (client.statisticsDashboard != null) {
            client.statisticsDashboard.setOnSelectIndividual(antId -> {
                if (client.simWorldViewer != null) {
                    client.simWorldViewer.setFollowedIndividualById(antId);
                }
            });
            client.statisticsDashboard.setOnTrackIndividual(antId -> {
                if (client.simWorldViewer != null) {
                    client.simWorldViewer.setFollowedIndividualById(antId);
                    client.simWorldViewer.setFollowAntCameraEnabled(true);
                    client.simWorldViewer.setAntTrackingEnabled(true);
                    if (client.chkAntTracking != null) {
                        client.chkAntTracking.setSelected(true);
                    }
                    if (client.simWorldViewer.getTrackedIndividualPane() != null) {
                        client.simWorldViewer.getTrackedIndividualPane().setVisible(true);
                    }
                    if (client.simSubTabs != null && client.visualTab != null) {
                        client.simSubTabs.getSelectionModel().select(client.visualTab);
                    }
                }
            });
        }

        client.gameView = client.simWorldViewer.getGameView();
        client.simWorldViewer.setRenderMode(WorldEditorPane.RenderMode.REALISTIC);
        client.simWorldViewer.setVisible(true);
        client.simWorldViewer.setManaged(true);

        client.simulationInactiveOverlay = new VBox(4);
        client.simulationInactiveOverlay.setAlignment(Pos.CENTER);
        client.simulationInactiveOverlay.getStyleClass().add("simulation-inactive-overlay");
        client.simulationInactiveOverlay.setMaxSize(360, 50);

        Label lblInactiveTitle = new Label("🎬 3D View Ready — Waiting for Launch");
        lblInactiveTitle.getStyleClass().add("simulation-inactive-title");

        client.simulationInactiveOverlay.setMouseTransparent(true);
        client.simulationInactiveOverlay.setPickOnBounds(false);
        client.simulationInactiveOverlay.setVisible(false);
        client.simulationInactiveOverlay.setManaged(false);
        client.simulationInactiveOverlay.getChildren().addAll(lblInactiveTitle);

        Button btnExitFullscreen = new Button("❌ Exit Fullscreen (ESC)");
        btnExitFullscreen.getStyleClass().add("fullscreen-exit-button");
        btnExitFullscreen.setVisible(false);
        btnExitFullscreen.setManaged(false);

        client.scoreboardOverlay = new MultiplayerScoreboardOverlay();
        client.scoreboardOverlay.setOnFocusColony(entry -> {
            if (client.simWorldViewer != null) {
                client.simWorldViewer.focusOnLocation(entry.nestX, entry.nestY, entry.nestZ);
            }
        });
        client.scoreboardOverlay.setOnClose(() -> {
            if (client.chkShowScoreboard != null) {
                client.chkShowScoreboard.setSelected(false);
            }
        });

        StackPane viewportStack = new StackPane(client.simWorldViewer, client.simulationInactiveOverlay, btnExitFullscreen, client.scoreboardOverlay);
        StackPane.setAlignment(client.simulationInactiveOverlay, Pos.TOP_CENTER);
        StackPane.setMargin(client.simulationInactiveOverlay, new Insets(15));
        StackPane.setAlignment(btnExitFullscreen, Pos.TOP_RIGHT);
        StackPane.setMargin(btnExitFullscreen, new Insets(15));
        StackPane.setAlignment(client.scoreboardOverlay, Pos.TOP_LEFT);
        StackPane.setMargin(client.scoreboardOverlay, new Insets(15));

        client.simulationInactiveOverlay.setVisible(false);
        client.simulationInactiveOverlay.setManaged(false);
        rootPane.setCenter(viewportStack);

        VBox sideControls = new VBox(10);
        sideControls.setPrefWidth(350);
        sideControls.setMinWidth(340);
        sideControls.setPadding(new Insets(10));
        sideControls.getStyleClass().add("card-pane");

        Label lblSideTitle = new Label();
        lblSideTitle.textProperty().bind(i18n.createStringBinding("sidebar.title"));
        lblSideTitle.getStyleClass().add("client-sidebar-title");

        client.chkShowScoreboard = new CheckBox();
        client.chkShowScoreboard.textProperty().bind(i18n.createStringBinding("multiplayer.scoreboard.chk"));
        client.chkShowScoreboard.setTooltip(new Tooltip(i18n.get("multiplayer.scoreboard.chk.tooltip", "Afficher ou masquer l'overlay du tableau des scores et colonies multijoueur")));
        client.chkShowScoreboard.getStyleClass().add("compact-checkbox");
        client.chkShowScoreboard.setSelected(true);
        client.chkShowScoreboard.setVisible(false);
        client.chkShowScoreboard.setManaged(false);
        client.chkShowScoreboard.selectedProperty().addListener((obs, oldV, newV) -> {
            if (client.scoreboardOverlay != null) {
                if (newV) {
                    client.scoreboardOverlay.showWithAnimation();
                } else {
                    client.scoreboardOverlay.hideWithAnimation();
                }
            }
        });

        HBox sideHeaderBox = new HBox(8, lblSideTitle, client.chkShowScoreboard);
        sideHeaderBox.setAlignment(Pos.CENTER_LEFT);

        Node playbackAndSpeedNode = (client.simControlPanel != null) ? client.simControlPanel.getPlaybackAndSpeedPanel() : new VBox();

        Button btnFullscreenMode = new Button();
        btnFullscreenMode.textProperty().bind(i18n.createStringBinding("sidebar.btn.fullscreen"));
        btnFullscreenMode.setMaxWidth(Double.MAX_VALUE);
        btnFullscreenMode.getStyleClass().add("fullscreen-toggle-button");
        Tooltip ttFS = new Tooltip();
        ttFS.textProperty().bind(i18n.createStringBinding("sidebar.btn.fullscreen.tt"));
        btnFullscreenMode.setTooltip(ttFS);

        wirePlaybackCallbacks();
        VBox mediaSection = mediaCaptureController.buildMediaSection(viewportStack, btnFullscreenMode);

        CheckBox chkMinimap = new CheckBox();
        VBox renderSection = buildRenderLayersSection(chkMinimap);
        VBox audioSection = buildAudioControlsSection();

        sideControls.getChildren().addAll(sideHeaderBox, playbackAndSpeedNode, new Separator(), mediaSection, renderSection, audioSection);

        ScrollPane sideScroll = new ScrollPane(sideControls);
        sideScroll.setMinWidth(350);
        sideScroll.setPrefWidth(350);
        sideScroll.setFitToWidth(true);
        sideScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sideScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sideScroll.getStyleClass().add("transparent-scroll-pane");

        rootPane.setRight(sideScroll);

        wireFullscreenSupport(rootPane, sideScroll, btnFullscreenMode, btnExitFullscreen, chkMinimap);

        return rootPane;
    }

    private void wirePlaybackCallbacks() {
        if (client.simControlPanel == null) return;
        client.simControlPanel.setOnPlay(v -> {
            if (client.simulationInactiveOverlay != null) client.simulationInactiveOverlay.setVisible(false);
            if (client.localSimulation != null) client.localSimulation.start();
            if (client.simWorldViewer != null && client.simWorldViewer.getGameView() != null && client.simWorldViewer.getGameView().getGameApp() != null) {
                client.simWorldViewer.getGameView().getGameApp().setSimulationPaused(false);
            }
            mediaCaptureController.onSimulationPlay();
        });
        client.simControlPanel.setOnPause(v -> {
            if (client.localSimulation != null) client.localSimulation.pause();
            if (client.simWorldViewer != null && client.simWorldViewer.getGameView() != null && client.simWorldViewer.getGameView().getGameApp() != null) {
                client.simWorldViewer.getGameView().getGameApp().setSimulationPaused(true);
            }
            mediaCaptureController.onSimulationPauseOrStop();
        });
        client.simControlPanel.setOnStop(v -> {
            if (client.localSimulation != null) {
                client.localSimulation.stop();
                client.localSimulation.reset(0);
                client.simControlPanel.updateTick(0, 0);
            }
            if (client.simWorldViewer != null && client.simWorldViewer.getGameView() != null && client.simWorldViewer.getGameView().getGameApp() != null) {
                client.simWorldViewer.getGameView().getGameApp().setSimulationPaused(true);
            }
            if (client.simWorldViewer != null) {
                client.simWorldViewer.resetIndividualTracking();
            }
            if (client.interventionPanel != null) {
                client.interventionPanel.resetEventsState();
            }
            if (client.statisticsDashboard != null) {
                client.statisticsDashboard.clear();
            }
            if (client.eventLogPane != null) {
                client.eventLogPane.clearLog();
            }
            if (client.simulationInactiveOverlay != null) client.simulationInactiveOverlay.setVisible(true);
            mediaCaptureController.onSimulationPauseOrStop();
        });
        client.simControlPanel.setOnRewind(steps -> {
            if (client.localSimulation != null) {
                client.localSimulation.pause();
                client.localSimulation.rewind(steps);
                long curTick = client.localSimulation.getTickCount();
                client.simControlPanel.updateTick(curTick, client.localSimulation.getHighestRecordedTick(), client.localSimulation.getElapsedSimulationSeconds());
                double stepSec = client.simControlPanel.getSimulationStepSeconds();
                if (client.interventionPanel != null) {
                    client.interventionPanel.processScheduledEvents(curTick);
                }
                if (client.statisticsDashboard != null) {
                    client.statisticsDashboard.truncateAfter(curTick * stepSec);
                }
                if (client.eventLogPane != null) {
                    client.eventLogPane.truncateAfterTick(curTick);
                }
                client.simWorldViewer.repaintAllViews();
            }
            mediaCaptureController.onSimulationPauseOrStop();
        });
        client.simControlPanel.setOnStepForward(steps -> {
            if (client.localSimulation != null) {
                client.localSimulation.pause();
                int count = steps != null ? steps : 100;
                for (int i = 0; i < count; i++) {
                    client.localSimulation.tick();
                }
                long curTick = client.localSimulation.getTickCount();
                client.simControlPanel.updateTick(curTick, client.localSimulation.getHighestRecordedTick(), client.localSimulation.getElapsedSimulationSeconds());
                if (client.interventionPanel != null) {
                    client.interventionPanel.processScheduledEvents(curTick);
                }
                client.simWorldViewer.repaintAllViews();
            }
            mediaCaptureController.onSimulationPauseOrStop();
        });
        client.simControlPanel.setOnSeek(tick -> {
            if (tick == 0) {
                mediaCaptureController.cancelAndResetVideoRecording();
                if (client.interventionPanel != null) {
                    client.interventionPanel.resetEventsState();
                }
                if (client.statisticsDashboard != null) {
                    client.statisticsDashboard.clear();
                }
                if (client.eventLogPane != null) {
                    client.eventLogPane.clearLog();
                }
                if (client.simWorldViewer != null) {
                    client.simWorldViewer.resetIndividualTracking();
                }
            }
            if (client.localSimulation != null) {
                client.localSimulation.pause();
                client.localSimulation.seekToTick(tick);
                long curTick = client.localSimulation.getTickCount();
                client.simControlPanel.updateTick(curTick, client.localSimulation.getHighestRecordedTick(), client.localSimulation.getElapsedSimulationSeconds());
                double stepSec = client.simControlPanel.getSimulationStepSeconds();
                if (client.interventionPanel != null) {
                    client.interventionPanel.processScheduledEvents(curTick);
                }
                if (client.statisticsDashboard != null && tick > 0) {
                    client.statisticsDashboard.truncateAfter(curTick * stepSec);
                }
                if (client.eventLogPane != null && tick > 0) {
                    client.eventLogPane.truncateAfterTick(curTick);
                }
                client.simWorldViewer.repaintAllViews();
            }
            mediaCaptureController.onSimulationPauseOrStop();
        });
        client.simControlPanel.setOnSpeedChange(speed -> {
            if (client.localSimulation != null) client.localSimulation.setSpeedMultiplier(speed);
            mediaCaptureController.onSimulationPauseOrStop();
        });
    }

    private VBox buildRenderLayersSection(CheckBox chkMinimap) {
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

    private VBox buildAudioControlsSection() {
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

    private void wireFullscreenSupport(BorderPane rootPane, ScrollPane sideScroll, Button btnFullscreenMode, Button btnExitFullscreen, CheckBox chkMinimap) {
        Runnable toggleFullscreen = () -> {
            Stage stage = rootPane.getScene() != null ? (Stage) rootPane.getScene().getWindow() : null;
            if (stage != null) {
                boolean willBeFS = !stage.isFullScreen();
                if (willBeFS) {
                    if (client.mainTabs != null && client.simTab != null) {
                        client.mainTabs.getSelectionModel().select(client.simTab);
                    }
                    if (client.simSubTabs != null && client.visualTab != null) {
                        client.simSubTabs.getSelectionModel().select(client.visualTab);
                    }
                }
                stage.setFullScreen(willBeFS);
            }
        };

        btnFullscreenMode.setOnAction(e -> toggleFullscreen.run());
        btnExitFullscreen.setOnAction(e -> toggleFullscreen.run());

        Consumer<Stage> registerFsListener = new Consumer<Stage>() {
            private final Set<Stage> registered = new HashSet<>();

            @Override
            public void accept(Stage stage) {
                if (stage == null || !registered.add(stage)) return;
                stage.fullScreenProperty().addListener((fsObs, oldFS, isFS) -> {
                    if (isFS) {
                        if (client.mainTabs != null && client.simTab != null) {
                            client.mainTabs.getSelectionModel().select(client.simTab);
                        }
                        if (client.simSubTabs != null && client.visualTab != null) {
                            client.simSubTabs.getSelectionModel().select(client.visualTab);
                        }
                        if (client.mainTabs != null) {
                            if (!client.mainTabs.getStyleClass().contains("tab-pane-fullscreen")) {
                                client.mainTabs.getStyleClass().add("tab-pane-fullscreen");
                            }
                            Node hdr = client.mainTabs.lookup(".tab-header-area");
                            if (hdr != null) { hdr.setVisible(false); hdr.setManaged(false); }
                        }
                        if (client.simSubTabs != null) {
                            if (!client.simSubTabs.getStyleClass().contains("tab-pane-fullscreen")) {
                                client.simSubTabs.getStyleClass().add("tab-pane-fullscreen");
                            }
                            Node hdr = client.simSubTabs.lookup(".tab-header-area");
                            if (hdr != null) { hdr.setVisible(false); hdr.setManaged(false); }
                        }
                        if (client.connectBox != null) {
                            client.connectBox.setVisible(false);
                            client.connectBox.setManaged(false);
                        }
                        rootPane.setRight(null);
                        if (client.simWorldViewer != null) {
                            client.simWorldViewer.setFullscreenMode(true);
                        }
                        if (client.worldEditorPane != null) {
                            client.worldEditorPane.setFullscreenMode(true);
                        }
                        btnExitFullscreen.setVisible(false);
                    } else {
                        if (client.mainTabs != null) {
                            client.mainTabs.getStyleClass().remove("tab-pane-fullscreen");
                            Node hdr = client.mainTabs.lookup(".tab-header-area");
                            if (hdr != null) { hdr.setVisible(true); hdr.setManaged(true); }
                        }
                        if (client.simSubTabs != null) {
                            client.simSubTabs.getStyleClass().remove("tab-pane-fullscreen");
                            Node hdr = client.simSubTabs.lookup(".tab-header-area");
                            if (hdr != null) { hdr.setVisible(true); hdr.setManaged(true); }
                        }
                        if (client.connectBox != null) {
                            client.connectBox.setVisible(true);
                            client.connectBox.setManaged(true);
                        }
                        rootPane.setRight(sideScroll);
                        if (client.simWorldViewer != null) {
                            client.simWorldViewer.setFullscreenMode(false);
                            client.simWorldViewer.setDualMinimapVisible(chkMinimap.isSelected());
                        }
                        if (client.worldEditorPane != null) {
                            client.worldEditorPane.setFullscreenMode(false);
                        }
                        btnExitFullscreen.setVisible(false);
                    }
                    client.update3DRenderingState();
                    if (client.simWorldViewer != null) {
                        client.simWorldViewer.repaintAllViews();
                    }
                    if (client.worldEditorPane != null) {
                        client.worldEditorPane.repaintAllViews();
                    }
                });
            }
        };

        Consumer<Scene> onSceneAttached = scene -> {
            if (scene == null) return;
            scene.setOnKeyPressed(ke -> {
                if (ke.getCode() == KeyCode.F11) {
                    toggleFullscreen.run();
                    ke.consume();
                }
            });
            scene.windowProperty().addListener((wObs, oldW, newW) -> {
                if (newW instanceof Stage s) registerFsListener.accept(s);
            });
            if (scene.getWindow() instanceof Stage s) {
                registerFsListener.accept(s);
            }
        };

        rootPane.sceneProperty().addListener((obs, oldScene, newScene) -> onSceneAttached.accept(newScene));
        onSceneAttached.accept(rootPane.getScene());
    }

    void setupMouseControls(GameViewPane view) {
        view.setOnMousePressed(e -> {
            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });

        view.setOnMouseDragged(e -> {
            double dx = e.getSceneX() - lastX;
            double dy = e.getSceneY() - lastY;

            if (e.isPrimaryButtonDown()) {
                view.getGameApp().rotateCamera((float) dx, (float) dy);
            } else if (e.isSecondaryButtonDown() || e.isShiftDown() || e.isMiddleButtonDown()) {
                view.getGameApp().panCamera((float) -dx, (float) dy);
            }

            lastX = e.getSceneX();
            lastY = e.getSceneY();
        });

        view.setOnScroll(e -> {
            double delta = e.getDeltaY();
            view.getGameApp().zoomCamera((float) delta * 0.05f, e.getX(), e.getY(), view.getWidth(), view.getHeight());
        });

        view.getGameApp().setSelectionListener(new JmeGameApp.ObjectSelectionListener() {
            @Override
            public void onIndividualSelected(String id, String caste, String stage, float health, float energy, float hunger, float age, String job) {
                if (client.simWorldViewer != null) {
                    client.simWorldViewer.setFollowedIndividualById(id);
                }
            }

            @Override
            public void onChamberSelected(String chamberId) {
                if (client.simWorldViewer != null) {
                    client.simWorldViewer.selectChamberById(chamberId);
                }
            }

            @Override
            public void onVoxelSelected(int x, int y, int z, String material, float moisture, float temp, float compaction) {
                if (client.simWorldViewer != null && client.simWorldViewer.getLblHoverInfo() != null) {
                    client.simWorldViewer.getLblHoverInfo().setText("📍 Voxel: (" + x + ", " + y + ", " + z + ") | " + material + " | Hum: " + (int) moisture + "% | Temp: " + String.format(Locale.US, "%.1f", temp) + "°C");
                }
            }

            @Override
            public void onHoverInfo(String text) {
                if (client.simWorldViewer != null && client.simWorldViewer.getLblHoverInfo() != null && text != null) {
                    client.simWorldViewer.getLblHoverInfo().setText(text);
                }
            }
        });

        view.setOnMouseMoved(e -> {
            if (view.getGameApp() != null) {
                view.getGameApp().hover(e.getX(), e.getY(), view.getWidth(), view.getHeight());
            }
        });

        view.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                view.getGameApp().resetCamera();
            } else if (e.getClickCount() == 1) {
                view.getGameApp().pick(e.getX(), e.getY(), view.getWidth(), view.getHeight());
            }
        });
    }
}
