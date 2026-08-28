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

        // Setup filtered list
        filteredEvents = new FilteredList<>(events, e -> true);

        // Top: Title and Filters
        setTop(createToolbar());

        // Center: Sortable TableView for Events
        eventTable = createEventTable();
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

            if (autoScrollCheck.isSelected() && !filteredEvents.isEmpty()) {
                eventTable.scrollTo(filteredEvents.size() - 1);
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

    private TableView<SimulationEvent> createEventTable() {
        TableView<SimulationEvent> table = new TableView<>(filteredEvents);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: #121215; -fx-control-inner-background: #121215;");

        // Column 1: Severity Badge
        TableColumn<SimulationEvent, SimulationEvent.Severity> colSeverity = new TableColumn<>("Sévérité");
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
                            badge.setText("CRITIQUE");
                            badge.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 2 6; -fx-background-radius: 4;");
                        }
                        case WARNING -> {
                            badge.setText("ATTENTION");
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
        TableColumn<SimulationEvent, Long> colId = new TableColumn<>("ID Événement");
        colId.setMinWidth(110);
        colId.setMaxWidth(130);
        colId.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getSequenceId()));
        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long seqId, boolean empty) {
                super.updateItem(seqId, empty);
                if (empty || seqId == null) {
                    setText(null);
                } else {
                    setText(String.format("EVT-%06d", seqId));
                    setStyle("-fx-text-fill: #a78bfa; -fx-font-family: monospace; -fx-font-weight: bold;");
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Column 3: Timestamp
        TableColumn<SimulationEvent, Instant> colTime = new TableColumn<>("Horodatage");
        colTime.setMinWidth(100);
        colTime.setMaxWidth(120);
        colTime.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTimestamp()));
        colTime.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Instant time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    try {
                        setText(time.atZone(ZoneId.systemDefault()).format(TIME_FMT));
                    } catch (Exception e) {
                        setText("--:--:--");
                    }
                    setStyle("-fx-text-fill: #38bdf8; -fx-font-family: monospace;");
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Column 4: Tick
        TableColumn<SimulationEvent, Long> colTick = new TableColumn<>("Pas (Tick)");
        colTick.setMinWidth(90);
        colTick.setMaxWidth(110);
        colTick.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(cellData.getValue().getTick()));
        colTick.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Long tick, boolean empty) {
                super.updateItem(tick, empty);
                if (empty || tick == null) {
                    setText(null);
                } else {
                    setText("T#" + tick);
                    setStyle("-fx-text-fill: #94a3b8; -fx-font-family: monospace;");
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Column 5: Event Type
        TableColumn<SimulationEvent, String> colType = new TableColumn<>("Type d'Événement");
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
                    setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: bold;");
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Column 6: Message & Details (Sortable by message text)
        TableColumn<SimulationEvent, String> colMessage = new TableColumn<>("Détails & Message");
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
                    setStyle("-fx-text-fill: #f4f4f5;");
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
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        HBox filters = new HBox(10);
        filters.setAlignment(Pos.CENTER_LEFT);

        // Search Field
        searchField = new TextField();
        searchField.setPromptText("Rechercher dans le journal...");
        searchField.setPrefWidth(220);
        searchField.textProperty().addListener((obs, oldV, newV) -> updateFilter());

        // Type filter
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
        severityFilter.getItems().addAll("Toutes sévérités", "Critique uniquement", "Info uniquement", "Attention uniquement");
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
        bar.setStyle("-fx-background-color: #18181b; -fx-border-color: #27272a; -fx-border-width: 1 0 0 0;");

        eventCountLabel = new Label("Total : 0 événements enregistrés | Affichés : 0");
        eventCountLabel.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 12px; -fx-font-weight: bold;");

        bar.getChildren().add(eventCountLabel);
        return bar;
    }

    private void updateStats() {
        eventCountLabel.setText(String.format(
            "Total capturé : %,d événements | Fenêtre mémoire : %,d derniers (Tampon rotatif de 10 000 max) | Affichés après filtre : %,d | Stream disque : Actif (logs/)",
            totalRecordedCount, events.size(), filteredEvents.size()));
    }

    private void exportEvents() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Exporter le journal des événements");
        fileChooser.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv"),
                new javafx.stage.FileChooser.ExtensionFilter("Fichiers JSON (*.json)", "*.json"));
        java.io.File file = fileChooser.showSaveDialog(getScene().getWindow());

        if (file != null) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {
                if (file.getName().endsWith(".json")) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.writerWithDefaultPrettyPrinter().writeValue(writer, events);
                } else {
                    // CSV Export
                    writer.println("SequenceID,Timestamp,Tick,Type,Severity,Message,Data");
                    for (SimulationEvent ev : events) {
                        String dataStr = ev.getData() != null ? ev.getData().toString().replace("\"", "'") : "";
                        writer.printf("%d,%s,%d,%s,%s,\"%s\",\"%s\"%n",
                                ev.getSequenceId(), ev.getTimestamp(), ev.getTick(), ev.getType(), ev.getSeverity(),
                                ev.getMessage() != null ? ev.getMessage().replace("\"", "'") : "", dataStr);
                    }
                }
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Exportation réussie dans " + file.getName()).show();
            } catch (Exception ex) {
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.ERROR, "Échec de l'exportation : " + ex.getMessage()).show();
            }
        }
    }

    private void showEventDetails(SimulationEvent event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'événement EVT-" + String.format("%06d", event.getSequenceId()));
        dialog.setHeaderText("EVT-" + String.format("%06d", event.getSequenceId()) + " | Pas #" + event.getTick() + " | Sévérité: " + event.getSeverity());

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
        grid.add(new Label("N° Séquence :"), 0, row);
        grid.add(new Label(String.format("EVT-%06d", event.getSequenceId())), 1, row++);

        grid.add(new Label("Horodatage :"), 0, row);
        grid.add(new Label(event.getTimestamp() != null ? event.getTimestamp().toString() : "N/A"), 1, row++);

        grid.add(new Label("Type d'Événement :"), 0, row);
        grid.add(new Label(event.getType() != null ? event.getType().name() : "N/A"), 1, row++);

        grid.add(new Label("Sévérité :"), 0, row);
        grid.add(new Label(event.getSeverity() != null ? event.getSeverity().name() : "INFO"), 1, row++);

        if (event.getData() != null && !event.getData().isEmpty()) {
            grid.add(new Label("Données associées :"), 0, row++);
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
        if (type == null) return "Événement";
        return switch (type) {
            case COLONY_FOUNDED -> "Fondation Colonie";
            case COLONY_DESTROYED -> "Destruction Colonie";
            case QUEEN_BORN -> "Naissance Reine";
            case QUEEN_DIED -> "Décès Reine";
            case WORKER_BORN -> "Naissance Ouvrière";
            case WORKER_DIED -> "Décès Ouvrière";
            case SOLDIER_BORN -> "Naissance Soldat";
            case SOLDIER_DIED -> "Décès Soldat";
            case FOOD_DISCOVERED -> "Nourriture Découverte";
            case FOOD_DEPLETED -> "Nourriture Épuisée";
            case NEST_EXPANDED -> "Extension du Nid";
            case NEST_DAMAGED -> "Nid Endommagé";
            case RAID_STARTED -> "Raid Commencé";
            case RAID_ENDED -> "Raid Terminé";
            case TERRITORY_CLAIMED -> "Territoire Conquis";
            case COMBAT_OCCURRED -> "Combat Interspécifique";
            case WEATHER_CHANGED -> "Changement Climat";
            case DISASTER_OCCURRED -> "Désastre Écologique";
            case SEASON_CHANGED -> "Changement Saison";
            case SIMULATION_STARTED -> "Départ Simulation";
            case SIMULATION_PAUSED -> "Pause Simulation";
            case SIMULATION_STOPPED -> "Arrêt Simulation";
            case TICK_COMPLETED -> "Pas Simulation";
            case MILESTONE_REACHED -> "Jalon Franchi";
            case GOD_MODE_INTERVENTION -> "Mode Divin";
            default -> type.name().replace("_", " ");
        };
    }

    private static String formatKey(String key) {
        if (key == null) return "";
        return switch (key) {
            case "colonyId", "colony" -> "Colonie";
            case "species" -> "Espèce";
            case "individualId" -> "ID Individu";
            case "caste" -> "Caste";
            case "stage" -> "Stade";
            case "job" -> "Métier";
            case "cause" -> "Cause de Décès";
            case "ageTicks" -> "Âge (pas)";
            case "attackerId" -> "ID Attaquant";
            case "attackerCaste" -> "Caste Attaquant";
            case "defenderId" -> "ID Défenseur";
            case "defenderCaste" -> "Caste Défenseur";
            case "damage" -> "Dégâts Infrigés";
            case "healthRemaining" -> "PV Restants";
            case "weatherState" -> "Conditions Météo";
            case "temperature" -> "Température (°C)";
            case "humidity" -> "Humidité (%)";
            case "windSpeed" -> "Vitesse Vent (km/h)";
            case "windDirection" -> "Direction Vent";
            case "rainfall" -> "Pluie (mm/h)";
            case "snowfall" -> "Neige (mm/h)";
            case "pressure" -> "Pression (hPa)";
            case "flightSuitability" -> "Aptitude Vol";
            case "resource" -> "Ressource";
            case "disasterType" -> "Type Désastre";
            case "affectedArea" -> "Zone Touchée";
            case "amount" -> "Quantité";
            case "x" -> "Position X";
            case "y" -> "Position Y";
            case "z" -> "Position Z (Profondeur)";
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
}
