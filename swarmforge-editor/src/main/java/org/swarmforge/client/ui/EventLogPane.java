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

import javafx.collections.transformation.SortedList;

import java.time.Instant;
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

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    private final ObservableList<SimulationEvent> events = FXCollections.observableArrayList();
    private final FilteredList<SimulationEvent> filteredEvents;
    private final SortedList<SimulationEvent> sortedEvents;
    private final TableView<SimulationEvent> eventTable;

    private ComboBox<String> typeFilter;
    private ComboBox<String> severityFilter;
    private TextField searchField;
    private CheckBox autoScrollCheck;
    private Label eventCountLabel;

    private SimulationEvent lastAddedEvent = null;
    private long totalRecordedCount = 0;

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
        TableView<SimulationEvent> table = new TableView<>(sortedEvents);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSortOrder().addListener((javafx.collections.ListChangeListener<TableColumn<SimulationEvent, ?>>) c -> updateSortButtonStyles());

        // Column 1: Severity Badge
        TableColumn<SimulationEvent, SimulationEvent.Severity> colSeverity = createSortableColumn("Severity");
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
                            badge.setText("CRITICAL");
                            badge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");
                        }
                        case WARNING -> {
                            badge.setText("WARNING");
                            badge.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: #18181b; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");
                        }
                        default -> {
                            badge.setText("INFO");
                            badge.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");
                        }
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Column 2: Event ID (EVT-XXXXXX)
        TableColumn<SimulationEvent, Long> colId = createSortableColumn("Event ID");
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

        // Column 3: Timestamp
        TableColumn<SimulationEvent, Instant> colTime = createSortableColumn("Timestamp");
        colTime.setMinWidth(100);
        colTime.setMaxWidth(120);
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
                        setText(time.atZone(ZoneId.systemDefault()).format(TIME_FMT));
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

        // Column 4: Tick
        TableColumn<SimulationEvent, Long> colTick = createSortableColumn("Tick");
        colTick.setMinWidth(90);
        colTick.setMaxWidth(110);
        colTick.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTick()));
        colTick.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long tick, boolean empty) {
                super.updateItem(tick, empty);
                if (empty || tick == null) {
                    setText(null);
                    getStyleClass().remove("evt-cell-tick");
                } else {
                    setText("T#" + tick);
                    if (!getStyleClass().contains("evt-cell-tick")) {
                        getStyleClass().add("evt-cell-tick");
                    }
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Column 5: Event Type
        TableColumn<SimulationEvent, String> colType = createSortableColumn("Event Type");
        colType.setMinWidth(160);
        colType.setMaxWidth(200);
        colType.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(formatTypeString(cellData.getValue().getType())));
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
        TableColumn<SimulationEvent, String> colMessage = createSortableColumn("Details & Message");
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
                            msgBuilder.append(formatKey(entry.getKey())).append(": ").append(formatValue(entry.getValue()));
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

        table.getColumns().addAll(colSeverity, colId, colTime, colTick, colType, colMessage);

        // Row factory for row background color & double click inspection
        table.setRowFactory(tv -> {
            TableRow<SimulationEvent> row = new TableRow<>() {
                @Override
                protected void updateItem(SimulationEvent ev, boolean empty) {
                    super.updateItem(ev, empty);
                    if (empty || ev == null) {
                        setStyle("-fx-background-color: transparent;");
                    } else if (ev.getSeverity() == SimulationEvent.Severity.CRITICAL) {
                        setStyle("-fx-background-color: rgba(239, 68, 68, 0.15);");
                    } else if (ev.getSeverity() == SimulationEvent.Severity.WARNING) {
                        setStyle("-fx-background-color: rgba(245, 158, 11, 0.12);");
                    } else {
                        setStyle("-fx-background-color: transparent;");
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    SimulationEvent selected = row.getItem();
                    if (selected != null) {
                        showEventDetails(selected);
                    }
                }
            });
            return row;
        });

        return table;
    }

    private VBox createToolbar() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox toolbar = new VBox(10);
        toolbar.setPadding(new Insets(0, 0, 10, 0));

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("log.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        // Search Field
        searchField = new TextField();
        searchField.promptTextProperty().bind(i18n.createStringBinding("log.search.prompt"));
        searchField.setPrefWidth(220);
        searchField.setTooltip(new Tooltip("Filter events by keyword (message, ID, data)..."));
        searchField.textProperty().addListener((obs, oldV, newV) -> updateFilter());

        // Type filter
        Label typeLabel = new Label();
        typeLabel.textProperty().bind(i18n.createStringBinding("log.filter.type"));
        typeFilter = new ComboBox<>();
        typeFilter.setTooltip(new Tooltip("Filter list by specific event type (Founding, Birth, Combat, etc.)."));
        typeFilter.getItems().add(i18n.get("log.type.all"));
        for (SimulationEvent.EventType type : SimulationEvent.EventType.values()) {
            if (type == SimulationEvent.EventType.INFO ||
                type == SimulationEvent.EventType.BIRTH ||
                type == SimulationEvent.EventType.DEATH ||
                type == SimulationEvent.EventType.SYSTEM ||
                type == SimulationEvent.EventType.DEBUG ||
                type == SimulationEvent.EventType.ERROR ||
                type == SimulationEvent.EventType.WARNING) {
                continue;
            }
            typeFilter.getItems().add(formatTypeString(type));
        }
        typeFilter.getSelectionModel().selectFirst();
        typeFilter.setOnAction(e -> updateFilter());

        // Severity filter
        Label sevLabel = new Label();
        sevLabel.textProperty().bind(i18n.createStringBinding("log.filter.severity"));
        severityFilter = new ComboBox<>();
        severityFilter.setTooltip(new Tooltip("Filter list by severity level (Info, Warning, Critical)."));
        severityFilter.getItems().addAll(
            i18n.get("log.severity.all"),
            i18n.get("log.severity.critical"),
            i18n.get("log.severity.info"),
            i18n.get("log.severity.warning")
        );
        severityFilter.getSelectionModel().selectFirst();
        severityFilter.setOnAction(e -> updateFilter());

        // Auto-scroll
        autoScrollCheck = new CheckBox();
        autoScrollCheck.textProperty().bind(i18n.createStringBinding("log.filter.auto_scroll"));
        autoScrollCheck.setSelected(true);
        autoScrollCheck.setTooltip(new Tooltip("Automatically scroll table when receiving new events."));

        // Clear button
        Button btnClear = new Button();
        btnClear.textProperty().bind(i18n.createStringBinding("log.btn.clear"));
        btnClear.setTooltip(new Tooltip("Clear all recorded events from log."));
        btnClear.setOnAction(e -> clearLog());

        // Export button
        Button btnExport = new Button();
        btnExport.textProperty().bind(i18n.createStringBinding("log.btn.export"));
        btnExport.setTooltip(new Tooltip("Export displayed events to CSV or JSON format."));
        btnExport.setOnAction(e -> exportEvents());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        filters.getChildren().addAll(searchField, typeLabel, typeFilter, sevLabel, severityFilter, autoScrollCheck,
                spacer, btnClear, btnExport);

        toolbar.getChildren().addAll(title, filters);
        return toolbar;
    }

    private void updateFilter() {
        String typeValue = typeFilter.getValue();
        String sevValue = severityFilter.getValue();
        String query = searchField.getText() != null ? searchField.getText().trim().toLowerCase() : "";

        Predicate<SimulationEvent> typePred = e -> {
            if ("Tous".equals(typeValue) || "All".equals(typeValue) || typeValue == null) return true;
            return formatTypeString(e.getType()).equals(typeValue) || e.getType().name().equals(typeValue);
        };
        Predicate<SimulationEvent> sevPred = e -> {
            if ("Toutes sévérités".equals(sevValue) || "All Severities".equals(sevValue) || "Tous".equals(sevValue) || sevValue == null) return true;
            if ("Critique uniquement".equals(sevValue) || "Critical Only".equals(sevValue) || "CRITICAL".equals(sevValue)) return e.getSeverity() == SimulationEvent.Severity.CRITICAL;
            if ("Info uniquement".equals(sevValue) || "Info Only".equals(sevValue) || "INFO".equals(sevValue)) return e.getSeverity() == SimulationEvent.Severity.INFO;
            if ("Attention uniquement".equals(sevValue) || "Warning Only".equals(sevValue) || "WARNING".equals(sevValue)) return e.getSeverity() == SimulationEvent.Severity.WARNING;
            return true;
        };
        Predicate<SimulationEvent> searchPred = e -> {
            if (query.isEmpty()) return true;
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains(query)) return true;
            if (e.getType() != null && e.getType().name().toLowerCase().contains(query)) return true;
            if (e.getData() != null && e.getData().toString().toLowerCase().contains(query)) return true;
            return false;
        };

        filteredEvents.setPredicate(typePred.and(sevPred).and(searchPred));
        updateStats();
    }

    private HBox createStatsBar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(6, 12, 6, 12));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.getStyleClass().add("legend-bar");

        eventCountLabel = new Label("Total: 0 recorded events | Displayed: 0");
        eventCountLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        bar.getChildren().add(eventCountLabel);
        return bar;
    }

    private void updateStats() {
        eventCountLabel.setText(String.format(
            "Total captured: %,d events | Memory buffer: %,d latest (10,000 max rolling buffer) | Filtered/Sorted: %,d | Disk stream: Active (logs/)",
            totalRecordedCount, events.size(), sortedEvents.size()));
    }

    private void exportEvents() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Event Log");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv"),
                new javafx.stage.FileChooser.ExtensionFilter("Fichiers JSON (*.json)", "*.json"));
        java.io.File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                if (file.getName().endsWith(".json")) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.writerWithDefaultPrettyPrinter().writeValue(writer, sortedEvents);
                } else {
                    // CSV Export
                    writer.println("SequenceID,Timestamp,Tick,Type,Severity,Message,Data");
                    for (SimulationEvent ev : sortedEvents) {
                        String dataStr = ev.getData() != null ? ev.getData().toString().replace("\"", "'") : "";
                        writer.printf("%d,%s,%d,%s,%s,\"%s\",\"%s\"%n",
                                ev.getSequenceId(), ev.getTimestamp(), ev.getTick(), ev.getType(), ev.getSeverity(),
                                ev.getMessage() != null ? ev.getMessage().replace("\"", "'") : "", dataStr);
                    }
                }
                // Toast notification — no blocking modal
                org.swarmforge.client.util.NotificationOverlay.show(
                    this,
                    "✅ Log exported: " + file.getName() + "  (" + sortedEvents.size() + " événements)",
                    org.swarmforge.client.util.NotificationOverlay.NotificationType.SUCCESS
                );
            } catch (Exception ex) {
                org.swarmforge.client.util.NotificationOverlay.show(
                    this,
                    "❌ Export failed: " + ex.getMessage(),
                    org.swarmforge.client.util.NotificationOverlay.NotificationType.ERROR
                );
            }
        }
    }

    private void showEventDetails(SimulationEvent event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Event Details EVT-" + String.format("%06d", event.getSequenceId()));
        dialog.setHeaderText("EVT-" + String.format("%06d", event.getSequenceId()) + " | Tick #" + event.getTick() + " | Severity: " + event.getSeverity());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.setStyle("-fx-background-color: #1e1e24;");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setPrefWidth(520);

        Label msgLabel = new Label("Message: " + event.getMessage());
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 0, 0));

        int row = 0;
        grid.add(new Label("Sequence No:"), 0, row);
        grid.add(new Label(String.format("EVT-%06d", event.getSequenceId())), 1, row++);

        grid.add(new Label("Timestamp:"), 0, row);
        grid.add(new Label(event.getTimestamp() != null ? event.getTimestamp().toString() : "N/A"), 1, row++);

        grid.add(new Label("Event Type:"), 0, row);
        grid.add(new Label(event.getType() != null ? event.getType().name() : "N/A"), 1, row++);

        grid.add(new Label("Severity:"), 0, row);
        grid.add(new Label(event.getSeverity() != null ? event.getSeverity().name() : "INFO"), 1, row++);

        if (event.getData() != null && !event.getData().isEmpty()) {
            grid.add(new Label("Associated Data:"), 0, row++);
            for (java.util.Map.Entry<String, Object> entry : event.getData().entrySet()) {
                Label k = new Label("  • " + formatKey(entry.getKey()) + " (" + entry.getKey() + ") :");
                k.setStyle("-fx-text-fill: #a1a1aa;");
                Label v = new Label(entry.getValue() != null ? entry.getValue().toString() : "null");
                v.setStyle("-fx-text-fill: #38bdf8; -fx-font-family: monospace;");
                grid.add(k, 0, row);
                grid.add(v, 1, row++);
            }
        }

        grid.getChildren().forEach(n -> {
            if (n instanceof Label l && !l.getStyle().contains("-fx-text-fill")) {
                l.setStyle("-fx-text-fill: #e4e4e7;");
            }
        });

        content.getChildren().addAll(msgLabel, new Separator(), grid);
        pane.setContent(content);
        dialog.showAndWait();
    }

    private static String formatTypeString(SimulationEvent.EventType type) {
        if (type == null) return "Event";
        return switch (type) {
            case COLONY_FOUNDED -> "Colony Founded";
            case COLONY_DESTROYED -> "Colony Destroyed";
            case QUEEN_BORN -> "Queen Born";
            case QUEEN_DIED -> "Queen Died";
            case WORKER_BORN -> "Worker Born";
            case WORKER_DIED -> "Worker Died";
            case SOLDIER_BORN -> "Soldier Born";
            case SOLDIER_DIED -> "Soldier Died";
            case FOOD_DISCOVERED -> "Food Discovered";
            case FOOD_DEPLETED -> "Food Depleted";
            case NEST_EXPANDED -> "Nest Expanded";
            case NEST_DAMAGED -> "Nest Damaged";
            case RAID_STARTED -> "Raid Started";
            case RAID_ENDED -> "Raid Ended";
            case TERRITORY_CLAIMED -> "Territory Claimed";
            case COMBAT_OCCURRED -> "Interspecific Combat";
            case WEATHER_CHANGED -> "Weather Changed";
            case DISASTER_OCCURRED -> "Ecological Disaster";
            case SEASON_CHANGED -> "Season Changed";
            case SIMULATION_STARTED -> "Simulation Started";
            case SIMULATION_PAUSED -> "Simulation Paused";
            case SIMULATION_STOPPED -> "Simulation Stopped";
            case TICK_COMPLETED -> "Simulation Step";
            case MILESTONE_REACHED -> "Milestone Reached";
            case GOD_MODE_INTERVENTION -> "God Mode Intervention";
            default -> type.name().replace("_", " ");
        };
    }

    private static String formatKey(String key) {
        if (key == null) return "";
        return switch (key) {
            case "colonyId", "colony" -> "Colony";
            case "species" -> "Species";
            case "individualId" -> "Individual ID";
            case "caste" -> "Caste";
            case "stage" -> "Stage";
            case "job" -> "Job";
            case "cause" -> "Cause of Death";
            case "ageTicks" -> "Age (ticks)";
            case "attackerId" -> "Attacker ID";
            case "attackerCaste" -> "Attacker Caste";
            case "defenderId" -> "Defender ID";
            case "defenderCaste" -> "Defender Caste";
            case "damage" -> "Damage Dealt";
            case "healthRemaining" -> "Remaining HP";
            case "weatherState" -> "Weather Conditions";
            case "temperature" -> "Temperature (°C)";
            case "humidity" -> "Humidity (%)";
            case "windSpeed" -> "Wind Speed (km/h)";
            case "windDirection" -> "Wind Direction";
            case "rainfall" -> "Rainfall (mm/h)";
            case "snowfall" -> "Snowfall (mm/h)";
            case "pressure" -> "Barometric Pressure (hPa)";
            case "flightSuitability" -> "Flight Suitability";
            case "resource" -> "Resource";
            case "disasterType" -> "Disaster Type";
            case "affectedArea" -> "Affected Area";
            case "amount" -> "Amount";
            case "x" -> "Position X";
            case "y" -> "Position Y";
            case "z" -> "Position Z (Depth)";
            default -> Character.toUpperCase(key.charAt(0)) + key.substring(1);
        };
    }

    private static String formatValue(Object value) {
        if (value == null) return "";
        String str = value.toString();
        if (str.length() == 36 && str.contains("-")) {
            return "#" + str.substring(0, 8).toUpperCase();
        }
        return str;
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
        Platform.runLater(() -> {
            events.clear();
            lastAddedEvent = null;
            totalRecordedCount = 0;
            EventBus.getInstance().clearHistory();
            updateStats();
        });
    }
}
