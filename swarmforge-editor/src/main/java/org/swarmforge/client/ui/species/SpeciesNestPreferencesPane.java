/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.ui.ComboBoxTooltipHelper;
import org.swarmforge.core.species.DefaultSpecies;

import java.util.function.Consumer;

/**
 * UI Pane for Species Nest Architecture Preferences, Thermal Tolerances, Aggression and Territoriality.
 */
public class SpeciesNestPreferencesPane {

    public final ComboBox<String> nestTypeCombo = new ComboBox<>(FXCollections.observableArrayList(
        "ARBOREAL_SILK_LEAF",
        "BAMBOO_STEM_NEST",
        "BIVOUAC_LIVING_NEST",
        "CARTON_NEST",
        "CATHEDRAL_MOUND",
        "MATURE",
        "MOUND",
        "PAPER_PEDUNCULATE",
        "SIMPLE",
        "SUBTERRANEAN_FUNGI_VAULT",
        "TREE",
        "WAX_COMB_HEXAGONAL",
        "WAX_POTS_CLUSTER"
    ));
    public final TextField optTempField = new TextField("24.0");
    public final TextField minTempField = new TextField("10.0");
    public final TextField maxTempField = new TextField("38.0");
    public final Slider aggressionSlider = new Slider(0.0, 1.0, 0.3);
    public final Slider territorialitySlider = new Slider(0.0, 1.0, 0.5);
    public final Button btnGenerateSpeciesNest = new Button("Synchronize Nest Architecture Generator");

    private final ScrollPane container;

    public SpeciesNestPreferencesPane(Runnable onFieldEdited, Runnable onValidate, Consumer<Void> onTriggerNestGen) {
        GridPane grid = SpeciesUIHelper.createGrid();

        ComboBoxTooltipHelper.setupDescriptiveComboBox(nestTypeCombo, SpeciesUIFormatting::getNestTypeTitle, SpeciesUIFormatting::getNestTypeDescription);
        nestTypeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(String v) {
                return SpeciesUIFormatting.getNestTypeTitle(v);
            }
            @Override
            public String fromString(String s) {
                for (String item : nestTypeCombo.getItems()) {
                    if (SpeciesUIFormatting.getNestTypeTitle(item).equals(s)) return item;
                }
                return s;
            }
        });
        nestTypeCombo.getSelectionModel().select("MATURE");

        aggressionSlider.setShowTickLabels(true);
        aggressionSlider.setShowTickMarks(true);

        territorialitySlider.setShowTickLabels(true);
        territorialitySlider.setShowTickMarks(true);

        btnGenerateSpeciesNest.setGraphic(new FontIcon(Feather.SETTINGS));
        btnGenerateSpeciesNest.getStyleClass().add("btn-secondary");
        btnGenerateSpeciesNest.setStyle("-fx-font-weight: bold;");
        btnGenerateSpeciesNest.setOnAction(e -> {
            if (onTriggerNestGen != null) onTriggerNestGen.accept(null);
        });

        grid.addRow(0, SpeciesUIHelper.createTooltipLabel("Specific Nest Type (NestType):", "Selects the preferred geometric architecture constructed by this species (e.g. wax comb, cathedral, mound, silk)."), nestTypeCombo);
        grid.addRow(1, SpeciesUIHelper.createTooltipLabel("Optimal Temperature (°C):", "Ideal internal nest temperature for brood incubation."), optTempField);
        grid.addRow(2, SpeciesUIHelper.createTooltipLabel("Minimum Temperature (°C):", "Threshold below which individuals enter lethargy/torpor."), minTempField);
        grid.addRow(3, SpeciesUIHelper.createTooltipLabel("Maximum Temperature (°C):", "Critical temperature threshold above which thermal shock and mortality occur."), maxTempField);
        grid.addRow(4, SpeciesUIHelper.createTooltipLabel("Aggression Level:", "Behavioral propensity to engage in combat against other colonies."), aggressionSlider);
        grid.addRow(5, SpeciesUIHelper.createTooltipLabel("Territoriality:", "Intensity of patrolling and exclusive defense of territory around the nest."), territorialitySlider);
        grid.addRow(6, SpeciesUIHelper.createTooltipLabel("Synchronized Nest Generation:", "Opens the Nest Generator and pre-configures architecture, substrate, and chamber distribution for this species."), btnGenerateSpeciesNest);

        container = SpeciesUIHelper.wrapScroll(grid);

        if (onFieldEdited != null) {
            nestTypeCombo.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            optTempField.textProperty().addListener((obs, oldV, newV) -> {
                onFieldEdited.run();
                if (onValidate != null) onValidate.run();
            });
            minTempField.textProperty().addListener((obs, oldV, newV) -> {
                onFieldEdited.run();
                if (onValidate != null) onValidate.run();
            });
            maxTempField.textProperty().addListener((obs, oldV, newV) -> {
                onFieldEdited.run();
                if (onValidate != null) onValidate.run();
            });
            aggressionSlider.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            territorialitySlider.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
        }
    }

    public ScrollPane getContainer() {
        return container;
    }

    public void populateFromSpecies(DefaultSpecies s) {
        if (s == null) return;
        if (s.getNestType() != null) nestTypeCombo.getSelectionModel().select(s.getNestType());
        optTempField.setText(String.valueOf(s.getOptimalTempCelsius()));
        minTempField.setText(String.valueOf(s.getMinTempCelsius()));
        maxTempField.setText(String.valueOf(s.getMaxTempCelsius()));
        aggressionSlider.setValue(s.getAggression());
        territorialitySlider.setValue(s.getTerritoriality());
    }

    public void applyToSpecies(DefaultSpecies s) {
        if (s == null) return;
        s.setNestType(nestTypeCombo.getValue());
        s.setOptimalTempCelsius(SpeciesUIHelper.parseFloat(optTempField.getText(), 24.0f));
        s.setMinTempCelsius(SpeciesUIHelper.parseFloat(minTempField.getText(), 10.0f));
        s.setMaxTempCelsius(SpeciesUIHelper.parseFloat(maxTempField.getText(), 38.0f));
        s.setAggression((float) aggressionSlider.getValue());
        s.setTerritoriality((float) territorialitySlider.getValue());
    }
}
