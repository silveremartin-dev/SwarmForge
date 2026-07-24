/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeatherSystem class.
 */
class WeatherSystemTest {

    private WeatherSystem weather;

    @BeforeEach
    void setUp() {
        // Paris coordinates
        weather = new WeatherSystem(48.8566, 2.3522);
    }

    @Test
    void testWeatherSystemCreation() {
        assertNotNull(weather);
        assertEquals(48.8566, weather.getLatitude(), 0.01);
        assertEquals(2.3522, weather.getLongitude(), 0.01);
    }

    @Test
    void testInitialConditions() {
        // Weather starts with some reasonable values
        assertTrue(weather.getTemperature() > -50 && weather.getTemperature() < 60);
        assertTrue(weather.getHumidity() >= 0 && weather.getHumidity() <= 100);
        assertTrue(weather.getWindSpeed() >= 0);
        assertTrue(weather.getWindDirection() >= 0 && weather.getWindDirection() < 360);
    }

    @Test
    void testAdvanceTime() {
        float initialTime = weather.getTimeOfDay();
        weather.advanceTime(1f);

        assertEquals(initialTime + 1f, weather.getTimeOfDay(), 0.01);
    }

    @Test
    void testAdvanceTimeDayRollover() {
        // Start at noon on day 180
        int initialDay = weather.getDayOfYear();
        weather.advanceTime(24f);

        // Should have advanced one day
        assertEquals((initialDay % 365) + 1, weather.getDayOfYear());
    }

    @Test
    void testDayNightCycle() {
        // Advance to different times and check isDaytime
        // Note: isDaytime is calculated internally based on latitude and season
        boolean foundDay = false;
        boolean foundNight = false;

        for (int hour = 0; hour < 24; hour++) {
            weather.advanceTime(1f);
            if (weather.isDaytime()) {
                foundDay = true;
            } else {
                foundNight = true;
            }
        }

        assertTrue(foundDay, "Should have daytime hours");
        assertTrue(foundNight, "Should have nighttime hours");
    }

    @Test
    void testTemperatureAtDepth() {
        float surfaceTemp = weather.getTemperature();
        float deepTemp = weather.getTemperatureAtDepth(50);

        // Deep underground should be more stable (closer to average annual temp)
        // At depth 50, it should be heavily blended towards avg temp
        assertNotEquals(surfaceTemp, deepTemp, 0.1f);
    }

    @Test
    void testSetTemperature() {
        weather.setTemperature(30f);
        assertEquals(30f, weather.getTemperature(), 0.01f);
    }

    @Test
    void testSetHumidity() {
        weather.setHumidity(80f);
        assertEquals(80f, weather.getHumidity(), 0.01f);
    }

    @Test
    void testSetWindSpeed() {
        weather.setWindSpeed(10f);
        assertEquals(10f, weather.getWindSpeed(), 0.01f);
    }

    @Test
    void testSetRainfall() {
        weather.setRainfall(5f);
        assertEquals(5f, weather.getRainfall(), 0.01f);
    }

    @Test
    void testGetTimeOfDay() {
        float time = weather.getTimeOfDay();
        assertTrue(time >= 0 && time < 24);
    }

    @Test
    void testGetDayOfYear() {
        int day = weather.getDayOfYear();
        assertTrue(day >= 1 && day <= 365);
    }
}
