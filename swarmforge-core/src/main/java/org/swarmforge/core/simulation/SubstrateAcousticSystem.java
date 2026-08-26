/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;

/**
 * Substrate Acoustic Vibration & Seismic Alarm Cascade System.
 * Models head-banging substrate vibrations (~1000 Hz) propagated through wood/soil tunnels
 * triggering instant colony-wide defensive alert state within milliseconds.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SubstrateAcousticSystem {

    public static class AcousticWave {
        public final float originX, originY, originZ;
        public final float frequencyHz;
        public final float amplitudeDb;
        public float remainingTicks;

        public AcousticWave(float x, float y, float z, float frequencyHz, float amplitudeDb) {
            this.originX = x;
            this.originY = y;
            this.originZ = z;
            this.frequencyHz = frequencyHz;
            this.amplitudeDb = amplitudeDb;
            this.remainingTicks = 5; // Fast transient wave
        }
    }

    public static AcousticWave triggerHeadBangingAlarm(Individual performer, float x, float y, float z) {
        if (performer == null || performer.getSpecies() == null) return null;
        if (!performer.getSpecies().canDrumSubstrate()) return null;

        return new AcousticWave(x, y, z, 1000.0f, 45.0f);
    }

    public static boolean perceiveSubstrateVibration(Individual listener, AcousticWave wave) {
        if (listener == null || wave == null || listener.getSpecies() == null) return false;
        if (!listener.getSpecies().hasSubstrateVibrationSensing()) return false;

        float dx = listener.getX() - wave.originX;
        float dy = listener.getY() - wave.originY;
        float dz = listener.getZ() - wave.originZ;
        float distSq = dx * dx + dy * dy + dz * dz;

        float threshold = listener.getSpecies().getVibrationSensitivityDb();
        float attenuatedDb = wave.amplitudeDb - (distSq * 0.5f);

        return attenuatedDb >= threshold;
    }
}
