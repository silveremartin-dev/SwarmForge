/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Immutable configuration snapshot defining the complete initialization parameters for a simulation scenario.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public record ScenarioSetupSnapshot(
    long seed,
    String startDateTimeIso,
    float simulationStepSeconds,
    double maxDurationValue,
    String maxDurationUnit,
    int minPopStopThreshold,
    String description,
    String selectedWorld,
    String selectedWeather,
    String selectedSpecies,
    String selectedNestType,
    int queenCount,
    int workerCount,
    int soldierCount,
    int broodCount,
    List<SpeciesConfigSnapshot> speciesSnapshots,
    boolean isMultiplayerOnly,
    int requiredPlayerCount,
    int gridTilesX,
    int gridTilesY,
    String engineBackend,
    String computeAcceleration
) {
    public ScenarioSetupSnapshot(
        long seed,
        String selectedWorld,
        String selectedWeather,
        String selectedSpecies,
        String selectedNestType,
        int queenCount,
        int workerCount,
        int soldierCount,
        int broodCount,
        List<SpeciesConfigSnapshot> speciesSnapshots
    ) {
        this(seed, null, 0.0166f, 100.0, "Days (d)", 0, "", selectedWorld, selectedWeather, selectedSpecies, selectedNestType, queenCount, workerCount, soldierCount, broodCount, speciesSnapshots, false, 1, 1, 1, "AUTO", "AUTO");
    }

    public ScenarioSetupSnapshot(
        long seed,
        String startDateTimeIso,
        float simulationStepSeconds,
        double maxDurationValue,
        String maxDurationUnit,
        int minPopStopThreshold,
        String description,
        String selectedWorld,
        String selectedWeather,
        String selectedSpecies,
        String selectedNestType,
        int queenCount,
        int workerCount,
        int soldierCount,
        int broodCount,
        List<SpeciesConfigSnapshot> speciesSnapshots
    ) {
        this(seed, startDateTimeIso, simulationStepSeconds, maxDurationValue, maxDurationUnit, minPopStopThreshold, description, selectedWorld, selectedWeather, selectedSpecies, selectedNestType, queenCount, workerCount, soldierCount, broodCount, speciesSnapshots, false, 1, 1, 1, "AUTO", "AUTO");
    }

    /**
     * Captures a complete scenario setup snapshot from the specified UI control panel on the FX thread.
     *
     * @param panel the UI control panel to inspect
     * @return a new immutable {@link ScenarioSetupSnapshot}
     */
    public static ScenarioSetupSnapshot capture(SimulationControlPanel panel) {
        List<SpeciesConfigSnapshot> snapshots = new ArrayList<>();
        for (SpeciesConfigCard card : panel.speciesCardList) {
            snapshots.add(card.getSnapshot());
        }
        String dtIso = panel.startDateTime != null ? panel.startDateTime.toString() : LocalDateTime.now().toString();
        double maxVal = panel.maxDurationSpinner != null && panel.maxDurationSpinner.getValue() != null ? panel.maxDurationSpinner.getValue() : 100.0;
        String maxUnit = panel.durationUnitCombo != null && panel.durationUnitCombo.getValue() != null ? panel.durationUnitCombo.getValue() : "Days (d)";
        int minPop = panel.minPopStopSpinner != null && panel.minPopStopSpinner.getValue() != null ? panel.minPopStopSpinner.getValue() : 0;
        String desc = panel.areaDescription != null ? panel.areaDescription.getText() : "";
        boolean mpOnly = panel.chkMultiplayerOnly != null && panel.chkMultiplayerOnly.isSelected();
        int reqPlayers = panel.spinRequiredPlayers != null && panel.spinRequiredPlayers.getValue() != null ? panel.spinRequiredPlayers.getValue() : 1;
        int tilesX = panel.spinGridTilesX != null && panel.spinGridTilesX.getValue() != null ? panel.spinGridTilesX.getValue() : 1;
        int tilesY = panel.spinGridTilesY != null && panel.spinGridTilesY.getValue() != null ? panel.spinGridTilesY.getValue() : 1;
        String engineBackend = panel.engineBackendCombo != null && panel.engineBackendCombo.getValue() != null ? panel.engineBackendCombo.getValue() : "AUTO";
        String computeAccel = panel.computeAccelerationCombo != null && panel.computeAccelerationCombo.getValue() != null ? panel.computeAccelerationCombo.getValue() : "AUTO";

        return new ScenarioSetupSnapshot(
            panel.getMasterSeed(), dtIso, panel.getSimulationStepSeconds(), maxVal, maxUnit, minPop, desc,
            panel.getSelectedWorld(), panel.getSelectedWeather(), panel.getSelectedSpecies(), panel.getSelectedNestType(),
            panel.getQueenCount(), panel.getWorkerCount(), panel.getSoldierCount(), panel.getBroodCount(),
            snapshots, mpOnly, reqPlayers, tilesX, tilesY, engineBackend, computeAccel
        );
    }
}
