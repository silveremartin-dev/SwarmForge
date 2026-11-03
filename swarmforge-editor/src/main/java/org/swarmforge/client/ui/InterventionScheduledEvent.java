/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.ui;

import org.swarmforge.client.ui.InterventionPanel.Category;

/**
 * Scheduled God Mode intervention event with precise target tick, category, parameters, and execution state.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class InterventionScheduledEvent {
    public long targetTick;
    public String timeFormatted;
    public Category category;
    public String eventType;
    public String colonyTarget;
    public String description;

    // Category-specific parameters
    public String entityAction = "SPAWN"; // SPAWN, KILL, EXTINCT, BROOD_KILL, BROOD_SPAWN
    public String caste = "Worker";
    public int count = 10;
    // Default position: CENTER of map (32, 32) at terrain surface level (1.0)
    public float posX = 32.0f;
    public float posY = 32.0f;
    public float posZ = 1.0f;

    public String resourceType = "Surface Food";
    public String foodNature = "Seeds & Grains";
    public float amount = 100f;

    public String disasterType = "Flood & Heavy Rain";
    public float intensity = 0.5f;
    public int durationMinutes = 60;

    // Abiotic Climate Parameters (Relative Offset vs Absolute Target)
    public boolean isRelativeAbiotic = true;
    public float tempCelsius = 22.0f;
    public float humidityPercent = 65.0f;
    public float windMetersPerSec = 1.5f;
    public float solarWattsPerM2 = 450.0f;

    public float deltaTempCelsius = 5.0f;
    public float deltaHumidityPercent = 20.0f;
    public float deltaWindMetersPerSec = 2.0f;
    public float deltaSolarWattsPerM2 = 100.0f;

    public float radius = 5.0f;

    public boolean executed = false;
    public boolean paused = false;

    public InterventionScheduledEvent(long targetTick, String timeFormatted, Category category, String eventType, String colonyTarget, String description) {
        this.targetTick = targetTick;
        this.timeFormatted = timeFormatted;
        this.category = category;
        this.eventType = eventType;
        this.colonyTarget = colonyTarget;
        this.description = description;
    }

    public InterventionPanel.ScheduledEvent copyWithOffset(long additionalTicks, double stepSec) {
        long newTarget = this.targetTick + additionalTicks;
        String newTime = InterventionPanel.formatTickToCalendarTime(newTarget, stepSec);
        InterventionPanel.ScheduledEvent clone = new InterventionPanel.ScheduledEvent(newTarget, newTime, this.category, this.eventType, this.colonyTarget, this.description);
        clone.entityAction = this.entityAction;
        clone.caste = this.caste;
        clone.count = this.count;
        clone.posX = this.posX;
        clone.posY = this.posY;
        clone.posZ = this.posZ;
        clone.resourceType = this.resourceType;
        clone.foodNature = this.foodNature;
        clone.amount = this.amount;
        clone.disasterType = this.disasterType;
        clone.intensity = this.intensity;
        clone.durationMinutes = this.durationMinutes;
        clone.isRelativeAbiotic = this.isRelativeAbiotic;
        clone.tempCelsius = this.tempCelsius;
        clone.humidityPercent = this.humidityPercent;
        clone.windMetersPerSec = this.windMetersPerSec;
        clone.solarWattsPerM2 = this.solarWattsPerM2;
        clone.deltaTempCelsius = this.deltaTempCelsius;
        clone.deltaHumidityPercent = this.deltaHumidityPercent;
        clone.deltaWindMetersPerSec = this.deltaWindMetersPerSec;
        clone.deltaSolarWattsPerM2 = this.deltaSolarWattsPerM2;
        clone.radius = this.radius;
        clone.executed = false;
        clone.paused = false;
        return clone;
    }
}
