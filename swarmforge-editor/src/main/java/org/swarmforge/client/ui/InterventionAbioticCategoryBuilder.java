/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;

/**
 * Builds the Abiotic Climate physical drivers intervention card (temperature,
 * humidity, wind, solar radiation, duration, and derived biological outputs)
 * for {@link InterventionCategoryBuilder}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
class InterventionAbioticCategoryBuilder {

    private final InterventionCategoryBuilder parent;
    private final InterventionPanel panel;

    InterventionAbioticCategoryBuilder(InterventionCategoryBuilder parent, InterventionPanel panel) {
        this.parent = parent;
        this.panel = panel;
    }

    Node createParametersSubBlockNode() {
        I18nManager i18n = I18nManager.getInstance();
        VBox box = new VBox(10);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        Label lblMode = new Label();
        lblMode.textProperty().bind(i18n.createStringBinding("god.abiotic.mode.label"));
        lblMode.setStyle("-fx-font-weight: bold;");
        parent.abioticModeSelect = new ComboBox<>(javafx.collections.FXCollections.observableArrayList(
                "📈 Relative Shift (Δ +/- relative to current climate)",
                "🎯 Fixed Absolute Value (Override current climate)"
        ));
        parent.abioticModeSelect.getSelectionModel().selectFirst();
        parent.abioticModeSelect.setPrefWidth(340);
        parent.abioticModeSelect.setTooltip(new Tooltip("Toggle between relative delta (+/- offset) and fixed absolute values for temperature, humidity, wind, and solar radiation."));

        Label lblDuration = new Label();
        lblDuration.textProperty().bind(i18n.createStringBinding("god.abiotic.duration.label"));
        lblDuration.setStyle("-fx-font-weight: bold;");
        parent.abioticDurationSlider = new Slider(5, 43200, 60);
        parent.abioticDurationSlider.setPrefWidth(200);
        parent.abioticDurationSlider.setTooltip(new Tooltip("Duration during which the abiotic modification remains active in spatio-temporal simulation."));
        parent.abioticDurationValLabel = new Label("60 min (1h)");
        parent.abioticDurationValLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
        parent.abioticDurationSlider.valueProperty().addListener((o, a, b) -> {
            int mins = b.intValue();
            parent.abioticDurationValLabel.setText(mins < 60 ? mins + " min" : (mins < 1440) ? (mins / 60) + "h " + (mins % 60) + "m" : (mins / 1440) + " Days");
        });

        Label lblTemp = new Label(i18n.get("god.abiotic.temp.label"));
        lblTemp.setStyle("-fx-font-weight: bold;");
        parent.deltaTempSlider = new Slider(-20.0, 20.0, 5.0);
        parent.deltaTempSlider.setPrefWidth(200);
        parent.deltaTempSlider.setTooltip(new Tooltip("Temperature delta or target absolute temperature (°C). Impacts nest metabolism and activity."));
        parent.deltaTempValLabel = new Label("+5.0 °C (Warming)");
        parent.deltaTempValLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");

        Label lblHumidity = new Label(i18n.get("god.abiotic.humidity.label"));
        lblHumidity.setStyle("-fx-font-weight: bold;");
        parent.deltaHumiditySlider = new Slider(-50.0, 50.0, 20.0);
        parent.deltaHumiditySlider.setPrefWidth(200);
        parent.deltaHumiditySlider.setTooltip(new Tooltip("Soil and air relative humidity delta or target percentage (%)."));
        parent.deltaHumidityValLabel = new Label("+20.0 % (Humidifying)");
        parent.deltaHumidityValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        Label lblWind = new Label(i18n.get("god.abiotic.wind.label"));
        lblWind.setStyle("-fx-font-weight: bold;");
        parent.deltaWindSlider = new Slider(-10.0, 15.0, 2.0);
        parent.deltaWindSlider.setPrefWidth(200);
        parent.deltaWindSlider.setTooltip(new Tooltip("Wind speed delta or fixed target (m/s). Regulates surface pheromone dissipation."));
        parent.deltaWindValLabel = new Label("+2.0 m/s");
        parent.deltaWindValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        Label lblSolar = new Label(i18n.get("god.abiotic.solar.label"));
        lblSolar.setStyle("-fx-font-weight: bold;");
        parent.deltaSolarSlider = new Slider(-500.0, 500.0, 100.0);
        parent.deltaSolarSlider.setPrefWidth(200);
        parent.deltaSolarSlider.setTooltip(new Tooltip("Direct solar radiation flux delta or fixed target (W/m²). Regulates photosynthesis and mound heating."));
        parent.deltaSolarValLabel = new Label("+100 W/m²");
        parent.deltaSolarValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

        parent.abioticModeSelect.valueProperty().addListener((o, oldVal, newVal) -> {
            boolean isRel = newVal != null && newVal.contains("Relative");
            if (isRel) {
                lblTemp.setText("Temperature Shift (Δ °C):");
                parent.deltaTempSlider.setMin(-20.0); parent.deltaTempSlider.setMax(20.0); parent.deltaTempSlider.setValue(5.0);
                parent.deltaTempValLabel.setText("+5.0 °C (Warming)");
                parent.deltaTempValLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");

                lblHumidity.setText("Humidity Shift (Δ %):");
                parent.deltaHumiditySlider.setMin(-50.0); parent.deltaHumiditySlider.setMax(50.0); parent.deltaHumiditySlider.setValue(20.0);
                parent.deltaHumidityValLabel.setText("+20.0 % (Humidifying)");
                parent.deltaHumidityValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

                lblWind.setText("Wind Shift (Δ m/s):");
                parent.deltaWindSlider.setMin(-10.0); parent.deltaWindSlider.setMax(15.0); parent.deltaWindSlider.setValue(2.0);
                parent.deltaWindValLabel.setText("+2.0 m/s");

                lblSolar.setText("Radiation Shift (Δ W/m²):");
                parent.deltaSolarSlider.setMin(-500.0); parent.deltaSolarSlider.setMax(500.0); parent.deltaSolarSlider.setValue(100.0);
                parent.deltaSolarValLabel.setText("+100 W/m²");
            } else {
                lblTemp.setText("Target Fixed Temperature (°C):");
                parent.deltaTempSlider.setMin(-10.0); parent.deltaTempSlider.setMax(50.0); parent.deltaTempSlider.setValue(22.0);
                parent.deltaTempValLabel.setText("22.0 °C");
                parent.deltaTempValLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");

                lblHumidity.setText("Target Fixed Humidity (%):");
                parent.deltaHumiditySlider.setMin(0.0); parent.deltaHumiditySlider.setMax(100.0); parent.deltaHumiditySlider.setValue(65.0);
                parent.deltaHumidityValLabel.setText("65.0 %");
                parent.deltaHumidityValLabel.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");

                lblWind.setText("Target Fixed Wind Speed (m/s):");
                parent.deltaWindSlider.setMin(0.0); parent.deltaWindSlider.setMax(30.0); parent.deltaWindSlider.setValue(1.5);
                parent.deltaWindValLabel.setText("1.5 m/s");

                lblSolar.setText("Target Fixed Solar Radiation (W/m²):");
                parent.deltaSolarSlider.setMin(0.0); parent.deltaSolarSlider.setMax(1200.0); parent.deltaSolarSlider.setValue(450.0);
                parent.deltaSolarValLabel.setText("450 W/m²");
            }
            updateDerivedPhysicalOutputs();
        });

        parent.deltaTempSlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            boolean isRel = parent.abioticModeSelect.getValue() != null && parent.abioticModeSelect.getValue().contains("Relative");
            if (isRel) {
                parent.deltaTempValLabel.setText(String.format("%+.1f °C (%s)", v, v > 0 ? "Warming" : v < 0 ? "Cooling" : "Unchanged"));
                parent.deltaTempValLabel.setStyle(v > 0 ? "-fx-text-fill: #ef4444; -fx-font-weight: bold;" : v < 0 ? "-fx-text-fill: #38bdf8; -fx-font-weight: bold;" : "-fx-text-fill: #94a3b8; -fx-font-weight: bold;");
            } else {
                parent.deltaTempValLabel.setText(String.format("%.1f °C", v));
                parent.deltaTempValLabel.setStyle("-fx-text-fill: #4ade80; -fx-font-weight: bold;");
            }
            updateDerivedPhysicalOutputs();
        });

        parent.deltaHumiditySlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            boolean isRel = parent.abioticModeSelect.getValue() != null && parent.abioticModeSelect.getValue().contains("Relative");
            if (isRel) {
                parent.deltaHumidityValLabel.setText(String.format("%+.1f %% (%s)", v, v > 0 ? "Humidifying" : v < 0 ? "Drying" : "Unchanged"));
            } else {
                parent.deltaHumidityValLabel.setText(String.format("%.1f %%", v));
            }
            updateDerivedPhysicalOutputs();
        });

        parent.deltaWindSlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            boolean isRel = parent.abioticModeSelect.getValue() != null && parent.abioticModeSelect.getValue().contains("Relative");
            if (isRel) {
                parent.deltaWindValLabel.setText(String.format("%+.1f m/s", v));
            } else {
                parent.deltaWindValLabel.setText(String.format("%.1f m/s", v));
            }
            updateDerivedPhysicalOutputs();
        });

        parent.deltaSolarSlider.valueProperty().addListener((o, a, b) -> {
            double v = b.doubleValue();
            boolean isRel = parent.abioticModeSelect.getValue() != null && parent.abioticModeSelect.getValue().contains("Relative");
            if (isRel) {
                parent.deltaSolarValLabel.setText(String.format("%+.0f W/m²", v));
            } else {
                parent.deltaSolarValLabel.setText(String.format("%.0f W/m²", v));
            }
            updateDerivedPhysicalOutputs();
        });

        grid.add(lblMode, 0, 0); grid.add(parent.abioticModeSelect, 1, 0);
        grid.add(lblTemp, 0, 1); grid.add(parent.deltaTempSlider, 1, 1); grid.add(parent.deltaTempValLabel, 2, 1);
        grid.add(lblHumidity, 0, 2); grid.add(parent.deltaHumiditySlider, 1, 2); grid.add(parent.deltaHumidityValLabel, 2, 2);
        grid.add(lblWind, 0, 3); grid.add(parent.deltaWindSlider, 1, 3); grid.add(parent.deltaWindValLabel, 2, 3);
        grid.add(lblSolar, 0, 4); grid.add(parent.deltaSolarSlider, 1, 4); grid.add(parent.deltaSolarValLabel, 2, 4);
        grid.add(lblDuration, 0, 5); grid.add(parent.abioticDurationSlider, 1, 5); grid.add(parent.abioticDurationValLabel, 2, 5);

        VBox derivedBox = new VBox(4);
        derivedBox.getStyleClass().add("header-banner");

        Label lblDerivedHeader = new Label();
        lblDerivedHeader.textProperty().bind(i18n.createStringBinding("god.derived.header"));
        lblDerivedHeader.getStyleClass().add("accent-title");

        parent.derivedPheroLabel = new Label();
        parent.derivedPheroLabel.setStyle("-fx-font-size: 11px;");

        parent.derivedPrimaryProductivityLabel = new Label();
        parent.derivedPrimaryProductivityLabel.setStyle("-fx-font-size: 11px;");

        parent.derivedOvipositionLabel = new Label();
        parent.derivedOvipositionLabel.setStyle("-fx-font-size: 11px;");

        derivedBox.getChildren().addAll(lblDerivedHeader, parent.derivedPheroLabel, parent.derivedPrimaryProductivityLabel, parent.derivedOvipositionLabel);

        updateDerivedPhysicalOutputs();

        HBox actionBtnRow = new HBox(8);
        Button btnScheduleAbiotic = new Button();
        btnScheduleAbiotic.textProperty().bind(i18n.createStringBinding("god.btn.schedule_abiotic"));
        btnScheduleAbiotic.getStyleClass().add("btn-primary");
        btnScheduleAbiotic.setGraphic(new FontIcon(Feather.SUN));
        btnScheduleAbiotic.tooltipProperty().bind(i18n.createTooltipBinding("god.abiotic.btn.tt"));
        btnScheduleAbiotic.setOnAction(e -> panel.scheduler.scheduleAbioticEvent());

        actionBtnRow.getChildren().add(btnScheduleAbiotic);

        box.getChildren().addAll(grid, derivedBox, actionBtnRow);
        return box;
    }

    void updateDerivedPhysicalOutputs() {
        if (parent.deltaTempSlider == null || parent.abioticModeSelect == null) return;

        boolean isRel = parent.abioticModeSelect.getValue() != null && parent.abioticModeSelect.getValue().contains("Relative");
        double temp = parent.deltaTempSlider.getValue();
        double hum = parent.deltaHumiditySlider != null ? parent.deltaHumiditySlider.getValue() : 0.0;
        double wind = parent.deltaWindSlider != null ? parent.deltaWindSlider.getValue() : 0.0;
        double solar = parent.deltaSolarSlider != null ? parent.deltaSolarSlider.getValue() : 0.0;

        if (isRel) {
            parent.derivedPheroLabel.setText(String.format("• Climate Delta: ΔT = %+.1f°C | ΔHumidity = %+.1f%% | ΔWind = %+.1fm/s", temp, hum, wind));
            parent.derivedPrimaryProductivityLabel.setText(String.format("• Relative Solar Radiation: %+.0f W/m² (Duration: %d min)", solar, (int) parent.abioticDurationSlider.getValue()));
            parent.derivedOvipositionLabel.setText("• Estimated Thermal Impact on Nest Activity: " + (temp > 0 ? "Metabolic Acceleration" : temp < 0 ? "Slowdown / Lethargy" : "Stability"));
        } else {
            parent.derivedPheroLabel.setText(String.format("• Fixed Absolute Climate: T = %.1f°C | Humidity = %.1f%% | Wind = %.1fm/s", temp, hum, wind));
            parent.derivedPrimaryProductivityLabel.setText(String.format("• Fixed Solar Radiation: %.0f W/m² (Duration: %d min)", solar, (int) parent.abioticDurationSlider.getValue()));
            parent.derivedOvipositionLabel.setText("• Fixed Metabolic Regime at " + String.format("%.1f°C", temp) + " (Override bioclimatic engine)");
        }
    }
}
