/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.ui.InterventionPanel.Category;
import org.swarmforge.client.ui.InterventionPanel.ScheduledEvent;

/**
 * Schedules and executes God-Mode intervention events for {@link InterventionPanel}.
 */
class InterventionEventScheduler {

    private final InterventionPanel panel;
    private final InterventionCategoryBuilder ui;

    InterventionEventScheduler(InterventionPanel panel, InterventionCategoryBuilder ui) {
        this.panel = panel;
        this.ui = ui;
    }

    void scheduleEntitiesEvent() {
        String colTarget = (panel.eventColonySelect != null && panel.eventColonySelect.getValue() != null) ? panel.eventColonySelect.getValue() : "All Colonies";
        String actionStr = ui.entityActionSelect.getValue();
        String caste = ui.casteSelect.getValue();
        int count = ui.antCountSpinner.getValue();
        float x = (float) panel.posXSlider.getValue();
        float y = (float) panel.posYSlider.getValue();
        float z = panel.getCalculatedSurfaceZ(x, y);

        ScheduledEvent ev = panel.createBaseEvent(Category.ENTITIES, actionStr,
                String.format("%s %d x %s à (%.1f, %.1f, %.1f)", actionStr, count, caste, x, y, z));
        ev.colonyTarget = colTarget;
        ev.caste = caste;
        ev.count = count;
        ev.posX = x; ev.posY = y; ev.posZ = z;
        ev.entityAction = actionStr.contains("Injection") || actionStr.contains("Apparition") ? "SPAWN" :
                          actionStr.contains("Couvain") ? "BROOD_KILL" :
                          actionStr.contains("Extinction") ? "EXTINCT" : "KILL";

        panel.addAndSortScheduledEvent(ev);
    }

    void scheduleResourceEvent() {
        String resType = ui.resourceTypeSelect.getValue();
        String foodNat = ui.foodNatureSelect.getValue();
        float qty = (float) ui.foodSlider.getValue();
        float x = (float) panel.posXSlider.getValue();
        float y = (float) panel.posYSlider.getValue();
        float z = panel.getCalculatedSurfaceZ(x, y);

        String unit = "g";
        if ((resType != null && resType.contains("Water")) ||
            (foodNat != null && (foodNat.contains("Honeydew") || foodNat.contains("Nectar") || foodNat.contains("Jelly")))) {
            unit = "mL";
        }

        ScheduledEvent ev = panel.createBaseEvent(Category.RESOURCE, resType,
                String.format("Apport %.0f %s (%s) - %s à (%.1f, %.1f, %.1f)", qty, unit, resType, foodNat, x, y, z));
        ev.resourceType = resType;
        ev.foodNature = foodNat;
        ev.amount = qty;
        ev.posX = x; ev.posY = y; ev.posZ = z;

        panel.addAndSortScheduledEvent(ev);
    }

    void scheduleDisasterEvent() {
        String disasterType = ui.disasterSelect.getValue();
        float intensity = (float) ui.intensitySlider.getValue();
        int durMins = (int) ui.durationSlider.getValue();

        ScheduledEvent ev = panel.createBaseEvent(Category.DISASTER, disasterType,
                String.format("Catastrophe %s (Intensité: %.1f, Durée: %d min)", disasterType, intensity, durMins));
        ev.disasterType = disasterType;
        ev.intensity = intensity;
        ev.durationMinutes = durMins;

        panel.addAndSortScheduledEvent(ev);
    }

    void scheduleAbioticEvent() {
        boolean isRel = ui.abioticModeSelect.getValue() != null && ui.abioticModeSelect.getValue().contains("Relative");
        int durMins = (int) ui.abioticDurationSlider.getValue();
        float valTemp = (float) ui.deltaTempSlider.getValue();
        float valHum = (float) ui.deltaHumiditySlider.getValue();
        float valWind = (float) ui.deltaWindSlider.getValue();
        float valSolar = (float) ui.deltaSolarSlider.getValue();

        String desc = isRel ?
                String.format("Δ Climat (%d min) : ΔT=%+.1f°C, ΔH=%+.0f%%, ΔVent=%+.1fm/s, ΔSol=%+.0fW/m²", durMins, valTemp, valHum, valWind, valSolar) :
                String.format("Climat Absolu (%d min) : T=%.1f°C, H=%.0f%%, Vent=%.1fm/s, Sol=%.0fW/m²", durMins, valTemp, valHum, valWind, valSolar);

        ScheduledEvent ev = panel.createBaseEvent(Category.ABIOTIC, isRel ? "Variation Climat" : "Climat Absolu Fixe", desc);
        ev.isRelativeAbiotic = isRel;
        ev.durationMinutes = durMins;
        if (isRel) {
            ev.deltaTempCelsius = valTemp;
            ev.deltaHumidityPercent = valHum;
            ev.deltaWindMetersPerSec = valWind;
            ev.deltaSolarWattsPerM2 = valSolar;
        } else {
            ev.tempCelsius = valTemp;
            ev.humidityPercent = valHum;
            ev.windMetersPerSec = valWind;
            ev.solarWattsPerM2 = valSolar;
        }

        panel.addAndSortScheduledEvent(ev);
    }

    void schedulePheromoneEvent() {
        String type = ui.pheromoneTypeSelect.getValue();
        float radius = (float) ui.pheroRadiusSlider.getValue();
        float intensity = (float) ui.pheroIntensitySlider.getValue();
        int durMins = (int) ui.pheroDurationSlider.getValue();
        float x = (float) panel.posXSlider.getValue();
        float y = (float) panel.posYSlider.getValue();
        float z = panel.getCalculatedSurfaceZ(x, y);

        ScheduledEvent ev = panel.createBaseEvent(Category.PHEROMONE, type,
                String.format("%s (Rayon: %.1fm, Intensité: %.1f, Durée: %d min) à (%.1f, %.1f, %.1f)", type, radius, intensity, durMins, x, y, z));
        ev.radius = radius;
        ev.intensity = intensity;
        ev.durationMinutes = durMins;
        ev.posX = x; ev.posY = y; ev.posZ = z;

        panel.addAndSortScheduledEvent(ev);
    }

    void scheduleInvasionEvent() {
        String type = ui.invasionTypeSelect.getValue();
        boolean isProtection = type != null && (type.contains("Protection") || type.contains("Absence") || type.contains("Sanctuary"));
        int count = isProtection ? 0 : ui.invasionCountSpinner.getValue();
        int durMins = (int) ui.invasionDurationSlider.getValue();
        float x = (float) panel.posXSlider.getValue();
        float y = (float) panel.posYSlider.getValue();
        float z = panel.getCalculatedSurfaceZ(x, y);

        String descStr = (count == 0 || isProtection)
                ? String.format("🛡️ Protection Prédatrice / Absence de Raids (%s, Durée: %d min)", type, durMins)
                : String.format("⚔️ %s (%d spécimens, Durée: %d min) à (%.1f, %.1f, %.1f)", type, count, durMins, x, y, z);

        ScheduledEvent ev = panel.createBaseEvent(Category.INVASION, type, descStr);
        ev.count = count;
        ev.durationMinutes = durMins;
        ev.posX = x; ev.posY = y; ev.posZ = z;

        panel.addAndSortScheduledEvent(ev);
    }

    void scheduleMutationEvent() {
        String type = ui.mutationTypeSelect.getValue();
        String casteScope = ui.mutationCasteScopeSelect != null ? ui.mutationCasteScopeSelect.getValue() : "👑 All Castes";
        float intensity = (float) ui.mutationIntensitySlider.getValue();
        int durMins = (int) ui.mutationDurationSlider.getValue();
        String targetCol = (panel.eventColonySelect != null && panel.eventColonySelect.getValue() != null) ? panel.eventColonySelect.getValue() : "All Colonies";

        String descStr = (intensity < 1.0f)
                ? String.format("%s sur %s [%s] (Afaiblissement: %.2fx / -%.0f%%, Durée: %d min)", type, targetCol, casteScope, intensity, (1.0f - intensity) * 100.0f, durMins)
                : String.format("%s sur %s [%s] (Intensité: %.1fx / +%.0f%%, Durée: %d min)", type, targetCol, casteScope, intensity, (intensity - 1.0f) * 100.0f, durMins);

        ScheduledEvent ev = panel.createBaseEvent(Category.MUTATION, type, descStr);
        ev.intensity = intensity;
        ev.durationMinutes = durMins;
        ev.caste = casteScope;
        ev.colonyTarget = targetCol;

        panel.addAndSortScheduledEvent(ev);
    }

    void executeScheduledEvent(ScheduledEvent ev) {
        String colStr = (ev.colonyTarget != null && !ev.colonyTarget.contains("Global")) ? ev.colonyTarget : "Colonie Primaire";
        if (panel.callback != null) {
            switch (ev.category) {
                case ENTITIES -> {
                    if (ev.caste != null && (ev.caste.contains("Brood") || ev.caste.contains("Couvain") || ev.caste.contains("Œufs") || ev.caste.contains("Larves") || ev.caste.contains("Nymphes"))) {
                        panel.callback.manageBrood(colStr, ev.caste, ev.entityAction, ev.count);
                    } else if ("KILL".equals(ev.entityAction) || "EXTINCT".equals(ev.entityAction) || "BROOD_KILL".equals(ev.entityAction)) {
                        panel.callback.killAnts(colStr, ev.caste, "EXTINCT".equals(ev.entityAction) ? 99999 : ev.count);
                    } else {
                        panel.callback.spawnAnts(colStr, ev.caste, ev.count, ev.posX, ev.posY, ev.posZ);
                    }
                }
                case RESOURCE -> panel.callback.spawnFood(ev.posX, ev.posY, ev.posZ, ev.amount);
                case DISASTER -> panel.callback.triggerDisaster(ev.disasterType, ev.intensity);
                case ABIOTIC -> {
                    if (ev.isRelativeAbiotic) {
                        panel.callback.modifyAbioticClimate(true, ev.deltaTempCelsius, ev.deltaHumidityPercent, ev.deltaWindMetersPerSec, ev.deltaSolarWattsPerM2, ev.durationMinutes);
                    } else {
                        panel.callback.modifyAbioticClimate(false, ev.tempCelsius, ev.humidityPercent, ev.windMetersPerSec, ev.solarWattsPerM2, ev.durationMinutes);
                        panel.callback.modifyParameter("temperatureCelsius", ev.tempCelsius);
                        panel.callback.modifyParameter("humidityPercent", ev.humidityPercent);
                        panel.callback.modifyParameter("windSpeed", ev.windMetersPerSec);
                        panel.callback.modifyParameter("solarRadiation", ev.solarWattsPerM2);
                    }
                }
                case PHEROMONE -> panel.callback.triggerPheromoneEvent(ev.eventType, ev.posX, ev.posY, ev.posZ, ev.intensity, ev.radius, ev.durationMinutes);
                case INVASION -> panel.callback.triggerInvasionEvent(ev.eventType, ev.posX, ev.posY, ev.posZ, ev.count, ev.durationMinutes);
                case MUTATION -> panel.callback.applyGeneticBoost(colStr, ev.eventType, ev.intensity, ev.durationMinutes);
            }
        }
    }
}
