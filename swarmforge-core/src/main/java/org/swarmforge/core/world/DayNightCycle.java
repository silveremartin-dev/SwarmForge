/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.world;

/**
 * Day/Night cycle system with lighting and behavior modifiers.
 *
 * @author Gemini AI Assistant
 */
public class DayNightCycle {

    public enum TimeOfDay {
        DAWN(0.0f, 0.25f, 0.6f), // Low light, warming
        DAY(0.25f, 0.75f, 1.0f), // Full light
        DUSK(0.75f, 0.85f, 0.5f), // Cooling
        NIGHT(0.85f, 1.0f, 0.1f); // Dark

        private final float startPhase;
        private final float endPhase;
        private final float lightLevel;

        TimeOfDay(float start, float end, float light) {
            this.startPhase = start;
            this.endPhase = end;
            this.lightLevel = light;
        }

        public float getLightLevel() {
            return lightLevel;
        }
    }

    private float currentPhase = 0.25f; // Start at morning
    private boolean paused = false;

    // Configurable day length in simulation ticks
    private int ticksPerDay = 2400; // 40 minutes at 60 tps
    private long tickCount = 0;

    /**
     * Advance the cycle by one tick.
     */
    public void tick() {
        if (paused)
            return;
        tickCount++;
        currentPhase = (tickCount % ticksPerDay) / (float) ticksPerDay;
    }

    /**
     * Get current time of day.
     */
    public TimeOfDay getTimeOfDay() {
        for (TimeOfDay tod : TimeOfDay.values()) {
            if (currentPhase >= tod.startPhase && currentPhase < tod.endPhase) {
                return tod;
            }
        }
        return TimeOfDay.NIGHT; // Wrap around
    }

    /**
     * Get light level (0-1) for rendering.
     */
    public float getLightLevel() {
        // Smooth interpolation between time periods
        TimeOfDay current = getTimeOfDay();
        TimeOfDay next = TimeOfDay.values()[(current.ordinal() + 1) % TimeOfDay.values().length];

        float phaseInPeriod = (currentPhase - current.startPhase) / (current.endPhase - current.startPhase);
        return lerp(current.lightLevel, next.lightLevel, phaseInPeriod);
    }

    /**
     * Get sun angle for shadows (0 = sunrise, 0.5 = noon, 1 = sunset).
     */
    public float getSunAngle() {
        // Sun rises at phase 0.0, peaks at 0.5, sets at 0.75
        if (currentPhase < 0.75f) {
            return currentPhase / 0.75f;
        }
        return 0; // Night
    }

    /**
     * Get ambient color tint for rendering.
     * Returns RGB as float[3].
     */
    public float[] getAmbientColor() {
        return switch (getTimeOfDay()) {
            case DAWN -> new float[] { 1.0f, 0.8f, 0.6f }; // Orange/pink
            case DAY -> new float[] { 1.0f, 1.0f, 1.0f }; // White
            case DUSK -> new float[] { 1.0f, 0.6f, 0.4f }; // Red/orange
            case NIGHT -> new float[] { 0.3f, 0.3f, 0.5f }; // Blue/grey
        };
    }

    // === Behavior Modifiers ===

    /**
     * Activity modifier for surface activities (foraging, etc).
     * Nocturnal species use inverse.
     */
    public float getSurfaceActivityModifier() {
        return switch (getTimeOfDay()) {
            case DAY -> 1.0f;
            case DAWN, DUSK -> 0.7f;
            case NIGHT -> 0.2f;
        };
    }

    /**
     * Temperature modifier based on time of day.
     */
    public float getTemperatureModifier() {
        return switch (getTimeOfDay()) {
            case DAWN -> 0.7f;
            case DAY -> 1.0f;
            case DUSK -> 0.8f;
            case NIGHT -> 0.5f;
        };
    }

    /**
     * Is it currently night time?
     */
    public boolean isNight() {
        return getTimeOfDay() == TimeOfDay.NIGHT;
    }

    /**
     * Get hour of day (0-23).
     */
    public int getHour() {
        return (int) (currentPhase * 24);
    }

    // === Configuration ===

    public void setTicksPerDay(int ticks) {
        this.ticksPerDay = ticks;
    }

    public int getTicksPerDay() {
        return ticksPerDay;
    }

    public void setPhase(float phase) {
        this.currentPhase = phase % 1.0f;
    }

    public float getPhase() {
        return currentPhase;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isPaused() {
        return paused;
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * Math.max(0, Math.min(1, t));
    }
}
