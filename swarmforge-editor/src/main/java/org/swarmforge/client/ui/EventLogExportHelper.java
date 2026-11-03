/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.Node;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.core.event.SimulationEvent;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Helper class for exporting simulation event log streams to CSV or JSON formats.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class EventLogExportHelper {

    private EventLogExportHelper() {
        // Utility class
    }

    /**
     * Prompts the user with a file save dialog and writes the event list to the chosen format (CSV or JSON).
     *
     * @param window the parent window for the file chooser dialog
     * @param parentNode the node for displaying notification overlays
     * @param scenarioName the current simulation scenario name
     * @param events the ordered list of simulation events to export
     */
    public static void exportEvents(Window window, Node parentNode, String scenarioName, List<SimulationEvent> events) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Event Log");
        String sanitizedScenario = (scenarioName != null && !scenarioName.isBlank())
                ? scenarioName.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "_").replaceAll("_+", "_")
                : "swarmforge";
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now());
        fileChooser.setInitialFileName(sanitizedScenario + "_event_log_" + timestamp + ".csv");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers CSV (*.csv)", "*.csv"),
                new FileChooser.ExtensionFilter("Fichiers JSON (*.json)", "*.json"));
        File file = fileChooser.showSaveDialog(window);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8)) {
                if (file.getName().endsWith(".json")) {
                    ObjectMapper mapper = new ObjectMapper();
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
                NotificationOverlay.show(
                    parentNode,
                    "✅ Log exported: " + file.getName() + "  (" + events.size() + " événements)",
                    NotificationOverlay.NotificationType.SUCCESS
                );
            } catch (Exception ex) {
                NotificationOverlay.show(
                    parentNode,
                    "❌ Export failed: " + ex.getMessage(),
                    NotificationOverlay.NotificationType.ERROR
                );
            }
        }
    }

    public static void copyLogPathToClipboard(Node parentNode, String scenarioName) {
        String sanitizedScenario = (scenarioName != null && !scenarioName.isBlank())
                ? scenarioName.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "_").replaceAll("_+", "_")
                : "swarmforge";
        java.io.File logDir = new java.io.File(System.getProperty("user.dir"), "logs");
        java.io.File logFile = new java.io.File(logDir, sanitizedScenario + "_simulation.log");
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(logFile.getAbsolutePath());
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
        NotificationOverlay.show(
                parentNode,
                "📋 Log path copied to clipboard:\n" + logFile.getAbsolutePath(),
                NotificationOverlay.NotificationType.SUCCESS
        );
    }

    public static java.util.function.Predicate<SimulationEvent> buildFilterPredicate(String typeValue, String sevValue, String query) {
        java.util.function.Predicate<SimulationEvent> typePred = e -> {
            if ("Tous".equals(typeValue) || "All".equals(typeValue) || typeValue == null) return true;
            return EventLogDetailDialog.formatTypeString(e.getType()).equals(typeValue) || e.getType().name().equals(typeValue);
        };
        java.util.function.Predicate<SimulationEvent> sevPred = e -> {
            if ("Toutes sévérités".equals(sevValue) || "All Severities".equals(sevValue) || "Tous".equals(sevValue) || sevValue == null) return true;
            if ("Critique uniquement".equals(sevValue) || "Critical Only".equals(sevValue) || "CRITICAL".equals(sevValue)) return e.getSeverity() == SimulationEvent.Severity.CRITICAL;
            if ("Info uniquement".equals(sevValue) || "Info Only".equals(sevValue) || "INFO".equals(sevValue)) return e.getSeverity() == SimulationEvent.Severity.INFO;
            if ("Attention uniquement".equals(sevValue) || "Warning Only".equals(sevValue) || "WARNING".equals(sevValue)) return e.getSeverity() == SimulationEvent.Severity.WARNING;
            return true;
        };
        java.util.function.Predicate<SimulationEvent> searchPred = e -> {
            if (query.isEmpty()) return true;
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains(query)) return true;
            if (e.getType() != null && e.getType().name().toLowerCase().contains(query)) return true;
            if (e.getData() != null && e.getData().toString().toLowerCase().contains(query)) return true;
            return false;
        };
        return typePred.and(sevPred).and(searchPred);
    }

    public static javafx.scene.layout.VBox createToolbar(
            EventLogPane pane,
            Runnable onUpdateFilter,
            Runnable onClearLog,
            Runnable onExport) {
        org.swarmforge.client.util.I18nManager i18n = org.swarmforge.client.util.I18nManager.getInstance();
        javafx.scene.layout.VBox toolbar = new javafx.scene.layout.VBox(10);
        toolbar.setPadding(new javafx.geometry.Insets(0, 0, 10, 0));

        javafx.scene.control.Label title = new javafx.scene.control.Label();
        title.textProperty().bind(i18n.createStringBinding("log.title"));
        title.getStyleClass().add("card-title");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        javafx.scene.layout.HBox filters = new javafx.scene.layout.HBox(10);
        filters.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        pane.searchField = new javafx.scene.control.TextField();
        pane.searchField.promptTextProperty().bind(i18n.createStringBinding("log.search.prompt"));
        pane.searchField.setPrefWidth(220);
        pane.searchField.setTooltip(new javafx.scene.control.Tooltip("Filter events by keyword (message, ID, data)..."));
        pane.searchField.textProperty().addListener((obs, oldV, newV) -> onUpdateFilter.run());

        javafx.scene.control.Label typeLabel = new javafx.scene.control.Label();
        typeLabel.textProperty().bind(i18n.createStringBinding("log.filter.type"));
        pane.typeFilter = new javafx.scene.control.ComboBox<>();
        pane.typeFilter.setTooltip(new javafx.scene.control.Tooltip("Filter list by specific event type (Founding, Birth, Combat, etc.)."));
        pane.typeFilter.getItems().add(i18n.get("log.type.all"));
        for (SimulationEvent.EventType type : SimulationEvent.EventType.values()) {
            if (type == SimulationEvent.EventType.INFO || type == SimulationEvent.EventType.BIRTH ||
                type == SimulationEvent.EventType.DEATH || type == SimulationEvent.EventType.SYSTEM ||
                type == SimulationEvent.EventType.DEBUG || type == SimulationEvent.EventType.ERROR ||
                type == SimulationEvent.EventType.WARNING) {
                continue;
            }
            pane.typeFilter.getItems().add(EventLogDetailDialog.formatTypeString(type));
        }
        pane.typeFilter.getSelectionModel().selectFirst();
        pane.typeFilter.setOnAction(e -> onUpdateFilter.run());

        javafx.scene.control.Label sevLabel = new javafx.scene.control.Label();
        sevLabel.textProperty().bind(i18n.createStringBinding("log.filter.severity"));
        pane.severityFilter = new javafx.scene.control.ComboBox<>();
        pane.severityFilter.setTooltip(new javafx.scene.control.Tooltip("Filter list by severity level (Info, Warning, Critical)."));
        pane.severityFilter.getItems().addAll(
            i18n.get("log.severity.all"),
            i18n.get("log.severity.critical"),
            i18n.get("log.severity.info"),
            i18n.get("log.severity.warning")
        );
        pane.severityFilter.getSelectionModel().selectFirst();
        pane.severityFilter.setOnAction(e -> onUpdateFilter.run());

        pane.autoScrollCheck = new javafx.scene.control.CheckBox();
        pane.autoScrollCheck.textProperty().bind(i18n.createStringBinding("log.filter.auto_scroll"));
        pane.autoScrollCheck.setSelected(true);
        pane.autoScrollCheck.setTooltip(new javafx.scene.control.Tooltip("Automatically scroll table when receiving new events."));

        javafx.scene.control.Button btnClear = new javafx.scene.control.Button();
        btnClear.textProperty().bind(i18n.createStringBinding("log.btn.clear"));
        btnClear.setTooltip(new javafx.scene.control.Tooltip("Clear all recorded events from log."));
        btnClear.setOnAction(e -> onClearLog.run());

        javafx.scene.control.Button btnExport = new javafx.scene.control.Button();
        btnExport.textProperty().bind(i18n.createStringBinding("log.btn.export"));
        btnExport.setTooltip(new javafx.scene.control.Tooltip("Export displayed events to CSV or JSON format."));
        btnExport.setOnAction(e -> onExport.run());

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        filters.getChildren().addAll(pane.searchField, typeLabel, pane.typeFilter, sevLabel, pane.severityFilter, pane.autoScrollCheck,
                spacer, btnClear, btnExport);

        toolbar.getChildren().addAll(title, filters);
        return toolbar;
    }
}
