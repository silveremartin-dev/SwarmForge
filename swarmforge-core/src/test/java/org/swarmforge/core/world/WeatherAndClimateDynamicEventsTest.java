/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.simulation.Simulation;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless Test Suite verifying dynamic changing weather and climate events:
 * - Markov state transitions (SUNNY, THUNDERSTORM, SNOW, TEMPEST)
 * - Dynamic wind, rain, temperature, and light modulation
 * - Behavioral flight constraints and foraging activity multipliers
 * - Subterranean soil thermal inertia and depth attenuation
 * - Seasonal cycle progression (Spring, Summer, Fall, Winter)
 */
public class WeatherAndClimateDynamicEventsTest {

    private Terrarium terrarium;
    private Simulation simulation;
    private WeatherSystem weather;
    private SeasonManager seasonManager;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(60, 60, 30);
        simulation = new Simulation(terrarium);
        weather = simulation.getWeather();
        seasonManager = simulation.getSeasonManager();
    }

    @Test
    @DisplayName("Verify Markov weather state transitions and physical atmospheric variables")
    void testMarkovWeatherTransitions() {
        // Trigger Sunny
        weather.triggerClimateEvent(WeatherMarkovChain.WeatherState.SUNNY);
        assertEquals(WeatherMarkovChain.WeatherState.SUNNY, weather.getWeatherState());
        assertFalse(weather.isRaining());
        assertTrue(weather.getCloudCover() <= 20.0f);

        // Trigger Thunderstorm
        weather.triggerClimateEvent(WeatherMarkovChain.WeatherState.THUNDERSTORM);
        assertEquals(WeatherMarkovChain.WeatherState.THUNDERSTORM, weather.getWeatherState());
        assertTrue(weather.getRainfall() > 0.0f, "Thunderstorm state must generate precipitation");
        assertTrue(weather.getCloudCover() >= 80.0f, "Thunderstorm cloud cover should be high");

        // Trigger Snow
        weather.triggerClimateEvent(WeatherMarkovChain.WeatherState.SNOW);
        assertEquals(WeatherMarkovChain.WeatherState.SNOW, weather.getWeatherState());
        assertTrue(weather.getSnowfall() > 0.0f, "Snow state must generate snowfall");

        // Trigger Tempest
        weather.triggerClimateEvent(WeatherMarkovChain.WeatherState.TEMPEST);
        assertEquals(WeatherMarkovChain.WeatherState.TEMPEST, weather.getWeatherState());
        assertTrue(weather.getWindSpeed() > 30.0f, "Tempest wind speed should be high");
    }

    @Test
    @DisplayName("Verify dynamic weather constraints on insect flight and foraging activity multipliers")
    void testWeatherImpactOnBiologicalFlightAndForaging() {
        // Mild sunny weather
        weather.triggerClimateEvent(WeatherMarkovChain.WeatherState.SUNNY);
        weather.setWindSpeed(10.0f);
        weather.setTemperature(22.0f);

        assertTrue(weather.canInsectsFly(), "Insects should fly under mild sunny weather");
        assertTrue(weather.getForagingMultiplier() > 0.5f, "Foraging multiplier should be high under mild weather");

        // Severe wind flight barrier (>32 km/h)
        weather.setWindSpeed(40.0f);
        assertFalse(weather.canInsectsFly(), "High winds (>32 km/h) must prevent insect flight");

        // Heavy rain flight barrier (>10 mm/h)
        weather.setWindSpeed(10.0f);
        weather.setRainfall(15.0f);
        assertFalse(weather.canInsectsFly(), "Heavy rainfall (>10 mm/h) must prevent insect flight");

        // Temperature extremes
        weather.setRainfall(0.0f);
        weather.setTemperature(2.0f);
        assertTrue(weather.getForagingMultiplier() <= 0.1f, "Cold ambient temperature (<5°C) must inhibit foraging");

        weather.setTemperature(45.0f);
        assertTrue(weather.getForagingMultiplier() <= 0.1f, "Extreme heat (>42°C) must inhibit foraging");
    }

    @Test
    @DisplayName("Verify subterranean soil thermal inertia and depth attenuation during ambient temperature spikes")
    void testSoilThermalInertiaAtDepth() {
        weather.setTemperature(38.0f); // Ambient surface heatwave

        float surfaceTemp = weather.getTemperatureAtDepth(0);
        float shallowTemp = weather.getTemperatureAtDepth(5);
        float deepTemp = weather.getTemperatureAtDepth(20);

        assertTrue(Math.abs(surfaceTemp - 38.0f) < 2.0f, "Surface temperature should track ambient surface heat");
        assertTrue(Math.abs(deepTemp - 38.0f) > Math.abs(shallowTemp - 38.0f),
                "Deep subterranean soil layers must attenuate ambient surface heatwave spikes");
    }

    @Test
    @DisplayName("Verify seasonal progression, seasonal multipliers, and diapause modulation")
    void testSeasonalCycleProgression() {
        seasonManager.setSeasonalEffectsEnabled(true);

        // Spring
        seasonManager.skipToSeason(Season.SPRING);
        assertEquals(Season.SPRING, seasonManager.getCurrentSeason());
        assertTrue(seasonManager.getActivityMultiplier() >= 1.0f);

        // Summer
        seasonManager.skipToSeason(Season.SUMMER);
        assertEquals(Season.SUMMER, seasonManager.getCurrentSeason());
        assertTrue(seasonManager.getFoodMultiplier() >= 1.0f);

        // Fall
        seasonManager.skipToSeason(Season.FALL);
        assertEquals(Season.FALL, seasonManager.getCurrentSeason());

        // Winter (Diapause)
        seasonManager.skipToSeason(Season.WINTER);
        assertEquals(Season.WINTER, seasonManager.getCurrentSeason());
        assertTrue(seasonManager.getActivityMultiplier() < 0.6f, "Winter activity multiplier must reflect diapause");
    }
}
