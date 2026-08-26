/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

/**
 * Tandem Running & Quorum Migration System (Temnothorax / Diacamma).
 * Simulates scout-led tandem recruitment where a leader ant guides a naive follower ant
 * via frequent antennal-gaster tactile contact.
 * When the quorum threshold of workers at the new candidate nest site is reached,
 * recruitment accelerates from tandem running to rapid carrying.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class TandemRunningSystem {

    public static final int QUORUM_THRESHOLD = 15;

    public static class TandemPair {
        public final Individual leader;
        public final Individual follower;
        public float distanceApartMm;

        public TandemPair(Individual leader, Individual follower) {
            this.leader = leader;
            this.follower = follower;
            this.distanceApartMm = 2.0f; // Close antennal contact
        }
    }

    /**
     * Updates tandem running progress; if follower falls behind (>8mm), leader pauses and waits for tactile tap.
     */
    public static boolean updateTandemStep(TandemPair pair, float leaderStepDist) {
        if (pair == null || !pair.leader.isAlive() || !pair.follower.isAlive()) return false;

        if (pair.distanceApartMm > 8.0f) {
            // Leader pauses waiting for follower antennal tap
            pair.distanceApartMm = Math.max(2.0f, pair.distanceApartMm - 1.5f);
            return false; // Paused
        } else {
            pair.follower.setPosition(pair.leader.getX(), pair.leader.getY(), pair.leader.getZ());
            return true; // Step executed
        }
    }

    /**
     * Evaluates if nest relocation recruitment should switch from Tandem Running to Rapid Carrying.
     */
    public static boolean isQuorumReached(int workersAtCandidateNest) {
        return workersAtCandidateNest >= QUORUM_THRESHOLD;
    }
}
