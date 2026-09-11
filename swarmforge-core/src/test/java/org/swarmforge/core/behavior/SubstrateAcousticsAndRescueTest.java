/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.structure.physics.SubstrateAcoustics;
import org.swarmforge.core.structure.physics.VoxelMaterial;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Substrate Bio-Acoustic Wave Propagation & Rescue Triangulation.
 */
class SubstrateAcousticsAndRescueTest {

    @Test
    @DisplayName("Verify Acoustic Wave Velocities across Different Substrates")
    void testWaveVelocities() {
        float vAir = SubstrateAcoustics.computeWaveVelocity(VoxelMaterial.AIR);
        assertEquals(343.0f, vAir, 1.0f, "Air sound speed should be ~343 m/s");

        float vSoil = SubstrateAcoustics.computeWaveVelocity(VoxelMaterial.SOIL);
        float vClay = SubstrateAcoustics.computeWaveVelocity(VoxelMaterial.CLAY);
        float vStone = SubstrateAcoustics.computeWaveVelocity(VoxelMaterial.STONE);

        assertTrue(vSoil > 100.0f, "Soil wave velocity should be > 100 m/s");
        assertTrue(vClay > vSoil, "Dense clay should transmit faster than loose soil");
        assertTrue(vStone > vClay, "Solid stone should have highest elastic wave velocity");
    }

    @Test
    @DisplayName("Verify Acoustic Attenuation & Distress Call Audibility in Substrate")
    void testAttenuationAndAudibility() {
        Terrarium terrarium = new Terrarium(50, 50, 30);
        float sourceDb = 80.0f;
        float freqHz = 850.0f;

        // Close distance (3 cells away): should be audible
        boolean audibleClose = SubstrateAcoustics.isSignalAudible(sourceDb, freqHz, 10f, 10f, 5f, 12f, 10f, 5f, terrarium);
        assertTrue(audibleClose, "Distress call at 2-3 cells distance should be audible to subgenual organs");

        // Far distance (45 cells away): should be heavily attenuated below 28 dB
        boolean audibleFar = SubstrateAcoustics.isSignalAudible(sourceDb, freqHz, 2f, 2f, 2f, 48f, 48f, 28f, terrarium);
        assertFalse(audibleFar, "Distress call across entire terrarium should be attenuated below threshold");
    }

    @Test
    @DisplayName("Verify Acoustic Gradient Direction Vector for Rescue Triangulation")
    void testAcousticGradientVector() {
        float[] gradient = SubstrateAcoustics.computeAcousticGradientVector(10f, 10f, 5f, 20f, 10f, 5f);
        assertNotNull(gradient);
        assertEquals(3, gradient.length);
        assertEquals(1.0f, gradient[0], 0.01f, "Gradient X should point towards source (+X)");
        assertEquals(0.0f, gradient[1], 0.01f, "Gradient Y should be zero");
        assertEquals(0.0f, gradient[2], 0.01f, "Gradient Z should be zero");
    }
}
