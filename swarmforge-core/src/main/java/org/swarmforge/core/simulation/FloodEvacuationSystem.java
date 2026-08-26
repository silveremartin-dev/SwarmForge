/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;

/**
 * Barometric Flood Evacuation System.
 * Models subterranean ant colonies sensing atmospheric/hydrostatic pressure drops
 * and humidity saturation prior to heavy rain, triggering emergency brood evacuation to upper chambers.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class FloodEvacuationSystem {

    public static boolean triggerFloodEvacuation(Individual worker, Colony colony, float atmosphericPressureHpa, float soilHumidity) {
        if (worker == null || colony == null || worker.getSpecies() == null) return false;
        if (!worker.getSpecies().canDetectHydrostaticPressure()) return false;

        if (atmosphericPressureHpa < 990.0f && soilHumidity > 0.85f) {
            // Rapid barometric drop + high soil moisture = impending flood
            return true;
        }
        return false;
    }
}
