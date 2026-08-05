/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.structure.physics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Structural Mechanics & Tunnel Stability Analyzer.
 * Computes overburden pressure, arch support factors, gallery collapse risks,
 * and triggers cave-ins (éboulements) when excavation exceeds material shear strength.
 */
public class StructuralStabilityAnalyzer implements Serializable {
    private static final long serialVersionUID = 1L;

    public record CaveInEvent(int x, int y, int z, float severity) implements Serializable {}

    /**
     * Re-calculates overburden stress distribution across the 3D voxel grid.
     */
    public void updateOverburdenStress(NestVoxelGrid grid) {
        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < depth; z++) {
                float cumulativeStress = 0.0f;
                // Calculate from top to bottom
                for (int y = height - 1; y >= 0; y--) {
                    NestVoxelGrid.VoxelCell cell = grid.getVoxel(x, y, z);
                    if (cell != null) {
                        float voxelWeightKPa = (cell.getMaterial().getDensityKgM3() * 9.81f * 0.1f) / 1000.0f; // per 10cm voxel height
                        cumulativeStress += voxelWeightKPa;
                        cell.setOverburdenStressKPa(cumulativeStress);
                    }
                }
            }
        }
    }

    /**
     * Compute arching support factor (0.0 = unsupported, 1.0 = fully supported by surrounding solid voxels).
     */
    public float calculateArchSupportFactor(NestVoxelGrid grid, int x, int y, int z) {
        int solidNeighbors = 0;
        int totalNeighbors = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    totalNeighbors++;
                    NestVoxelGrid.VoxelCell neighbor = grid.getVoxel(x + dx, y + dy, z + dz);
                    if (neighbor != null && neighbor.getMaterial().isSolid() && !neighbor.isCollapsed()) {
                        solidNeighbors++;
                    }
                }
            }
        }
        return (float) solidNeighbors / totalNeighbors;
    }

    /**
     * Calculates the risk of a gallery collapse (0.0 to 1.0) at voxel (x, y, z).
     */
    public float calculateCollapseRisk(NestVoxelGrid grid, int x, int y, int z, float saturationLevel) {
        NestVoxelGrid.VoxelCell cell = grid.getVoxel(x, y, z);
        if (cell == null) return 0.0f;

        // Air voxels cannot collapse themselves, but adjacent roof voxels can!
        VoxelMaterial mat = cell.getMaterial();
        float effectiveShearStrength = mat.getShearStrengthKPa() * (1.0f - saturationLevel * 0.5f);
        if (mat == VoxelMaterial.AIR) {
            // Check roof voxel above
            NestVoxelGrid.VoxelCell roof = grid.getVoxel(x, y + 1, z);
            if (roof == null || !roof.getMaterial().isSolid()) return 0.0f;
            mat = roof.getMaterial();
            cell = roof;
            effectiveShearStrength = mat.getShearStrengthKPa() * (1.0f - saturationLevel * 0.5f);
        }

        float archSupport = calculateArchSupportFactor(grid, x, y, z);
        float baselineIntegrity = grid.getNestType().getStructuralIntegrityBaseline();
        float stress = cell.getOverburdenStressKPa();

        // Safety Factor SF = (Effective Shear Strength * Arch Support * BaselineIntegrity) / Overburden Stress
        float capacity = effectiveShearStrength * archSupport * baselineIntegrity;
        if (capacity <= 0.001f) return 1.0f;

        float safetyFactor = capacity / Math.max(0.1f, stress);
        if (safetyFactor >= 1.5f) {
            return 0.0f; // Completely safe
        }
        return Math.min(1.0f, (1.5f - safetyFactor) / 1.5f);
    }

    /**
     * Evaluates grid and triggers cave-ins (éboulements) for voxels exceeding safety thresholds.
     */
    public List<CaveInEvent> evaluateAndTriggerCaveIns(NestVoxelGrid grid, float saturationLevel, Random rng) {
        updateOverburdenStress(grid);
        List<CaveInEvent> events = new ArrayList<>();

        int width = grid.getWidth();
        int height = grid.getHeight();
        int depth = grid.getDepth();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height - 1; y++) {
                for (int z = 0; z < depth; z++) {
                    NestVoxelGrid.VoxelCell cell = grid.getVoxel(x, y, z);
                    if (cell != null && cell.getMaterial() == VoxelMaterial.AIR) {
                        float risk = calculateCollapseRisk(grid, x, y, z, saturationLevel);
                        if (risk > 0.4f && rng.nextFloat() < (risk * 0.2f)) {
                            // Collapse roof into gallery voxel!
                            NestVoxelGrid.VoxelCell roof = grid.getVoxel(x, y + 1, z);
                            if (roof != null && roof.getMaterial().isSolid()) {
                                VoxelMaterial collapsedMat = roof.getMaterial();
                                roof.setMaterial(VoxelMaterial.AIR);
                                cell.setMaterial(collapsedMat);
                                cell.setCollapsed(true);
                                events.add(new CaveInEvent(x, y, z, risk));
                            }
                        }
                    }
                }
            }
        }
        return events;
    }
}
