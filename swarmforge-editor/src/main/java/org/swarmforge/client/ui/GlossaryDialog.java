/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.swarmforge.client.util.I18nManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Universal Multilingual Glossary & Pedagogical Guide Dialog for SwarmForge.
 * Entries are systematically sorted in alphabetical order per tab with expanded descriptions.
 * Formatted cleanly for both Dark and Light themes.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class GlossaryDialog {

    public static void show() {
        show(null);
    }

    public static void show(String searchTerm) {
        I18nManager i18n = I18nManager.getInstance();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(i18n.get("glossary.dialog.title", "Glossaire & Guide Pédagogique"));
        dialog.setHeaderText(i18n.get("glossary.dialog.header", "Glossaire des concepts scientifiques, écologiques et éthologiques"));

        TextField searchField = new TextField(searchTerm != null ? searchTerm : "");
        searchField.setPromptText("Rechercher une notion...");
        searchField.setPrefWidth(320);

        TabPane tabPane = new TabPane();
        tabPane.setPrefSize(840, 560);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Nest Architectures
        VBox vNest = createSortedTabBox(i18n, new String[][]{
            {"glossary.nest.wax_comb.title", "glossary.nest.wax_comb.desc"},
            {"glossary.nest.wax_pots.title", "glossary.nest.wax_pots.desc"},
            {"glossary.nest.paper_pedunculate.title", "glossary.nest.paper_pedunculate.desc"},
            {"glossary.nest.cathedral.title", "glossary.nest.cathedral.desc"},
            {"glossary.nest.arboreal_silk.title", "glossary.nest.arboreal_silk.desc"},
            {"glossary.nest.fungi_vault.title", "glossary.nest.fungi_vault.desc"},
            {"glossary.nest.carton.title", "glossary.nest.carton.desc"},
            {"glossary.nest.bamboo.title", "glossary.nest.bamboo.desc"},
            {"glossary.nest.bivouac.title", "glossary.nest.bivouac.desc"},
            {"glossary.nest.mound.title", "glossary.nest.mound.desc"},
            {"glossary.nest.wood.title", "glossary.nest.wood.desc"},
            {"glossary.nest.subterranean.title", "glossary.nest.subterranean.desc"},
            {"glossary.nest.subterranean_lime.title", "glossary.nest.subterranean_lime.desc"},
            {"glossary.nest.arboreal_carton.title", "glossary.nest.arboreal_carton.desc"}
        });

        // Tab 2: Queens & Sociality
        VBox vSocial = createSortedTabBox(i18n, new String[][]{
            {"glossary.social.queen_mode.title", "glossary.social.queen_mode.desc"},
            {"glossary.social.king.title", "glossary.social.king.desc"},
            {"glossary.social.nuptial.title", "glossary.social.nuptial.desc"},
            {"glossary.social.inhibition.title", "glossary.social.inhibition.desc"},
            {"glossary.social.trophallaxis.title", "glossary.social.trophallaxis.desc"},
            {"glossary.social.polyethism.title", "glossary.social.polyethism.desc"},
            {"glossary.social.stigmergy.title", "glossary.social.stigmergy.desc"}
        });

        // Tab 3: Environment & Soil
        VBox vEnv = createSortedTabBox(i18n, new String[][]{
            {"glossary.env.moisture.title", "glossary.env.moisture.desc"},
            {"glossary.env.temperature.title", "glossary.env.temperature.desc"},
            {"glossary.env.co2.title", "glossary.env.co2.desc"},
            {"glossary.env.solar.title", "glossary.env.solar.desc"},
            {"glossary.env.magnetic.title", "glossary.env.magnetic.desc"},
            {"glossary.env.soil_layers.title", "glossary.env.soil_layers.desc"},
            {"glossary.env.pressure.title", "glossary.env.pressure.desc"},
            {"glossary.env.trail_pheromones.title", "glossary.env.trail_pheromones.desc"}
        });

        // Tab 4: Behavioral Reasoning Engines
        VBox vReasoning = createSortedTabBox(i18n, new String[][]{
            {"glossary.reasoning.fsm.title", "glossary.reasoning.fsm.desc"},
            {"glossary.reasoning.fuzzy.title", "glossary.reasoning.fuzzy.desc"},
            {"glossary.reasoning.bdi.title", "glossary.reasoning.bdi.desc"},
            {"glossary.reasoning.nn.title", "glossary.reasoning.nn.desc"},
            {"glossary.reasoning.blackboard.title", "glossary.reasoning.blackboard.desc"},
            {"glossary.reasoning.bulk.title", "glossary.reasoning.bulk.desc"},
            {"glossary.reasoning.quorum.title", "glossary.reasoning.quorum.desc"}
        });

        // Tab 5: Sensors & Biomechanics
        VBox vSensors = createSortedTabBox(i18n, new String[][]{
            {"glossary.biomech.subgenual.title", "glossary.biomech.subgenual.desc"},
            {"glossary.biomech.uv.title", "glossary.biomech.uv.desc"},
            {"glossary.biomech.mandible.title", "glossary.biomech.mandible.desc"},
            {"glossary.biomech.autothysis.title", "glossary.biomech.autothysis.desc"},
            {"glossary.biomech.arolia.title", "glossary.biomech.arolia.desc"},
            {"glossary.biomech.antennal_olfaction.title", "glossary.biomech.antennal_olfaction.desc"}
        });

        Tab tabNest = new Tab(i18n.get("glossary.tab.nest"), new ScrollPane(vNest));
        Tab tabSocial = new Tab(i18n.get("glossary.tab.social"), new ScrollPane(vSocial));
        Tab tabEnv = new Tab(i18n.get("glossary.tab.environment"), new ScrollPane(vEnv));
        Tab tabReasoning = new Tab(i18n.get("glossary.tab.reasoning"), new ScrollPane(vReasoning));
        Tab tabSensors = new Tab(i18n.get("glossary.tab.biomechanics"), new ScrollPane(vSensors));

        tabPane.getTabs().addAll(tabNest, tabSocial, tabEnv, tabReasoning, tabSensors);

        if (searchTerm != null && !searchTerm.isBlank()) {
            String lowerTerm = searchTerm.toLowerCase();
            if (lowerTerm.contains("nest") || lowerTerm.contains("nid") || lowerTerm.contains("wax") || lowerTerm.contains("cire") || lowerTerm.contains("mound")) {
                tabPane.getSelectionModel().select(tabNest);
            } else if (lowerTerm.contains("queen") || lowerTerm.contains("reine") || lowerTerm.contains("social") || lowerTerm.contains("king") || lowerTerm.contains("roi") || lowerTerm.contains("nuptial") || lowerTerm.contains("troph") || lowerTerm.contains("stig")) {
                tabPane.getSelectionModel().select(tabSocial);
            } else if (lowerTerm.contains("env") || lowerTerm.contains("temp") || lowerTerm.contains("press") || lowerTerm.contains("sol") || lowerTerm.contains("micro") || lowerTerm.contains("phero")) {
                tabPane.getSelectionModel().select(tabEnv);
            } else if (lowerTerm.contains("fsm") || lowerTerm.contains("bdi") || lowerTerm.contains("décision") || lowerTerm.contains("reason") || lowerTerm.contains("quorum")) {
                tabPane.getSelectionModel().select(tabReasoning);
            } else if (lowerTerm.contains("subgenual") || lowerTerm.contains("vibration") || lowerTerm.contains("uv") || lowerTerm.contains("autothys") || lowerTerm.contains("arolia") || lowerTerm.contains("mandib") || lowerTerm.contains("olfac")) {
                tabPane.getSelectionModel().select(tabSensors);
            }
        }

        VBox contentBox = new VBox(12);
        Label searchLabel = new Label("🔍 Rechercher dans le Glossaire :");
        searchLabel.setTooltip(new Tooltip("Filtrez ou recherchez un terme scientifique ou éthologique dans le glossaire."));
        searchField.setTooltip(new Tooltip("Entrez un terme pour filtrer les catégories du glossaire."));
        HBox searchRow = new HBox(10, searchLabel, searchField);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        contentBox.getChildren().addAll(searchRow, tabPane);

        dialog.getDialogPane().setContent(contentBox);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private static VBox createSortedTabBox(I18nManager i18n, String[][] keyPairs) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(15));

        List<GlossaryEntry> entries = new ArrayList<>();
        for (String[] pair : keyPairs) {
            String titleKey = pair[0];
            String descKey = pair[1];
            String title = i18n.get(titleKey, titleKey);
            String desc = i18n.get(descKey, descKey);
            entries.add(new GlossaryEntry(title, desc));
        }

        // Alphabetical sort by localized title systematically
        entries.sort(Comparator.comparing(e -> e.title.toLowerCase()));

        for (GlossaryEntry entry : entries) {
            addEntry(box, entry.title, entry.description);
        }

        return box;
    }

    private static void addEntry(VBox box, String title, String description) {
        VBox entryCard = new VBox(4);
        entryCard.getStyleClass().add("card-pane");
        entryCard.setPadding(new Insets(10));

        Label t = new Label("• " + title);
        t.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: -fx-accent;");
        t.setTooltip(new Tooltip("Notion scientifique : " + title));

        Label d = new Label(description);
        d.setWrapText(true);
        d.setStyle("-fx-font-size: 12px; -fx-line-spacing: 3px;");

        entryCard.getChildren().addAll(t, d);
        box.getChildren().add(entryCard);
    }

    private static class GlossaryEntry {
        final String title;
        final String description;

        GlossaryEntry(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }
}
