/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;
import org.swarmforge.core.scenario.Scenario;
import org.swarmforge.core.species.DefaultSpecies;

import java.util.Map;
import java.util.Optional;

/**
 * Handles scenario preset creation, persistence, validation, and deletion workflows
 * for {@link SimulationControlPanel}.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class ScenarioPresetActionHelper {

    private ScenarioPresetActionHelper() {
    }

    static void handleSaveScenario(SimulationControlPanel panel) {
        String desc = panel.areaDescription.getText() != null ? panel.areaDescription.getText().trim() : "";
        if (desc.isEmpty()) {
            ThemeManager.createAlert(
                Alert.AlertType.WARNING,
                "Description required: Please enter a scientific description in the 'Scientific Scenario Description' area before saving the preset."
            ).show();
            return;
        }

        String name = panel.comboMeta.getValue() != null ? panel.comboMeta.getValue().replace("🔬 ", "").replace("⚔️ ", "").replace("📌 ", "") : "New Scenario";
        TextInputDialog dialog = ThemeManager.createTextInputDialog(name);
        dialog.setTitle("Save Scenario Preset");
        dialog.setHeaderText("Scenario preset name (Description captured):");
        dialog.showAndWait().ifPresent(scenarioName -> {
            if (!scenarioName.trim().isEmpty()) {
                String clean = scenarioName.trim();
                String itemTitle = "📌 " + clean;
                if ((panel.scenarioPresetManager != null && panel.scenarioPresetManager.getPresetNames().contains(clean)) || panel.comboMeta.getItems().contains(itemTitle)) {
                    Alert confirmAlert = ThemeManager.createAlert(
                        Alert.AlertType.CONFIRMATION,
                        "Scenario preset '" + clean + "' already exists.\n\nDo you want to overwrite it with current configuration?"
                    );
                    confirmAlert.setTitle("Overwrite Existing Scenario");
                    confirmAlert.setHeaderText("Overwrite Confirmation");
                    Optional<ButtonType> res = confirmAlert.showAndWait();
                    if (res.isEmpty() || res.get() != ButtonType.OK) {
                        return;
                    }
                }

                Scenario sc = new Scenario(clean.toLowerCase().replace(" ", "_"), clean, desc);
                sc.setAuthor(panel.getPlayerAlias());
                sc.setMasterSeed(panel.getMasterSeed());
                sc.setBiomeName(panel.getSelectedWorld());
                sc.setMaxDurationValue(panel.maxDurationSpinner.getValue() != null ? panel.maxDurationSpinner.getValue() : 100.0);
                sc.setMaxDurationUnit(panel.durationUnitCombo.getValue() != null ? panel.durationUnitCombo.getValue() : "Days (d)");
                sc.setMinPopulationStopThreshold(panel.getMinPopulationStopThreshold());
                sc.setSimulationStepSeconds(panel.getSimulationStepSeconds());
                sc.setMultiplayerOnly(panel.chkMultiplayerOnly.isSelected());
                sc.setRequiredPlayerCount(panel.spinRequiredPlayers.getValue() != null ? panel.spinRequiredPlayers.getValue() : 1);
                sc.setGridTilesX(panel.spinGridTilesX.getValue() != null ? panel.spinGridTilesX.getValue() : 1);
                sc.setGridTilesY(panel.spinGridTilesY.getValue() != null ? panel.spinGridTilesY.getValue() : 1);
                for (SpeciesConfigCard card : panel.speciesCardList) {
                    String spName = card.getSpeciesName();
                    sc.addColony(new Scenario.ColonySetup(spName, spName.toLowerCase().replace(" ", "_"), card.getQueenCount(), card.getWorkerCount(), card.getSoldierCount(), card.getInitialFood(), Map.of()));
                    if (panel.speciesPresetManager != null) {
                        DefaultSpecies sp = panel.speciesPresetManager.getPresetOrFallback(spName);
                        if (sp != null) {
                            sc.embedSpecies(sp);
                        }
                    }
                }

                if (panel.worldPresetManager != null && panel.getSelectedWorld() != null && panel.worldPresetManager.contains(panel.getSelectedWorld())) {
                    sc.setEmbeddedWorldConfig(panel.worldPresetManager.get(panel.getSelectedWorld()));
                }
                if (panel.weatherPresetManager != null && panel.getSelectedWeather() != null && panel.weatherPresetManager.contains(panel.getSelectedWeather())) {
                    sc.setEmbeddedClimateConfig(panel.weatherPresetManager.get(panel.getSelectedWeather()));
                }

                panel.scenarioPresetManager.save(sc);

                if (!panel.comboMeta.getItems().contains(itemTitle)) panel.comboMeta.getItems().add(itemTitle);
                panel.comboMeta.getSelectionModel().select(itemTitle);
                NotificationOverlay.show(panel, "Scenario preset saved: " + clean + " (v" + sc.getVersion() + ")", NotificationOverlay.NotificationType.SUCCESS);
            }
        });
    }

    static void handleDeleteScenario(SimulationControlPanel panel) {
        String selected = panel.comboMeta.getValue();
        if (selected == null || selected.isEmpty()) return;
        String clean = selected.replace("🔬 ", "").replace("⚔️ ", "").replace("📌 ", "").trim();
        Alert alert = ThemeManager.createAlert(
            Alert.AlertType.CONFIRMATION,
            "Are you sure you want to delete scenario preset '" + selected + "'?"
        );
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean deleted = panel.scenarioPresetManager.delete(clean);
                if (deleted) {
                    panel.comboMeta.getItems().remove(selected);
                    if (!panel.comboMeta.getItems().isEmpty()) panel.comboMeta.getSelectionModel().selectFirst();
                    NotificationOverlay.show(panel, "Scenario deleted: " + clean, NotificationOverlay.NotificationType.INFO);
                } else {
                    ThemeManager.createAlert(
                        Alert.AlertType.INFORMATION,
                        "Built-in Academic or Multiplayer reference scenarios are protected and cannot be deleted."
                    ).show();
                }
            }
        });
    }
}
