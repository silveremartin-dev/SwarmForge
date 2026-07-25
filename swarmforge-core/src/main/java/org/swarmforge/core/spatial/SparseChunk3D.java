/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import org.swarmforge.core.domain.TerrariumCell;

/**
 * Sparse 3D Chunk representing a 16mm^3 macro region.
 * Can exist as an implicit unmaterialized chunk (32 bytes RAM)
 * or subdivide into a 64x64x64 micro-voxel grid (250 µm) upon insect excavation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SparseChunk3D {

    private final int macroX;
    private final int macroY;
    private final int macroZ;
    private final TerrariumCell.Material defaultMaterial;

    private boolean isMaterialized = false;
    private byte[][][] microVoxels = null; // 64x64x64 micro voxels upon excavation

    public SparseChunk3D(int macroX, int macroY, int macroZ, TerrariumCell.Material defaultMaterial) {
        this.macroX = macroX;
        this.macroY = macroY;
        this.macroZ = macroZ;
        this.defaultMaterial = defaultMaterial;
    }

    /**
     * Materializes the chunk into micro-voxels upon insect excavation or structural modification.
     */
    public synchronized void materialize() {
        if (isMaterialized) return;
        int sz = SubMillimeterWorld.CHUNK_SIZE;
        microVoxels = new byte[sz][sz][sz];
        byte matId = (byte) defaultMaterial.ordinal();
        for (int x = 0; x < sz; x++)
            for (int y = 0; y < sz; y++)
                for (int z = 0; z < sz; z++)
                    microVoxels[x][y][z] = matId;
        isMaterialized = true;
    }

    public TerrariumCell.Material getVoxelMaterial(int lx, int ly, int lz) {
        if (!isMaterialized) return defaultMaterial;
        byte ordinal = microVoxels[lx][ly][lz];
        TerrariumCell.Material[] values = TerrariumCell.Material.values();
        return (ordinal >= 0 && ordinal < values.length) ? values[ordinal] : defaultMaterial;
    }

    public void setVoxelMaterial(int lx, int ly, int lz, TerrariumCell.Material mat) {
        if (!isMaterialized) materialize();
        microVoxels[lx][ly][lz] = (byte) mat.ordinal();
    }

    public boolean isMaterialized() { return isMaterialized; }
    public int getMacroX() { return macroX; }
    public int getMacroY() { return macroY; }
    public int getMacroZ() { return macroZ; }
}
