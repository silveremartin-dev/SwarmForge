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
import org.swarmforge.client.ui.ComboBoxTooltipHelper;
import org.swarmforge.core.species.DefaultSpecies;

/**
 * UI Pane for Species Diet, Trophallaxis, Water & Food Consumption, Locomotion & Muscle Strength.
 */
public class SpeciesDietPane {

    public final ComboBox<String> primaryDietCombo = new ComboBox<>(FXCollections.observableArrayList(
        "FUNGUS", "HONEYDEW", "INSECTS_MEAT", "OMNIVORE", "SEEDS", "SUGARS_NECTAR", "WOOD_CELLULOSE"
    ));
    public final ComboBox<String> secondaryDietCombo = new ComboBox<>(FXCollections.observableArrayList(
        "FUNGUS", "HONEYDEW", "INSECTS_MEAT", "NONE", "SEEDS", "SUGARS_NECTAR", "WOOD_CELLULOSE"
    ));
    public final TextField foodConsumptionField = new TextField("0.5");
    public final TextField waterReqField = new TextField("0.2");
    public final TextField workerLifespanField = new TextField("5000");
    public final TextField workerSpeedField = new TextField("0.5");
    public final CheckBox flyCheckBox = new CheckBox("Workers capable of flight (Winged species)");
    public final TextField viewDistanceField = new TextField("5.0");
    public final TextField metabolismField = new TextField("1.0");
    public final TextField strengthField = new TextField("5.0");

    private final ScrollPane container;

    public SpeciesDietPane(Runnable onFieldEdited) {
        GridPane grid = SpeciesUIHelper.createGrid();

        ComboBoxTooltipHelper.setupDescriptiveComboBox(primaryDietCombo, SpeciesUIFormatting::getDietTitle, SpeciesUIFormatting::getDietDescription);
        primaryDietCombo.getSelectionModel().select("HONEYDEW");

        ComboBoxTooltipHelper.setupDescriptiveComboBox(secondaryDietCombo, SpeciesUIFormatting::getDietTitle, SpeciesUIFormatting::getDietDescription);
        secondaryDietCombo.getSelectionModel().select("INSECTS_MEAT");

        grid.addRow(0, SpeciesUIHelper.createTooltipLabel("Primary Diet Source:", "Primary trophic source consumed for metabolic energy of the colony.", primaryDietCombo, "trophallaxis"), primaryDietCombo);
        grid.addRow(1, SpeciesUIHelper.createTooltipLabel("Secondary Diet Source:", "Complementary trophic source (e.g., protein intake during brood rearing).", secondaryDietCombo, "trophallaxis"), secondaryDietCombo);
        grid.addRow(2, SpeciesUIHelper.createTooltipLabel("Metabolic Consumption (g/ind/day):", "Daily food mass consumed per adult individual.", foodConsumptionField, "metabolism"), foodConsumptionField);
        grid.addRow(3, SpeciesUIHelper.createTooltipLabel("Water Requirement (mL/ind/day):", "Daily water volume needed for hydration and metabolism.", waterReqField, "water"), waterReqField);
        grid.addRow(4, SpeciesUIHelper.createTooltipLabel("Worker Lifespan (days):", "Average lifespan of an adult worker before natural senescence.", workerLifespanField), workerLifespanField);
        grid.addRow(5, SpeciesUIHelper.createTooltipLabel("Locomotion Speed (m/s):", "Standard surface movement speed of workers in meters per second.", workerSpeedField), workerSpeedField);
        grid.addRow(6, SpeciesUIHelper.createTooltipLabel("Visual Detection Distance (cm):", "Visual perception radius for food resources and enemies in centimeters.", viewDistanceField), viewDistanceField);
        grid.addRow(7, SpeciesUIHelper.createTooltipLabel("Worker Flight Capability:", "Check if workers of this species are winged and capable of flight (e.g. Honeybees, Wasps).", flyCheckBox, "flight"), flyCheckBox);
        grid.addRow(8, SpeciesUIHelper.createTooltipLabel("Global Metabolic Factor (0.1-5.0):", "Multiplier for energy expenditure rate and reserve consumption.", metabolismField, "metabolism"), metabolismField);
        grid.addRow(9, SpeciesUIHelper.createTooltipLabel("Physio-Muscular Strength (N):", "General physical strength index and mechanical resistance.", strengthField, "mandibule"), strengthField);

        container = SpeciesUIHelper.wrapScroll(grid);

        if (onFieldEdited != null) {
            primaryDietCombo.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            secondaryDietCombo.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            foodConsumptionField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            waterReqField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            workerLifespanField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            workerSpeedField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            flyCheckBox.selectedProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            viewDistanceField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            metabolismField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            strengthField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
        }
    }

    public ScrollPane getContainer() {
        return container;
    }

    public void populateFromSpecies(DefaultSpecies s) {
        if (s == null) return;
        if (s.getPrimaryDiet() != null) primaryDietCombo.getSelectionModel().select(s.getPrimaryDiet());
        if (s.getSecondaryDiet() != null) secondaryDietCombo.getSelectionModel().select(s.getSecondaryDiet());
        foodConsumptionField.setText(String.valueOf(s.getDailyFoodConsumption()));
        waterReqField.setText(String.valueOf(s.getWaterRequirement()));
        workerLifespanField.setText(String.valueOf(s.getWorkerLifespan()));
        workerSpeedField.setText(String.valueOf(s.getWorkerSpeed()));
        viewDistanceField.setText(String.valueOf(s.getViewDistance()));
        flyCheckBox.setSelected(s.isWorkersCanFly());
        metabolismField.setText(String.valueOf(s.getMetabolism()));
        strengthField.setText(String.valueOf(s.getStrength()));
    }

    public void applyToSpecies(DefaultSpecies s) {
        if (s == null) return;
        s.setPrimaryDiet(primaryDietCombo.getValue());
        s.setSecondaryDiet(secondaryDietCombo.getValue());
        s.setDailyFoodConsumption(SpeciesUIHelper.parseFloat(foodConsumptionField.getText(), 0.5f));
        s.setWaterRequirement(SpeciesUIHelper.parseFloat(waterReqField.getText(), 0.2f));
        s.setWorkerLifespan(SpeciesUIHelper.parseInt(workerLifespanField.getText(), 5000));
        s.setWorkerSpeed(SpeciesUIHelper.parseFloat(workerSpeedField.getText(), 0.5f));
        s.setViewDistance(SpeciesUIHelper.parseFloat(viewDistanceField.getText(), 5.0f));
        s.setWorkersCanFly(flyCheckBox.isSelected());
        s.setMetabolism(SpeciesUIHelper.parseFloat(metabolismField.getText(), 1.0f));
        s.setStrength(SpeciesUIHelper.parseFloat(strengthField.getText(), 5.0f));
    }
}
