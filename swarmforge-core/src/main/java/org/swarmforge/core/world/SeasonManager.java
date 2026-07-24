/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.event.SimulationEvent;

/**
 * Manages seasonal cycles in the simulation.
 * Affects weather, food availability, and ant behavior.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class SeasonManager {

    private final Simulation simulation;
    private final WeatherSystem weather;

    // Time tracking
    private int dayOfYear = 79; // Start at spring equinox (March 20)
    private int ticksPerDay = 1440; // 24 minutes real-time = 1 day
    private int tickCounter = 0;

    // Current state
    private Season currentSeason = Season.SPRING;
    private float transitionProgress = 0f;

    // Configuration
    private boolean seasonalEffectsEnabled = true;
    private float seasonSpeed = 1.0f;

    public SeasonManager(Simulation simulation) {
        this.simulation = simulation;
        this.weather = simulation.getWeather();
        updateFromDayOfYear();
    }

    /**
     * Process seasonal progression each tick.
     */
    public void tick() {
        if (!seasonalEffectsEnabled)
            return;

        tickCounter++;

        // Progress day counter
        if (tickCounter >= ticksPerDay / seasonSpeed) {
            tickCounter = 0;
            advanceDay();
        }

        // Apply seasonal effects to weather
        applySeasonalEffects();
    }

    /**
     * Advance to the next day.
     */
    private void advanceDay() {
        dayOfYear = (dayOfYear + 1) % 365;
        updateFromDayOfYear();
    }

    /**
     * Update season based on day of year.
     */
    private void updateFromDayOfYear() {
        Season newSeason = Season.fromDayOfYear(dayOfYear);

        if (newSeason != currentSeason) {
            Season oldSeason = currentSeason;
            currentSeason = newSeason;
            transitionProgress = 0f;

            simulation.queueEvent(new SimulationEvent(
                    SimulationEvent.EventType.SEASON_CHANGED,
                    simulation.getTickCount(),
                    "Season changed: " + oldSeason.getDisplayName() + " → " + newSeason.getDisplayName()));
        }

        // Calculate transition progress within season (0-1)
        int seasonLength = 91; // ~3 months
        int seasonStartDay = switch (currentSeason) {
            case SPRING -> 0;
            case SUMMER -> 91;
            case FALL -> 182;
            case WINTER -> 273;
        };
        transitionProgress = (float) ((dayOfYear - seasonStartDay + 365) % 365) / seasonLength;
    }

    /**
     * Apply current season's effects to the weather system.
     */
    /**
     * Apply current season's effects to the weather system.
     */
    private void applySeasonalEffects() {
        switch (currentSeason) {
            case SPRING -> {
                weather.setTemperatureOffset(5f * transitionProgress);
                weather.setRainMultiplier(1.2f);
            }
            case SUMMER -> {
                weather.setTemperatureOffset(5f);
                weather.setRainMultiplier(0.5f);
            }
            case FALL -> {
                weather.setTemperatureOffset(5f - (10f * transitionProgress));
                weather.setRainMultiplier(1.0f);
            }
            case WINTER -> {
                weather.setTemperatureOffset(-10f);
                weather.setRainMultiplier(0.8f);
            }
        }
    }

    // === Getters ===

    public Season getCurrentSeason() {
        return currentSeason;
    }

    public int getDayOfYear() {
        return dayOfYear;
    }

    public float getTransitionProgress() {
        return transitionProgress;
    }

    public float getFoodMultiplier() {
        return currentSeason.getFoodMultiplier();
    }

    public float getActivityMultiplier() {
        return currentSeason.getActivityMultiplier();
    }

    public boolean areMatingFlightsAllowed() {
        return currentSeason.areMatingFlightsAllowed();
    }

    // === Configuration ===

    public void setDayOfYear(int day) {
        this.dayOfYear = day % 365;
        updateFromDayOfYear();
    }

    public void setTicksPerDay(int ticks) {
        this.ticksPerDay = ticks;
    }

    public void setSeasonSpeed(float speed) {
        this.seasonSpeed = Math.max(0.1f, speed);
    }

    public void setSeasonalEffectsEnabled(boolean enabled) {
        this.seasonalEffectsEnabled = enabled;
    }

    public boolean isSeasonalEffectsEnabled() {
        return seasonalEffectsEnabled;
    }

    /**
     * Skip to a specific season.
     */
    public void skipToSeason(Season season) {
        this.dayOfYear = switch (season) {
            case SPRING -> 79; // March 20
            case SUMMER -> 172; // June 21
            case FALL -> 266; // September 23
            case WINTER -> 355; // December 21
        };
        updateFromDayOfYear();
    }
}
