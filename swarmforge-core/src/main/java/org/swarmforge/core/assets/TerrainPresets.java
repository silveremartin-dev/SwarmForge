/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.assets;

/**
 * Pre-configured terrain presets for quick world generation.
 */
public enum TerrainPresets {

    TEMPERATE_FOREST("Temperate Forest",
            "Rolling hills with rich soil, deciduous forest cover",
            0.03f, // noise scale
            8.0f, // roughness
            0.6f, // soil depth ratio
            0.3f// stone percentage
    ),

    TROPICAL_RAINFOREST("Tropical Rainforest",
            "Dense multi-layer forest, high humidity, deep soil",
            0.02f,
            6.0f,
            0.8f,
            0.1f),

    DESERT("Desert",
            "Flat dunes and rocky outcrops, sparse vegetation",
            0.05f,
            3.0f,
            0.2f,
            0.6f),

    SAVANNA("Savanna",
            "Open grassland with scattered trees",
            0.04f,
            4.0f,
            0.5f,
            0.2f),

    MEDITERRANEAN("Mediterranean",
            "Rocky hills, dry climate, sparse shrubland",
            0.035f,
            10.0f,
            0.4f,
            0.5f),

    TUNDRA("Tundra",
            "Frozen permafrost, shallow soil, sparse lichen",
            0.02f,
            2.0f,
            0.15f,
            0.7f);

    private final String displayName;
    private final String description;
    private final float noiseScale;
    private final float roughness;
    private final float soilDepthRatio;
    private final float stonePercentage;

    TerrainPresets(String displayName, String description,
            float noiseScale, float roughness,
            float soilDepthRatio, float stonePercentage) {
        this.displayName = displayName;
        this.description = description;
        this.noiseScale = noiseScale;
        this.roughness = roughness;
        this.soilDepthRatio = soilDepthRatio;
        this.stonePercentage = stonePercentage;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public float getNoiseScale() {
        return noiseScale;
    }

    public float getRoughness() {
        return roughness;
    }

    public float getSoilDepthRatio() {
        return soilDepthRatio;
    }

    public float getStonePercentage() {
        return stonePercentage;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
