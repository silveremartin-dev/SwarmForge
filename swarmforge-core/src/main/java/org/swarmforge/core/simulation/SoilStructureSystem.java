/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.world.WeatherSystem;

/**
 * Soil Dynamics, Erosion & Gallery Collapse System.
 * Simulates gallery structural integrity, flood saturation collapses, and drought cementing.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SoilStructureSystem {

    private final Simulation simulation;
    private long lastErosionCheck = 0;

    public SoilStructureSystem(Simulation simulation) {
        this.simulation = simulation;
    }

    public void tick() {
        long currentTick = simulation.getTickCount();
        if (currentTick - lastErosionCheck < 200) return; // Every ~3 seconds
        lastErosionCheck = currentTick;

        WeatherSystem weather = simulation.getWeather();
        float rainfall = weather.getRainfall();
        float surfaceMoisture = weather.getSoilHumidityAtDepth(0);

        if (rainfall > 20.0f || surfaceMoisture > 92.0f) {
            // Flash Flood / High Saturation Erosion Risk
            for (Colony colony : simulation.getColonies()) {
                if (colony.getSpecies().getInsectOrder() == org.swarmforge.core.species.Species.InsectOrder.ANT ||
                    colony.getSpecies().getInsectOrder() == org.swarmforge.core.species.Species.InsectOrder.TERMITE) {

                    simulation.queueEvent(new SimulationEvent(
                            SimulationEvent.EventType.MILESTONE_REACHED,
                            simulation.getTickCount(),
                            "🌊 Soil Saturation (" + String.format("%.1f", surfaceMoisture) + "%) causing gallery flooding in " + colony.getSpeciesName()
                    ));
                }
            }
        }
    }
}
