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
import org.swarmforge.core.event.SimulationEvent;
import org.swarmforge.core.simulation.Simulation;

import java.util.List;

import org.swarmforge.core.world.Season;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auto-Test Suite verifying Weather System physics, Markov state transitions,
 * diurnal solar cycles, insect flight/foraging constraints, soil thermal inertia,
 * and SeasonManager progression integration.
 */
public class WeatherAndSeasonAutoTest {

    private WeatherSystem weather;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        Terrarium terrarium = new Terrarium(50, 50, 20);
        simulation = new Simulation(terrarium);
        weather = simulation.getWeather();
    }

    @Test
    @DisplayName("Verify continuous atmospheric evolution, diurnal solar cycle, and time advancement")
    void testAtmosphereAdvanceTimeAndDiurnalCycle() {
        float initialTime = weather.getTimeOfDay();
        int initialDay = weather.getDayOfYear();

        // Advance 6 hours
        weather.advanceTime(6.0f);
        assertEquals((initialTime + 6.0f) % 24.0f, weather.getTimeOfDay(), 0.01f);

        // Advance 20 hours to wrap around to next day
        weather.advanceTime(20.0f);
        assertEquals((initialDay % 365) + 1, weather.getDayOfYear());

        // Check solar parameters
        float sunAngle = weather.getSunAngle();
        assertTrue(sunAngle >= 0.0f && sunAngle <= 1.0f, "Sun angle must be normalized between 0.0 and 1.0");

        float lightLevel = weather.getLightLevel();
        assertTrue(lightLevel >= 0.05f && lightLevel <= 1.0f, "Light level must be within realistic bounds");
        assertTrue(weather.getTemperatureKelvin() > 200.0f, "Temperature Kelvin should be physically realistic");
    }

    @Test
    @DisplayName("Verify Markov weather state transitions and atmospheric physics output")
    void testMarkovChainWeatherStateTransitions() {
        // Trigger Thunderstorm
        weather.triggerClimateEvent(WeatherMarkovChain.WeatherState.THUNDERSTORM);
        assertEquals("THUNDERSTORM", weather.getCurrentWeatherType());
        assertTrue(weather.getRainfall() > 0.0f, "Thunderstorm must generate rainfall");
        assertTrue(weather.getCloudCover() >= 90.0f, "Thunderstorm cloud cover should be high");

        // Trigger Snow
        weather.triggerClimateEvent(WeatherMarkovChain.WeatherState.SNOW);
        assertEquals(WeatherMarkovChain.WeatherState.SNOW, weather.getWeatherState());
        assertTrue(weather.getSnowfall() > 0.0f, "Snow state must generate snowfall");
        assertEquals(0.0f, weather.getRainfall(), 0.001f, "Snow state rainfall should be zero");

        // Trigger Tempest
        weather.triggerClimateEvent(WeatherMarkovChain.WeatherState.TEMPEST);
        assertTrue(weather.getWindSpeed() > 30.0f, "Tempest wind speed must be high");
        assertTrue(weather.getWindSpeedMs() > 8.0f, "Wind speed in m/s should match conversion formula");

        // Trigger Sunny
        weather.triggerClimateEvent(WeatherMarkovChain.WeatherState.SUNNY);
        assertFalse(weather.isRaining(), "Sunny weather should have zero precipitation");
        assertTrue(weather.getCloudCover() <= 15.0f, "Sunny weather should have minimal cloud cover");
    }

    @Test
    @DisplayName("Verify insect flight suitability and foraging activity multipliers under weather conditions")
    void testInsectFlightSuitabilityAndForagingMultipliers() {
        // Clear/Sunny conditions
        weather.triggerClimateEvent(WeatherMarkovChain.WeatherState.SUNNY);
        weather.setWindSpeed(10.0f);
        weather.setTemperature(22.0f);

        assertTrue(weather.canInsectsFly(), "Insects should fly under mild sunny conditions");
        float foragerMult = weather.getForagingMultiplier();
        assertTrue(foragerMult > 0.5f, "Foraging activity multiplier should be high in good weather");

        // High wind flight barrier (>32 km/h)
        weather.setWindSpeed(45.0f);
        assertFalse(weather.canInsectsFly(), "High winds (>32 km/h) must prevent insect flight");

        // Heavy rain flight barrier (>10 mm/h)
        weather.setWindSpeed(10.0f);
        weather.setRainfall(15.0f);
        assertFalse(weather.canInsectsFly(), "Heavy rain (>10 mm/h) must prevent insect flight");

        // Extreme temperature inhibition
        weather.setRainfall(0.0f);
        weather.setTemperature(2.0f);
        assertTrue(weather.getForagingMultiplier() <= 0.1f, "Cold temperatures (<5°C) must inhibit foraging activity");

        weather.setTemperature(45.0f);
        assertTrue(weather.getForagingMultiplier() <= 0.1f, "Extreme heat (>42°C) must inhibit foraging activity");
    }

    @Test
    @DisplayName("Verify subterranean soil thermal inertia and hydric depth attenuation")
    void testSoilHydricCouplingAndThermalInertia() {
        weather.setTemperature(35.0f); // Surface heatwave

        float surfaceTemp = weather.getTemperatureAtDepth(0);
        float shallowTemp = weather.getTemperatureAtDepth(5);
        float deepTemp = weather.getTemperatureAtDepth(20);

        // Subterranean temperatures should attenuate towards annual baseline average
        assertTrue(Math.abs(surfaceTemp - 35.0f) < 2.0f, "Surface depth temp should track ambient surface temp");
        assertTrue(Math.abs(deepTemp - 35.0f) > Math.abs(shallowTemp - 35.0f),
                "Deeper soil layers should attenuate extreme surface temperature spikes");

        float surfaceHumidity = weather.getSoilHumidityAtDepth(0);
        float deepHumidity = weather.getSoilHumidityAtDepth(15);
        assertTrue(surfaceHumidity >= 0.0f && surfaceHumidity <= 100.0f, "Soil humidity must be valid percentage");
        assertTrue(deepHumidity >= 0.0f && deepHumidity <= 100.0f, "Soil humidity must be valid percentage");
    }

    @Test
    @DisplayName("Verify SeasonManager progression, season change events, and biological multipliers")
    void testSeasonManagerProgressionAndEvents() {
        SeasonManager seasonManager = simulation.getSeasonManager();
        seasonManager.setSeasonalEffectsEnabled(true);

        // Test Spring
        seasonManager.skipToSeason(Season.SPRING);
        assertEquals(Season.SPRING, seasonManager.getCurrentSeason());
        assertTrue(seasonManager.getActivityMultiplier() >= 1.0f);

        // Test Summer
        seasonManager.skipToSeason(Season.SUMMER);
        assertEquals(Season.SUMMER, seasonManager.getCurrentSeason());
        assertTrue(seasonManager.getFoodMultiplier() >= 1.0f);

        // Test Fall
        seasonManager.skipToSeason(Season.FALL);
        assertEquals(Season.FALL, seasonManager.getCurrentSeason());

        // Test Winter (Diapause)
        seasonManager.skipToSeason(Season.WINTER);
        assertEquals(Season.WINTER, seasonManager.getCurrentSeason());
        assertTrue(seasonManager.getActivityMultiplier() < 0.6f, "Winter activity multiplier must reflect diapause");

        // Verify SEASON_CHANGED event recorded in simulation event queue
        java.util.Queue<SimulationEvent> events = simulation.getEventQueue();
        boolean foundSeasonEvent = events.stream()
                .anyMatch(e -> e.getType() == SimulationEvent.EventType.SEASON_CHANGED);
        assertTrue(foundSeasonEvent, "Simulation event queue must contain SEASON_CHANGED event after season transition");
    }

    @Test
    @DisplayName("Verify multi-tick headless integration between Simulation, SeasonManager, and WeatherSystem")
    void testSimulationTickWeatherIntegration() {
        long initialTick = simulation.getTickCount();

        for (int i = 0; i < 100; i++) {
            simulation.tick();
        }

        assertEquals(initialTick + 100L, simulation.getTickCount(), "Simulation tick counter must advance 100 ticks");
        assertNotNull(simulation.getWeather().getWeatherState(), "Weather state must remain valid after 100 ticks");
    }
}
