/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.ecology.ai;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Predator;
import org.swarmforge.core.domain.Predator.HuntingState;
import org.swarmforge.core.simulation.Simulation;

import java.util.List;

/**
 * Strategy for aerial hunters (Bird).
 * Circles above and swoops down to attack.
 */
public class SwoopStrategy implements HuntingStrategy {

    @Override
    public void update(Predator predator, Simulation simulation) {
        Individual target = predator.getCurrentTarget();

        if (target == null || !target.isAlive()) {
            target = findNearestSurfaceAnt(predator, simulation);
            predator.setCurrentTarget(target);
        }

        if (target == null) {
            predator.setState(HuntingState.STALKING);
            // Circle pattern
            float angle = (simulation.getTickCount() * 0.02f) % (float) (2 * Math.PI);
            // Assuming predator circles around its spawn point or current "territory"
            // center
            // For simplicity, circling current position (drift) or specific point
            // Let's circle around current X/Y as a center point, but we need to track that
            // center.
            // Simplified: circle around a slightly offset point to simulate hovering

            // Actually, better logic: Move in a circle pattern relative to time
            // We need to keep the predator moving.
            // Let's just wander high up.
            predator.setPosition(
                    predator.getX() + (float) Math.cos(angle) * 0.5f,
                    predator.getY() + (float) Math.sin(angle) * 0.5f,
                    simulation.getTerrarium().getDepth() + 10); // Stay high
            return;
        }

        // Swoop down
        predator.setState(HuntingState.CHASING);
        predator.moveToward(target.getX(), target.getY(), 2.0f);

        // Adjust Z to swoop
        float dx = target.getX() - predator.getX();
        float dy = target.getY() - predator.getY();
        float distH = (float) Math.sqrt(dx * dx + dy * dy);

        // As we get closer horizontally, drop height
        float targetZ = target.getZ();
        float currentZ = predator.getZ();
        float desiredZ = targetZ + (distH * 0.5f); // 45 degree slope roughly

        if (currentZ > desiredZ) {
            predator.setPosition(predator.getX(), predator.getY(), Math.max(targetZ, currentZ - 1.0f));
        }

        if (predator.distanceTo(target) < 2.0f) {
            predator.setState(HuntingState.ATTACKING);
            boolean killed = predator.attack(target);
            if (killed) {
                predator.setCurrentTarget(null);
                // Fly up could be handled next tick by "target == null" logic resetting height
            }
        }
    }

    private Individual findNearestSurfaceAnt(Predator predator, Simulation simulation) {
        float surfaceZ = simulation.getTerrarium().getDepth() - 5;
        float searchRadius = 50.0f;
        List<Individual> nearby = simulation.getSpatialIndex().queryRadius(
                predator.getX(), predator.getY(), predator.getZ(), searchRadius);

        Individual nearest = null;
        float nearestDist = Float.MAX_VALUE;

        for (Individual ant : nearby) {
            if (ant.getZ() >= surfaceZ) { // Check visibility?
                float dist = predator.distanceTo(ant);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearest = ant;
                }
            }
        }
        return nearest;
    }
}
