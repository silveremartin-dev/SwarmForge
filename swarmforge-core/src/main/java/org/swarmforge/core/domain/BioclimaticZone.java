/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.Map;

/**
 * Bioclimatic Zone Classifier & Coupling System.
 * Connects geographic coordinates (Latitude, Longitude, Altitude), annual climate,
 * soil substrates, botanical tree species distribution, and insect species adequation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public enum BioclimaticZone {

    TROPICAL_RAINFOREST("🌴 Tropical Rainforest", 25.0f, 2200.0f, 85.0f,
            Map.of(TreeSpecies.ACACIA, 50, TreeSpecies.OAK, 20, TreeSpecies.BAMBOO, 15, TreeSpecies.DEAD_LOG, 15),
            "Atta, Oecophylla, Pseudomyrmex, Paraponera"),

    ARID_DESERT("🏜️ Arid Desert & Cactus", 32.0f, 120.0f, 20.0f,
            Map.of(TreeSpecies.CACTUS, 60, TreeSpecies.ACACIA, 20, TreeSpecies.DEAD_LOG, 20),
            "Cataglyphis, Myrmecocystus, Pogonomyrmex"),

    MEDITERRANEAN("🌿 Mediterranean Scrub & Maquis", 18.0f, 550.0f, 55.0f,
            Map.of(TreeSpecies.PINE, 50, TreeSpecies.OAK, 30, TreeSpecies.DEAD_LOG, 20),
            "Messor, Crematogaster, Aphaenogaster, Formica"),

    TEMPERATE_FOREST("🌳 Temperate Deciduous Forest", 12.0f, 850.0f, 65.0f,
            Map.of(TreeSpecies.OAK, 45, TreeSpecies.PINE, 25, TreeSpecies.BIRCH, 15, TreeSpecies.DEAD_LOG, 15),
            "Lasius, Formica, Camponotus, Temnothorax"),

    BOREAL_TAIGA("🌲 Boreal Forest / Taiga", 2.0f, 600.0f, 70.0f,
            Map.of(TreeSpecies.PINE, 60, TreeSpecies.BIRCH, 30, TreeSpecies.DEAD_LOG, 10),
            "Formica lugubris, Camponotus herculeanus"),

    ARCTIC_TUNDRA("❄️ Arctic Tundra & Permafrost", -8.0f, 300.0f, 50.0f,
            Map.of(TreeSpecies.BIRCH, 40, TreeSpecies.DEAD_LOG, 20),
            "Leptothorax acervorum, Formica gagates");

    private final String displayName;
    private final float avgTemperature;
    private final float annualRainfallMm;
    private final float avgHumidity;
    private final Map<TreeSpecies, Integer> defaultTreeDistribution;
    private final String recommendedInsectSpecies;

    BioclimaticZone(String displayName, float avgTemperature, float annualRainfallMm, float avgHumidity,
                    Map<TreeSpecies, Integer> defaultTreeDistribution, String recommendedInsectSpecies) {
        this.displayName = displayName;
        this.avgTemperature = avgTemperature;
        this.annualRainfallMm = annualRainfallMm;
        this.avgHumidity = avgHumidity;
        this.defaultTreeDistribution = defaultTreeDistribution;
        this.recommendedInsectSpecies = recommendedInsectSpecies;
    }

    public static BioclimaticZone classify(double lat, double annualTemp, double annualRainfall) {
        double absLat = Math.abs(lat);
        if (absLat > 65 || annualTemp < 0) {
            return ARCTIC_TUNDRA;
        } else if (annualRainfall < 220) {
            return ARID_DESERT;
        } else if (absLat < 23.5 && annualRainfall > 1500) {
            return TROPICAL_RAINFOREST;
        } else if (absLat >= 30 && absLat <= 45 && annualRainfall < 700 && annualTemp > 14) {
            return MEDITERRANEAN;
        } else if (annualTemp < 6) {
            return BOREAL_TAIGA;
        } else {
            return TEMPERATE_FOREST;
        }
    }

    public String getDisplayName() { return displayName; }
    public float getAvgTemperature() { return avgTemperature; }
    public float getAnnualRainfallMm() { return annualRainfallMm; }
    public float getAvgHumidity() { return avgHumidity; }
    public Map<TreeSpecies, Integer> getDefaultTreeDistribution() { return defaultTreeDistribution; }
    public String getRecommendedInsectSpecies() { return recommendedInsectSpecies; }
}
