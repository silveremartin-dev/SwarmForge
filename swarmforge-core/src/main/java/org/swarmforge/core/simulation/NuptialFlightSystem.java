/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.species.Species;
import org.swarmforge.core.world.WeatherSystem;

/**
 * Multi-Species Nuptial Flight & Swarming Engine (Vol Nuptial & Essaimage).
 * Triggers mass reproductive dispersal flights for Ants, Bees, Wasps, and Termites
 * based on real-time atmospheric triggers (Post-rain humidity, temperature, wind speed, solar photoperiod).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NuptialFlightSystem {

    private final Simulation simulation;
    private long lastFlightCheckTick = 0;

    public NuptialFlightSystem(Simulation simulation) {
        this.simulation = simulation;
    }

    public void tick() {
        long currentTick = simulation.getTickCount();
        if (currentTick - lastFlightCheckTick < 120) return; // Check every 2 seconds
        lastFlightCheckTick = currentTick;

        WeatherSystem weather = simulation.getWeather();

        for (Colony colony : simulation.getColonies()) {
            if (colony.getPopulation() < 500) continue; // Mature colonies only

            Species.InsectOrder order = colony.getSpecies().getInsectOrder();
            boolean triggersSwarm = checkConditions(order, weather);

            if (triggersSwarm) {
                triggerSwarmingEvent(colony, order);
            }
        }
    }

    private boolean checkConditions(Species.InsectOrder order, WeatherSystem weather) {
        float temp = weather.getTemperature();
        float humidity = weather.getHumidity();
        float wind = weather.getWindSpeed();
        boolean isDay = weather.isDaytime();
        float rain = weather.getRainfall();

        if (rain > 1.0f) return false; // Flight inhibited during active heavy rain

        return switch (order) {
            case ANT -> (temp >= 20.0f && temp <= 32.0f && humidity >= 70.0f && wind <= 12.0f && isDay);
            case BEE -> (temp >= 18.0f && temp <= 30.0f && wind <= 15.0f && isDay && weather.getWeatherState().flightSuitability > 0.8f);
            case WASP -> (temp >= 15.0f && temp <= 26.0f && wind <= 18.0f && isDay);
            case TERMITE -> (temp >= 22.0f && humidity >= 75.0f && wind <= 8.0f); // Often post-rain dusk/warm night
            case APHID -> (temp >= 16.0f && temp <= 28.0f && wind <= 10.0f && isDay); // Winged alate dispersal
            case THRIPS -> (temp >= 20.0f && temp <= 32.0f && wind <= 8.0f && isDay); // Gall colonization flight
            case BEETLE -> (temp >= 18.0f && humidity >= 65.0f && wind <= 14.0f); // Wood dispersal flight
        };
    }

    private void triggerSwarmingEvent(Colony colony, Species.InsectOrder order) {
        String message = switch (order) {
            case ANT -> "👑 Massive Nuptial Flight triggered for " + colony.getSpeciesName() + "! New queens dispersing.";
            case BEE -> "🐝 Honey Bee Swarm (Essaimage) departing from " + colony.getSpeciesName() + " hive!";
            case WASP -> "🐝 Fertile Wasp Gynes taking flight from " + colony.getSpeciesName() + " nest.";
            case TERMITE -> "🪲 Subterranean Termite Alates swarming from " + colony.getSpeciesName() + " mound.";
            case APHID -> "🌿 Winged Aphid Alates dispersing to form new plant gall colonies (" + colony.getSpeciesName() + ").";
            case THRIPS -> "🌾 Winged Thrips dispersing from Acacia gall (" + colony.getSpeciesName() + ").";
            case BEETLE -> "🌲 Ambrosia Wood Beetles dispersing to excavate new tree galleries (" + colony.getSpeciesName() + ").";
        };

        simulation.queueEvent(new SimulationEvent(
                SimulationEvent.EventType.MILESTONE_REACHED,
                simulation.getTickCount(),
                message
        ));
    }
}
