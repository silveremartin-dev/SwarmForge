/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.kordamp.ikonli.javafx.FontIcon;
import org.swarmforge.client.util.I18nManager;

/**
 * Builds the ecological compatibility and species adaptation cards
 * for {@link WorldEditorPane}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class WorldEditorDiagnosticsCardBuilder {

    private WorldEditorDiagnosticsCardBuilder() {
    }

    static VBox buildDiagBlock(WorldEditorPane pane) {
        pane.lblBioclimaticZoneBadge.getStyleClass().add("accent-title");

        GridPane diagGrid = new GridPane();
        diagGrid.setHgap(10); diagGrid.setVgap(4);
        diagGrid.getStyleClass().add("diag-card");

        pane.lblAttaCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #4ade80;");
        pane.lblAphidCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8;");
        pane.lblWoodNestCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f59e0b;");
        pane.lblAcaciaAntCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #e879f9;");
        pane.lblCactusAntCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f43f5e;");
        pane.lblPogonomyrmexCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #eab308;");
        pane.lblTermiteCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #a3e635;");
        pane.lblApisCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #fbbf24;");
        pane.lblVespulaCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #f97316;");
        pane.lblSolenopsisCompatScore.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");

        Label lblDiagZone = new Label(); lblDiagZone.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.zone"));
        Label lblDiagAtta = new Label(); lblDiagAtta.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.atta"));
        Label lblDiagAphid = new Label(); lblDiagAphid.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.aphid"));
        Label lblDiagWood = new Label(); lblDiagWood.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.wood"));
        Label lblDiagPogo = new Label(); lblDiagPogo.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.pogonomyrmex"));
        Label lblDiagSole = new Label(); lblDiagSole.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.solenopsis"));

        diagGrid.add(lblDiagZone, 0, 0); diagGrid.add(pane.lblBioclimaticZoneBadge, 1, 0);
        diagGrid.add(lblDiagAtta, 0, 1); diagGrid.add(pane.lblAttaCompatScore, 1, 1);
        diagGrid.add(lblDiagAphid, 0, 2); diagGrid.add(pane.lblAphidCompatScore, 1, 2);
        diagGrid.add(lblDiagWood, 0, 3); diagGrid.add(pane.lblWoodNestCompatScore, 1, 3);
        diagGrid.add(new Label("🌵 Pseudomyrmex (Acacia) :"), 0, 4); diagGrid.add(pane.lblAcaciaAntCompatScore, 1, 4);
        diagGrid.add(new Label("🏜️ Desert Ants (Cactus) :"), 0, 5); diagGrid.add(pane.lblCactusAntCompatScore, 1, 5);
        diagGrid.add(lblDiagPogo, 0, 6); diagGrid.add(pane.lblPogonomyrmexCompatScore, 1, 6);
        diagGrid.add(new Label("🪵 Reticulitermes (Termites) :"), 0, 7); diagGrid.add(pane.lblTermiteCompatScore, 1, 7);
        diagGrid.add(new Label("🐝 Apis mellifera (Honey Bees) :"), 0, 8); diagGrid.add(pane.lblApisCompatScore, 1, 8);
        diagGrid.add(new Label("🐝 Vespula vulgaris (Wasps) :"), 0, 9); diagGrid.add(pane.lblVespulaCompatScore, 1, 9);
        diagGrid.add(lblDiagSole, 0, 10); diagGrid.add(pane.lblSolenopsisCompatScore, 1, 10);

        WorldEditorGeoAndBioclimateHelper.updateEcologicalCompatibilityScores(pane);
        Label lblDiagTitle = new Label(); lblDiagTitle.textProperty().bind(I18nManager.getInstance().createStringBinding("world.diag.title"));
        return new VBox(8, lblDiagTitle, diagGrid);
    }

    static VBox buildAdaptBlock(WorldEditorPane pane) {
        VBox adaptBox = new VBox(6); adaptBox.getStyleClass().add("diag-card");
        Label lblAdapt = new Label(); lblAdapt.textProperty().bind(I18nManager.getInstance().createStringBinding("world.adapt.title"));
        lblAdapt.getStyleClass().add("purple-title");
        pane.speciesAdaptCombo = new ComboBox<>();
        pane.speciesAdaptCombo.getItems().addAll(
            "🐝 Apis mellifera (Honey Bee - Flowery Meadow & Nectar)",
            "🍃 Atta sexdens (Leafcutter Ant - Tropical Rainforest)",
            "🐜 Camponotus ligniperda (Carpenter Ant - Oak Forest & Dead Wood)",
            "🌵 Cataglyphis bombycina (Sahara Silver Ant - Arid Sahara Desert)",
            "🌲 Formica rufa (Red Wood Ant - Taiga / Pine Forest)",
            "🍯 Lasius niger (Black Garden Ant - Temperate Forest)",
            "🌾 Messor barbarus (Harvester Ant - Semi-Arid Steppe)",
            "🌵 Pseudomyrmex gracilis (Acacia Ant - Tropical Savanna)",
            "🕳️ Reticulitermes lucifugus (Termite - Decaying Wood & Caves)",
            "🔥 Solenopsis invicta (Red Imported Fire Ant - Humid Plains & Savanna)"
        );
        FXCollections.sort(pane.speciesAdaptCombo.getItems());
        pane.speciesAdaptCombo.getSelectionModel().selectFirst();
        pane.speciesAdaptCombo.setMaxWidth(Double.MAX_VALUE);

        Button btnAdapt = new Button(); btnAdapt.textProperty().bind(I18nManager.getInstance().createStringBinding("world.adapt.btn"));
        btnAdapt.setMaxWidth(Double.MAX_VALUE); btnAdapt.getStyleClass().add("btn-primary");
        btnAdapt.setGraphic(new FontIcon(Feather.CPU)); btnAdapt.tooltipProperty().bind(I18nManager.getInstance().createTooltipBinding("world.adapt.btn.tt"));
        btnAdapt.setOnAction(e -> WorldEditorGeoAndBioclimateHelper.handleAdaptWorldToSpecies(pane));

        pane.lblAdaptStatus = new Label(); pane.lblAdaptStatus.textProperty().bind(I18nManager.getInstance().createStringBinding("world.adapt.status"));
        pane.lblAdaptStatus.setStyle("-fx-font-size: 10px; -fx-text-fill: #4ade80;");
        adaptBox.getChildren().addAll(lblAdapt, pane.speciesAdaptCombo, btnAdapt, pane.lblAdaptStatus);
        return adaptBox;
    }
}
