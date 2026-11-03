/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * Encapsulates the scheduled intervention events queue view, controls,
 * chronological sorting, repeating, and deletion dialogs.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class InterventionEventQueueCard {

    /**
     * Builds the Scheduled Event Queue Box with Multi-Selection, Chronological Sorting,
     * Repeat Event capability, and clean time formatting.
     */
    public static VBox createQueueBlock(
            ListView<InterventionPanel.ScheduledEvent> scheduledEventsListView,
            ObservableList<InterventionPanel.ScheduledEvent> scheduledEventsList,
            DoubleSupplier simulationStepSecSupplier,
            Runnable onEventsModified) {

        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(10);
        box.setPadding(new Insets(12));
        box.getStyleClass().add("queue-box");

        Label lblQueueTitle = new Label();
        lblQueueTitle.textProperty().bind(i18n.createStringBinding("god.queue.title"));
        lblQueueTitle.getStyleClass().add("accent-title");

        scheduledEventsListView.setPrefHeight(180);
        scheduledEventsListView.getStyleClass().add("log-text-area");
        scheduledEventsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        scheduledEventsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(InterventionPanel.ScheduledEvent ev, boolean empty) {
                super.updateItem(ev, empty);
                if (empty || ev == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    HBox cellBox = new HBox(8);
                    cellBox.setAlignment(Pos.CENTER_LEFT);
                    cellBox.setPadding(new Insets(4, 8, 4, 8));

                    FontIcon catIconNode = new FontIcon(ev.category.icon);
                    catIconNode.setIconSize(12);
                    catIconNode.setIconColor(javafx.scene.paint.Color.WHITE);

                    Label badgeCat = new Label(" " + ev.category.getDisplayName(), catIconNode);
                    badgeCat.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 3;", ev.category.color));

                    Label timeLabel = new Label("[" + ev.timeFormatted + "]");
                    timeLabel.getStyleClass().add("accent-title");

                    Label descLabel = new Label(ev.eventType + (ev.colonyTarget != null ? " (" + ev.colonyTarget + ")" : "") + " : " + ev.description);
                    HBox.setHgrow(descLabel, Priority.ALWAYS);

                    FontIcon stIconNode = new FontIcon(ev.executed ? Feather.CHECK_CIRCLE : ev.paused ? Feather.PAUSE_CIRCLE : Feather.CLOCK);
                    stIconNode.setIconSize(11);
                    stIconNode.setIconColor(javafx.scene.paint.Color.WHITE);

                    String statusText = " " + (ev.executed ? i18n.get("god.status.executed") : ev.paused ? i18n.get("god.status.paused") : i18n.get("god.status.pending"));
                    String statusBg = ev.executed ? "#16a34a" : ev.paused ? "#d97706" : "#0284c7";
                    Label statusBadge = new Label(statusText, stIconNode);
                    statusBadge.setStyle(String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 3;", statusBg));

                    cellBox.getChildren().addAll(badgeCat, timeLabel, descLabel, statusBadge);

                    if (ev.executed) {
                        cellBox.setOpacity(0.75);
                    } else {
                        cellBox.setOpacity(1.0);
                    }

                    setGraphic(cellBox);
                }
            }
        });

        // Queue Control Toolbar Buttons
        HBox btnToolbar = new HBox(8);
        btnToolbar.setAlignment(Pos.CENTER_LEFT);

        Button btnRepeatEv = new Button();
        btnRepeatEv.textProperty().bind(i18n.createStringBinding("god.queue.btn.repeat"));
        btnRepeatEv.getStyleClass().add("btn-secondary");
        btnRepeatEv.tooltipProperty().bind(i18n.createTooltipBinding("god.queue.repeat.tt"));
        btnRepeatEv.setOnAction(e -> {
            repeatSelectedEvents(scheduledEventsListView, scheduledEventsList, simulationStepSecSupplier.getAsDouble(), 10);
            if (onEventsModified != null) onEventsModified.run();
        });

        Button btnDeleteEv = new Button();
        btnDeleteEv.textProperty().bind(i18n.createStringBinding("god.queue.btn.delete"));
        btnDeleteEv.getStyleClass().add("btn-danger");
        btnDeleteEv.tooltipProperty().bind(i18n.createTooltipBinding("god.queue.delete.tt"));
        btnDeleteEv.setOnAction(e -> {
            deleteSelectedEvents(scheduledEventsListView, scheduledEventsList);
            if (onEventsModified != null) onEventsModified.run();
        });

        Button btnClearAll = new Button();
        btnClearAll.textProperty().bind(i18n.createStringBinding("god.queue.btn.clear_all"));
        btnClearAll.getStyleClass().add("btn-danger");
        btnClearAll.tooltipProperty().bind(i18n.createTooltipBinding("god.queue.clear_all.tt"));
        btnClearAll.setOnAction(e -> {
            clearAllUnexecuted(scheduledEventsList);
            if (onEventsModified != null) onEventsModified.run();
        });

        btnToolbar.getChildren().addAll(btnRepeatEv, btnDeleteEv, btnClearAll);

        box.getChildren().addAll(lblQueueTitle, scheduledEventsListView, btnToolbar);
        return box;
    }

    public static void sortScheduledEvents(ObservableList<InterventionPanel.ScheduledEvent> scheduledEventsList) {
        scheduledEventsList.sort(Comparator.comparingLong((InterventionPanel.ScheduledEvent ev) -> ev.targetTick));
    }

    public static void repeatSelectedEvents(
            ListView<InterventionPanel.ScheduledEvent> listView,
            ObservableList<InterventionPanel.ScheduledEvent> scheduledEventsList,
            double simulationStepSec,
            int offsetSeconds) {

        List<InterventionPanel.ScheduledEvent> selected = new ArrayList<>(listView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;

        long offsetTicks = Math.max(1, Math.round(offsetSeconds / (simulationStepSec > 0 ? simulationStepSec : 0.016666666666666666)));
        List<InterventionPanel.ScheduledEvent> newEvents = new ArrayList<>();

        for (InterventionPanel.ScheduledEvent ev : selected) {
            if (ev != null) {
                InterventionPanel.ScheduledEvent clone = ev.copyWithOffset(offsetTicks, simulationStepSec);
                newEvents.add(clone);
            }
        }

        scheduledEventsList.addAll(newEvents);
        sortScheduledEvents(scheduledEventsList);

        listView.getSelectionModel().clearSelection();
        for (InterventionPanel.ScheduledEvent clone : newEvents) {
            listView.getSelectionModel().select(clone);
        }
    }

    public static void deleteSelectedEvents(
            ListView<InterventionPanel.ScheduledEvent> listView,
            ObservableList<InterventionPanel.ScheduledEvent> scheduledEventsList) {

        List<InterventionPanel.ScheduledEvent> selected = new ArrayList<>(listView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Supprimer les événements sélectionnés (" + selected.size() + ") ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation de suppression");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                for (InterventionPanel.ScheduledEvent ev : selected) {
                    if (ev != null && !ev.executed) {
                        scheduledEventsList.remove(ev);
                    }
                }
                sortScheduledEvents(scheduledEventsList);
            }
        });
    }

    public static void clearAllUnexecuted(ObservableList<InterventionPanel.ScheduledEvent> scheduledEventsList) {
        if (scheduledEventsList.isEmpty()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment effacer tous les événements planifiés non exécutés ?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Confirmation d'effacement de la file");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                scheduledEventsList.removeIf(ev -> !ev.executed);
                sortScheduledEvents(scheduledEventsList);
            }
        });
    }

    public static void scrollToFirstUpcomingEvent(
            ListView<InterventionPanel.ScheduledEvent> listView,
            ObservableList<InterventionPanel.ScheduledEvent> scheduledEventsList,
            long currentSimulationTick) {

        if (listView == null || scheduledEventsList.isEmpty()) return;
        for (int i = 0; i < scheduledEventsList.size(); i++) {
            InterventionPanel.ScheduledEvent ev = scheduledEventsList.get(i);
            if (!ev.executed || ev.targetTick >= currentSimulationTick) {
                listView.scrollTo(i);
                break;
            }
        }
    }

    public static javafx.scene.Node createSpatialPositionSubBlockNode(InterventionPanel p) {
        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(10);
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(12); grid.setVgap(10);

        p.terrainDimensionsInfoLabel = new Label(i18n.get("god.spatial.terrain_coverage",
                p.terrainWidthMeters, p.terrainLengthMeters, p.terrainDepthMeters, p.terrainMaxAltitudeMeters));
        p.terrainDimensionsInfoLabel.setStyle("-fx-text-fill: #0ea5e9; -fx-font-size: 11px; -fx-font-weight: bold;");

        Label lblX = new Label(); lblX.textProperty().bind(i18n.createStringBinding("god.spatial.x.label"));
        lblX.setStyle("-fx-font-weight: bold;"); lblX.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.x.tt"));
        p.posXSlider = new javafx.scene.control.Slider(0.0, p.terrainWidthMeters, p.terrainWidthMeters / 2.0);
        p.posXSlider.setPrefWidth(260); p.posXSlider.setMajorTickUnit(Math.max(1.0, p.terrainWidthMeters / 4.0));
        p.posXSlider.setMinorTickCount(3); p.posXSlider.setSnapToTicks(false);
        p.posXSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.x.slider.tt"));
        p.posXValLabel = new Label(); p.posXValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        Runnable updateXText = () -> {
            double v = p.posXSlider.getValue(), c = p.terrainWidthMeters / 2.0;
            double pct = Math.min(100.0, Math.max(0.0, (v / p.terrainWidthMeters) * 100.0));
            String note = (Math.abs(v - c) < (p.terrainWidthMeters * 0.05)) ? i18n.get("god.spatial.center") :
                          (v < p.terrainWidthMeters * 0.15) ? i18n.get("god.spatial.far_west") :
                          (v > p.terrainWidthMeters * 0.85) ? i18n.get("god.spatial.far_east") : "";
            p.posXValLabel.setText(String.format("%.1f m / %.1f m (%.0f%%%s)", v, p.terrainWidthMeters, pct, note));
        };
        p.posXSlider.valueProperty().addListener((o, a, b) -> updateXText.run());
        updateXText.run();

        Label lblY = new Label(); lblY.textProperty().bind(i18n.createStringBinding("god.spatial.y.label"));
        lblY.setStyle("-fx-font-weight: bold;"); lblY.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.y.tt"));
        p.posYSlider = new javafx.scene.control.Slider(0.0, p.terrainLengthMeters, p.terrainLengthMeters / 2.0);
        p.posYSlider.setPrefWidth(260); p.posYSlider.setMajorTickUnit(Math.max(1.0, p.terrainLengthMeters / 4.0));
        p.posYSlider.setMinorTickCount(3); p.posYSlider.setSnapToTicks(false);
        p.posYSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.y.slider.tt"));
        p.posYValLabel = new Label(); p.posYValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        Runnable updateYText = () -> {
            double v = p.posYSlider.getValue(), c = p.terrainLengthMeters / 2.0;
            double pct = Math.min(100.0, Math.max(0.0, (v / p.terrainLengthMeters) * 100.0));
            String note = (Math.abs(v - c) < (p.terrainLengthMeters * 0.05)) ? i18n.get("god.spatial.center") :
                          (v < p.terrainLengthMeters * 0.15) ? i18n.get("god.spatial.far_south") :
                          (v > p.terrainLengthMeters * 0.85) ? i18n.get("god.spatial.far_north") : "";
            p.posYValLabel.setText(String.format("%.1f m / %.1f m (%.0f%%%s)", v, p.terrainLengthMeters, pct, note));
        };
        p.posYSlider.valueProperty().addListener((o, a, b) -> updateYText.run());
        updateYText.run();

        Label lblZ = new Label(); lblZ.textProperty().bind(i18n.createStringBinding("god.spatial.z.label"));
        lblZ.setStyle("-fx-font-weight: bold;"); lblZ.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.z.tt"));
        p.posZSlider = new javafx.scene.control.Slider(-p.terrainDepthMeters, p.terrainMaxAltitudeMeters, 1.0);
        p.posZSlider.setPrefWidth(260); p.posZSlider.setMajorTickUnit(5.0); p.posZSlider.setMinorTickCount(4);
        p.posZSlider.setSnapToTicks(false); p.posZSlider.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.z.slider.tt"));
        p.posZValLabel = new Label(); p.posZValLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");

        p.atSurfaceCheckBox = new javafx.scene.control.CheckBox();
        p.atSurfaceCheckBox.textProperty().bind(i18n.createStringBinding("god.spatial.atsurface"));
        p.atSurfaceCheckBox.setSelected(true);
        p.atSurfaceCheckBox.setStyle("-fx-font-weight: bold; -fx-text-fill: #22c55e; -fx-cursor: hand;");
        p.atSurfaceCheckBox.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.atsurface.tt"));

        Runnable updateZText = () -> {
            boolean atSurf = p.atSurfaceCheckBox.isSelected();
            p.posZSlider.setDisable(atSurf);
            if (atSurf) {
                double x = p.posXSlider != null ? p.posXSlider.getValue() : 0;
                double y = p.posYSlider != null ? p.posYSlider.getValue() : 0;
                p.posZValLabel.setText(String.format(i18n.get("god.z_surface"), x, y, p.getCalculatedSurfaceZ(x, y)));
                p.posZValLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            } else {
                double v = p.posZSlider.getValue();
                String desc = (v < 0.0) ? i18n.get("god.spatial.subterranean", v, p.terrainDepthMeters) :
                              (Math.abs(v - 1.0) < 0.5) ? i18n.get("god.spatial.surface_level") :
                              (v <= 5.0) ? i18n.get("god.spatial.low_relief", v) :
                              i18n.get("god.spatial.aerial", v, p.terrainMaxAltitudeMeters);
                p.posZValLabel.setText(desc);
                p.posZValLabel.setStyle(v < 0.0 ? "-fx-text-fill: #f59e0b; -fx-font-weight: bold;" : v > 5.0 ? "-fx-text-fill: #c084fc; -fx-font-weight: bold;" : "-fx-text-fill: #4ade80; -fx-font-weight: bold;");
            }
        };
        p.atSurfaceCheckBox.selectedProperty().addListener((o, a, b) -> updateZText.run());
        p.posZSlider.valueProperty().addListener((o, a, b) -> updateZText.run());
        p.posXSlider.valueProperty().addListener((o, a, b) -> { if (p.atSurfaceCheckBox.isSelected()) updateZText.run(); });
        p.posYSlider.valueProperty().addListener((o, a, b) -> { if (p.atSurfaceCheckBox.isSelected()) updateZText.run(); });
        updateZText.run();

        grid.add(lblX, 0, 0); grid.add(p.posXSlider, 1, 0); grid.add(p.posXValLabel, 2, 0);
        grid.add(lblY, 0, 1); grid.add(p.posYSlider, 1, 1); grid.add(p.posYValLabel, 2, 1);
        grid.add(lblZ, 0, 2); grid.add(p.posZSlider, 1, 2); grid.add(p.posZValLabel, 2, 2);
        grid.add(p.atSurfaceCheckBox, 1, 3, 2, 1);

        Button btnResetCenter = new Button();
        btnResetCenter.textProperty().bind(i18n.createStringBinding("god.spatial.reset.btn"));
        btnResetCenter.getStyleClass().addAll("btn-secondary", "action-outline-button");
        btnResetCenter.tooltipProperty().bind(i18n.createTooltipBinding("god.spatial.reset.btn.tt"));
        btnResetCenter.setOnAction(e -> {
            p.posXSlider.setValue(p.terrainWidthMeters / 2.0);
            p.posYSlider.setValue(p.terrainLengthMeters / 2.0);
            p.posZSlider.setValue(1.0);
            p.atSurfaceCheckBox.setSelected(true);
        });
        box.getChildren().addAll(p.terrainDimensionsInfoLabel, grid, btnResetCenter);
        return box;
    }
}
