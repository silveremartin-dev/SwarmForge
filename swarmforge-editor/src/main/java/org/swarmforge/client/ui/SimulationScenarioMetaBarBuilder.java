/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.feather.Feather;
import org.swarmforge.client.util.I18nManager;
import org.swarmforge.core.scenario.AcademicScenarios;
import org.swarmforge.core.scenario.Scenario;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the top meta-scenario selection row, preset action buttons, and scenario description box.
 */
class SimulationScenarioMetaBarBuilder {

    static List<HBox> buildScenarioMetaRows(
            SimulationControlPanel panel,
            SimulationScenarioIoHelper ioHelper,
            SimulationConfigGridBuilder gridBuilder,
            I18nManager i18n,
            ComboBox<String> comboMeta,
            ScenarioPresetManager scenarioPresetManager,
            TextArea areaDescription
    ) {
        Button bSaveScenario = gridBuilder.createScenarioActionButton("common.btn.save", Feather.SAVE, "btn-secondary", "sim.btn.save.tt", ioHelper::handleSaveScenario);
        Button bDeleteScenario = gridBuilder.createScenarioActionButton("common.btn.delete", Feather.TRASH_2, "btn-danger", "sim.btn.delete.tt", ioHelper::handleDeleteScenario);
        Button bExportScenario = gridBuilder.createScenarioActionButton("common.btn.export", Feather.DOWNLOAD, "btn-secondary", "sim.btn.export.tt", ioHelper::handleExportScenario);
        Button bImportScenario = gridBuilder.createScenarioActionButton("common.btn.import", Feather.UPLOAD, "btn-secondary", "sim.btn.import.tt", ioHelper::handleImportScenario);

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label lblMeta = new Label();
        lblMeta.textProperty().bind(i18n.createStringBinding("sim.preset.meta"));
        lblMeta.getStyleClass().add("purple-accent-title");
        lblMeta.setMinWidth(Region.USE_PREF_SIZE);

        List<String> scenarioList = new ArrayList<>();
        scenarioList.add(i18n.get("sim.preset.custom_new", "[➕ Scénario Personnalisé (Création libre)]"));

        for (Scenario sc : AcademicScenarios.getAllAcademicScenarios(12345L)) {
            scenarioList.add("🔬 " + sc.getTitle());
        }
        for (Scenario sc : AcademicScenarios.getAllMultiplayerScenarios(12345L)) {
            scenarioList.add("⚔️ " + sc.getTitle());
        }
        for (String name : scenarioPresetManager.getPresetNames()) {
            if (!name.startsWith("ACAD_") && !name.startsWith("MP_") && !scenarioList.contains(name)
                    && !scenarioList.contains("🔬 " + name) && !scenarioList.contains("⚔️ " + name) && !scenarioList.contains("📌 " + name)) {
                scenarioList.add("📌 " + name);
            }
        }

        comboMeta.getItems().setAll(scenarioList);
        comboMeta.setEditable(false);
        comboMeta.setMaxWidth(Double.MAX_VALUE);
        comboMeta.setTooltip(new Tooltip(i18n.get("sim.preset.meta.tt", "Sélectionnez un scénario académique ou multijoueur, ou composez un scénario personnalisé.")));
        HBox.setHgrow(comboMeta, Priority.ALWAYS);
        comboMeta.setOnAction(e -> panel.applyMetaPreset(comboMeta.getValue()));

        metaRow.getChildren().addAll(lblMeta, comboMeta);

        HBox presetActionsRow = new HBox(8, bSaveScenario, bDeleteScenario, bExportScenario, bImportScenario);
        presetActionsRow.setAlignment(Pos.CENTER_LEFT);

        if (!comboMeta.getItems().isEmpty()) {
            comboMeta.getSelectionModel().select(comboMeta.getItems().size() > 1 ? 1 : 0);
            panel.applyMetaPreset(comboMeta.getValue());
        }

        return List.of(metaRow, presetActionsRow);
    }

    static VBox buildDescriptionBox(I18nManager i18n, TextArea areaDescription) {
        VBox descBox = new VBox(4);
        Label lblDesc = new Label();
        lblDesc.textProperty().bind(i18n.createStringBinding("sim.desc.title"));
        lblDesc.getStyleClass().addAll("sub-title-gray", "card-caption-title");
        areaDescription.setPrefRowCount(2);
        areaDescription.setPromptText("Scenario description and experimental objectives...");
        areaDescription.setWrapText(true);
        areaDescription.getStyleClass().add("compact-text-area");
        descBox.getChildren().addAll(lblDesc, areaDescription);
        return descBox;
    }
}
