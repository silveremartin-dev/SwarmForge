/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.swarmforge.client.util.I18nManager;

import java.util.Random;

/**
 * Builds the structural biotope and vegetation distribution card
 * for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorStructuresCardBuilder {

    private WorldEditorStructuresCardBuilder() {
    }

    static VBox buildStructBlock(WorldEditorPane pane) {
        pane.structSeedField = new TextField("555123");
        pane.structSeedField.setPrefWidth(90);
        pane.structSeedField.setOnAction(e -> pane.regenerateAndRepaint());
        pane.structSeedField.textProperty().addListener((o, a, b) -> pane.regenerateAndRepaint());

        Button btnRandomStruct = new Button("🎲 Seed");
        btnRandomStruct.setStyle("-fx-background-color: #7c3aed; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
        btnRandomStruct.setOnAction(e -> {
            pane.structSeedField.setText(String.valueOf(new Random().nextLong(100000, 9999999)));
            pane.regenerateAndRepaint();
        });

        Label lblSeedStruct = new Label();
        lblSeedStruct.textProperty().bind(I18nManager.getInstance().createStringBinding("world.seed.struct"));
        lblSeedStruct.setStyle("-fx-text-fill: #aaa; -fx-font-size: 11px;");
        HBox seedBox = new HBox(6, lblSeedStruct, pane.structSeedField, btnRandomStruct);
        seedBox.setAlignment(Pos.CENTER_LEFT);

        pane.treeCountSlider = pane.mkSlider(0, 200, 15);
        pane.treeCountSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.struct.tree_count.tt"));
        pane.hollowLogsSlider = pane.mkSlider(0, 8, 3);
        pane.hollowLogsSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.struct.hollow_logs.tt"));
        pane.rockCrevicesSlider = pane.mkSlider(0, 8, 3);
        pane.rockCrevicesSlider.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.struct.rock_crevices.tt"));
        pane.addLsn(pane.treeCountSlider, pane.hollowLogsSlider);
        pane.addFloraLsn(pane.rockCrevicesSlider);

        pane.comboTreeSpecies = new ComboBox<>();
        pane.comboTreeSpecies.getItems().addAll(
            "🌳 Oak & Hardwood Forest Biome (Atta / Camponotus)",
            "🌲 Pine Forest Biome (Formica / Cinara Aphids)",
            "🌵 Acacia Savannah Biome (Pseudomyrmex / Nectaries)",
            "🏜️ Desert & Saguaro Cactus Biome (Myrmecocystus / Cephalotes)",
            "🌿 Birch & Grasses Biome (Messor / Foraging)",
            "🎋 Bamboo Grove Biome (Temnothorax / Colobopsis)",
            "🪵 Dead Wood & Rotting Stumps Biome"
        );
        pane.comboTreeSpecies.getSelectionModel().selectFirst();
        pane.comboTreeSpecies.setPrefWidth(270);
        pane.comboTreeSpecies.setOnAction(e -> WorldEditorGeoAndBioclimateHelper.updateTreeSpeciesSpinnersFromPreset(pane));

        pane.oakPctSpinner      = pane.mkSpinner(0, 100, 60);
        pane.pinePctSpinner     = pane.mkSpinner(0, 100, 20);
        pane.acaciaPctSpinner   = pane.mkSpinner(0, 100, 0);
        pane.birchPctSpinner    = pane.mkSpinner(0, 100, 10);
        pane.bambooPctSpinner   = pane.mkSpinner(0, 100, 0);
        pane.cactusPctSpinner   = pane.mkSpinner(0, 100, 0);
        pane.deadWoodPctSpinner = pane.mkSpinner(0, 100, 10);

        for (Spinner<Integer> sp : new Spinner[]{pane.oakPctSpinner, pane.pinePctSpinner, pane.acaciaPctSpinner, pane.birchPctSpinner, pane.bambooPctSpinner, pane.cactusPctSpinner, pane.deadWoodPctSpinner}) {
            sp.valueProperty().addListener((o, a, b) -> {
                WorldEditorGeoAndBioclimateHelper.updateEcologicalCompatibilityScores(pane);
                pane.syncTreeSpeciesTo3DView();
                pane.repaintAllViews();
            });
        }

        GridPane botGrid = new GridPane();
        botGrid.setHgap(8); botGrid.setVgap(6);

        Label lblOak      = new Label(); lblOak.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.oak_pct"));
        Label lblPine     = new Label(); lblPine.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.pine_pct"));
        Label lblAcacia   = new Label(); lblAcacia.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.acacia_pct"));
        Label lblCactus   = new Label(); lblCactus.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.cactus_pct"));
        Label lblBirch    = new Label(); lblBirch.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.birch_pct"));
        Label lblBamboo   = new Label(); lblBamboo.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.bamboo_pct"));
        Label lblDeadwood = new Label(); lblDeadwood.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.deadwood_pct"));

        botGrid.add(lblOak, 0, 0);      botGrid.add(pane.oakPctSpinner, 1, 0);
        botGrid.add(lblPine, 0, 1);     botGrid.add(pane.pinePctSpinner, 1, 1);
        botGrid.add(lblAcacia, 0, 2);   botGrid.add(pane.acaciaPctSpinner, 1, 2);
        botGrid.add(lblCactus, 0, 3);   botGrid.add(pane.cactusPctSpinner, 1, 3);
        botGrid.add(lblBirch, 0, 4);    botGrid.add(pane.birchPctSpinner, 1, 4);
        botGrid.add(lblBamboo, 0, 5);   botGrid.add(pane.bambooPctSpinner, 1, 5);
        botGrid.add(lblDeadwood, 0, 6); botGrid.add(pane.deadWoodPctSpinner, 1, 6);

        Label lblTreeCount       = new Label(); lblTreeCount.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.tree_count"));
        Label lblHollowLogs      = new Label(); lblHollowLogs.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.hollow_logs"));
        Label lblRockCrevices    = new Label(); lblRockCrevices.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.rock_crevices"));
        Label lblDominantSpecies = new Label(); lblDominantSpecies.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.dominant_species"));
        Label lblMatrixHeader    = new Label(); lblMatrixHeader.textProperty().bind(I18nManager.getInstance().createStringBinding("world.struct.matrix_header"));

        return new VBox(8,
                seedBox,
                lblTreeCount, pane.sv(pane.treeCountSlider, ""),
                lblHollowLogs, pane.sv(pane.hollowLogsSlider, ""),
                lblRockCrevices, pane.sv(pane.rockCrevicesSlider, ""),
                new Separator(),
                lblDominantSpecies,
                pane.comboTreeSpecies,
                lblMatrixHeader,
                botGrid
        );
    }
}
