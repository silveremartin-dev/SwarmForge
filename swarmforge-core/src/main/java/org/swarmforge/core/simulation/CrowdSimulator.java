/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.gpu.GpuExecutor;
import java.util.List;

/**
 * Crowd/flocking simulation using Structure of Arrays (SoA) layout.
 * Optimized for GPU execution with bulk updates.
 * 
 * Implements Boids-like behavior for efficient mass simulation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class CrowdSimulator {

    private final GpuExecutor gpu;

    // Structure of Arrays for GPU-friendly access
    private float[] positionsX;
    private float[] positionsY;
    private float[] positionsZ;
    private float[] headings;
    private float[] speeds;
    private float[] energies;
    private boolean[] alive;

    private int capacity;
    private int count;

    public CrowdSimulator(GpuExecutor gpu, int initialCapacity) {
        this.gpu = gpu;
        this.capacity = initialCapacity;
        allocateArrays(initialCapacity);
    }

    private void allocateArrays(int size) {
        positionsX = new float[size];
        positionsY = new float[size];
        positionsZ = new float[size];
        headings = new float[size];
        speeds = new float[size];
        energies = new float[size];
        alive = new boolean[size];
    }

    /**
     * Load individuals from a colony into SoA format.
     */
    public void loadFromColony(Colony colony) {
        List<Individual> individuals = colony.getLivingIndividuals();
        count = individuals.size();

        if (count > capacity) {
            capacity = count * 2;
            allocateArrays(capacity);
        }

        for (int i = 0; i < count; i++) {
            Individual ind = individuals.get(i);
            positionsX[i] = ind.getX();
            positionsY[i] = ind.getY();
            positionsZ[i] = ind.getZ();
            headings[i] = ind.getHeading();
            speeds[i] = 0.5f; // Default speed
            energies[i] = ind.getEnergy();
            alive[i] = ind.isAlive();
        }
    }

    /**
     * Execute one simulation step using GPU-accelerated updates.
     */
    public void step(float[] pheromones, int width, int height, int depth) {
        if (count == 0)
            return;

        // Pack positions for GPU
        float[] positions = packPositions();
        float[] gradients = new float[count];

        // Calculate pheromone gradients (where to go)
        gpu.executeGradientCalc(
                positions, pheromones, gradients,
                width, height, depth,
                0, // Follow food pheromone
                count);

        // Update headings based on gradients
        for (int i = 0; i < count; i++) {
            if (!Float.isNaN(gradients[i])) {
                // Blend current heading with gradient direction
                float targetHeading = gradients[i];
                float diff = targetHeading - headings[i];
                // Normalize angle
                while (diff > Math.PI)
                    diff -= 2 * Math.PI;
                while (diff < -Math.PI)
                    diff += 2 * Math.PI;
                headings[i] += diff * 0.1f; // Turn rate
            }
        }

        // Update positions
        gpu.executePositionUpdate(positions, headings, speeds, count);

        // Unpack positions
        unpackPositions(positions);

        // Update energy
        for (int i = 0; i < count; i++) {
            energies[i] -= 0.01f;
            if (energies[i] <= 0) {
                alive[i] = false;
            }
        }
    }

    /**
     * Write SoA data back to Individual objects.
     */
    public void syncToColony(Colony colony) {
        List<Individual> individuals = colony.getLivingIndividuals();
        int syncCount = Math.min(count, individuals.size());

        for (int i = 0; i < syncCount; i++) {
            Individual ind = individuals.get(i);
            ind.setPosition(positionsX[i], positionsY[i], positionsZ[i]);
            ind.setHeading(headings[i]);
            ind.setEnergy(energies[i]);
        }
    }

    private float[] packPositions() {
        float[] packed = new float[count * 3];
        for (int i = 0; i < count; i++) {
            packed[i * 3] = positionsX[i];
            packed[i * 3 + 1] = positionsY[i];
            packed[i * 3 + 2] = positionsZ[i];
        }
        return packed;
    }

    private void unpackPositions(float[] packed) {
        for (int i = 0; i < count; i++) {
            positionsX[i] = packed[i * 3];
            positionsY[i] = packed[i * 3 + 1];
            positionsZ[i] = packed[i * 3 + 2];
        }
    }

    public int getCount() {
        return count;
    }

    public int getLivingCount() {
        int living = 0;
        for (int i = 0; i < count; i++) {
            if (alive[i])
                living++;
        }
        return living;
    }
}
