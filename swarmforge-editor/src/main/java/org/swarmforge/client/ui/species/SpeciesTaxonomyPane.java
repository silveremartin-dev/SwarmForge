/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui.species;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.swarmforge.client.ui.ComboBoxTooltipHelper;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.species.DefaultSpecies;
import org.swarmforge.core.species.SpeciesCategory;

import java.awt.Desktop;
import java.net.URI;

/**
 * UI Pane for Species Taxonomy, binomial nomenclature, ecological status and notes.
 */
public class SpeciesTaxonomyPane {

    public final TextField commonNameField = new TextField("Black Garden Ant");
    public final TextField scientificNameField = new TextField("Lasius niger");
    public final TextField genusField = new TextField("Lasius");
    public final ComboBox<String> insectTypeCombo = new ComboBox<>(FXCollections.observableArrayList("ANT", "BEE", "OTHER", "TERMITE", "WASP"));
    public final ComboBox<SpeciesCategory> categoryCombo = new ComboBox<>(FXCollections.observableArrayList(SpeciesCategory.values()));
    public final TextArea descriptionArea = new TextArea("Species description and ecological notes...");

    private final ScrollPane container;

    public SpeciesTaxonomyPane(Runnable onFieldEdited, Runnable onValidate) {
        GridPane grid = SpeciesUIHelper.createGrid();

        ComboBoxTooltipHelper.setupDescriptiveComboBox(insectTypeCombo,
            val -> switch (val != null ? val : "") {
                case "ANT" -> I18nManager.getInstance().get("species.type.ant");
                case "BEE" -> I18nManager.getInstance().get("species.type.bee");
                case "WASP" -> I18nManager.getInstance().get("species.type.wasp");
                case "TERMITE" -> I18nManager.getInstance().get("species.type.termite");
                default -> I18nManager.getInstance().get("species.type.other");
            },
            val -> switch (val != null ? val : "") {
                case "ANT" -> I18nManager.getInstance().get("species.type.ant.desc");
                case "BEE" -> I18nManager.getInstance().get("species.type.bee.desc");
                case "WASP" -> I18nManager.getInstance().get("species.type.wasp.desc");
                case "TERMITE" -> I18nManager.getInstance().get("species.type.termite.desc");
                default -> I18nManager.getInstance().get("species.type.other.desc");
            }
        );
        insectTypeCombo.getSelectionModel().select("ANT");

        ComboBoxTooltipHelper.setupDescriptiveComboBox(categoryCombo,
            cat -> cat != null ? cat.label : "",
            cat -> cat != null ? cat.label : ""
        );
        categoryCombo.getSelectionModel().select(SpeciesCategory.EUSOCIAL_PRIMARY);
        categoryCombo.setOnAction(e -> {
            if (onValidate != null) onValidate.run();
        });

        Label categoryHintLabel = new Label("💡 This editor manages eusocial species (Ants, Bees, Wasps, Termites). Prey, predators, and commensals are managed in the dedicated 'Associated Species & Commensals' panel.");
        categoryHintLabel.getStyleClass().add("help-entry-desc");
        categoryHintLabel.setStyle("-fx-font-size: 11px; -fx-wrap-text: true;");

        HBox taxonLinks = new HBox(10);
        taxonLinks.setAlignment(Pos.CENTER_LEFT);
        Label lblLinksTitle = new Label(I18nManager.getInstance().get("species.taxonomy_sheet"));
        lblLinksTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");

        Hyperlink linkFormicidae = new Hyperlink("🐜 Formicidae (Ants)");
        linkFormicidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Formicidae"));

        Hyperlink linkApidae = new Hyperlink("🐝 Apidae (Bees)");
        linkApidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Apidae"));

        Hyperlink linkVespidae = new Hyperlink("🐝 Vespidae (Wasps)");
        linkVespidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Vespidae"));

        Hyperlink linkTermitoidae = new Hyperlink("🐜 Termitoidae (Termites)");
        linkTermitoidae.setOnAction(e -> openWiki("https://fr.wikipedia.org/wiki/Termite"));

        taxonLinks.getChildren().addAll(lblLinksTitle, linkFormicidae, linkApidae, linkVespidae, linkTermitoidae);

        descriptionArea.setPrefRowCount(4);

        grid.addRow(0, SpeciesUIHelper.createTooltipLabel("Common Name:", "Vernacular name of the species in common language.", commonNameField), commonNameField);
        grid.addRow(1, SpeciesUIHelper.createTooltipLabel("Scientific Name (Binomial):", "Official Latin binomial nomenclature (e.g., Lasius niger, Formica rufa, Atta cephalotes).", scientificNameField), scientificNameField);
        grid.addRow(2, SpeciesUIHelper.createTooltipLabel("Taxonomic Genus:", "Genus to which the species belongs (e.g., Lasius, Formica, Atta, Apis, Solenopsis, Vespula, Reticulitermes).", genusField), genusField);
        grid.addRow(3, SpeciesUIHelper.createTooltipLabel("Taxonomic Order / Family:", "Major taxonomic group of eusocial insects (Ant, Bee, Wasp, Termite).", insectTypeCombo), insectTypeCombo);
        grid.addRow(4, new Label(""), taxonLinks);
        grid.addRow(5, SpeciesUIHelper.createTooltipLabel("Ecological Role / Category:", "Trophic status and functional role in simulation ecosystem.", categoryCombo), categoryCombo);
        grid.addRow(6, new Label(""), categoryHintLabel);
        grid.addRow(7, SpeciesUIHelper.createTooltipLabel("Description & Ecological Notes:", "Summary of species biology, habitat, and behavior.", descriptionArea), descriptionArea);

        container = SpeciesUIHelper.wrapScroll(grid);

        if (onFieldEdited != null) {
            commonNameField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            scientificNameField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            genusField.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            insectTypeCombo.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            categoryCombo.valueProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
            descriptionArea.textProperty().addListener((obs, oldV, newV) -> onFieldEdited.run());
        }
    }

    private void openWiki(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            org.swarmforge.client.ui.GlossaryDialog.show();
        }
    }

    public ScrollPane getContainer() {
        return container;
    }

    public void populateFromSpecies(DefaultSpecies s) {
        if (s == null) return;
        commonNameField.setText(s.getCommonName() != null ? s.getCommonName() : "");
        scientificNameField.setText(s.getScientificName() != null ? s.getScientificName() : "");
        genusField.setText(s.getGenus() != null ? s.getGenus() : "");
        if (s.getInsectType() != null) {
            insectTypeCombo.getSelectionModel().select(s.getInsectType());
        }
        categoryCombo.getSelectionModel().select(s.getCategory() != null ? s.getCategory() : SpeciesCategory.EUSOCIAL_PRIMARY);
        descriptionArea.setText(s.getDescription() != null ? s.getDescription() : "");
    }

    public void applyToSpecies(DefaultSpecies s) {
        if (s == null) return;
        s.setCommonName(commonNameField.getText().trim());
        s.setScientificName(scientificNameField.getText().trim());
        s.setGenus(genusField.getText().trim());
        s.setInsectType(insectTypeCombo.getValue());
        s.setCategory(categoryCombo.getValue() != null ? categoryCombo.getValue() : SpeciesCategory.EUSOCIAL_PRIMARY);
        s.setDescription(descriptionArea.getText());
    }
}
