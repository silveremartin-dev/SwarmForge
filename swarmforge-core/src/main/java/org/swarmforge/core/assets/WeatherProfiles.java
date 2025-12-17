/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.assets;

/**
 * Pre-configured weather profiles for different climates.
 */
public enum WeatherProfiles {

    TEMPERATE("Temperate",
            "Four distinct seasons, moderate precipitation",
            15.0f, 25.0f, // summer temp range
            -5.0f, 10.0f, // winter temp range
            0.3f, // rain probability
            0.1f// storm probability
    ),

    TROPICAL("Tropical",
            "Hot year-round, monsoon season, high humidity",
            28.0f, 35.0f,
            22.0f, 30.0f,
            0.6f,
            0.2f),

    ARID("Arid/Desert",
            "Hot days, cold nights, rare but intense storms",
            30.0f, 45.0f,
            5.0f, 20.0f,
            0.05f,
            0.15f),

    MEDITERRANEAN("Mediterranean",
            "Dry hot summers, mild wet winters",
            25.0f, 35.0f,
            8.0f, 18.0f,
            0.1f, // summer
            0.4f// winter bias handled separately
    ),

    ARCTIC("Arctic/Tundra",
            "Extremely cold, short summer, permafrost",
            5.0f, 15.0f,
            -30.0f, -5.0f,
            0.2f,
            0.3f);

    private final String name;
    private final String description;
    private final float summerTempMin, summerTempMax;
    private final float winterTempMin, winterTempMax;
    private final float rainProbability;
    private final float stormProbability;

    WeatherProfiles(String name, String description,
            float summerMin, float summerMax,
            float winterMin, float winterMax,
            float rainProb, float stormProb) {
        this.name = name;
        this.description = description;
        this.summerTempMin = summerMin;
        this.summerTempMax = summerMax;
        this.winterTempMin = winterMin;
        this.winterTempMax = winterMax;
        this.rainProbability = rainProb;
        this.stormProbability = stormProb;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public float getSummerTempMin() {
        return summerTempMin;
    }

    public float getSummerTempMax() {
        return summerTempMax;
    }

    public float getWinterTempMin() {
        return winterTempMin;
    }

    public float getWinterTempMax() {
        return winterTempMax;
    }

    public float getRainProbability() {
        return rainProbability;
    }

    public float getStormProbability() {
        return stormProbability;
    }

    @Override
    public String toString() {
        return name;
    }
}
