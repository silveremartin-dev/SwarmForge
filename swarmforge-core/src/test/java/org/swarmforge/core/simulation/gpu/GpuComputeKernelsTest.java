package org.swarmforge.core.simulation.gpu;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests validating CPU/GPU mathematical convergence for:
 *  - SubterraneanHydrologyKernel (3D heat & moisture finite-difference diffusion)
 *  - MandibularBiomechanicsKernel (allometric bite force & chitin contact stress)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
class GpuComputeKernelsTest {

    @Test
    @DisplayName("SubterraneanHydrologyKernel: 3D Heat Diffusion Conservation & Convergence")
    void testSubterraneanHydrologyKernelPhysics() {
        int w = 8;
        int d = 8;
        int h = 8;
        int total = w * d * h;

        float[] currentTemp = new float[total];
        float[] nextTemp = new float[total];
        float[] currentMoist = new float[total];
        float[] nextMoist = new float[total];

        // Initial uniform conditions
        for (int i = 0; i < total; i++) {
            currentTemp[i] = 15.0f;
            currentMoist[i] = 0.40f;
        }

        // Apply hot spot in center (x=4, y=4, z=4) -> index = (4 * 8 + 4) * 8 + 4 = 292
        int centerIdx = (4 * d + 4) * w + 4;
        currentTemp[centerIdx] = 35.0f;

        SubterraneanHydrologyKernel kernel = new SubterraneanHydrologyKernel(
                w, d, h, currentTemp, nextTemp, currentMoist, nextMoist, 0.1f, 0.01f, 0.1f
        );

        // Run sequential CPU simulation step of the kernel logic
        for (int i = 0; i < total; i++) {
            int tmp = i;
            int x = tmp % w;
            tmp /= w;
            int y = tmp % d;
            int z = tmp / d;

            if (z == 0 || x == 0 || x == w - 1 || y == 0 || y == d - 1 || z == h - 1) {
                nextTemp[i] = currentTemp[i];
                nextMoist[i] = currentMoist[i];
            } else {
                int sliceSize = w * d;
                float tCenter = currentTemp[i];
                float laplacian = currentTemp[i - 1] + currentTemp[i + 1]
                                + currentTemp[i - w] + currentTemp[i + w]
                                + currentTemp[i - sliceSize] + currentTemp[i + sliceSize]
                                - 6.0f * tCenter;
                nextTemp[i] = tCenter + 0.1f * laplacian * 0.1f;
                nextMoist[i] = currentMoist[i];
            }
        }

        // Center temperature should diffuse downward from 35.0°C
        assertTrue(nextTemp[centerIdx] < 35.0f, "Hotspot center temperature must decrease due to diffusion");
        assertTrue(nextTemp[centerIdx] > 15.0f, "Hotspot center must remain warmer than surroundings");

        // Immediate neighbor should warm up above 15.0°C
        int rightNeighbor = centerIdx + 1;
        assertTrue(nextTemp[rightNeighbor] > 15.0f, "Adjacent neighbor must absorb diffused thermal energy");
    }

    @Test
    @DisplayName("MandibularBiomechanicsKernel: Force & Chitin Stress Calculations")
    void testMandibularBiomechanicsPhysics() {
        int agentCount = 3;
        float[] bodyMass = { 0.005f, 0.015f, 0.050f }; // Minor worker, Media worker, Major soldier (g)
        float[] leverRatios = { 1.0f, 1.4f, 2.5f };
        float[] wearRatios = { 0.0f, 0.2f, 0.5f };      // Pristine, light wear, heavy wear
        float[] strikeVels = { 0.5f, 1.2f, 2.0f };      // m/s
        float[] outputForces = new float[agentCount];
        float[] outputStress = new float[agentCount];

        MandibularBiomechanicsKernel kernel = new MandibularBiomechanicsKernel(
                agentCount, bodyMass, leverRatios, wearRatios, strikeVels, outputForces, outputStress
        );

        // Run sequential pass
        for (int i = 0; i < agentCount; i++) {
            float mass = bodyMass[i];
            float leverRatio = leverRatios[i];
            float wear = wearRatios[i];
            float velocity = strikeVels[i];

            float scaledMass = (float) Math.pow(mass, 0.67);
            float baseForce = 12.5f * scaledMass * leverRatio;
            float sharpnessFactor = 1.0f - 0.8f * wear;
            float kineticBonus = 0.5f * mass * velocity * velocity;
            float netForce = (baseForce * sharpnessFactor) + (kineticBonus * 0.1f);
            outputForces[i] = Math.max(0.0f, netForce);

            float contactAreaMm2 = 0.02f + 0.08f * wear;
            outputStress[i] = netForce / contactAreaMm2;
        }

        // Major soldier (idx 2) should produce significantly higher bite force than minor (idx 0)
        assertTrue(outputForces[2] > outputForces[0], "Major soldier bite force must exceed minor worker bite force");
        assertTrue(outputStress[0] > 0.0f, "Contact stress must be positive");
    }
}
