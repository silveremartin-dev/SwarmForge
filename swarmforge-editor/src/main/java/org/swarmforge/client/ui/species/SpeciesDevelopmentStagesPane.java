/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.swarmforge.client.ui.ComboBoxTooltipHelper;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.species.DefaultSpecies;

import java.util.Locale;

/**
 * UI Pane for Development Stages (Egg, Larva, Pupa durations), nutritional caste determination thresholds,
 * queen pheromone inhibition, and social immunity/grooming.
 */
public class SpeciesDevelopmentStagesPane {

    public final TextField eggDurationField = new TextField("300");
    public final TextField larvaDurationField = new TextField("600");
    public final ComboBox<String> larvaDietCombo = new ComboBox<>(FXCollections.observableArrayList(
        "CELLULOSE", "FUNGUS", "HIGH_PROTEIN_MEAT", "OMNIVORE", "SEEDS", "SUGAR_HONEY"
    ));
    public final TextField pupaDurationField = new TextField("500");

    public final TextField proteinMinorField = new TextField("0.35");
    public final TextField proteinMajorField = new TextField("0.70");
    public final TextField proteinSoldierField = new TextField("0.85");
    public final TextField proteinQueenField = new TextField("0.95");
    public final Slider pheroInhibSlider = new Slider(0.0, 1.0, 0.8);
    public final CheckBox haplodiploidyCheckBox = new CheckBox("Arrhenotoky / Haplodiploidy (Unfertilized egg = Male)");

    public final Slider pathogenResistanceSlider = new Slider(0.0, 1.0, 0.5);
    public final Slider groomingSlider = new Slider(0.0, 1.0, 0.7);

    private final ScrollPane container;

    public SpeciesDevelopmentStagesPane(Runnable onFieldEdited) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(15));

        // Metamorphosis stage durations card
        VBox cardDurations = new VBox(10);
        cardDurations.getStyleClass().add("card-pane");
        Label titleDurations = new Label("🌱 Metamorphosis Stage Durations");
        titleDurations.getStyleClass().add("card-title");

        GridPane gridDurations = SpeciesUIHelper.createGrid();

        ComboBoxTooltipHelper.setupDescriptiveComboBox(larvaDietCombo,
            val -> switch (val != null ? val : "") {
                case "CELLULOSE" -> I18nManager.getInstance().get("species.larva_diet.cellulose");
                case "FUNGUS" -> I18nManager.getInstance().get("species.larva_diet.fungus");
                case "HIGH_PROTEIN_MEAT" -> I18nManager.getInstance().get("species.larva_diet.high_protein_meat");
                case "OMNIVORE" -> I18nManager.getInstance().get("species.larva_diet.omnivore");
                case "SEEDS" -> I18nManager.getInstance().get("species.larva_diet.seeds");
                case "SUGAR_HONEY" -> I18nManager.getInstance().get("species.larva_diet.sugar_honey");
                default -> "";
            },
            val -> switch (val != null ? val : "") {
                case "CELLULOSE" -> I18nManager.getInstance().get("species.larva_diet.cellulose.desc");
                case "FUNGUS" -> I18nManager.getInstance().get("species.larva_diet.fungus.desc");
                case "HIGH_PROTEIN_MEAT" -> I18nManager.getInstance().get("species.larva_diet.high_protein_meat.desc");
                case "OMNIVORE" -> I18nManager.getInstance().get("species.larva_diet.omnivore.desc");
                case "SEEDS" -> I18nManager.getInstance().get("species.larva_diet.seeds.desc");
                case "SUGAR_HONEY" -> I18nManager.getInstance().get("species.larva_diet.sugar_honey.desc");
                default -> "";
            }
        );
        larvaDietCombo.getSelectionModel().select("HIGH_PROTEIN_MEAT");

        gridDurations.addRow(0, SpeciesUIHelper.createTooltipLabel("Egg Stage Duration (days):", "Incubation period required before first larval instar.", eggDurationField), eggDurationField);
        gridDurations.addRow(1, SpeciesUIHelper.createTooltipLabel("Larval Stage Duration (days):", "Period of intensive larval growth and feeding.", larvaDurationField), larvaDurationField);
        gridDurations.addRow(2, SpeciesUIHelper.createTooltipLabel("Larval Diet Requirement:", "Specific food provided by nurse workers to developing larvae.", larvaDietCombo), larvaDietCombo);
        gridDurations.addRow(3, SpeciesUIHelper.createTooltipLabel("Pupal Stage / Cocoon Duration (days):", "Metamorphosis duration required to form adult imago.", pupaDurationField), pupaDurationField);
        cardDurations.getChildren().addAll(titleDurations, gridDurations);

        // Caste determination matrix card
        VBox cardMatrix = new VBox(10);
        cardMatrix.getStyleClass().add("card-pane");
        Label titleMatrix = new Label("🔬 Caste Determination Matrix (Nutrition & Pheromones)");
        titleMatrix.getStyleClass().add("card-title");

        GridPane gridMatrix = SpeciesUIHelper.createGrid();
        pheroInhibSlider.setShowTickLabels(true);
        pheroInhibSlider.setShowTickMarks(true);
        haplodiploidyCheckBox.setSelected(true);

        gridMatrix.addRow(0, SpeciesUIHelper.createTooltipLabel("Minor Worker Protein Threshold (%):", "Minimum larval protein fraction required to differentiate a minor worker.", proteinMinorField), proteinMinorField);
        gridMatrix.addRow(1, SpeciesUIHelper.createTooltipLabel("Major Worker Protein Threshold (%):", "Protein fraction required to differentiate a major worker.", proteinMajorField), proteinMajorField);
        gridMatrix.addRow(2, SpeciesUIHelper.createTooltipLabel("Soldier Protein Threshold (%):", "Protein threshold required for soldier caste differentiation.", proteinSoldierField), proteinSoldierField);
        gridMatrix.addRow(3, SpeciesUIHelper.createTooltipLabel("Royal Food Protein Threshold (%):", "Maximum nutritional threshold inducing fertile queen differentiation.", proteinQueenField), proteinQueenField);
        gridMatrix.addRow(4, SpeciesUIHelper.createTooltipLabel("Queen Pheromonal Inhibition:", "Chemical inhibition emitted by queen to suppress queen rearing.", pheroInhibSlider, "Inhibition"), pheroInhibSlider);
        gridMatrix.addRow(5, SpeciesUIHelper.createTooltipLabel("Male Determination (Arrhenotoky):", "Arrhenotoky / Haplodiploidy: unfertilized eggs produce males, fertilized eggs produce females.", haplodiploidyCheckBox, "Haplodiploidy"), haplodiploidyCheckBox);
        cardMatrix.getChildren().addAll(titleMatrix, gridMatrix);

        // Immunity and hygiene defense card
        VBox cardImmunity = new VBox(10);
        cardImmunity.getStyleClass().add("card-pane");
        Label titleImmunity = new Label("🛡️ Immunity & Health Defense (Allogrooming & Hygiene)");
        titleImmunity.getStyleClass().add("card-title");

        GridPane gridImmunity = SpeciesUIHelper.createGrid();
        pathogenResistanceSlider.setShowTickLabels(true);
        pathogenResistanceSlider.setShowTickMarks(true);
        groomingSlider.setShowTickLabels(true);
        groomingSlider.setShowTickMarks(true);

        gridImmunity.addRow(0, SpeciesUIHelper.createTooltipLabel("Pathogen Immune Resistance (%):", "Physiological resistance to fungal/bacterial pathogens and spores.", pathogenResistanceSlider), pathogenResistanceSlider);
        gridImmunity.addRow(1, SpeciesUIHelper.createTooltipLabel("Social Grooming Efficacy (%):", "Efficacy of mutual grooming and de-parasitism in reducing spore load.", groomingSlider), groomingSlider);
        cardImmunity.getChildren().addAll(titleImmunity, gridImmunity);

        box.getChildren().addAll(cardDurations, cardMatrix, cardImmunity);
        container = SpeciesUIHelper.wrapScroll(box);

        if (onFieldEdited != null) {
            eggDurationField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            larvaDurationField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            larvaDietCombo.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            pupaDurationField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            proteinMinorField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            proteinMajorField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            proteinSoldierField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            proteinQueenField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            pheroInhibSlider.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            haplodiploidyCheckBox.selectedProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            pathogenResistanceSlider.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            groomingSlider.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
        }
    }

    public ScrollPane getContainer() {
        return container;
    }

    public void populateFromSpecies(DefaultSpecies s) {
        if (s == null) return;
        eggDurationField.setText(String.valueOf(s.getEggStageDuration()));
        larvaDurationField.setText(String.valueOf(s.getLarvaStageDuration()));
        if (s.getLarvaDietRequirement() != null) larvaDietCombo.getSelectionModel().select(s.getLarvaDietRequirement());
        pupaDurationField.setText(String.valueOf(s.getPupaStageDuration()));

        proteinMinorField.setText(String.format(Locale.US, "%.2f", s.getProteinThresholdMinor()));
        proteinMajorField.setText(String.format(Locale.US, "%.2f", s.getProteinThresholdMajor()));
        proteinSoldierField.setText(String.format(Locale.US, "%.2f", s.getProteinThresholdSoldier()));
        proteinQueenField.setText(String.format(Locale.US, "%.2f", s.getProteinThresholdQueen()));
        pheroInhibSlider.setValue(s.getQueenPheromoneInhibitionFactor());
        haplodiploidyCheckBox.setSelected(s.isHaplodiploidyEnabled());
        pathogenResistanceSlider.setValue(s.getPathogenResistance());
        groomingSlider.setValue(s.getGroomingDefenseEfficacy());
    }

    public void applyToSpecies(DefaultSpecies s) {
        if (s == null) return;
        s.setEggStageDuration(SpeciesUIHelper.parseInt(eggDurationField.getText(), 300));
        s.setLarvaStageDuration(SpeciesUIHelper.parseInt(larvaDurationField.getText(), 600));
        s.setLarvaDietRequirement(larvaDietCombo.getValue());
        s.setPupaStageDuration(SpeciesUIHelper.parseInt(pupaDurationField.getText(), 500));

        s.setProteinThresholdMinor(SpeciesUIHelper.parseFloat(proteinMinorField.getText(), 0.35f));
        s.setProteinThresholdMajor(SpeciesUIHelper.parseFloat(proteinMajorField.getText(), 0.70f));
        s.setProteinThresholdSoldier(SpeciesUIHelper.parseFloat(proteinSoldierField.getText(), 0.85f));
        s.setProteinThresholdQueen(SpeciesUIHelper.parseFloat(proteinQueenField.getText(), 0.95f));
        s.setQueenPheromoneInhibitionFactor((float) pheroInhibSlider.getValue());
        s.setHaplodiploidyEnabled(haplodiploidyCheckBox.isSelected());
        s.setPathogenResistance((float) pathogenResistanceSlider.getValue());
        s.setGroomingDefenseEfficacy((float) groomingSlider.getValue());
    }
}
