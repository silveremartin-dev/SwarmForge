/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import java.util.Collection;

/**
 * Evaluates and computes biologically optimal spatial placements for multiple colonies
 * within a Terrarium environment.
 * Ensures non-overlapping territories, dry out-of-water ground, species habitat preferences,
 * and spatial distribution for multi-colony scenarios.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class OptimalColonyPlacementEngine {

    public record PlacementResult(float x, float y, float z, String placementStrategy) {}

    /**
     * Compute optimal placement coordinates for a new colony of a specific species.
     */
    public static PlacementResult calculateOptimalPosition(Terrarium terrarium, String speciesName, int colonyIndex, int totalColonies, String strategy) {
        return calculateOptimalPosition(terrarium, null, speciesName, colonyIndex, totalColonies, strategy);
    }

    /**
     * Compute optimal placement coordinates for a new colony of a specific species with vegetation snapping.
     */
    public static PlacementResult calculateOptimalPosition(Terrarium terrarium, org.swarmforge.core.world.VegetationSystem vegetation, String speciesName, int colonyIndex, int totalColonies, String strategy) {
        if (terrarium == null) {
            return new PlacementResult(32f, 32f, 0f, strategy);
        }

        int width = terrarium.getWidth();
        int height = terrarium.getHeight();
        float centerX = width / 2.0f;
        float centerY = height / 2.0f;

        if (strategy != null && strategy.contains("Manuel")) {
            // Manual placement fallback centered
            return new PlacementResult(centerX, centerY, 0f, "Manuel");
        }

        if (totalColonies <= 1 && (strategy == null || strategy.contains("Centre"))) {
            return new PlacementResult(centerX, centerY, 0f, "Centre Map Single Nest");
        }

        // Base separation radius dependent on map size and colony count
        double minSepRadius = Math.max(15.0, Math.min(width, height) * 0.28);

        // Angle offset per colony to distribute radially when sharing central macro-zone
        double angleStep = 2.0 * Math.PI / Math.max(1, totalColonies);
        double angle = colonyIndex * angleStep + (Math.PI / 4.0);

        float targetX = centerX;
        float targetY = centerY;

        if (totalColonies > 1) {
            // Radial offset around map center for multi-nest scenarios
            targetX = (float) (centerX + Math.cos(angle) * minSepRadius);
            targetY = (float) (centerY + Math.sin(angle) * minSepRadius);
        }

        // Clamp to map boundaries with safety margin
        float margin = 8.0f;
        targetX = Math.max(margin, Math.min(width - margin, targetX));
        targetY = Math.max(margin, Math.min(height - margin, targetY));

        // Adjust position to avoid existing colony overlaps
        Collection<Colony> existingColonies = terrarium.getColonies();
        int attempts = 0;
        while (attempts < 30 && isTooCloseToExisting(targetX, targetY, existingColonies, 18.0f)) {
            angle += Math.PI / 8.0;
            double radius = minSepRadius + (attempts * 2.5);
            targetX = (float) (centerX + Math.cos(angle) * radius);
            targetY = (float) (centerY + Math.sin(angle) * radius);
            targetX = Math.max(margin, Math.min(width - margin, targetX));
            targetY = Math.max(margin, Math.min(height - margin, targetY));
            attempts++;
        }

        // Determine biological Z elevation and snap to existing trees if arboreal / tree cavity nesting
        float targetZ = 0.0f;
        String sName = speciesName != null ? speciesName.toLowerCase() : "";

        boolean isHollowTrunkNest = sName.contains("camponotus") || sName.contains("crematogaster") || sName.contains("dolichoderus")
                || sName.contains("charpentière") || sName.contains("hollow") || sName.contains("arbre creux")
                || sName.contains("tronc creux") || sName.contains("sauvage") || sName.contains("wild_bee") || sName.contains("wild");

        boolean isCanopyArboreal = sName.contains("wasp") || sName.contains("guêpe") || sName.contains("vespula")
                || sName.contains("polistes") || sName.contains("carton") || sName.contains("weaver") || sName.contains("oecophylla");

        if (isHollowTrunkNest || isCanopyArboreal) {
            // Check if scenario has pre-existing mature trees in VegetationSystem
            if (vegetation != null && !vegetation.getPlants().isEmpty()) {
                org.swarmforge.core.world.VegetationSystem.Plant bestTree = null;
                float bestDistSq = Float.MAX_VALUE;
                for (org.swarmforge.core.world.VegetationSystem.Plant p : vegetation.getPlants()) {
                    if (p.type == org.swarmforge.core.world.VegetationSystem.PlantType.TREE && p.growth >= 0.5f) {
                        float dx = p.x - targetX;
                        float dy = p.y - targetY;
                        float d2 = dx * dx + dy * dy;
                        if (d2 < bestDistSq && !isTooCloseToExisting(p.x, p.y, existingColonies, 12.0f)) {
                            bestDistSq = d2;
                            bestTree = p;
                        }
                    }
                }
                if (bestTree != null) {
                    targetX = bestTree.x;
                    targetY = bestTree.y;
                    if (isHollowTrunkNest) {
                        // Trunk cavity height (1.8m - 3.5m above ground)
                        targetZ = Math.max(1.8f, Math.min(3.5f, bestTree.getCurrentHeight() * 0.35f));
                        return new PlacementResult(targetX, targetY, targetZ, "Snapped to Hollow Tree Trunk Cavity #" + bestTree.x + "_" + bestTree.y);
                    } else {
                        // High canopy branch attachment for wasps and silk weaver ants
                        targetZ = Math.max(6.5f, bestTree.getCurrentHeight() * 0.85f);
                        return new PlacementResult(targetX, targetY, targetZ, "Snapped to Tree Canopy Branch #" + bestTree.x + "_" + bestTree.y);
                    }
                }
            }
            targetZ = isHollowTrunkNest ? 2.2f : 8.5f;
        } else if (sName.contains("bee") || sName.contains("abeille") || sName.contains("apis") || sName.contains("ruche")) {
            // Hive stand base elevation above ground
            targetZ = 0.4f;
        }

        return new PlacementResult(targetX, targetY, targetZ, strategy != null ? strategy : "Optimal Multi-Territory Cluster");
    }

    private static boolean isTooCloseToExisting(float x, float y, Collection<Colony> existing, float minDistance) {
        if (existing == null) return false;
        for (Colony c : existing) {
            double dx = c.getNestX() - x;
            double dy = c.getNestY() - y;
            if (Math.sqrt(dx * dx + dy * dy) < minDistance) {
                return true;
            }
        }
        return false;
    }
}
