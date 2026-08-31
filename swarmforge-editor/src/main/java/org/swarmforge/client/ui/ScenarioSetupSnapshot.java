package org.swarmforge.client.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    List<SpeciesConfigSnapshot> speciesSnapshots
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
        this(seed, null, 0.0166f, 100.0, "Days (d)", 0, "", selectedWorld, selectedWeather, selectedSpecies, selectedNestType, queenCount, workerCount, soldierCount, broodCount, speciesSnapshots);
    }
}
