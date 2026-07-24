/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.studio.editors;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Editor for defining Species behavior, life cycle, and castes.
 */
public class SpeciesEditorView extends ScrollPane {

    private TextField latinNameField;
    private TextField commonNameField;
    private Slider aggressionSlider;
    private Spinner<Integer> foragingRangeSpinner;

    public SpeciesEditorView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(20));

        Label title = new Label("Species Definition");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        content.getChildren().add(title);

        // Basic Info
        GridPane basicInfo = new GridPane();
        basicInfo.setHgap(10);
        basicInfo.setVgap(10);

        basicInfo.add(new Label("Latin Name:"), 0, 0);
        latinNameField = new TextField("Lasius niger");
        basicInfo.add(latinNameField, 1, 0);

        basicInfo.add(new Label("Common Name:"), 0, 1);
        commonNameField = new TextField("Black Garden Ant");
        basicInfo.add(commonNameField, 1, 1);

        content.getChildren().add(new TitledPane("Taxonomy", basicInfo));

        // Castes
        VBox casteBox = new VBox(10);
        ListView<String> casteList = new ListView<>(FXCollections.observableArrayList("Queen", "Worker", "Male"));
        casteList.setPrefHeight(100);
        casteBox.getChildren().addAll(casteList, new Button("Add Caste..."));

        content.getChildren().add(new TitledPane("Castes & Morphology", casteBox));

        // Behavioral Traits
        GridPane traits = new GridPane();
        traits.setHgap(10);
        traits.setVgap(10);

        traits.add(new Label("Aggression Level:"), 0, 0);
        aggressionSlider = new Slider(0, 1, 0.3);
        traits.add(aggressionSlider, 1, 0);

        traits.add(new Label("Foraging Range (m):"), 0, 1);
        foragingRangeSpinner = new Spinner<>(10, 500, 50);
        traits.add(foragingRangeSpinner, 1, 1);

        content.getChildren().add(new TitledPane("Behavioral Traits", traits));

        setContent(content);
        setFitToWidth(true);
    }

    public org.swarmforge.core.species.Species getSpecies() {
        org.swarmforge.core.species.CustomSpecies species = new org.swarmforge.core.species.CustomSpecies();
        species.setScientificName(latinNameField.getText());
        species.setCommonName(commonNameField.getText());
        // species.setAggression(aggressionSlider.getValue()); // CustomSpecies doesn't
        // have this yet, need to add if needed
        species.setViewDistance(foragingRangeSpinner.getValue().floatValue());

        return species;
    }
}
