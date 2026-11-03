/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.ui.EventLogPane;
import org.swarmforge.client.ui.GlossaryPaneView;
import org.swarmforge.client.ui.InterventionPanel;
import org.swarmforge.client.ui.SettingsPane;
import org.swarmforge.client.ui.SimulationControlPanel;
import org.swarmforge.client.ui.StatisticsDashboard;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.client.util.NotificationOverlay;

/**
 * Builds and configures the main Simulation Tab and its subtabs (Controls, Visual 3D Viewport,
 * God Mode Intervention, Statistics, Event Log, Settings, Glossary).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class ClientSimulationTabBuilder {

    private ClientSimulationTabBuilder() {}

    public static Node buildSimulationManager(SwarmForgeClient client) {
        I18nManager i18n = I18nManager.getInstance();
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        HBox connectBox = new HBox(12);
        client.connectBox = connectBox;
        connectBox.setAlignment(Pos.CENTER_LEFT);
        connectBox.getStyleClass().add("header-banner");

        client.statsLabel = new Label();
        client.statsLabel.getStyleClass().add("stats-status-label");
        client.statsLabel.tooltipProperty().bind(i18n.createTooltipBinding("label.stats.tt"));

        client.syncLabel = new Label();
        client.syncLabel.getStyleClass().add("sync-status-label");
        client.syncLabel.tooltipProperty().bind(i18n.createTooltipBinding("label.sync.tt"));

        Region bannerSpacer = new Region();
        HBox.setHgrow(bannerSpacer, Priority.ALWAYS);

        connectBox.getChildren().addAll(client.statsLabel, bannerSpacer, client.syncLabel);
        pane.setTop(connectBox);
        BorderPane.setMargin(connectBox, new Insets(0, 0, 10, 0));

        client.simSubTabs = new TabPane();
        client.simSubTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> client.update3DRenderingState());
        TabPane subTabs = client.simSubTabs;

        Tab controlsTab = new Tab();
        controlsTab.textProperty().bind(i18n.createStringBinding("sim.title"));
        controlsTab.setGraphic(new FontIcon(Feather.SLIDERS));
        VBox controlsInner = new VBox(12);
        controlsInner.setPadding(new Insets(10));

        client.simControlPanel = new SimulationControlPanel();
        client.simControlPanel.setWorldConfigSupplier(() -> (client.worldEditorPane != null) ? client.worldEditorPane.getConfiguration() : null);
        controlsInner.getChildren().add(client.simControlPanel);
        controlsTab.setContent(new ScrollPane(controlsInner));

        client.visualTab = new Tab();
        client.visualTab.textProperty().bind(i18n.createStringBinding("tab.visual_view"));
        client.visualTab.setGraphic(new FontIcon(Feather.EYE));
        client.visualTab.setContent(client.viewportBuilder.createVisualSimulationViewport());
        client.visualTab.setDisable(true);

        client.godTab = new Tab();
        client.godTab.textProperty().bind(i18n.createStringBinding("tab.god_mode"));
        client.godTab.setGraphic(new FontIcon(Feather.ZAP));
        client.interventionPanel = new InterventionPanel();
        client.interventionPanel.setCallback(new ClientInterventionBridge(client));
        client.godTab.setContent(client.interventionPanel);
        client.godTab.setDisable(true);

        client.statsTab = new Tab();
        client.statsTab.textProperty().bind(i18n.createStringBinding("tab.stats"));
        client.statsTab.setGraphic(new FontIcon(Feather.BAR_CHART_2));
        client.statisticsDashboard = new StatisticsDashboard();
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
        client.statsTab.setContent(client.statisticsDashboard);

        client.eventLogTab = new Tab();
        client.eventLogTab.textProperty().bind(i18n.createStringBinding("tab.log"));
        client.eventLogTab.setGraphic(new FontIcon(Feather.LIST));
        client.eventLogPane = new EventLogPane();
        if (client.simControlPanel != null) {
            client.eventLogPane.setStartDateTime(client.simControlPanel.getStartDateTime());
            client.eventLogPane.setSimulationStepSeconds(client.simControlPanel.getSimulationStepSeconds());
            client.simControlPanel.setOnStartDateTimeChange(newDt -> {
                if (client.eventLogPane != null) {
                    client.eventLogPane.setStartDateTime(newDt);
                }
            });
            client.simControlPanel.setOnStepChange(newStep -> {
                if (client.eventLogPane != null) {
                    client.eventLogPane.setSimulationStepSeconds(newStep);
                }
                if (client.localSimulation != null) {
                    client.localSimulation.setSimulationStepSeconds(newStep);
                }
            });
        }
        client.eventLogTab.setContent(client.eventLogPane);

        client.setSimTabsEnabled(false);

        client.scenarioHelper.wireSimulationControlCallbacks();

        subTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            client.update3DRenderingState();
            if (newTab == client.godTab) {
                if (client.simControlPanel != null && client.simControlPanel.isPlaying()) {
                    client.simControlPanel.pauseSimulation();
                    NotificationOverlay.show(
                            subTabs,
                            "⏸️ MODE DIVIN : SIMULATION MISE EN PAUSE AUTOMATIQUEMENT\nLa simulation a été mise en pause pour vous permettre d'agencer et programmer vos événements en toute sécurité.",
                            NotificationOverlay.NotificationType.WARNING
                    );
                }
                if (client.interventionPanel != null) {
                    client.interventionPanel.setSimulationRunning(false);
                    long currentTick = client.localSimulation != null ? client.localSimulation.getTickCount() : 0;
                    double stepSec = client.simControlPanel != null ? client.simControlPanel.getSimulationStepSeconds() : 0.0166f;
                    client.interventionPanel.syncCurrentSimulationTime(currentTick, stepSec);
                }
            }
        });

        Tab simSettingsTab = new Tab();
        simSettingsTab.textProperty().bind(i18n.createStringBinding("tab.settings"));
        simSettingsTab.setGraphic(new FontIcon(Feather.SETTINGS));
        simSettingsTab.setContent(new SettingsPane());

        client.glossaryPaneView = new GlossaryPaneView();
        client.glossaryTab = new Tab();
        client.glossaryTab.textProperty().bind(i18n.createStringBinding("tab.glossary"));
        client.glossaryTab.setGraphic(new FontIcon(Feather.BOOK_OPEN));
        client.glossaryTab.setContent(client.glossaryPaneView);

        subTabs.getTabs().addAll(controlsTab, client.visualTab, client.godTab, client.statsTab, client.eventLogTab, simSettingsTab, client.glossaryTab);
        pane.setCenter(subTabs);

        client.scenarioHelper.startLocalSimulationLoop();
        client.hudUpdater.startMainHudTimer();
        return pane;
    }
}
