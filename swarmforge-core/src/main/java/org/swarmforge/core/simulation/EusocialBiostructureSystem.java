/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import java.util.List;

/**
 * Self-Assembled Functional Biostructure System (Army Ants Eciton / Fire Ants Solenopsis).
 * Simulates workers interlocking legs and mandibles to construct:
 * 1. Living Bridges across physical terrain gaps.
 * 2. Water Rafts during high flood/rainfall saturation.
 * 3. Bivouac Walls protecting the queen and brood in open terrain.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class EusocialBiostructureSystem {

    public enum BiostructureType {
        LIVING_BRIDGE,
        WATER_RAFT,
        BIVOUAC_WALL
    }

    public static class BiostructureAnchor {
        public final BiostructureType type;
        public final float x, y, z;
        public int participatingWorkers;

        public BiostructureAnchor(BiostructureType type, float x, float y, float z) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.z = z;
            this.participatingWorkers = 0;
        }
    }

    /**
     * Evaluates whether a worker should join a self-assembled living bridge across a terrain gap.
     */
    public static boolean shouldJoinLivingBridge(Individual worker, float gapWidthMm, int currentBridgeWorkers) {
        if (worker == null || !worker.isAlive()) return false;
        // Joining probability increases with gap width and decreases if bridge has sufficient workers
        float requiredWorkers = gapWidthMm * 2.5f;
        return currentBridgeWorkers < requiredWorkers;
    }

    /**
     * Evaluates flood raft formation during extreme water saturation.
     */
    public static boolean triggerRaftAssembly(List<Individual> colonyWorkers, float waterLevelPercent) {
        if (waterLevelPercent < 85.0f || colonyWorkers.isEmpty()) return false;
        // Lock 70% of available workers into floating raft matrix
        int raftWorkers = (int) (colonyWorkers.size() * 0.70f);
        return raftWorkers > 10;
    }
}
