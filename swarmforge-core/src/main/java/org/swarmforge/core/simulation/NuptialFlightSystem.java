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
    private final java.util.Map<java.util.UUID, Long> lastFlightTickPerColony = new java.util.HashMap<>();
    private static final long NUPTIAL_FLIGHT_COOLDOWN_TICKS = 18000L; // Cooldown to avoid repetitive event spamming

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

        if (isNuptialSeason && temp >= 22.0f && humidity >= 65.0f && (weather.getWindSpeed() <= 15.0f || weather.getWindSpeedMs() <= 4.2f)) {
            for (Colony colony : simulation.getColonies()) {
                long lastFlight = lastFlightTickPerColony.getOrDefault(colony.getId(), -NUPTIAL_FLIGHT_COOLDOWN_TICKS);
                if (currentTick - lastFlight >= NUPTIAL_FLIGHT_COOLDOWN_TICKS && colony.getPopulation() > 50 && colony.getFoodStored() > 100.0f) {
                    lastFlightTickPerColony.put(colony.getId(), currentTick);
                    colony.setFoodStored(Math.max(0.0f, colony.getFoodStored() - 30.0f));

                    float windSpeed = weather.getWindSpeed();
                    String windDir = weather.getWindDirection();
                    float windRad = switch (windDir != null ? windDir.toUpperCase() : "N") {
                        case "E" -> 0.0f;
                        case "NE" -> 0.7854f;
                        case "N" -> 1.5708f;
                        case "NW" -> 2.3562f;
                        case "W" -> 3.1416f;
                        case "SW" -> 3.9270f;
                        case "S" -> 4.7124f;
                        case "SE" -> 5.4978f;
                        default -> 1.5708f;
                    };
                    float flightDist = 15.0f + (windSpeed * 0.8f);
                    float landX = colony.getNestX() + (float) Math.cos(windRad) * flightDist;
                    float landY = colony.getNestY() + (float) Math.sin(windRad) * flightDist;

                    simulation.queueEvent(new SimulationEvent(
                            SimulationEvent.EventType.NUPTIAL_FLIGHT,
                            currentTick,
                            colony.getId(),
                            String.format(java.util.Locale.US,
                                "Nuptial flight: Alate reproductives from %s swarming on wind vector %s (%.1f km/h) reaching (%.1f, %.1f) under optimal conditions (%.1f°C, %.0f%% RH)",
                                colony.getSpeciesName(), windDir, windSpeed, landX, landY, temp, humidity)
                    ));
                    LOG.fine("Nuptial flight conditions satisfied for colony " + colony.getId());
                }
            }
        }
    }
}
