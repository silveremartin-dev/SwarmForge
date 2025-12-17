/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.swarmforge.core.event.EventBus;
import org.swarmforge.core.event.SimulationEvent;

import java.time.format.DateTimeFormatter;
import java.util.function.Predicate;

/**
 * Event Log Panel - Display and filter simulation events.
 * Shows real-time event stream with filtering and severity indicators.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EventLogPane extends BorderPane {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final ObservableList<SimulationEvent> events = FXCollections.observableArrayList();
    private final FilteredList<SimulationEvent> filteredEvents;
    private final ListView<SimulationEvent> eventListView;

    private ComboBox<String> typeFilter;
    private ComboBox<String> severityFilter;
    private CheckBox autoScrollCheck;
    private Label eventCountLabel;

    public EventLogPane() {
        setPadding(new Insets(10));
        setStyle("-fx-background-color: #1a1a2e;");

        // Setup filtered list
        filteredEvents = new FilteredList<>(events, e -> true);

        // Top: Title and Filters
        setTop(createToolbar());

        // Center: Event List
        eventListView = new ListView<>(filteredEvents);
        eventListView.setCellFactory(list -> new EventCell());
        eventListView.setStyle("-fx-background-color: #0a0a1a;");
        setCenter(eventListView);

        // Bottom: Stats
        setBottom(createStatsBar());

        // Subscribe to EventBus
        EventBus.getInstance().subscribeAll(event -> Platform.runLater(() -> {
            events.add(event);
            if (autoScrollCheck.isSelected()) {
                eventListView.scrollTo(events.size() - 1);
            }
            updateStats();
        }));
    }

    private VBox createToolbar() {
        VBox toolbar = new VBox(10);
        toolbar.setPadding(new Insets(0, 0, 10, 0));

        Label title = new Label("📋 Event Log");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00d4ff;");

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        // Type filter
        Label typeLabel = new Label("Type:");
        typeLabel.setStyle("-fx-text-fill: white;");
        typeFilter = new ComboBox<>();
        typeFilter.getItems().add("All");
        for (SimulationEvent.EventType type : SimulationEvent.EventType.values()) {
            typeFilter.getItems().add(type.name());
        }
        typeFilter.getSelectionModel().selectFirst();
        typeFilter.setOnAction(e -> updateFilter());

        // Severity filter
        Label sevLabel = new Label("Severity:");
        sevLabel.setStyle("-fx-text-fill: white;");
        severityFilter = new ComboBox<>();
        severityFilter.getItems().addAll("All", "INFO", "WARNING", "CRITICAL");
        severityFilter.getSelectionModel().selectFirst();
        severityFilter.setOnAction(e -> updateFilter());

        // Auto-scroll
        autoScrollCheck = new CheckBox("Auto-scroll");
        autoScrollCheck.setSelected(true);
        autoScrollCheck.setStyle("-fx-text-fill: white;");

        // Clear button
        Button btnClear = new Button("🗑 Clear");
        btnClear.setOnAction(e -> {
            events.clear();
            EventBus.getInstance().clearHistory();
            updateStats();
        });

        // Export button
        Button btnExport = new Button("📤 Export");
        btnExport.setOnAction(e -> exportEvents());

        filters.getChildren().addAll(typeLabel, typeFilter, sevLabel, severityFilter, autoScrollCheck,
                new Region() {
                    {
                        HBox.setHgrow(this, Priority.ALWAYS);
                    }
                },
                btnClear, btnExport);

        toolbar.getChildren().addAll(title, filters);
        return toolbar;
    }

    private HBox createStatsBar() {
        HBox bar = new HBox(20);
        bar.setPadding(new Insets(10, 0, 0, 0));
        bar.setAlignment(Pos.CENTER_LEFT);

        eventCountLabel = new Label("Events: 0");
        eventCountLabel.setStyle("-fx-text-fill: #888;");

        bar.getChildren().add(eventCountLabel);
        return bar;
    }

    private void updateFilter() {
        String typeValue = typeFilter.getValue();
        String sevValue = severityFilter.getValue();

        Predicate<SimulationEvent> typePred = e -> "All".equals(typeValue) || e.getType().name().equals(typeValue);
        Predicate<SimulationEvent> sevPred = e -> "All".equals(sevValue) || e.getSeverity().name().equals(sevValue);

        filteredEvents.setPredicate(typePred.and(sevPred));
        updateStats();
    }

    private void updateStats() {
        eventCountLabel.setText("Events: " + events.size() + " (showing " + filteredEvents.size() + ")");
    }

    private void exportEvents() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Event Log");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new javafx.stage.FileChooser.ExtensionFilter("JSON Files", "*.json"));
        java.io.File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                if (file.getName().endsWith(".json")) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.writerWithDefaultPrettyPrinter().writeValue(writer, events);
                } else {
                    // CSV
                    writer.println("Timestamp,Tick,Type,Severity,Message");
                    for (SimulationEvent ev : events) {
                        writer.printf("%s,%d,%s,%s,\"%s\"%n",
                                ev.getTimestamp(), ev.getTick(), ev.getType(), ev.getSeverity(), ev.getMessage());
                    }
                }
                new Alert(Alert.AlertType.INFORMATION, "Export successful!").show();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
            }
        }
    }

    /**
     * Custom cell for event display with color coding.
     */
    private static class EventCell extends ListCell<SimulationEvent> {
        @Override
        protected void updateItem(SimulationEvent event, boolean empty) {
            super.updateItem(event, empty);
            if (empty || event == null) {
                setText(null);
                setGraphic(null);
                setStyle("-fx-background-color: transparent;");
            } else {
                HBox box = new HBox(10);
                box.setAlignment(Pos.CENTER_LEFT);

                // Severity indicator
                Label sevLabel = new Label(getSeverityIcon(event.getSeverity()));
                sevLabel.setMinWidth(25);

                // Time
                String time = event.getTimestamp().atZone(java.time.ZoneId.systemDefault())
                        .format(TIME_FMT);
                Label timeLabel = new Label(time);
                timeLabel.setStyle("-fx-text-fill: #888; -fx-font-family: monospace;");
                timeLabel.setMinWidth(60);

                // Tick
                Label tickLabel = new Label("[" + event.getTick() + "]");
                tickLabel.setStyle("-fx-text-fill: #666; -fx-font-family: monospace;");
                tickLabel.setMinWidth(70);

                // Type
                Label typeLabel = new Label(formatType(event.getType()));
                typeLabel.setStyle("-fx-text-fill: " + getTypeColor(event.getType()) + "; -fx-font-weight: bold;");
                typeLabel.setMinWidth(120);

                // Message
                Label msgLabel = new Label(event.getMessage());
                msgLabel.setStyle("-fx-text-fill: white;");

                box.getChildren().addAll(sevLabel, timeLabel, tickLabel, typeLabel, msgLabel);
                setGraphic(box);

                // Background based on severity
                setStyle("-fx-background-color: " + getBackgroundColor(event.getSeverity()) + ";");
            }
        }

        private String getSeverityIcon(SimulationEvent.Severity sev) {
            return switch (sev) {
                case INFO -> "ℹ️";
                case WARNING -> "⚠️";
                case CRITICAL -> "🔴";
            };
        }

        private String getBackgroundColor(SimulationEvent.Severity sev) {
            return switch (sev) {
                case INFO -> "transparent";
                case WARNING -> "rgba(255, 193, 7, 0.1)";
                case CRITICAL -> "rgba(220, 53, 69, 0.2)";
            };
        }

        private String formatType(SimulationEvent.EventType type) {
            return type.name().replace("_", " ");
        }

        private String getTypeColor(SimulationEvent.EventType type) {
            return switch (type) {
                case COLONY_FOUNDED, QUEEN_BORN, WORKER_BORN, SOLDIER_BORN -> "#28a745";
                case COLONY_DESTROYED, QUEEN_DIED, WORKER_DIED, SOLDIER_DIED -> "#dc3545";
                case RAID_STARTED, COMBAT_OCCURRED -> "#ff6b6b";
                case FOOD_DISCOVERED, NEST_EXPANDED -> "#17a2b8";
                case DISASTER_OCCURRED -> "#ffc107";
                case WEATHER_CHANGED, SEASON_CHANGED -> "#6c757d";
                default -> "#00d4ff";
            };
        }
    }

    /**
     * Add an event manually (for non-EventBus events).
     */
    public void addEvent(SimulationEvent event) {
        Platform.runLater(() -> {
            events.add(event);
            if (autoScrollCheck.isSelected()) {
                eventListView.scrollTo(events.size() - 1);
            }
            updateStats();
        });
    }
}
