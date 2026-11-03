/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.swarmforge.core.event.SimulationEvent;

import java.util.Map;

/**
 * Inspection modal dialog displaying detailed parameters and contextual metadata of a {@link SimulationEvent}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public final class EventLogDetailDialog {

    private EventLogDetailDialog() {
        // Utility dialog class
    }

    /**
     * Opens a detailed modal inspector for the specified event.
     *
     * @param event the simulation event to inspect
     */
    public static void show(SimulationEvent event) {
        if (event == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Event Details EVT-" + String.format("%06d", event.getSequenceId()));
        dialog.setHeaderText("EVT-" + String.format("%06d", event.getSequenceId()) + " | Tick #" + event.getTick() + " | Severity: " + event.getSeverity());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.getStyleClass().add("card-pane");

        VBox content = new VBox(12);
        content.setPadding(new Insets(15));
        content.setPrefWidth(520);

        Label msgLabel = new Label("Message: " + event.getMessage());
        msgLabel.setWrapText(true);
        msgLabel.getStyleClass().add("bold-label");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(10, 0, 0, 0));

        int row = 0;
        grid.add(new Label("Sequence No:"), 0, row);
        Label seqLbl = new Label(String.format("EVT-%06d", event.getSequenceId()));
        seqLbl.getStyleClass().add("accent-text");
        grid.add(seqLbl, 1, row++);

        grid.add(new Label("Timestamp:"), 0, row);
        grid.add(new Label(event.getTimestamp() != null ? event.getTimestamp().toString() : "N/A"), 1, row++);

        grid.add(new Label("Event Type:"), 0, row);
        grid.add(new Label(event.getType() != null ? formatTypeString(event.getType()) : "N/A"), 1, row++);

        grid.add(new Label("Severity:"), 0, row);
        grid.add(new Label(event.getSeverity() != null ? event.getSeverity().name() : "INFO"), 1, row++);

        if (event.getData() != null && !event.getData().isEmpty()) {
            grid.add(new Label("Associated Data:"), 0, row++);
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                Label k = new Label("  • " + formatKey(entry.getKey()) + " (" + entry.getKey() + ") :");
                k.getStyleClass().add("sub-title-gray");
                Label v = new Label(formatValue(entry.getValue()));
                v.getStyleClass().add("accent-text");
                grid.add(k, 0, row);
                grid.add(v, 1, row++);
            }
        }

        content.getChildren().addAll(msgLabel, new Separator(), grid);
        pane.setContent(content);
        dialog.showAndWait();
    }

    /**
     * Formats an event type into a user-friendly human-readable label.
     *
     * @param type the event type
     * @return readable event type name
     */
    public static String formatTypeString(SimulationEvent.EventType type) {
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

    /**
     * Formats data keys into standard scientific / metric labels.
     *
     * @param key the raw map key
     * @return human-readable metric key
     */
    public static String formatKey(String key) {
        if (key == null) return "";
        return switch (key) {
            case "colonyId", "colony" -> "Colony";
            case "species" -> "Species";
            case "individualId" -> "Individual ID";
            case "caste" -> "Caste";
            case "stage" -> "Stage";
            case "job" -> "Job";
            case "cause" -> "Cause of Death";
            case "ageDays", "age", "ageTicks" -> "Âge (jours)";
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

    /**
     * Formats data values, shortening long identifiers.
     *
     * @param value the raw object value
     * @return formatted string
     */
    public static String formatValue(Object value) {
        if (value == null) return "";
        String str = value.toString();
        if (str.length() == 36 && str.contains("-")) {
            return "#" + str.substring(0, 8).toUpperCase();
        }
        return str;
    }
}
