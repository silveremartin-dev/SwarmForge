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
    private final ClientViewportLayersPanelBuilder layersPanelBuilder;
    private final ClientViewportInputHelper inputHelper;

    ClientViewportSidebarBuilder(SwarmForgeClient client) {
        this.client = client;
        this.mediaCaptureController = new ClientMediaCaptureController(client);
        this.layersPanelBuilder = new ClientViewportLayersPanelBuilder(client);
        this.inputHelper = new ClientViewportInputHelper(client);
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
        VBox renderSection = layersPanelBuilder.buildRenderLayersSection(chkMinimap);
        VBox audioSection = layersPanelBuilder.buildAudioControlsSection();

        sideControls.getChildren().addAll(sideHeaderBox, playbackAndSpeedNode, new Separator(), mediaSection, renderSection, audioSection);

        ScrollPane sideScroll = new ScrollPane(sideControls);
        sideScroll.setMinWidth(350);
        sideScroll.setPrefWidth(350);
        sideScroll.setFitToWidth(true);
        sideScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sideScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sideScroll.getStyleClass().add("transparent-scroll-pane");

        rootPane.setRight(sideScroll);

        inputHelper.wireFullscreenSupport(rootPane, sideScroll, btnFullscreenMode, btnExitFullscreen, chkMinimap);

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

    void setupMouseControls(GameViewPane view) {
        inputHelper.setupMouseControls(view);
    }
}
