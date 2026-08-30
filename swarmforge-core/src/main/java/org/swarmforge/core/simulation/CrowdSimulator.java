/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.gpu.SparsePheromoneGrid;

import java.util.Arrays;
import java.util.List;

/**
 * Crowd/flocking simulation using Structure of Arrays (SoA) layout.
 * Optimized for high-performance CPU mass simulation with spatial hash grid.
 * 
 * Implements Reynolds Boids rules (Separation, Alignment, Cohesion) and Pheromone gradient navigation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class CrowdSimulator {

    // Structure of Arrays (SoA)
    private float[] positionsX;
    private float[] positionsY;
    private float[] positionsZ;
    private float[] headings;
    private float[] speeds;
    private float[] maxSpeeds;
    private float[] energies;
    private float[] metabolisms;
    private boolean[] alive;

    private int capacity;
    private int count;
    private long stepCounter = 0;
    private float simulationStepSeconds = 0.016666667f;

    public float getSimulationStepSeconds() { return simulationStepSeconds; }
    public void setSimulationStepSeconds(float stepSeconds) { this.simulationStepSeconds = Math.max(0.0001f, stepSeconds); }

    // Primitive Spatial Hash Grid for O(N) neighbor lookup
    private static final float CELL_SIZE = 10.0f; // Matches NEIGHBOR_RADIUS
    private int gridWidth;
    private int gridHeight;
    private int[] head = new int[0];
    private int[] next = new int[0];

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
        maxSpeeds = new float[size];
        energies = new float[size];
        metabolisms = new float[size];
        alive = new boolean[size];
        next = new int[size];
    }

    /**
     * Load individuals from a colony into SoA format.
     */
    public void loadFromColony(Colony colony) {
        if (colony == null) return;
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
            maxSpeeds[i] = Math.max(0.02f, ind.getCurrentMovementSpeed() * 0.1f);
            speeds[i] = Math.max(0.01f, maxSpeeds[i] * 0.5f);
            energies[i] = ind.getEnergy();
            metabolisms[i] = ind.getEffectiveMetabolismRate();
            alive[i] = ind.isAlive();
        }
    }

    /**
     * Build primitive spatial grid for O(1) cell lookup and O(N) neighbor search.
     */
    private void buildSpatialGrid(int worldWidth, int worldHeight) {
        gridWidth = (int) Math.ceil(worldWidth / CELL_SIZE) + 1;
        gridHeight = (int) Math.ceil(worldHeight / CELL_SIZE) + 1;
        int totalCells = gridWidth * gridHeight;

        if (head.length < totalCells) {
            head = new int[totalCells];
        }
        Arrays.fill(head, 0, totalCells, -1);

        for (int i = 0; i < count; i++) {
            if (!alive[i]) continue;
            int cx = Math.max(0, Math.min(gridWidth - 1, (int) (positionsX[i] / CELL_SIZE)));
            int cy = Math.max(0, Math.min(gridHeight - 1, (int) (positionsY[i] / CELL_SIZE)));
            int cellIdx = cx + cy * gridWidth;
            next[i] = head[cellIdx];
            head[cellIdx] = i;
        }
    }

    /**
     * Execute one simulation step using CPU updates with Boids & Spatial Hash Grid.
     */
    public void step(float[] pheromones, int width, int height, int depth) {
        step(pheromones, null, width, height, depth);
    }

    /**
     * Overloaded step method supporting optional SparsePheromoneGrid reference for trail depositing.
     */
    public void step(float[] pheromones, SparsePheromoneGrid pheromoneGrid, int width, int height, int depth) {
        if (count == 0) return;

        stepCounter++;
        int sampleInterval = Math.max(1, count > 10000 ? 5 : (count > 2000 ? 3 : 1));
        float depositAmount = 0.5f * (simulationStepSeconds / 0.016666667f) * sampleInterval;

        // Boids parameters tuned for realistic ant behavior
        final float NEIGHBOR_RADIUS = 10.0f;
        final float NEIGHBOR_RADIUS_SQ = NEIGHBOR_RADIUS * NEIGHBOR_RADIUS;
        final float SEPARATION_RADIUS = 2.5f;
        final float SEPARATION_WEIGHT = 1.2f;
        final float ALIGNMENT_WEIGHT = 0.8f;
        final float COHESION_WEIGHT = 0.8f;
        final float PHEROMONE_WEIGHT = 0.8f;
        final float WANDER_WEIGHT = 0.2f;
        final float MAX_SPEED = 0.12f;   // Realistic maximum speed (metres per tick)
        final float MAX_FORCE = 0.02f;   // Maximum steering force per tick

        // Build spatial partition
        buildSpatialGrid(width, height);

        // Compute velocity vectors
        float[] velocitiesX = new float[count];
        float[] velocitiesY = new float[count];

        for (int i = 0; i < count; i++) {
            if (!alive[i]) continue;
            velocitiesX[i] = (float) Math.cos(headings[i]) * speeds[i];
            velocitiesY[i] = (float) Math.sin(headings[i]) * speeds[i];
        }

        // Calculate Boids forces using spatial grid
        for (int i = 0; i < count; i++) {
            if (!alive[i]) continue;

            float separationX = 0, separationY = 0;
            float alignmentX = 0, alignmentY = 0;
            float cohesionX = 0, cohesionY = 0;
            int separationCount = 0;
            int flockCount = 0;

            int cx = Math.max(0, Math.min(gridWidth - 1, (int) (positionsX[i] / CELL_SIZE)));
            int cy = Math.max(0, Math.min(gridHeight - 1, (int) (positionsY[i] / CELL_SIZE)));

            // Query 3x3 adjacent spatial cells
            for (int nx = Math.max(0, cx - 1); nx <= Math.min(gridWidth - 1, cx + 1); nx++) {
                for (int ny = Math.max(0, cy - 1); ny <= Math.min(gridHeight - 1, cy + 1); ny++) {
                    int j = head[nx + ny * gridWidth];
                    while (j != -1) {
                        if (i != j && alive[j]) {
                            float dx = positionsX[j] - positionsX[i];
                            float dy = positionsY[j] - positionsY[i];
                            float distSq = dx * dx + dy * dy;

                            if (distSq < NEIGHBOR_RADIUS_SQ && distSq > 0.0001f) {
                                float dist = (float) Math.sqrt(distSq);
                                flockCount++;

                                alignmentX += velocitiesX[j];
                                alignmentY += velocitiesY[j];

                                cohesionX += positionsX[j];
                                cohesionY += positionsY[j];

                                if (dist < SEPARATION_RADIUS) {
                                    float factor = (SEPARATION_RADIUS - dist) / SEPARATION_RADIUS;
                                    separationX -= (dx / dist) * factor;
                                    separationY -= (dy / dist) * factor;
                                    separationCount++;
                                }
                            }
                        }
                        j = next[j];
                    }
                }
            }

            // Combine forces
            float forceX = 0, forceY = 0;

            if (separationCount > 0) {
                forceX += (separationX / separationCount) * SEPARATION_WEIGHT;
                forceY += (separationY / separationCount) * SEPARATION_WEIGHT;
            }

            if (flockCount > 0) {
                alignmentX /= flockCount;
                alignmentY /= flockCount;
                forceX += (alignmentX - velocitiesX[i]) * ALIGNMENT_WEIGHT;
                forceY += (alignmentY - velocitiesY[i]) * ALIGNMENT_WEIGHT;

                cohesionX = (cohesionX / flockCount) - positionsX[i];
                cohesionY = (cohesionY / flockCount) - positionsY[i];
                forceX += cohesionX * COHESION_WEIGHT * 0.01f;
                forceY += cohesionY * COHESION_WEIGHT * 0.01f;
            }

            // Pheromone gradient attraction
            if (pheromones != null) {
                int px = (int) positionsX[i];
                int py = (int) positionsY[i];
                int pz = (int) positionsZ[i];

                float left = getPheromone(pheromones, px - 1, py, pz, width, height, depth);
                float right = getPheromone(pheromones, px + 1, py, pz, width, height, depth);
                float up = getPheromone(pheromones, px, py - 1, pz, width, height, depth);
                float down = getPheromone(pheromones, px, py + 1, pz, width, height, depth);

                float gradX = right - left;
                float gradY = down - up;
                forceX += gradX * PHEROMONE_WEIGHT;
                forceY += gradY * PHEROMONE_WEIGHT;
            }

            // Wandering force
            java.util.Random rng = java.util.concurrent.ThreadLocalRandom.current();
            forceX += (rng.nextFloat() - 0.5f) * WANDER_WEIGHT;
            forceY += (rng.nextFloat() - 0.5f) * WANDER_WEIGHT;

            // Clamp force
            float forceMag = (float) Math.hypot(forceX, forceY);
            if (forceMag > MAX_FORCE) {
                forceX = (forceX / forceMag) * MAX_FORCE;
                forceY = (forceY / forceMag) * MAX_FORCE;
            }

            // Apply force to velocity
            velocitiesX[i] += forceX;
            velocitiesY[i] += forceY;

            // Clamp speed to individual's species/caste max speed
            float speed = (float) Math.hypot(velocitiesX[i], velocitiesY[i]);
            float individualMaxSpeed = maxSpeeds[i] > 0.001f ? maxSpeeds[i] : 0.12f;
            if (speed > individualMaxSpeed) {
                velocitiesX[i] = (velocitiesX[i] / speed) * individualMaxSpeed;
                velocitiesY[i] = (velocitiesY[i] / speed) * individualMaxSpeed;
                speed = individualMaxSpeed;
            }
            speeds[i] = Math.max(0.005f, speed);

            // Update position
            positionsX[i] += velocitiesX[i];
            positionsY[i] += velocitiesY[i];

            if (speed > 0.001f) {
                headings[i] = (float) Math.atan2(velocitiesY[i], velocitiesX[i]);
            }

            // Soft & hard boundary clamping to prevent leaving map
            if (positionsX[i] < 2.0f) {
                positionsX[i] = 2.0f;
                velocitiesX[i] = Math.abs(velocitiesX[i]) * 0.5f;
            } else if (positionsX[i] >= width - 2.0f) {
                positionsX[i] = width - 3.0f;
                velocitiesX[i] = -Math.abs(velocitiesX[i]) * 0.5f;
            }

            if (positionsY[i] < 2.0f) {
                positionsY[i] = 2.0f;
                velocitiesY[i] = Math.abs(velocitiesY[i]) * 0.5f;
            } else if (positionsY[i] >= height - 2.0f) {
                positionsY[i] = height - 3.0f;
                velocitiesY[i] = -Math.abs(velocitiesY[i]) * 0.5f;
            }

            // Deposit trail pheromone into grid if active using rotational interleaving (all ants deposit equally over time)
            if (pheromoneGrid != null && (stepCounter + i) % sampleInterval == 0) {
                pheromoneGrid.deposit((int) positionsX[i], (int) positionsY[i], (int) positionsZ[i], org.swarmforge.core.domain.PheromoneType.HOME_TRAIL.getIndex(), depositAmount);
            }

            // Realistic metabolic energy consumption per tick (scaled per individual and dt in seconds)
            float individualMetabolism = metabolisms[i] > 0.001f ? metabolisms[i] : 1.0f;
            energies[i] -= 0.0001f * individualMetabolism * (simulationStepSeconds / 0.016666667f);
            if (energies[i] <= 0f) {
                energies[i] = 0f;
                alive[i] = false;
            }
        }
    }

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

    public void syncToColony(Colony colony) {
        if (colony == null) return;
        List<Individual> individuals = colony.getLivingIndividuals();
        int syncCount = Math.min(count, individuals.size());

        for (int i = 0; i < syncCount; i++) {
            Individual ind = individuals.get(i);
            ind.setPosition(positionsX[i], positionsY[i], positionsZ[i]);
            ind.setHeading(headings[i]);
            ind.setEnergy(energies[i]);
            if (!alive[i]) {
                ind.takeDamage(1000.0f); // Kill dead individuals in colony domain
            }
        }
    }

    public int getCount() {
        return count;
    }

    public int getLivingCount() {
        int living = 0;
        for (int i = 0; i < count; i++) {
            if (alive[i]) living++;
        }
        return living;
    }
}

