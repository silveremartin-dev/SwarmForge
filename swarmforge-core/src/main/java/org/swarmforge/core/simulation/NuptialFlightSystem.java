/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.world.Season;
import org.swarmforge.core.world.SeasonManager;
import org.swarmforge.core.world.WeatherSystem;

import java.util.logging.Logger;

/**
 * Nuptial Flight Dispersal & Mating Swarm System.
 * Simulates atmospheric conditions triggering synchronized alate reproductive emergence
 * (warm, humid, post-precipitation conditions during spring/summer).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NuptialFlightSystem {

    private static final Logger LOG = Logger.getLogger(NuptialFlightSystem.class.getName());

    private final Simulation simulation;
    private long lastCheckTick = 0;

    public NuptialFlightSystem(Simulation simulation) {
        this.simulation = simulation;
    }

    /**
     * Executes periodic checks for nuptial flight conditions.
     */
    public void tick() {
        long currentTick = simulation.getTickCount();
        if (currentTick - lastCheckTick < 600) { // Check every ~10 seconds
            return;
        }
        lastCheckTick = currentTick;

        WeatherSystem weather = simulation.getWeather();
        SeasonManager seasonManager = simulation.getSeasonManager();
        if (weather == null || seasonManager == null) {
            return;
        }

        // Biological trigger: Late Spring / Early Summer, warm (>22°C), high humidity (>65%), low wind (<15 km/h)
        Season currentSeason = seasonManager.getCurrentSeason();
        boolean isNuptialSeason = (currentSeason == Season.SPRING || currentSeason == Season.SUMMER);
        float temp = weather.getTemperature();
        float humidity = weather.getHumidity();
        float wind = weather.getWindSpeed();

        if (isNuptialSeason && temp >= 22.0f && humidity >= 65.0f && wind <= 4.2f) {
            for (Colony colony : simulation.getColonies()) {
                if (colony.getPopulation() > 50 && colony.getFoodStored() > 100.0f) {
                    simulation.queueEvent(new SimulationEvent(
                            SimulationEvent.EventType.NUPTIAL_FLIGHT,
                            currentTick,
                            colony.getId(),
                            "Nuptial flight triggered for colony " + colony.getSpeciesName() + " under optimal thermal conditions (" + temp + "°C, " + humidity + "% RH)"
                    ));
                    LOG.fine("Nuptial flight conditions satisfied for colony " + colony.getId());
                }
            }
        }
    }
}
