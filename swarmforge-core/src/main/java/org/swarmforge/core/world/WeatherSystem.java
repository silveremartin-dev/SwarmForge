/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

import java.time.LocalTime;
import java.util.Random;

/**
 * Weather and climate system for the simulation.
 * Provides temperature, humidity, wind, and daylight calculations.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WeatherSystem {

    private final Random random;

    // Location
    private double latitude;
    private double longitude;

    // Current conditions
    private float temperature; // Celsius
    private float humidity; // 0-100%
    private float windSpeed; // m/s
    private float windDirection; // degrees
    private float rainfall; // mm/hour
    private boolean isDaytime;

    // Time
    private int dayOfYear; // 1-365
    private float timeOfDay; // 0-24 hours

    public WeatherSystem(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.random = new Random();
        this.dayOfYear = 180; // Summer start
        this.timeOfDay = 12f;
        updateConditions();
    }

    /**
     * Advance time and update weather conditions.
     *
     * @param hours Hours to advance
     */
    public void advanceTime(float hours) {
        timeOfDay += hours;
        while (timeOfDay >= 24) {
            timeOfDay -= 24;
            dayOfYear = (dayOfYear % 365) + 1;
        }
        updateConditions();
    }

    private void updateConditions() {
        // Calculate base temperature from latitude and season
        float seasonalOffset = (float) Math.cos((dayOfYear - 172) * 2 * Math.PI / 365) * 15;
        float latitudeEffect = (float) (30 - Math.abs(latitude) * 0.5);
        float baseTemp = latitudeEffect + seasonalOffset;

        // Daily temperature variation
        float dailyVariation = (float) Math.sin((timeOfDay - 6) * Math.PI / 12) * 8;
        temperature = baseTemp + dailyVariation + (random.nextFloat() - 0.5f) * 2;

        // Humidity inversely related to temperature
        humidity = Math.max(20, Math.min(100, 70 - temperature * 0.5f + random.nextFloat() * 20));

        // Wind
        windSpeed = 2 + random.nextFloat() * 5;
        windDirection = random.nextFloat() * 360;

        // Rainfall chance based on humidity
        rainfall = humidity > 80 && random.nextFloat() < 0.3f ? random.nextFloat() * 10 : 0;

        // Daylight calculation
        float dayLength = calculateDayLength();
        float sunrise = 12 - dayLength / 2;
        float sunset = 12 + dayLength / 2;
        isDaytime = timeOfDay >= sunrise && timeOfDay <= sunset;
    }

    private float calculateDayLength() {
        // Simplified day length calculation based on latitude and season
        double declination = 23.45 * Math.sin(Math.toRadians((dayOfYear - 81) * 360.0 / 365));
        double latRad = Math.toRadians(latitude);
        double decRad = Math.toRadians(declination);
        double hourAngle = Math.acos(-Math.tan(latRad) * Math.tan(decRad));
        return (float) (2 * Math.toDegrees(hourAngle) / 15);
    }

    /**
     * Get temperature at a specific depth underground.
     *
     * @param depth Depth in cells (positive = underground)
     * @return Temperature at that depth
     */
    public float getTemperatureAtDepth(int depth) {
        // Underground temperature stabilizes around average annual temp
        float avgTemp = (float) (30 - Math.abs(latitude) * 0.5);
        float surfaceTemp = temperature;
        float blend = Math.min(1, depth / 20f);
        return surfaceTemp * (1 - blend) + avgTemp * blend;
    }

    // Getters
    public float getTemperature() {
        return temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public float getWindDirection() {
        return windDirection;
    }

    public float getRainfall() {
        return rainfall;
    }

    public boolean isDaytime() {
        return isDaytime;
    }

    public int getDayOfYear() {
        return dayOfYear;
    }

    public float getTimeOfDay() {
        return timeOfDay;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    // Setters for external weather data integration
    public void setTemperature(float t) {
        this.temperature = t;
    }

    public void setHumidity(float h) {
        this.humidity = h;
    }

    public void setWindSpeed(float s) {
        this.windSpeed = s;
    }

    public void setRainfall(float r) {
        this.rainfall = r;
    }
}
