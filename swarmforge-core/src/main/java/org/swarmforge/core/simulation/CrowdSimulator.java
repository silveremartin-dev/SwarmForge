/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;

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

    // Structure of Arrays (CPU)
    private float[] positionsX;
    private float[] positionsY;
    private float[] positionsZ;
    private float[] headings;
    private float[] speeds;
    private float[] energies;
    private boolean[] alive;

    private int capacity;
    private int count;

    public CrowdSimulator(int initialCapacity) {
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
     * Execute one simulation step using CPU updates with full Boids algorithm.
     * Implements Reynolds rules: Separation, Alignment, Cohesion.
     * 
     * Algorithm credit: Craig Reynolds (1986) - "Flocks, Herds, and Schools"
     */
    public void step(float[] pheromones, int width, int height, int depth) {
        if (count == 0)
            return;

        // Boids parameters
        final float NEIGHBOR_RADIUS = 10.0f;
        final float SEPARATION_RADIUS = 3.0f;
        final float SEPARATION_WEIGHT = 1.5f;
        final float ALIGNMENT_WEIGHT = 1.0f;
        final float COHESION_WEIGHT = 1.0f;
        final float PHEROMONE_WEIGHT = 0.8f;
        final float WANDER_WEIGHT = 0.3f;
        final float MAX_SPEED = 0.8f;
        final float MAX_FORCE = 0.05f;

        // Velocity arrays
        float[] velocitiesX = new float[count];
        float[] velocitiesY = new float[count];

        // Compute current velocities from heading and speed
        for (int i = 0; i < count; i++) {
            velocitiesX[i] = (float) Math.cos(headings[i]) * speeds[i];
            velocitiesY[i] = (float) Math.sin(headings[i]) * speeds[i];
        }

        // Calculate Boids forces for each individual
        for (int i = 0; i < count; i++) {
            if (!alive[i])
                continue;

            float separationX = 0, separationY = 0;
            float alignmentX = 0, alignmentY = 0;
            float cohesionX = 0, cohesionY = 0;
            int separationCount = 0;
            int flockCount = 0;

            // Check all neighbors (O(n²) - could optimize with spatial hash)
            for (int j = 0; j < count; j++) {
                if (i == j || !alive[j])
                    continue;

                float dx = positionsX[j] - positionsX[i];
                float dy = positionsY[j] - positionsY[i];
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < NEIGHBOR_RADIUS && dist > 0.001f) {
                    flockCount++;

                    // Alignment: average velocity of neighbors
                    alignmentX += velocitiesX[j];
                    alignmentY += velocitiesY[j];

                    // Cohesion: average position of neighbors
                    cohesionX += positionsX[j];
                    cohesionY += positionsY[j];

                    // Separation: steer away from close neighbors
                    if (dist < SEPARATION_RADIUS) {
                        float factor = (SEPARATION_RADIUS - dist) / SEPARATION_RADIUS;
                        separationX -= (dx / dist) * factor;
                        separationY -= (dy / dist) * factor;
                        separationCount++;
                    }
                }
            }

            // Normalize forces
            float forceX = 0, forceY = 0;

            // Separation force
            if (separationCount > 0) {
                forceX += (separationX / separationCount) * SEPARATION_WEIGHT;
                forceY += (separationY / separationCount) * SEPARATION_WEIGHT;
            }

            // Alignment force
            if (flockCount > 0) {
                alignmentX /= flockCount;
                alignmentY /= flockCount;
                // Steer towards average velocity
                forceX += (alignmentX - velocitiesX[i]) * ALIGNMENT_WEIGHT;
                forceY += (alignmentY - velocitiesY[i]) * ALIGNMENT_WEIGHT;
            }

            // Cohesion force
            if (flockCount > 0) {
                cohesionX = cohesionX / flockCount - positionsX[i];
                cohesionY = cohesionY / flockCount - positionsY[i];
                forceX += cohesionX * COHESION_WEIGHT * 0.01f;
                forceY += cohesionY * COHESION_WEIGHT * 0.01f;
            }

            // Pheromone attraction (gradient following)
            if (pheromones != null) {
                int px = (int) positionsX[i];
                int py = (int) positionsY[i];
                int pz = (int) positionsZ[i];

                // Sample gradient - only need directional samples
                float left = getPheromone(pheromones, px - 1, py, pz, width, height, depth);
                float right = getPheromone(pheromones, px + 1, py, pz, width, height, depth);
                float up = getPheromone(pheromones, px, py - 1, pz, width, height, depth);
                float down = getPheromone(pheromones, px, py + 1, pz, width, height, depth);

                float gradX = right - left;
                float gradY = down - up;
                forceX += gradX * PHEROMONE_WEIGHT;
                forceY += gradY * PHEROMONE_WEIGHT;
            }

            // Random wandering
            forceX += (Math.random() - 0.5) * WANDER_WEIGHT;
            forceY += (Math.random() - 0.5) * WANDER_WEIGHT;

            // Limit force magnitude
            float forceMag = (float) Math.sqrt(forceX * forceX + forceY * forceY);
            if (forceMag > MAX_FORCE) {
                forceX = (forceX / forceMag) * MAX_FORCE;
                forceY = (forceY / forceMag) * MAX_FORCE;
            }

            // Apply force to velocity
            velocitiesX[i] += forceX;
            velocitiesY[i] += forceY;

            // Limit speed
            float speed = (float) Math.sqrt(velocitiesX[i] * velocitiesX[i] + velocitiesY[i] * velocitiesY[i]);
            if (speed > MAX_SPEED) {
                velocitiesX[i] = (velocitiesX[i] / speed) * MAX_SPEED;
                velocitiesY[i] = (velocitiesY[i] / speed) * MAX_SPEED;
            }
            speeds[i] = Math.max(0.1f, speed);

            // Update position
            positionsX[i] += velocitiesX[i];
            positionsY[i] += velocitiesY[i];

            // Update heading from velocity
            if (speed > 0.01f) {
                headings[i] = (float) Math.atan2(velocitiesY[i], velocitiesX[i]);
            }

            // Bounds check (soft bounce)
            if (positionsX[i] < 2) {
                positionsX[i] = 2;
                velocitiesX[i] = Math.abs(velocitiesX[i]) * 0.5f;
            }
            if (positionsX[i] >= width - 2) {
                positionsX[i] = width - 3;
                velocitiesX[i] = -Math.abs(velocitiesX[i]) * 0.5f;
            }
            if (positionsY[i] < 2) {
                positionsY[i] = 2;
                velocitiesY[i] = Math.abs(velocitiesY[i]) * 0.5f;
            }
            if (positionsY[i] >= height - 2) {
                positionsY[i] = height - 3;
                velocitiesY[i] = -Math.abs(velocitiesY[i]) * 0.5f;
            }

            // Update Energy
            energies[i] -= 0.01f;
            if (energies[i] <= 0) {
                alive[i] = false;
            }
        }
    }

    /**
     * Get pheromone value at position with bounds checking.
     */
    private float getPheromone(float[] pheromones, int x, int y, int z, int width, int height, int depth) {
        if (x < 0 || x >= width || y < 0 || y >= height || z < 0 || z >= depth) {
            return 0f;
        }
        int index = x + y * width + z * width * height;
        if (index < 0 || index >= pheromones.length) {
            return 0f;
        }
        return pheromones[index];
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
