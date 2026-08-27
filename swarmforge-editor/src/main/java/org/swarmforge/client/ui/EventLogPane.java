/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
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
    private TextField searchField;
    private CheckBox autoScrollCheck;
    private Label eventCountLabel;

    public EventLogPane() {
        setPadding(new Insets(10));

        // Setup filtered list
        filteredEvents = new FilteredList<>(events, e -> true);

        // Top: Title and Filters
        setTop(createToolbar());

        // Center: Event List with Double Click Details
        eventListView = new ListView<>(filteredEvents);
        eventListView.setCellFactory(list -> new EventCell());
        eventListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SimulationEvent selected = eventListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showEventDetails(selected);
                }
            }
        });

        setCenter(eventListView);

        // Bottom: Stats
        setBottom(createStatsBar());

        // Subscribe to EventBus
        EventBus.getInstance().subscribeAll(event -> Platform.runLater(() -> {
            events.add(event);
            if (autoScrollCheck.isSelected() && !filteredEvents.isEmpty()) {
                eventListView.scrollTo(filteredEvents.size() - 1);
            }
            updateStats();
        }));
    }

    private VBox createToolbar() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        VBox toolbar = new VBox(10);
        toolbar.setPadding(new Insets(0, 0, 10, 0));

        Label title = new Label();
        title.textProperty().bind(i18n.createStringBinding("log.title"));
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        // Search Field
        searchField = new TextField();
        searchField.setPromptText("Rechercher dans les événements...");
        searchField.setPrefWidth(200);
        searchField.textProperty().addListener((obs, oldV, newV) -> updateFilter());

        // Type filter (Excludes redundant generic types: INFO, DEBUG, WARNING, ERROR, SYSTEM, BIRTH, DEATH)
        Label typeLabel = new Label();
        typeLabel.textProperty().bind(i18n.createStringBinding("log.filter.type"));
        typeLabel.setStyle("-fx-text-fill: white;");
        typeFilter = new ComboBox<>();
        typeFilter.getItems().add("Tous");
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
        sevLabel.setStyle("-fx-text-fill: white;");
        severityFilter = new ComboBox<>();
        severityFilter.getItems().addAll("All Severities", "Critical Only", "Info Only", "Warning Only");
        severityFilter.getSelectionModel().selectFirst();
        severityFilter.setOnAction(e -> updateFilter());

        // Auto-scroll
        autoScrollCheck = new CheckBox();
        autoScrollCheck.textProperty().bind(i18n.createStringBinding("log.filter.auto_scroll"));
        autoScrollCheck.setSelected(true);
        autoScrollCheck.setStyle("-fx-text-fill: white;");

        // Clear button
        Button btnClear = new Button();
        btnClear.textProperty().bind(i18n.createStringBinding("log.btn.clear"));
        btnClear.setOnAction(e -> {
            events.clear();
            EventBus.getInstance().clearHistory();
            updateStats();
        });

        // Export button
        Button btnExport = new Button();
        btnExport.textProperty().bind(i18n.createStringBinding("log.btn.export"));
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
            if ("All Severities".equals(sevValue) || "Tous".equals(sevValue) || "All".equals(sevValue) || sevValue == null) return true;
            if ("Critical Only".equals(sevValue) || "CRITICAL".equals(sevValue)) return e.getSeverity() == SimulationEvent.Severity.CRITICAL;
            if ("Info Only".equals(sevValue) || "INFO".equals(sevValue)) return e.getSeverity() == SimulationEvent.Severity.INFO;
            if ("Warning Only".equals(sevValue) || "WARNING".equals(sevValue)) return e.getSeverity() == SimulationEvent.Severity.WARNING;
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

    private void updateStats() {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        eventCountLabel.setText(i18n.get("log.stats.events", events.size(), filteredEvents.size()));
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
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Export successful!").show();
            } catch (Exception ex) {
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Export failed: " + ex.getMessage()).show();
            }
        }
    }

    private void showEventDetails(SimulationEvent event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'événement - " + formatTypeString(event.getType()));
        dialog.setHeaderText("Pas #" + event.getTick() + " | Sévérité: " + event.getSeverity());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.setStyle("-fx-background-color: #1e1e24;");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setPrefWidth(500);

        Label msgLabel = new Label("Message: " + event.getMessage());
        msgLabel.setWrapText(true);
        msgLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 0, 0));

        int row = 0;
        grid.add(new Label("Horodatage:"), 0, row);
        grid.add(new Label(event.getTimestamp() != null ? event.getTimestamp().toString() : "N/A"), 1, row++);

        grid.add(new Label("Type d'Événement:"), 0, row);
        grid.add(new Label(event.getType() != null ? event.getType().name() : "N/A"), 1, row++);

        grid.add(new Label("Sévérité:"), 0, row);
        grid.add(new Label(event.getSeverity() != null ? event.getSeverity().name() : "INFO"), 1, row++);

        if (event.getData() != null && !event.getData().isEmpty()) {
            grid.add(new Label("Données associées:"), 0, row++);
            for (java.util.Map.Entry<String, Object> entry : event.getData().entrySet()) {
                Label k = new Label("  • " + entry.getKey() + ":");
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
        if (type == null) return "ℹ️ Événement";
        return switch (type) {
            case COLONY_FOUNDED -> "🏰 Fondation Colonie";
            case COLONY_DESTROYED -> "💥 Destruction Colonie";
            case QUEEN_BORN -> "👑 Naissance Reine";
            case QUEEN_DIED -> "💀 Décès Reine";
            case WORKER_BORN -> "🐜 Naissance Ouvrière";
            case WORKER_DIED -> "🪦 Décès Ouvrière";
            case SOLDIER_BORN -> "🛡️ Naissance Soldat";
            case SOLDIER_DIED -> "⚔️ Décès Soldat";
            case FOOD_DISCOVERED -> "🍃 Nourriture Découverte";
            case FOOD_DEPLETED -> "🥀 Nourriture Épuisée";
            case NEST_EXPANDED -> "🏗️ Extension du Nid";
            case NEST_DAMAGED -> "🏚️ Nid Endommagé";
            case RAID_STARTED -> "🚨 Raid Commencé";
            case RAID_ENDED -> "🏳️ Raid Terminé";
            case TERRITORY_CLAIMED -> "🚩 Territoire Conquis";
            case COMBAT_OCCURRED -> "⚔️ Combat Interspécifique";
            case WEATHER_CHANGED -> "🌧️ Changement Climat";
            case DISASTER_OCCURRED -> "🌋 Désastre Écologique";
            case SEASON_CHANGED -> "🍂 Changement Saison";
            case SIMULATION_STARTED -> "▶️ Départ Simulation";
            case SIMULATION_PAUSED -> "⏸️ Pause Simulation";
            case SIMULATION_STOPPED -> "⏹️ Arrêt Simulation";
            case TICK_COMPLETED -> "⏱️ Pas Simulation";
            case MILESTONE_REACHED -> "🏆 Jalon Franchi";
            case GOD_MODE_INTERVENTION -> "⚡ Mode Divin";
            default -> "ℹ️ " + type.name().replace("_", " ");
        };
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
                String time = "--:--:--";
                if (event.getTimestamp() != null) {
                    try {
                        time = event.getTimestamp().atZone(java.time.ZoneId.systemDefault()).format(TIME_FMT);
                    } catch (Exception ignored) {}
                }
                Label timeLabel = new Label(time);
                timeLabel.setStyle("-fx-text-fill: #888; -fx-font-family: monospace;");
                timeLabel.setMinWidth(60);

                // Tick
                Label tickLabel = new Label("[" + event.getTick() + "]");
                tickLabel.setStyle("-fx-text-fill: #666; -fx-font-family: monospace;");
                tickLabel.setMinWidth(70);

                // Type
                Label typeLabel = new Label(formatTypeString(event.getType()));
                typeLabel.setStyle("-fx-text-fill: " + getTypeColor(event.getType()) + "; -fx-font-weight: bold;");
                typeLabel.setMinWidth(140);

                // Message & Numerical Data Details with Human Readable Labels & Formatted IDs
                String rawMsg = event.getMessage() != null ? event.getMessage() : "";
                StringBuilder msgBuilder = new StringBuilder(rawMsg);
                if (event.getData() != null && !event.getData().isEmpty()) {
                    msgBuilder.append("  📊 [");
                    boolean first = true;
                    for (java.util.Map.Entry<String, Object> entry : event.getData().entrySet()) {
                        if (entry == null || entry.getKey() == null) continue;
                        if (!first) msgBuilder.append(", ");
                        msgBuilder.append(formatKey(entry.getKey())).append(": ").append(formatValue(entry.getValue()));
                        first = false;
                    }
                    msgBuilder.append("]");
                }
                Label msgLabel = new Label(msgBuilder.toString());
                msgLabel.setStyle("-fx-text-fill: white;");

                box.getChildren().addAll(sevLabel, timeLabel, tickLabel, typeLabel, msgLabel);
                setGraphic(box);

                // Background based on severity
                setStyle("-fx-background-color: " + getBackgroundColor(event.getSeverity()) + ";");
            }
        }

        private String formatKey(String key) {
            if (key == null) return "";
            return switch (key) {
                case "colonyId", "colony" -> "Colonie";
                case "species" -> "Espèce";
                case "caste" -> "Caste";
                case "stage" -> "Stade";
                case "cause" -> "Cause";
                case "attackerId" -> "Attaquant";
                case "defenderId" -> "Défenseur";
                case "disasterType" -> "Type";
                case "affectedArea" -> "Zone touchée";
                case "amount" -> "Quantité";
                default -> Character.toUpperCase(key.charAt(0)) + key.substring(1);
            };
        }

        private String formatValue(Object value) {
            if (value == null) return "";
            String str = value.toString();
            if (str.length() == 36 && str.contains("-")) {
                return "Nid #" + str.substring(0, 8).toUpperCase();
            }
            return str;
        }

        private String getSeverityIcon(SimulationEvent.Severity sev) {
            if (sev == null) return "ℹ️";
            if (sev == SimulationEvent.Severity.WARNING) return "⚠️";
            if (sev == SimulationEvent.Severity.CRITICAL) return "🔴";
            return "ℹ️";
        }

        private String getBackgroundColor(SimulationEvent.Severity sev) {
            if (sev == null) return "transparent";
            if (sev == SimulationEvent.Severity.WARNING) return "rgba(255, 193, 7, 0.1)";
            if (sev == SimulationEvent.Severity.CRITICAL) return "rgba(220, 53, 69, 0.2)";
            return "transparent";
        }

        private String getTypeColor(SimulationEvent.EventType type) {
            if (type == null) return "#e4e4e7";
            return switch (type.name()) {
                case "COLONY_FOUNDED", "QUEEN_BORN", "WORKER_BORN", "SOLDIER_BORN" -> "#28a745";
                case "COLONY_DESTROYED", "QUEEN_DIED", "WORKER_DIED", "SOLDIER_DIED" -> "#dc3545";
                case "RAID_STARTED", "COMBAT_OCCURRED" -> "#ff6b6b";
                case "FOOD_DISCOVERED", "NEST_EXPANDED" -> "#a1a1aa";
                case "DISASTER_OCCURRED" -> "#ffc107";
                case "WEATHER_CHANGED", "SEASON_CHANGED" -> "#6c757d";
                default -> "#e4e4e7";
            };
        }
    }

    /**
     * Add an event manually (for non-EventBus events).
     */
    public void addEvent(SimulationEvent event) {
        Platform.runLater(() -> {
            events.add(event);
            if (autoScrollCheck.isSelected() && !filteredEvents.isEmpty()) {
                eventListView.scrollTo(filteredEvents.size() - 1);
            }
            updateStats();
        });
    }
}
