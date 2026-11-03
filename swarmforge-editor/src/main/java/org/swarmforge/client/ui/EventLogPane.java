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
    <T> TableColumn<SimulationEvent, T> createSortableColumn(String title) {
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
        TableView<SimulationEvent> table = new TableView<>(sortedEvents);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSortOrder().addListener((javafx.collections.ListChangeListener<TableColumn<SimulationEvent, ?>>) c -> updateSortButtonStyles());

        EventLogTableColumnsBuilder.setupColumns(this, table, startDateTime, simulationStepSeconds);
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
