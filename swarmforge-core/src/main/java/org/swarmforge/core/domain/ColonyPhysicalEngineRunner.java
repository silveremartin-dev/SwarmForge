/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import org.swarmforge.core.epidemiology.IndividualInfection;
import org.swarmforge.core.epidemiology.SocialImmunityManager;
import org.swarmforge.core.structure.physics.NestVoxelGrid;
import org.swarmforge.core.structure.physics.PassiveVentilationEngine;
import org.swarmforge.core.structure.physics.StructuralStabilityAnalyzer;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Executes colony-level physical simulations including structural stability,
 * ventilation airflow, and social immunity epidemiology.
 */
public final class ColonyPhysicalEngineRunner {

    private ColonyPhysicalEngineRunner() {}

    public static void tickPhysicalEngines(
            NestVoxelGrid nestVoxelGrid,
            PassiveVentilationEngine ventilationEngine,
            StructuralStabilityAnalyzer stabilityAnalyzer,
            SocialImmunityManager socialImmunityManager,
            Map<UUID, IndividualInfection> infectionRegistry,
            List<Individual> livingIndividuals,
            int population,
            int age,
            float ambientTemp,
            float ambientCo2,
            float windSpeed,
            float waterSaturation,
            float deltaSeconds,
            Random rng
    ) {
        if (nestVoxelGrid == null) return;

        if (ventilationEngine != null) {
            ventilationEngine.simulateVentilationStep(nestVoxelGrid, ambientTemp, ambientCo2, windSpeed, population);
        }

        if (stabilityAnalyzer != null && age % 60 == 0) {
            stabilityAnalyzer.evaluateAndTriggerCaveIns(nestVoxelGrid, waterSaturation, rng != null ? rng : new Random());
        }

        if (socialImmunityManager != null && age % 30 == 0) {
            socialImmunityManager.simulateEpidemiologyStep(infectionRegistry, livingIndividuals, nestVoxelGrid, rng != null ? rng : new Random(), deltaSeconds);
        }
    }
}
