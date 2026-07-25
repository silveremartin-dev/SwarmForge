/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.swarmforge.client.util.I18nManager;

/**
 * Universal Multilingual Glossary & Pedagogical Guide Dialog for SwarmForge.
 * Formatted cleanly for both Dark and Light themes.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class GlossaryDialog {

    public static void show() {
        I18nManager i18n = I18nManager.getInstance();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(i18n.get("glossary.dialog.title"));
        dialog.setHeaderText(i18n.get("glossary.dialog.header"));

        TabPane tabPane = new TabPane();
        tabPane.setPrefSize(750, 520);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Nest Architectures
        VBox vNest = new VBox(10); vNest.setPadding(new Insets(15));
        addEntry(vNest, i18n.get("glossary.nest.wax_comb.title"), i18n.get("glossary.nest.wax_comb.desc"));
        addEntry(vNest, i18n.get("glossary.nest.wax_pots.title"), i18n.get("glossary.nest.wax_pots.desc"));
        addEntry(vNest, i18n.get("glossary.nest.paper_pedunculate.title"), i18n.get("glossary.nest.paper_pedunculate.desc"));
        addEntry(vNest, i18n.get("glossary.nest.cathedral.title"), i18n.get("glossary.nest.cathedral.desc"));
        addEntry(vNest, i18n.get("glossary.nest.arboreal_silk.title"), i18n.get("glossary.nest.arboreal_silk.desc"));
        addEntry(vNest, i18n.get("glossary.nest.fungi_vault.title"), i18n.get("glossary.nest.fungi_vault.desc"));
        addEntry(vNest, i18n.get("glossary.nest.carton.title"), i18n.get("glossary.nest.carton.desc"));
        addEntry(vNest, i18n.get("glossary.nest.bamboo.title"), i18n.get("glossary.nest.bamboo.desc"));
        addEntry(vNest, i18n.get("glossary.nest.bivouac.title"), i18n.get("glossary.nest.bivouac.desc"));
        addEntry(vNest, i18n.get("glossary.nest.mound.title"), i18n.get("glossary.nest.mound.desc"));
        addEntry(vNest, i18n.get("glossary.nest.wood.title"), i18n.get("glossary.nest.wood.desc"));
        addEntry(vNest, i18n.get("glossary.nest.subterranean.title"), i18n.get("glossary.nest.subterranean.desc"));

        // Tab 2: Queens & Sociality
        VBox vSocial = new VBox(10); vSocial.setPadding(new Insets(15));
        addEntry(vSocial, i18n.get("glossary.social.queen_mode.title"), i18n.get("glossary.social.queen_mode.desc"));
        addEntry(vSocial, i18n.get("glossary.social.king.title"), i18n.get("glossary.social.king.desc"));
        addEntry(vSocial, i18n.get("glossary.social.nuptial.title"), i18n.get("glossary.social.nuptial.desc"));
        addEntry(vSocial, i18n.get("glossary.social.inhibition.title"), i18n.get("glossary.social.inhibition.desc"));

        // Tab 3: Environment & Soil
        VBox vEnv = new VBox(10); vEnv.setPadding(new Insets(15));
        addEntry(vEnv, i18n.get("glossary.env.moisture.title"), i18n.get("glossary.env.moisture.desc"));
        addEntry(vEnv, i18n.get("glossary.env.temperature.title"), i18n.get("glossary.env.temperature.desc"));
        addEntry(vEnv, i18n.get("glossary.env.co2.title"), i18n.get("glossary.env.co2.desc"));
        addEntry(vEnv, i18n.get("glossary.env.solar.title"), i18n.get("glossary.env.solar.desc"));
        addEntry(vEnv, i18n.get("glossary.env.magnetic.title"), i18n.get("glossary.env.magnetic.desc"));
        addEntry(vEnv, i18n.get("glossary.env.soil_layers.title"), i18n.get("glossary.env.soil_layers.desc"));

        // Tab 4: Behavioral Reasoning Engines
        VBox vReasoning = new VBox(10); vReasoning.setPadding(new Insets(15));
        addEntry(vReasoning, i18n.get("glossary.reasoning.fsm.title"), i18n.get("glossary.reasoning.fsm.desc"));
        addEntry(vReasoning, i18n.get("glossary.reasoning.fuzzy.title"), i18n.get("glossary.reasoning.fuzzy.desc"));
        addEntry(vReasoning, i18n.get("glossary.reasoning.bdi.title"), i18n.get("glossary.reasoning.bdi.desc"));
        addEntry(vReasoning, i18n.get("glossary.reasoning.nn.title"), i18n.get("glossary.reasoning.nn.desc"));
        addEntry(vReasoning, i18n.get("glossary.reasoning.blackboard.title"), i18n.get("glossary.reasoning.blackboard.desc"));
        addEntry(vReasoning, i18n.get("glossary.reasoning.bulk.title"), i18n.get("glossary.reasoning.bulk.desc"));

        // Tab 5: Sensors & Biomechanics
        VBox vSensors = new VBox(10); vSensors.setPadding(new Insets(15));
        addEntry(vSensors, i18n.get("glossary.biomech.subgenual.title"), i18n.get("glossary.biomech.subgenual.desc"));
        addEntry(vSensors, i18n.get("glossary.biomech.uv.title"), i18n.get("glossary.biomech.uv.desc"));
        addEntry(vSensors, i18n.get("glossary.biomech.mandible.title"), i18n.get("glossary.biomech.mandible.desc"));
        addEntry(vSensors, i18n.get("glossary.biomech.autothysis.title"), i18n.get("glossary.biomech.autothysis.desc"));
        addEntry(vSensors, i18n.get("glossary.biomech.arolia.title"), i18n.get("glossary.biomech.arolia.desc"));

        Tab tabNest = new Tab(i18n.get("glossary.tab.nest"), new ScrollPane(vNest));
        Tab tabSocial = new Tab(i18n.get("glossary.tab.social"), new ScrollPane(vSocial));
        Tab tabEnv = new Tab(i18n.get("glossary.tab.environment"), new ScrollPane(vEnv));
        Tab tabReasoning = new Tab(i18n.get("glossary.tab.reasoning"), new ScrollPane(vReasoning));
        Tab tabSensors = new Tab(i18n.get("glossary.tab.biomechanics"), new ScrollPane(vSensors));

        tabPane.getTabs().addAll(tabNest, tabSocial, tabEnv, tabReasoning, tabSensors);
        dialog.getDialogPane().setContent(tabPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private static void addEntry(VBox box, String title, String description) {
        Label t = new Label("• " + title + " : ");
        t.setStyle("-fx-font-weight: bold; -fx-text-fill: -fx-accent; -fx-min-width: 200px;");
        Label d = new Label(description);
        d.setWrapText(true);
        HBox row = new HBox(5, t, d);
        row.setPadding(new Insets(4, 0, 4, 0));
        box.getChildren().add(row);
    }
}
