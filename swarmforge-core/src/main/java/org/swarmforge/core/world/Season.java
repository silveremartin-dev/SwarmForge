/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

/**
 * Represents the four seasons with their effects on the simulation.
 * Each season affects temperature, food availability, and ant behavior.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public enum Season {

    /**
     * Spring: Warming temperatures, increased food, mating flights.
     */
    SPRING(
            "Spring",
            10f, 22f, // temp range
            60f, 80f, // humidity range
            1.2f, // food multiplier
            1.5f, // activity multiplier
            true// mating flights allowed
    ),

    /**
     * Summer: Hot temperatures, peak activity, abundant food.
     */
    SUMMER(
            "Summer",
            20f, 35f,
            40f, 70f,
            1.5f,
            1.3f,
            false),

    /**
     * Fall/Autumn: Cooling, food storage, preparation for winter.
     */
    FALL(
            "Autumn",
            8f, 20f,
            50f, 75f,
            0.8f,
            1.0f,
            true),

    /**
     * Winter: Cold, minimal activity, survival mode.
     */
    WINTER(
            "Winter",
            -10f, 8f,
            30f, 60f,
            0.1f,
            0.3f,
            false);

    private final String displayName;
    private final float minTemp;
    private final float maxTemp;
    private final float minHumidity;
    private final float maxHumidity;
    private final float foodMultiplier;
    private final float activityMultiplier;
    private final boolean matingFlightsAllowed;

    Season(String displayName, float minTemp, float maxTemp,
            float minHumidity, float maxHumidity,
            float foodMultiplier, float activityMultiplier,
            boolean matingFlightsAllowed) {
        this.displayName = displayName;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.minHumidity = minHumidity;
        this.maxHumidity = maxHumidity;
        this.foodMultiplier = foodMultiplier;
        this.activityMultiplier = activityMultiplier;
        this.matingFlightsAllowed = matingFlightsAllowed;
    }

    // Getters
    public String getDisplayName() {
        return displayName;
    }

    public float getMinTemp() {
        return minTemp;
    }

    public float getMaxTemp() {
        return maxTemp;
    }

    public float getMinHumidity() {
        return minHumidity;
    }

    public float getMaxHumidity() {
        return maxHumidity;
    }

    public float getFoodMultiplier() {
        return foodMultiplier;
    }

    public float getActivityMultiplier() {
        return activityMultiplier;
    }

    public boolean areMatingFlightsAllowed() {
        return matingFlightsAllowed;
    }

    /**
     * Get typical temperature for this season.
     */
    public float getTypicalTemp() {
        return (minTemp + maxTemp) / 2f;
    }

    /**
     * Get the next season in the cycle.
     */
    public Season next() {
        return switch (this) {
            case SPRING -> SUMMER;
            case SUMMER -> FALL;
            case FALL -> WINTER;
            case WINTER -> SPRING;
        };
    }

    /**
     * Get season from day of year (0-364).
     */
    public static Season fromDayOfYear(int dayOfYear) {
        int day = dayOfYear % 365;
        if (day < 91)
            return SPRING; // Mar-May
        if (day < 182)
            return SUMMER; // Jun-Aug
        if (day < 273)
            return FALL; // Sep-Nov
        return WINTER; // Dec-Feb
    }
}
