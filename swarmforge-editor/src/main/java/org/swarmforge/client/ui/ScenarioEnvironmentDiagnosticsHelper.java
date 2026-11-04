/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.core.species.Species;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Diagnostic helper estimating ambient microclimate, water table depth,
 * and recommending compatible biomes for species tolerance envelopes.
 */
final class ScenarioEnvironmentDiagnosticsHelper {

    private ScenarioEnvironmentDiagnosticsHelper() {}

    static float estimateWaterTableDepth(String world) {
        if (world == null) return 15.0f;
        String s = world.toLowerCase();
        if (s.contains("marécage") || s.contains("swamp") || s.contains("wetland") || s.contains("mangrove")) {
            return 4.0f;
        } else if (s.contains("tropical") || s.contains("jungle") || s.contains("rainforest") || s.contains("amazon")) {
            return 8.0f;
        } else if (s.contains("arid") || s.contains("desert") || s.contains("désert") || s.contains("savanna")) {
            return 35.0f;
        } else if (s.contains("alpin") || s.contains("toundra") || s.contains("taïga")) {
            return 10.0f;
        }
        return 15.0f;
    }

    static float estimateAmbientTemperature(String world, String weather) {
        String wStr = (world != null ? world : "") + " " + (weather != null ? weather : "");
        String s = wStr.toLowerCase();

        Matcher matcher = Pattern.compile("(-?\\d+(?:\\.\\d+)?)\\s*°?C").matcher(wStr);
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        if (s.contains("arctic") || s.contains("polar") || s.contains("toundra") || s.contains("alpin") || s.contains("taïga") || s.contains("taiga")) {
            return 8.0f;
        } else if (s.contains("arid") || s.contains("desert") || s.contains("désert") || s.contains("chebbi") || s.contains("savanna")) {
            return 32.0f;
        } else if (s.contains("tropical") || s.contains("manaus") || s.contains("rainforest") || s.contains("jungle") || s.contains("amazon")) {
            return 28.0f;
        } else if (s.contains("mediterran") || s.contains("garrigue")) {
            return 25.0f;
        } else {
            return 20.0f;
        }
    }

    static float estimateAmbientHumidity(String world, String weather) {
        String wStr = (world != null ? world : "") + " " + (weather != null ? weather : "");
        String s = wStr.toLowerCase();

        Matcher matcher = Pattern.compile("(\\d+)\\s*%").matcher(wStr);
        if (matcher.find()) {
            try {
                return Float.parseFloat(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }

        if (s.contains("arid") || s.contains("desert") || s.contains("désert") || s.contains("chebbi")) {
            return 25.0f;
        } else if (s.contains("tropical") || s.contains("manaus") || s.contains("rainforest") || s.contains("jungle") || s.contains("amazon") || s.contains("humid")) {
            return 85.0f;
        } else if (s.contains("savanna") || s.contains("mediterran") || s.contains("garrigue")) {
            return 45.0f;
        } else if (s.contains("arctic") || s.contains("polar") || s.contains("toundra") || s.contains("taïga") || s.contains("taiga")) {
            return 50.0f;
        } else {
            return 65.0f;
        }
    }

    static void autoSelectCompatibleEnvironmentForSpecies(SimulationControlPanel panel, Species sp) {
        if (sp == null || panel == null) return;
        float minT = sp.getMinTempCelsius();
        float maxT = sp.getMaxTempCelsius();
        float minH = sp.getMinHumidityPercent();

        if (minT >= 26.0f || minH <= 25.0f) {
            SpeciesConfigCard.selectComboIfPresent(panel.comboWorld, "Acacia Savanna (Serengeti, TZ)");
            SpeciesConfigCard.selectComboIfPresent(panel.comboWeather, "Savanna");
        } else if (maxT <= 18.0f) {
            SpeciesConfigCard.selectComboIfPresent(panel.comboWorld, "Boreal Taiga (Rovaniemi, FI)");
            SpeciesConfigCard.selectComboIfPresent(panel.comboWeather, "Taiga");
        } else if (minH >= 65.0f || minT >= 22.0f) {
            SpeciesConfigCard.selectComboIfPresent(panel.comboWorld, "Tropical Rainforest (Manaus, BR)");
            SpeciesConfigCard.selectComboIfPresent(panel.comboWeather, "Tropical");
        } else {
            SpeciesConfigCard.selectComboIfPresent(panel.comboWorld, "Temperate Deciduous (Fontainebleau, FR)");
            SpeciesConfigCard.selectComboIfPresent(panel.comboWeather, "Temperate");
        }
    }
}
