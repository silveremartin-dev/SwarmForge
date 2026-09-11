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
        float deltaSeconds = (currentTick - lastMicroclimateTick) * simulation.getSimulationStepSeconds();
        if (deltaSeconds < 1.5f) return; // Evaluate every 1.5 real seconds
        lastMicroclimateTick = currentTick;

        Terrarium terrarium = simulation.getTerrarium();
        if (terrarium == null) return;

        org.swarmforge.core.world.WeatherSystem weather = simulation.getWeather();
        float externalTemp = weather != null ? weather.getTemperature() : 20.0f;
        float externalWindMs = weather != null ? weather.getWindSpeedMs() : 1.0f;

        for (Colony colony : simulation.getColonies()) {
            int antCount = colony.getPopulation();
            if (antCount == 0) continue;

            // Compute respiration CO2 production and O2 consumption (RQ ≈ 0.85)
            float co2ProductionRate = antCount * 0.0012f * deltaSeconds;
            float o2ConsumptionRate = co2ProductionRate / 0.85f;

            // Sample nest chambers
            int nestX = (int) colony.getNestX();
            int nestY = (int) colony.getNestY();
            int nestZ = (int) colony.getNestZ(); // Subterranean chamber depth

            if (terrarium.inBounds(nestX, nestY, nestZ)) {
                TerrariumCell cell = terrarium.getCell(nestX, nestY, nestZ);
                float nestTemp = cell.temperature();

                // Stack effect / thermal buoyancy ventilation formula:
                // v_draft = sqrt(g * h * |T_nest - T_ext| / T_ext_K) + 0.12 * v_wind
                float chimneyHeightM = Math.max(0.2f, Math.abs(nestZ) * 0.1f);
                float deltaT = Math.abs(nestTemp - externalTemp);
                float extKelvin = Math.max(240.0f, externalTemp + 273.15f);
                float buoyancyVelocity = (float) Math.sqrt(Math.max(0.0f, (9.81f * chimneyHeightM * deltaT) / extKelvin));
                float totalDraftVelocity = buoyancyVelocity + externalWindMs * 0.12f;

                // Dynamic ventilation purge rate based on stack airflow
                float purgeFraction = Math.min(0.40f, (0.015f + 0.035f * totalDraftVelocity) * deltaSeconds);

                // CO2 & O2 gas exchange
                float ambientCo2 = TerrariumCell.DEFAULT_CO2;
                float ambientO2 = TerrariumCell.DEFAULT_O2;
                float currentCo2 = cell.co2() + co2ProductionRate;
                currentCo2 = currentCo2 - (currentCo2 - ambientCo2) * purgeFraction;

                float currentO2 = cell.o2() - o2ConsumptionRate;
                currentO2 = currentO2 + (ambientO2 - currentO2) * purgeFraction;

                // Thermal equilibration draft
                float thermalEquilRate = Math.min(0.15f, 0.01f * totalDraftVelocity * deltaSeconds);
                float updatedTemp = nestTemp + (externalTemp - nestTemp) * thermalEquilRate;

                // Update cell atmospheric state
                terrarium.setCell(new TerrariumCell(nestX, nestY, nestZ, cell.material(), cell.pheromones(),
                        updatedTemp, cell.humidity(), currentCo2, currentO2, cell.n2o(),
                        cell.light(), cell.windX(), cell.windY(), cell.pressure()));

                // Hypercapnia check (CO2 exceeds species sensitivity threshold in ppm)
                float co2LimitPpm = colony.getSpecies() != null ? Math.max(15000.0f, colony.getSpecies().getGasSensitivityCo2Ppm() * 50.0f) : 25000.0f;
                if (currentCo2 > co2LimitPpm) {
                    simulation.queueEvent(new SimulationEvent(
                            SimulationEvent.EventType.MILESTONE_REACHED,
                            currentTick,
                            "💨 High CO2 (" + String.format("%.2f%%, %d ppm", currentCo2 / 10000.0f, (int) currentCo2) + ") in " + colony.getSpeciesName() + " nest triggering ventilation shaft excavation!"
                    ));
                }
            }
        }
    }
}
