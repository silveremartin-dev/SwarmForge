/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.plugin;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.world.NestGenerator.NestType;

/**
 * Interface for pluggable nest architecture definitions.
 * Plugins can implement this to define custom nest structures, chamber arrangements,
 * and excavation patterns tailored to specific eusocial species.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public interface NestArchitecture extends java.io.Serializable {

    /**
     * Unique identifier for this nest architecture (e.g. "atta_cathedral_mound").
     */
    String getId();

    /**
     * Display name for the editor UI.
     */
    String getName();

    /**
     * Scientific or common species identifier associated with this nest pattern.
     */
    String getAssociatedSpecies();

    /**
     * Base nest type enum.
     */
    NestType getNestType();

    /**
     * Default tunnel radius in centimeters.
     */
    float getTunnelRadius();

    /**
     * Default chamber radius in centimeters.
     */
    float getChamberRadius();

    /**
     * Branching angle in degrees.
     */
    float getBranchAngle();

    /**
     * Maximum subterranean/altitude depth in cells.
     */
    int getMaxDepth();

    /**
     * Custom nest generation logic. Allows species plugins to carve specialized chambers,
     * ventilation shafts, fungus gardens, or royal cells into the terrarium.
     *
     * @param terrarium Target terrarium environment
     * @param x Entrance X position
     * @param y Entrance Y position
     * @param z Entrance Z position
     * @param scale Nest size scale factor
     * @return Number of chambers created
     */
    default int generateCustomNest(Terrarium terrarium, int x, int y, int z, float scale) {
        org.swarmforge.core.world.NestGenerator generator = new org.swarmforge.core.world.NestGenerator(terrarium)
                .tunnelRadius(getTunnelRadius())
                .chamberRadius(getChamberRadius())
                .branchAngle(getBranchAngle())
                .maxDepth(getMaxDepth());
        return generator.generate(x, y, z, getNestType(), scale);
    }
}
