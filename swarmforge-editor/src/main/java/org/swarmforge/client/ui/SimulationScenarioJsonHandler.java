/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import org.swarmforge.client.util.NotificationOverlay;
import org.swarmforge.client.util.ThemeManager;

import java.io.File;
import java.time.LocalDateTime;

/**
 * Handles JSON file import and export serialization for scenario setup configurations.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
final class SimulationScenarioJsonHandler {

    private SimulationScenarioJsonHandler() {}

    static void handleExportScenario(SimulationControlPanel panel) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Scenario Configuration");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        chooser.setInitialFileName("scenario-preset.json");
        File file = chooser.showSaveDialog(panel.getScene().getWindow());
        if (file != null) {
            try {
                ScenarioSetupSnapshot snapshot = panel.captureSetupSnapshotOnFXThread();
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                mapper.writeValue(file, snapshot);
                NotificationOverlay.show(
                    panel,
                    "Scenario successfully exported to " + file.getName(),
                    NotificationOverlay.NotificationType.SUCCESS
                );
            } catch (Exception ex) {
                ThemeManager.createAlert(
                    Alert.AlertType.ERROR,
                    "Error exporting scenario: " + ex.getMessage()
                ).show();
            }
        }
    }

    static void handleImportScenario(SimulationControlPanel panel) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Import Scenario Configuration");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        File file = chooser.showOpenDialog(panel.getScene().getWindow());
        if (file != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ScenarioSetupSnapshot snapshot = mapper.readValue(file, ScenarioSetupSnapshot.class);
                if (snapshot != null) {
                    if (snapshot.seed() != 0) {
                        panel.txtSeed.setText(String.valueOf(snapshot.seed()));
                    }
                    if (snapshot.startDateTimeIso() != null && !snapshot.startDateTimeIso().isEmpty()) {
                        try {
                            panel.startDateTime = LocalDateTime.parse(snapshot.startDateTimeIso());
                            if (panel.startDatePicker != null) panel.startDatePicker.setValue(panel.startDateTime.toLocalDate());
                            if (panel.startTimeHourSpinner != null) panel.startTimeHourSpinner.getValueFactory().setValue(panel.startDateTime.getHour());
                            if (panel.startTimeMinuteSpinner != null) panel.startTimeMinuteSpinner.getValueFactory().setValue(panel.startDateTime.getMinute());
                            if (panel.startTimeSecondSpinner != null) panel.startTimeSecondSpinner.getValueFactory().setValue(panel.startDateTime.getSecond());
                            panel.updateTick(panel.currentTick, panel.maxTick);
                        } catch (Exception ignored) {}
                    }
                    if (snapshot.simulationStepSeconds() > 0) {
                        panel.simulationStepSeconds = snapshot.simulationStepSeconds();
                        selectStepComboByDt(panel, panel.simulationStepSeconds);
                        panel.updateStepDtLabel();
                    }
                    if (snapshot.maxDurationValue() > 0 && panel.maxDurationSpinner != null) {
                        panel.maxDurationSpinner.getValueFactory().setValue(snapshot.maxDurationValue());
                    }
                    if (snapshot.maxDurationUnit() != null && panel.durationUnitCombo != null) {
                        SimulationScenarioIoHelper.selectComboIfPresent(panel.durationUnitCombo, snapshot.maxDurationUnit());
                    }
                    if (snapshot.minPopStopThreshold() >= 0 && panel.minPopStopSpinner != null) {
                        panel.minPopStopSpinner.getValueFactory().setValue(snapshot.minPopStopThreshold());
                    }
                    if (snapshot.description() != null && panel.areaDescription != null) {
                        panel.areaDescription.setText(snapshot.description());
                    }
                    if (snapshot.selectedWorld() != null) SimulationScenarioIoHelper.selectComboIfPresent(panel.comboWorld, snapshot.selectedWorld());
                    if (snapshot.selectedWeather() != null) SimulationScenarioIoHelper.selectComboIfPresent(panel.comboWeather, snapshot.selectedWeather());

                    if (panel.chkMultiplayerOnly != null) {
                        panel.chkMultiplayerOnly.setSelected(snapshot.isMultiplayerOnly());
                    }
                    if (panel.spinRequiredPlayers != null && snapshot.requiredPlayerCount() > 0) {
                        panel.spinRequiredPlayers.getValueFactory().setValue(snapshot.requiredPlayerCount());
                    }
                    if (panel.spinGridTilesX != null && snapshot.gridTilesX() > 0) {
                        panel.spinGridTilesX.getValueFactory().setValue(snapshot.gridTilesX());
                    }
                    if (panel.spinGridTilesY != null && snapshot.gridTilesY() > 0) {
                        panel.spinGridTilesY.getValueFactory().setValue(snapshot.gridTilesY());
                    }
                    if (snapshot.engineBackend() != null && panel.engineBackendCombo != null) {
                        SimulationScenarioIoHelper.selectComboIfPresent(panel.engineBackendCombo, snapshot.engineBackend());
                    }
                    if (snapshot.computeAcceleration() != null && panel.computeAccelerationCombo != null) {
                        SimulationScenarioIoHelper.selectComboIfPresent(panel.computeAccelerationCombo, snapshot.computeAcceleration());
                    }

                    panel.speciesCardList.clear();
                    if (snapshot.speciesSnapshots() != null && !snapshot.speciesSnapshots().isEmpty()) {
                        for (SpeciesConfigSnapshot sSnap : snapshot.speciesSnapshots()) {
                            panel.addSpeciesCard(sSnap.speciesName());
                            if (!panel.speciesCardList.isEmpty()) {
                                SpeciesConfigCard card = panel.speciesCardList.get(panel.speciesCardList.size() - 1);
                                if (sSnap.nestType() != null) card.setNestType(sSnap.nestType());
                                card.setQueenCount(sSnap.queenCount());
                                card.setWorkerCount(sSnap.workerCount());
                                card.setSoldierCount(sSnap.soldierCount());
                                card.setInitialFood(sSnap.initialFood());
                                if (sSnap.placementStrategy() != null) card.setNestPlacement(sSnap.placementStrategy());
                                card.setCustomX(sSnap.customX());
                                card.setCustomZ(sSnap.customZ());
                                if (sSnap.nestRelation() != null) card.setNestRelation(sSnap.nestRelation());
                                if (sSnap.accessorySnapshots() != null) {
                                    card.setAccessorySnapshots(sSnap.accessorySnapshots());
                                }
                            }
                        }
                    }
                    panel.refreshSpeciesListContainer();
                    panel.updateDurationCalculatedLabel();
                    panel.updateValidationPanel();
                    NotificationOverlay.show(
                        panel,
                        "Scenario successfully imported from " + file.getName(),
                        NotificationOverlay.NotificationType.SUCCESS
                    );
                }
            } catch (Exception ex) {
                ThemeManager.createAlert(
                    Alert.AlertType.ERROR,
                    "Error importing scenario: " + ex.getMessage()
                ).show();
            }
        }
    }

    private static void selectStepComboByDt(SimulationControlPanel panel, float dt) {
        if (panel.scenarioStepCombo == null) return;
        if (Math.abs(dt - 0.0166f) < 0.005f) {
            panel.scenarioStepCombo.getSelectionModel().select(0);
        } else if (Math.abs(dt - 0.05f) < 0.01f) {
            panel.scenarioStepCombo.getSelectionModel().select(1);
        } else if (Math.abs(dt - 0.1f) < 0.01f) {
            panel.scenarioStepCombo.getSelectionModel().select(2);
        } else if (Math.abs(dt - 1.0f) < 0.1f) {
            panel.scenarioStepCombo.getSelectionModel().select(3);
        } else if (Math.abs(dt - 5.0f) < 0.5f) {
            panel.scenarioStepCombo.getSelectionModel().select(4);
        }
    }
}
