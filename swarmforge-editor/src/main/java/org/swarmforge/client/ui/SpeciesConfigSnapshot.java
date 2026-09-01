package org.swarmforge.client.ui;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.swarmforge.core.behavior.ReasoningArchitecture.ArchitectureType;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpeciesConfigSnapshot(
    String speciesName,
    String nestType,
    int queenCount,
    int workerCount,
    int soldierCount,
    int broodCount,
    int initialFood,
    ArchitectureType workerEngine,
    ArchitectureType soldierEngine,
    ArchitectureType queenEngine,
    List<AccessoryConfigSnapshot> accessorySnapshots,
    String placementStrategy,
    double customX,
    double customZ,
    String nestRelation
) {
    public SpeciesConfigSnapshot(
        String speciesName,
        String nestType,
        int queenCount,
        int workerCount,
        int soldierCount,
        int broodCount,
        int initialFood,
        ArchitectureType workerEngine,
        ArchitectureType soldierEngine,
        ArchitectureType queenEngine,
        List<AccessoryConfigSnapshot> accessorySnapshots
    ) {
        this(speciesName, nestType, queenCount, workerCount, soldierCount, broodCount, initialFood, workerEngine, soldierEngine, queenEngine, accessorySnapshots, "Center of Map", 0.0, 0.0, "Monogyne Autonomous Territory");
    }
}

