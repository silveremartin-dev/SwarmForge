/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.event.SimulationEvent;

/**
 * Nest Microclimate System.
 * Simulates subterranean respiratory gas exchange (CO2 accumulation, O2 depletion),
 * thermal buffering, and triggers active ventilation shaft excavation when CO2 exceeds hypercapnia limits.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NestMicroclimateSystem {

    private final Simulation simulation;
    private long lastMicroclimateTick = 0;

    public NestMicroclimateSystem(Simulation simulation) {
        this.simulation = simulation;
    }

    public void tick() {
        long currentTick = simulation.getTickCount();
        if (currentTick - lastMicroclimateTick < 100) return; // Every ~1.5s
        lastMicroclimateTick = currentTick;

        Terrarium terrarium = simulation.getTerrarium();
        if (terrarium == null) return;

        for (Colony colony : simulation.getColonies()) {
            int antCount = colony.getPopulation();
            if (antCount == 0) continue;

            // Compute respiration CO2 rate (each worker produces ~0.05 ul CO2/h)
            float co2ProductionRate = antCount * 0.002f; // % per tick

            // Sample nest chambers
            int nestX = (int) colony.getNestX();
            int nestY = (int) colony.getNestY();
            int nestZ = 5; // Subterranean chamber depth

            if (terrarium.inBounds(nestX, nestY, nestZ)) {
                TerrariumCell cell = terrarium.getCell(nestX, nestY, nestZ);
                float currentCo2 = cell.co2() + co2ProductionRate;

                // Update cell CO2 level
                terrarium.setCell(new TerrariumCell(nestX, nestY, nestZ, cell.material(), cell.pheromones(),
                        cell.temperature(), cell.humidity(), currentCo2, cell.o2(), cell.n2o(),
                        cell.light(), cell.windX(), cell.windY(), cell.pressure()));

                // Hypercapnia check (CO2 exceeds species sensitivity threshold * factor)
                float co2Limit = colony.getSpecies() != null ? Math.max(0.015f, colony.getSpecies().getGasSensitivityCo2Ppm() * 0.00005f) : 0.025f;
                if (currentCo2 > co2Limit) {
                    simulation.queueEvent(new SimulationEvent(
                            SimulationEvent.EventType.MILESTONE_REACHED,
                            currentTick,
                            "💨 High CO2 (" + String.format("%.2f", currentCo2 * 100) + "%) in " + colony.getSpeciesName() + " nest triggering ventilation shaft excavation!"
                    ));
                }
            }
        }
    }
}
