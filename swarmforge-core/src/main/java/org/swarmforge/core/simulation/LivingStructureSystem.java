/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;

import java.util.ArrayList;
import java.util.List;

/**
 * Living Architecture & Infrastructure System.
 * Models self-assembled structures built from living insect bodies:
 * - Eciton bivouacs, living bridges & ladders
 * - Oecophylla leaf-stitching chains
 * - Solenopsis flood rafts
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class LivingStructureSystem {

    public enum StructureType {
        LIVING_BRIDGE,   // Eciton gap bridging
        BIVOUAC_NEST,    // Temporary body nest
        LEAF_CHAIN,      // Oecophylla leaf pulling
        FLOOD_RAFT       // Solenopsis water raft
    }

    public static class LivingChain {
        private final StructureType type;
        private final List<Individual> linkedIndividuals = new ArrayList<>();
        private float tensionForce;

        public LivingChain(StructureType type) {
            this.type = type;
        }

        public void addLink(Individual ant) {
            linkedIndividuals.add(ant);
        }

        public boolean isStable() {
            return linkedIndividuals.size() >= 3;
        }

        public StructureType getType() { return type; }
        public List<Individual> getLinkedIndividuals() { return linkedIndividuals; }
    }

    /**
     * Evaluates whether a gap between two coordinates (x1,y1,z1) and (x2,y2,z2)
     * requires and supports a living ant bridge structure.
     */
    public static boolean canFormLivingBridge(float gapDistanceMm, int availableWorkersCount) {
        float requiredWorkers = gapDistanceMm / 3.0f; // Each ant spans ~3mm
        return availableWorkersCount >= requiredWorkers * 1.5f; // Requires reserve capacity
    }
}
