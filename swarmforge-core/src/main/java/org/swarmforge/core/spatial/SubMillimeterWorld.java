/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import org.swarmforge.core.domain.TerrariumCell;

import java.util.HashMap;
import java.util.Map;

/**
 * Sub-Millimeter Spatial Coordinator.
 * Bridges the World Editor Macro Grid (128x128x32) with Micro-Voxels (250 µm / 0.25mm resolution).
 * Uses sparse chunk materialization and power-of-2 bitwise operations for high-performance memory management.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SubMillimeterWorld {

    public static final float MICRO_VOXEL_SIZE_MM = 0.25f; // 250 micrometers
    public static final int CHUNK_SHIFT = 6; // 2^6 = 64 micro-voxels per macro voxel side
    public static final int CHUNK_SIZE = 1 << CHUNK_SHIFT; // 64

    private final int macroWidth;
    private final int macroHeight;
    private final int macroDepth;

    // Sparse storage of explicit materialized sub-chunks
    private final Map<Long, SparseChunk3D> chunkMap = new HashMap<>();

    public SubMillimeterWorld(int macroWidth, int macroHeight, int macroDepth) {
        this.macroWidth = macroWidth;
        this.macroHeight = macroHeight;
        this.macroDepth = macroDepth;
    }

    /**
     * Converts a micro-voxel coordinate (250 µm) to its parent macro-voxel index via zero-cost bitwise shift.
     */
    public int toMacroCoord(int microCoord) {
        return microCoord >> CHUNK_SHIFT;
    }

    /**
     * Converts a micro-voxel coordinate to its local offset inside the chunk (0..63).
     */
    public int toLocalCoord(int microCoord) {
        return microCoord & (CHUNK_SIZE - 1);
    }

    /**
     * Computes a unique 64-bit spatial hash key for a macro chunk coordinate.
     */
    private long getChunkKey(int macroX, int macroY, int macroZ) {
        return (((long) macroX & 0xFFFFF) << 40) | (((long) macroY & 0xFFFFF) << 20) | ((long) macroZ & 0xFFFFF);
    }

    /**
     * Retrieves or materializes a sparse chunk at the given macro coordinates.
     */
    public SparseChunk3D getOrCreateChunk(int macroX, int macroY, int macroZ, TerrariumCell.Material defaultMaterial) {
        long key = getChunkKey(macroX, macroY, macroZ);
        return chunkMap.computeIfAbsent(key, k -> new SparseChunk3D(macroX, macroY, macroZ, defaultMaterial));
    }

    public int getMacroWidth() { return macroWidth; }
    public int getMacroHeight() { return macroHeight; }
    public int getMacroDepth() { return macroDepth; }
}
