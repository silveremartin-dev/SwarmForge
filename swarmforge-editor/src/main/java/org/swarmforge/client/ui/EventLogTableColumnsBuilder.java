/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.util.SimulationTimeConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Builds table columns, custom cell renderers, and row inspection bindings for {@link EventLogPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class EventLogTableColumnsBuilder {

    private static final DateTimeFormatter REAL_DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");
    private static final DateTimeFormatter SIM_DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");

    private EventLogTableColumnsBuilder() {
    }

    static void setupColumns(EventLogPane pane, TableView<SimulationEvent> table,
                             LocalDateTime startDateTime, float simulationStepSeconds) {
        I18nManager i18n = I18nManager.getInstance();

        // Column 1: Severity Badge
        TableColumn<SimulationEvent, SimulationEvent.Severity> colSeverity = pane.createSortableColumn(i18n.get("log.col.severity"));
        colSeverity.setMinWidth(90);
        colSeverity.setMaxWidth(110);
        colSeverity.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSeverity()));
        colSeverity.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(SimulationEvent.Severity sev, boolean empty) {
                super.updateItem(sev, empty);
                if (empty || sev == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label();
                    switch (sev) {
                        case CRITICAL -> {
                            badge.setText(i18n.get("log.severity.badge.critical"));
                            badge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");
                        }
                        case WARNING -> {
                            badge.setText(i18n.get("log.severity.badge.warning"));
                            badge.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: #18181b; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");
                        }
                        default -> {
                            badge.setText(i18n.get("log.severity.badge.info"));
                            badge.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");
                        }
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Column 2: Event ID (EVT-XXXXXX)
        TableColumn<SimulationEvent, Long> colId = pane.createSortableColumn(i18n.get("log.col.id"));
        colId.setMinWidth(110);
        colId.setMaxWidth(130);
        colId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSequenceId()));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long seqId, boolean empty) {
                super.updateItem(seqId, empty);
                if (empty || seqId == null) {
                    setText(null);
                    getStyleClass().remove("evt-cell-id");
                } else {
                    setText(String.format("EVT-%06d", seqId));
                    if (!getStyleClass().contains("evt-cell-id")) {
                        getStyleClass().add("evt-cell-id");
                    }
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Column 3: Real Timestamp (Date + Time)
        TableColumn<SimulationEvent, Instant> colTime = pane.createSortableColumn(i18n.get("log.col.timestamp"));
        colTime.setMinWidth(180);
        colTime.setMaxWidth(220);
        colTime.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTimestamp()));
        colTime.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Instant time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                    getStyleClass().remove("evt-cell-time");
                } else {
                    try {
                        setText(time.atZone(ZoneId.systemDefault()).format(REAL_DATETIME_FMT));
                    } catch (Exception e) {
                        setText("--:--:--");
                    }
                    if (!getStyleClass().contains("evt-cell-time")) {
                        getStyleClass().add("evt-cell-time");
                    }
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Column 4: Sim Time (Simulated Date + Time + SI Metric duration in tooltip!)
        TableColumn<SimulationEvent, Long> colTick = pane.createSortableColumn(i18n.get("log.col.sim_time"));
        colTick.setMinWidth(180);
        colTick.setMaxWidth(220);
        colTick.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTick()));
        colTick.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long tick, boolean empty) {
                super.updateItem(tick, empty);
                if (empty || tick == null) {
                    setText(null);
                    setTooltip(null);
                    getStyleClass().remove("evt-cell-tick");
                } else {
                    SimulationEvent ev = getTableRow() != null ? getTableRow().getItem() : null;
                    double evSec = (ev != null && ev.getSimTimeSeconds() >= 0.0)
                            ? ev.getSimTimeSeconds()
                            : (tick * (double) simulationStepSeconds);
                    LocalDateTime simDt = startDateTime.plusNanos((long) (evSec * 1_000_000_000L));
                    String formattedDt = simDt.format(SIM_DATETIME_FMT);
                    String formattedDuration = SimulationTimeConverter.formatSeconds(evSec);
                    setText(formattedDt);
                    setTooltip(new Tooltip("Date & Heure Simulées : " + formattedDt + "\nDurée : " + formattedDuration + "\n" + SimulationTimeConverter.getTechnicalTooltip(tick, simulationStepSeconds)));
                    if (!getStyleClass().contains("evt-cell-tick")) {
                        getStyleClass().add("evt-cell-tick");
                    }
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Column 5: Event Type
        TableColumn<SimulationEvent, String> colType = pane.createSortableColumn(i18n.get("log.col.type"));
        colType.setMinWidth(160);
        colType.setMaxWidth(200);
        colType.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(EventLogDetailDialog.formatTypeString(cellData.getValue().getType())));
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String typeStr, boolean empty) {
                super.updateItem(typeStr, empty);
                if (empty || typeStr == null) {
                    setText(null);
                } else {
                    setText(typeStr);
                    setStyle("-fx-font-weight: bold;");
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Column 6: Message & Details
        TableColumn<SimulationEvent, String> colMessage = pane.createSortableColumn(i18n.get("log.col.message"));
        colMessage.setCellValueFactory(cellData -> {
            SimulationEvent ev = cellData.getValue();
            if (ev == null) return new ReadOnlyObjectWrapper<>("");
            return new ReadOnlyObjectWrapper<>(ev.getMessage() != null ? ev.getMessage() : "");
        });
        colMessage.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    SimulationEvent ev = getTableRow() != null ? getTableRow().getItem() : null;
                    String rawMsg = msg != null ? msg : "";
                    StringBuilder msgBuilder = new StringBuilder(rawMsg);
                    if (ev != null && ev.getData() != null && !ev.getData().isEmpty()) {
                        msgBuilder.append("  📊 [");
                        boolean first = true;
                        for (java.util.Map.Entry<String, Object> entry : ev.getData().entrySet()) {
                            if (entry == null || entry.getKey() == null) continue;
                            if (!first) msgBuilder.append(", ");
                            msgBuilder.append(EventLogDetailDialog.formatKey(entry.getKey())).append(": ").append(EventLogDetailDialog.formatValue(entry.getValue()));
                            first = false;
                        }
                        msgBuilder.append("]");
                    }
                    setText(msgBuilder.toString());
                    setStyle("");
                    setWrapText(false);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        table.getColumns().addAll(colSeverity, colId, colTick, colTime, colType, colMessage);

        // Row factory for row background color & double click inspection
        table.setRowFactory(tv -> {
            TableRow<SimulationEvent> row = new TableRow<>() {
                @Override
                protected void updateItem(SimulationEvent ev, boolean empty) {
                    super.updateItem(ev, empty);
                    getStyleClass().removeAll("log-row-critical", "log-row-warning");
                    if (!empty && ev != null) {
                        if (ev.getSeverity() == SimulationEvent.Severity.CRITICAL) {
                            getStyleClass().add("log-row-critical");
                        } else if (ev.getSeverity() == SimulationEvent.Severity.WARNING) {
                            getStyleClass().add("log-row-warning");
                        }
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SimulationEvent selected = row.getItem();
                    if (selected != null) {
                        EventLogDetailDialog.show(selected);
                    }
                }
            });
            return row;
        });
    }
}
