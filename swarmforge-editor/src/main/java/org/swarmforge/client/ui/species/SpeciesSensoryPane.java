/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.swarmforge.client.ui.GlossaryDialog;
import org.swarmforge.core.species.DefaultSpecies;

/**
 * Self-contained UI Pane and Data Binder for the Sensory & Perception systems tab
 * in the Species Editor (Magnetoreception, Hygroreception, Electroreception, UV compass, etc.).
 */
public class SpeciesSensoryPane {

    public final CheckBox hasMagnetoreceptionCheckBox = new CheckBox("Magnetoreception (Earth Magnetic Field - Termites / Compass Ant)");
    public final TextField magnetoSensField = new TextField("5.0");
    public final TextField thermoSensField = new TextField("0.5");
    public final TextField gasSensField = new TextField("400.0");
    public final TextField visualAcuityField = new TextField("1.0");
    public final TextField viewDistanceField = new TextField("5.0");
    public final TextField minLightField = new TextField("0.05");

    public final CheckBox hasVibrationSensingCheckBox = new CheckBox("Substrate Vibration Perception (Johnston Organ / Alarm Drumming)");
    public final TextField vibrationSensField = new TextField("10.0");

    public final CheckBox hasHygroreceptionCheckBox = new CheckBox("Hygroreception (Relative Humidity Gradient Sensing)");
    public final TextField hygroSensField = new TextField("2.0");

    public final CheckBox hasElectrosensingCheckBox = new CheckBox("Electroreception (Atmospheric & Floral Electrostatic Charges - Bees / Wasps)");
    public final TextField electroSensField = new TextField("50.0");

    public final CheckBox hasPolarizedLightCheckBox = new CheckBox("UV Polarized Light Navigation (Celestial Compass - Bees / Cataglyphis)");

    private final ScrollPane container;

    public SpeciesSensoryPane() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        // 1. Sensory Card
        VBox cardSensors = new VBox(10);
        cardSensors.getStyleClass().add("card-pane");
        Label titleSensors = new Label("📡 Sensory Systems & Environmental Perception (SI)");
        titleSensors.getStyleClass().add("card-title");

        GridPane gridSensors = createGrid();

        hasVibrationSensingCheckBox.setSelected(true);
        hasHygroreceptionCheckBox.setSelected(true);

        gridSensors.addRow(0, createTooltipLabel("Magnetoreception (Earth Field):", "Ability to perceive Earth's magnetic field lines for navigation.", hasMagnetoreceptionCheckBox, "Magnetoreception"), hasMagnetoreceptionCheckBox);
        gridSensors.addRow(1, createTooltipLabel("Magnetic Field Threshold (µT):", "Minimum sensitivity of magnetic perception in micro-Teslas.", magnetoSensField), magnetoSensField);
        gridSensors.addRow(2, createTooltipLabel("Thermal Sensitivity (Δ°C/mm):", "Ability to detect temperature gradients for thermoregulation.", thermoSensField), thermoSensField);
        gridSensors.addRow(3, createTooltipLabel("CO₂ Threshold (ppm):", "CO₂ perception threshold for nest ventilation control in ppm.", gasSensField), gasSensField);
        gridSensors.addRow(4, createTooltipLabel("Visual Acuity (0-1):", "Relative visual resolution provided by compound eye ommatidia.", visualAcuityField), visualAcuityField);
        gridSensors.addRow(5, createTooltipLabel("Visual Perception Radius (cm):", "Maximum visual perception range for objects, threats, and food.", viewDistanceField), viewDistanceField);
        gridSensors.addRow(6, createTooltipLabel("Minimum Light Threshold (lux):", "Minimum light level enabling nocturnal or crepuscular vision.", minLightField), minLightField);
        gridSensors.addRow(7, createTooltipLabel("Substrate Vibration Sensing (Subgenual):", "Sensitivity to seismic and mechanical vibrations transmitted through soil.", hasVibrationSensingCheckBox, "Subgenual"), hasVibrationSensingCheckBox);
        gridSensors.addRow(8, createTooltipLabel("Substrate Vibration Threshold (dB):", "Minimum vibration intensity measurable by subgenual organ in decibels.", vibrationSensField), vibrationSensField);
        gridSensors.addRow(9, createTooltipLabel("Hygroreception (Relative Humidity):", "Ability to perceive atmospheric and soil moisture gradients.", hasHygroreceptionCheckBox), hasHygroreceptionCheckBox);
        gridSensors.addRow(10, createTooltipLabel("Humidity Sensitivity (%):", "Minimum humidity gradient detectable by antennal receptors in %.", hygroSensField), hygroSensField);
        gridSensors.addRow(11, createTooltipLabel("Electrostatic Field Perception:", "Sensitivity to atmospheric and floral electrostatic charges.", hasElectrosensingCheckBox), hasElectrosensingCheckBox);
        gridSensors.addRow(12, createTooltipLabel("Atmospheric Electric Threshold (V/m):", "Electric field perception threshold in Volts per meter.", electroSensField), electroSensField);
        gridSensors.addRow(13, createTooltipLabel("UV Polarized Light Compass:", "Use of skylight UV polarization patterns for path integration navigation.", hasPolarizedLightCheckBox, "UV"), hasPolarizedLightCheckBox);

        // Conditional field disabling: gray out sensitivity fields when capability is absent
        magnetoSensField.disableProperty().bind(hasMagnetoreceptionCheckBox.selectedProperty().not());
        vibrationSensField.disableProperty().bind(hasVibrationSensingCheckBox.selectedProperty().not());
        hygroSensField.disableProperty().bind(hasHygroreceptionCheckBox.selectedProperty().not());
        electroSensField.disableProperty().bind(hasElectrosensingCheckBox.selectedProperty().not());

        cardSensors.getChildren().addAll(titleSensors, gridSensors);

        // Dynamic Plugin Extensibility Card
        VBox cardPlugins = new VBox(10);
        cardPlugins.getStyleClass().add("card-pane");
        Label titlePlugins = new Label("🔌 Extension Attributes & Plugins (Dynamic Extensibility)");
        titlePlugins.getStyleClass().add("card-title");

        GridPane gridPlugins = createGrid();
        TextField customKeyField = new TextField();
        customKeyField.setPromptText("Parameter Key (e.g. bioluminescence)");
        TextField customValueField = new TextField();
        customValueField.setPromptText("Value (e.g. 1.5 or true)");

        Button btnAddCustomAttr = new Button("Add Plugin Attribute");
        btnAddCustomAttr.getStyleClass().add("btn-secondary");
        btnAddCustomAttr.setOnAction(e -> {
            String k = customKeyField.getText().trim();
            if (!k.isEmpty()) {
                org.swarmforge.client.util.ThemeManager.createAlert(Alert.AlertType.INFORMATION, "Extension attribute '" + k + "' registered for active species.").show();
            }
        });

        gridPlugins.addRow(0, createWhiteLabel("Plugin Parameter Key:"), customKeyField);
        gridPlugins.addRow(1, createWhiteLabel("Value:"), customValueField);
        gridPlugins.addRow(2, new Label(), btnAddCustomAttr);

        cardPlugins.getChildren().addAll(titlePlugins, gridPlugins);

        box.getChildren().addAll(cardSensors, cardPlugins);

        container = new ScrollPane(box);
        container.setFitToWidth(true);
        container.setStyle("-fx-background-color: transparent;");
    }

    public ScrollPane getContainer() {
        return container;
    }

    public void populateFromSpecies(DefaultSpecies s) {
        if (s == null) return;
        hasMagnetoreceptionCheckBox.setSelected(s.hasMagnetoreception());
        magnetoSensField.setText(String.valueOf(s.getMagnetoreceptionSensitivity()));
        thermoSensField.setText(String.valueOf(s.getThermoreceptionSensitivity()));
        gasSensField.setText(String.valueOf(s.getGasSensitivityCo2Ppm()));
        visualAcuityField.setText(String.valueOf(s.getVisualAcuity()));
        viewDistanceField.setText(String.valueOf(s.getViewDistance()));
        minLightField.setText(String.valueOf(s.getMinLightLevelThreshold()));
        hasVibrationSensingCheckBox.setSelected(s.hasSubstrateVibrationSensing());
        vibrationSensField.setText(String.valueOf(s.getVibrationSensitivityDb()));
        hasHygroreceptionCheckBox.setSelected(s.hasHygroreception());
        hygroSensField.setText(String.valueOf(s.getHygroreceptionSensitivityPercent()));
        hasElectrosensingCheckBox.setSelected(s.hasElectrosensing());
        electroSensField.setText(String.valueOf(s.getElectroceptionSensitivityVolts()));
        hasPolarizedLightCheckBox.setSelected(s.hasPolarizedLightNavigation());
    }

    public void applyToSpecies(DefaultSpecies s) {
        if (s == null) return;
        s.setHasMagnetoreception(hasMagnetoreceptionCheckBox.isSelected());
        s.setMagnetoreceptionSensitivity(parseFloat(magnetoSensField.getText(), 5.0f));
        s.setThermoreceptionSensitivity(parseFloat(thermoSensField.getText(), 0.5f));
        s.setGasSensitivityCo2Ppm(parseFloat(gasSensField.getText(), 400.0f));
        s.setVisualAcuity(parseFloat(visualAcuityField.getText(), 1.0f));
        s.setViewDistance(parseFloat(viewDistanceField.getText(), 5.0f));
        s.setMinLightLevelThreshold(parseFloat(minLightField.getText(), 0.05f));
        s.setHasSubstrateVibrationSensing(hasVibrationSensingCheckBox.isSelected());
        s.setVibrationSensitivityDb(parseFloat(vibrationSensField.getText(), 10.0f));
        s.setHasHygroreception(hasHygroreceptionCheckBox.isSelected());
        s.setHygroreceptionSensitivityPercent(parseFloat(hygroSensField.getText(), 2.0f));
        s.setHasElectrosensing(hasElectrosensingCheckBox.isSelected());
        s.setElectroceptionSensitivityVolts(parseFloat(electroSensField.getText(), 50.0f));
        s.setHasPolarizedLightNavigation(hasPolarizedLightCheckBox.isSelected());
    }

    private static float parseFloat(String txt, float defaultVal) {
        if (txt == null || txt.isBlank()) return defaultVal;
        try {
            return Float.parseFloat(txt.trim());
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    private GridPane createGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(15));
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(240);
        c1.setPrefWidth(260);
        c1.setHgrow(Priority.NEVER);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);
        return grid;
    }

    private Label createWhiteLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("bold-label");
        return l;
    }

    private Label createTooltipLabel(String text, String tooltip, Control target) {
        return createTooltipLabel(text, tooltip, target, null);
    }

    private Label createTooltipLabel(String text, String tooltip, Control target, String glossaryTerm) {
        Label l = new Label(text);
        l.setTooltip(new Tooltip(tooltip));
        if (target != null) {
            target.setTooltip(new Tooltip(tooltip));
        }
        if (glossaryTerm != null && !glossaryTerm.isEmpty()) {
            l.getStyleClass().add("glossary-link");
            l.setCursor(javafx.scene.Cursor.HAND);
            l.setOnMouseClicked(e -> GlossaryDialog.show(glossaryTerm));
        } else {
            l.getStyleClass().add("bold-label");
        }
        return l;
    }
}
