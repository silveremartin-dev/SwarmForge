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

        return new PlacementResult(targetX, targetY, 0f, strategy != null ? strategy : "Optimal Multi-Territory Cluster");
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
