/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.compute;

/**
 * Interface for distributed compute cluster management.
 * Allows core simulation to decouple from server implementation.
 */
public interface ComputeCluster {

    /**
     * Dispatch pheromone task to a node (preferably GPU).
     * 
     * @return true if dispatched and successful
     */
    boolean dispatchPheromoneTask(int w, int h, int d, float[] data);

    /**
     * Dispatch pathfinding task to a node.
     * 
     * @return path as list of int[3] (x,y,z) or null if failed
     */
    java.util.List<int[]> dispatchPathfindingTask(int startX, int startY, int startZ,
            int goalX, int goalY, int goalZ,
            int w, int h, int d,
            byte[] walkableData);
}
