/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

/**
 * Honeybee (Apis mellifera) Waggle Dance & Recruitment Vector System.
 * Translates distance and direction to rich floral patches into a symbolic figure-eight dance on vertical comb.
 * Angle relative to gravity = angle relative to solar azimuth.
 * Dance duration = distance to patch.
 * Abdomen waggle frequency = patch nectar profitability (Brix % sugar concentration).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WaggleDanceSystem {

    public static class WaggleDanceMessage {
        public final float solarAngleRad;  // Direction relative to sun
        public final float distanceMeters; // Distance to resource patch
        public final float brixProfitability; // Sugar concentration (0-100%)

        public WaggleDanceMessage(float solarAngleRad, float distanceMeters, float brixProfitability) {
            this.solarAngleRad = solarAngleRad;
            this.distanceMeters = distanceMeters;
            this.brixProfitability = brixProfitability;
        }
    }

    /**
     * Encodes a discovered floral resource patch into a Waggle Dance message.
     */
    public static WaggleDanceMessage encodeFloralPatch(float patchX, float patchY, float hiveX, float hiveY,
                                                      float sunAzimuthRad, float brixSugarPercent) {
        float dx = patchX - hiveX;
        float dy = patchY - hiveY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float resourceAngle = (float) Math.atan2(dy, dx);
        float relativeSolarAngle = resourceAngle - sunAzimuthRad;

        return new WaggleDanceMessage(relativeSolarAngle, distance, brixSugarPercent);
    }

    /**
     * Calculates the duration of the waggle phase in seconds (1 sec ~ 1000m distance).
     */
    public static float calculateDanceDurationSec(WaggleDanceMessage dance) {
        return Math.max(0.5f, dance.distanceMeters / 1000.0f);
    }
}
