/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

/**
 * Interface for external weather data providers.
 * Implementations can connect to weather APIs like OpenWeatherMap.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public interface WeatherProvider {

    /**
     * Weather data record.
     */
    record WeatherData(
            float temperature, // Celsius
            float humidity, // Percentage
            float windSpeed, // m/s
            float windDirection, // Degrees
            float rainfall, // mm/hour
            float cloudCover, // Percentage
            float pressure, // hPa
            String condition, // Description
            long timestamp // Unix timestamp
    ) {
    }

    /**
     * Fetch current weather for a location.
     * 
     * @param latitude  Latitude
     * @param longitude Longitude
     * @return Current weather data
     * @throws WeatherException if fetch fails
     */
    WeatherData getCurrentWeather(double latitude, double longitude) throws WeatherException;

    /**
     * Check if the provider is available.
     */
    boolean isAvailable();

    /**
     * Get the provider name.
     */
    String getProviderName();

    /**
     * Exception for weather API errors.
     */
    class WeatherException extends Exception {
        public WeatherException(String message) {
            super(message);
        }

        public WeatherException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
