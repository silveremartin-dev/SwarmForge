/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.swarmforge.core.event.EventBus;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.util.SimulationTimeConverter;

import javafx.collections.transformation.SortedList;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Event Log Panel - Display and filter simulation events with a sortable TableView.
 * Shows real-time event stream with sortable columns, severity indicators, and detail inspection.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EventLogPane extends BorderPane {

    private static final DateTimeFormatter REAL_DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");
    private static final DateTimeFormatter SIM_DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");

    private final ObservableList<SimulationEvent> events = FXCollections.observableArrayList();
    private final FilteredList<SimulationEvent> filteredEvents;
    private final SortedList<SimulationEvent> sortedEvents;
    private final TableView<SimulationEvent> eventTable;

    ComboBox<String> typeFilter;
    ComboBox<String> severityFilter;
    TextField searchField;
    CheckBox autoScrollCheck;
    private Label eventCountLabel;
    private Label logPathLabel;

    private SimulationEvent lastAddedEvent = null;
    private long totalRecordedCount = 0;
    private String scenarioName = "swarmforge";
    private float simulationStepSeconds = 0.0166f;
    private LocalDateTime startDateTime = LocalDateTime.of(2026, 3, 20, 8, 0, 0);

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = (startDateTime != null) ? startDateTime : LocalDateTime.of(2026, 3, 20, 8, 0, 0);
        if (eventTable != null) {
            eventTable.refresh();
        }
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public float getSimulationStepSeconds() {
        return simulationStepSeconds;
    }

    public void setSimulationStepSeconds(float stepSeconds) {
        if (stepSeconds > 0) {
            this.simulationStepSeconds = stepSeconds;
            if (eventTable != null) {
                eventTable.refresh();
            }
        }
    }

    public String getScenarioName() {
        return scenarioName;
    }

    public void setScenarioName(String scenarioName) {
        if (scenarioName != null && !scenarioName.isBlank()) {
            this.scenarioName = scenarioName;
            updateLogPathDisplay();
            updateStats();
        }
    }

    private void updateLogPathDisplay() {
        if (logPathLabel == null) return;
        Runnable action = () -> {
            String sanitizedScenario = (scenarioName != null && !scenarioName.isBlank())
                    ? scenarioName.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "_").replaceAll("_+", "_")
                    : "swarmforge";
            java.io.File logDir = new java.io.File(System.getProperty("user.dir"), "logs");
            java.io.File logFile = new java.io.File(logDir, sanitizedScenario + "_simulation.log");
            String pathStr = logFile.getAbsolutePath();
            logPathLabel.setText("📁 Log Path: " + pathStr);
            logPathLabel.setTooltip(new Tooltip("Full simulation log storage path: " + pathStr + "\n(Click to copy path to clipboard)"));
        };
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    public EventLogPane() {
        setPadding(new Insets(10));

        // Setup filtered list and sorted list
        filteredEvents = new FilteredList<>(events, e -> true);
        sortedEvents = new SortedList<>(filteredEvents);

        // Top: Title and Filters
        setTop(createToolbar());

        // Center: Sortable TableView for Events
        eventTable = createEventTable();
        sortedEvents.comparatorProperty().bind(eventTable.comparatorProperty());

        setCenter(eventTable);

        // Bottom: Stats Bar
        setBottom(createStatsBar());

        // Populate initial history if EventBus has history
        java.util.List<SimulationEvent> history = EventBus.getInstance().getHistory();
        if (history != null && !history.isEmpty()) {
            events.addAll(history);
            totalRecordedCount = history.size();
            updateStats();
        }

        // Subscribe to EventBus with throttling for duplicate events
        EventBus.getInstance().subscribeAll(event -> Platform.runLater(() -> {
            if (isDuplicateEvent(event)) {
                return;
            }
            lastAddedEvent = event;
            totalRecordedCount++;
            events.add(event);

            // Cap UI event log size to prevent memory leaks in ultra-long runs (max 10,000)
            if (events.size() > 10000) {
                events.remove(0, events.size() - 10000);
            }

            if (autoScrollCheck.isSelected() && !sortedEvents.isEmpty()) {
                eventTable.scrollTo(sortedEvents.size() - 1);
            }
            updateStats();
        }));
    }

    private boolean isDuplicateEvent(SimulationEvent event) {
        if (lastAddedEvent == null) return false;
        if (event.getType() == SimulationEvent.EventType.SIMULATION_PAUSED ||
            event.getType() == SimulationEvent.EventType.SIMULATION_STARTED ||
            event.getType() == SimulationEvent.EventType.SIMULATION_STOPPED) {
            return lastAddedEvent.getType() == event.getType() &&
                    Objects.equals(lastAddedEvent.getMessage(), event.getMessage());
        }
        return false;
    }

    /**
     * Creates a column header graphic with column title and ▲ (ASC), ▼ (DESC), ↺ (RESET) sort buttons.
     */
    private <T> TableColumn<SimulationEvent, T> createSortableColumn(String title) {
        TableColumn<SimulationEvent, T> col = new TableColumn<>();
        Label lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        Button btnAsc = new Button("▲");
        Button btnDesc = new Button("▼");
        Button btnReset = new Button("↺");

        btnAsc.getStyleClass().add("sort-btn");
        btnDesc.getStyleClass().add("sort-btn");
        btnReset.getStyleClass().add("sort-btn");

        btnAsc.setTooltip(new Tooltip("Sort ascending (A-Z / 0-9)"));
        btnDesc.setTooltip(new Tooltip("Sort descending (Z-A / 9-0)"));
        btnReset.setTooltip(new Tooltip("Reset sort (Default order)"));

        btnAsc.setOnAction(e -> {
            eventTable.getSortOrder().clear();
            col.setSortType(TableColumn.SortType.ASCENDING);
            eventTable.getSortOrder().add(col);
            updateSortButtonStyles();
        });

        btnDesc.setOnAction(e -> {
            eventTable.getSortOrder().clear();
            col.setSortType(TableColumn.SortType.DESCENDING);
            eventTable.getSortOrder().add(col);
            updateSortButtonStyles();
        });

        btnReset.setOnAction(e -> {
            eventTable.getSortOrder().clear();
            updateSortButtonStyles();
        });

        col.getProperties().put("btnAsc", btnAsc);
        col.getProperties().put("btnDesc", btnDesc);
        col.getProperties().put("btnReset", btnReset);

        HBox btnBox = new HBox(2, btnAsc, btnDesc, btnReset);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        HBox header = new HBox(6, lbl, btnBox);
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(lbl, Priority.ALWAYS);

        col.setGraphic(header);
        return col;
    }

    private void updateSortButtonStyles() {
        ObservableList<TableColumn<SimulationEvent, ?>> sortOrder = eventTable.getSortOrder();
        TableColumn<SimulationEvent, ?> activeCol = sortOrder.isEmpty() ? null : sortOrder.get(0);

        for (TableColumn<SimulationEvent, ?> col : eventTable.getColumns()) {
            Button btnAsc = (Button) col.getProperties().get("btnAsc");
            Button btnDesc = (Button) col.getProperties().get("btnDesc");
            Button btnReset = (Button) col.getProperties().get("btnReset");

            if (btnAsc == null || btnDesc == null || btnReset == null) continue;

            btnAsc.getStyleClass().removeAll("sort-btn", "sort-btn-active");
            btnDesc.getStyleClass().removeAll("sort-btn", "sort-btn-active");
            btnReset.getStyleClass().removeAll("sort-btn", "sort-btn-active");

            if (col.equals(activeCol)) {
                if (col.getSortType() == TableColumn.SortType.ASCENDING) {
                    btnAsc.getStyleClass().add("sort-btn-active");
                    btnDesc.getStyleClass().add("sort-btn");
                    btnReset.getStyleClass().add("sort-btn");
                } else if (col.getSortType() == TableColumn.SortType.DESCENDING) {
                    btnAsc.getStyleClass().add("sort-btn");
                    btnDesc.getStyleClass().add("sort-btn-active");
                    btnReset.getStyleClass().add("sort-btn");
                } else {
                    btnAsc.getStyleClass().add("sort-btn");
                    btnDesc.getStyleClass().add("sort-btn");
                    btnReset.getStyleClass().add("sort-btn");
                }
            } else {
                btnAsc.getStyleClass().add("sort-btn");
                btnDesc.getStyleClass().add("sort-btn");
                btnReset.getStyleClass().add("sort-btn");
            }
        }
    }

    private TableView<SimulationEvent> createEventTable() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        TableView<SimulationEvent> table = new TableView<>(sortedEvents);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSortOrder().addListener((javafx.collections.ListChangeListener<TableColumn<SimulationEvent, ?>>) c -> updateSortButtonStyles());

        // Column 1: Severity Badge
        TableColumn<SimulationEvent, SimulationEvent.Severity> colSeverity = createSortableColumn(i18n.get("log.col.severity"));
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
        TableColumn<SimulationEvent, Long> colId = createSortableColumn(i18n.get("log.col.id"));
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
        TableColumn<SimulationEvent, Instant> colTime = createSortableColumn(i18n.get("log.col.timestamp"));
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
        TableColumn<SimulationEvent, Long> colTick = createSortableColumn(i18n.get("log.col.sim_time"));
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
        TableColumn<SimulationEvent, String> colType = createSortableColumn(i18n.get("log.col.type"));
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
        TableColumn<SimulationEvent, String> colMessage = createSortableColumn(i18n.get("log.col.message"));
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

        return table;
    }

    private VBox createToolbar() {
        return EventLogExportHelper.createToolbar(this, this::updateFilter, this::clearLog, this::exportEvents);
    }

    private void updateFilter() {
        String typeValue = typeFilter.getValue();
        String sevValue = severityFilter.getValue();
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";
        filteredEvents.setPredicate(EventLogExportHelper.buildFilterPredicate(typeValue, sevValue, query));
        updateStats();
    }

    private HBox createStatsBar() {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(6, 12, 6, 12));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("legend-bar");

        eventCountLabel = new Label("Total: 0 recorded events | Displayed: 0");
        eventCountLabel.getStyleClass().add("bold-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Copyable Full Simulation Log Path Pill
        logPathLabel = new Label();
        logPathLabel.getStyleClass().addAll("accent-text", "ctrl-btn");
        logPathLabel.setOnMouseClicked(e -> EventLogExportHelper.copyLogPathToClipboard(this, scenarioName));
        updateLogPathDisplay();

        bar.getChildren().addAll(eventCountLabel, spacer, logPathLabel);
        return bar;
    }

    private void updateStats() {
        if (eventCountLabel == null) return;
        Runnable action = () -> {
            org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
            eventCountLabel.setText(i18n.get("log.stats.summary",
                totalRecordedCount, events.size(), sortedEvents.size()));
            updateLogPathDisplay();
        };
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }

    private void exportEvents() {
        EventLogExportHelper.exportEvents(getScene() != null ? getScene().getWindow() : null, this, scenarioName, sortedEvents);
    }

    /**
     * Add an event manually (for non-EventBus events).
     */
    public void addEvent(SimulationEvent event) {
        Platform.runLater(() -> {
            events.add(event);
            if (autoScrollCheck.isSelected() && !filteredEvents.isEmpty()) {
                eventTable.scrollTo(filteredEvents.size() - 1);
            }
            updateStats();
        });
    }

    /**
     * Clear all recorded log events and reset event bus history.
     */
    public void clearLog() {
        Runnable doClear = () -> {
            events.clear();
            lastAddedEvent = null;
            totalRecordedCount = 0;
            EventBus.getInstance().clearHistory();
            updateStats();
        };
        if (Platform.isFxApplicationThread()) {
            doClear.run();
        } else {
            Platform.runLater(doClear);
        }
    }

    /**
     * Truncate log events occurring after targetTick.
     */
    public void truncateAfterTick(long targetTick) {
        Runnable doTruncate = () -> {
            events.removeIf(e -> e.getTick() > targetTick);
            EventBus.getInstance().truncateAfter(targetTick);
            totalRecordedCount = events.size();
            updateStats();
        };
        if (Platform.isFxApplicationThread()) {
            doTruncate.run();
        } else {
            Platform.runLater(doTruncate);
        }
    }
}
