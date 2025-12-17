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
import java.util.Random;

/**
 * Strategy for active hunters (Beetle, Lizard).
 * Actively pursues the nearest prey.
 */
public class ChaseStrategy implements HuntingStrategy {

    private final Random random = new Random();

    @Override
    public void update(Predator predator, Simulation simulation) {
        Individual target = predator.getCurrentTarget();

        // Find new target if current is invalid
        if (target == null || !target.isAlive()) {
            target = findNearestAnt(predator, simulation);
            predator.setCurrentTarget(target);
        }

        if (target == null) {
            predator.setState(HuntingState.IDLE);
            wander(predator);
            return;
        }

        // Chase logic
        predator.setState(HuntingState.CHASING);
        predator.moveToward(target.getX(), target.getY(), 1.0f);

        // Attack logic
        if (predator.distanceTo(target) < 1.5f) {
            predator.setState(HuntingState.ATTACKING);
            predator.attack(target);
        }
    }

    private void wander(Predator predator) {
        if (random.nextFloat() < 0.05f) { // 5% chance to change direction
            float wanderX = predator.getX() + (random.nextFloat() - 0.5f) * 20f;
            float wanderY = predator.getY() + (random.nextFloat() - 0.5f) * 20f;
            predator.moveToward(wanderX, wanderY, 0.5f);
        }
    }

    private Individual findNearestAnt(Predator predator, Simulation simulation) {
        float searchRadius = predator.getType().getVisionRange();
        List<Individual> nearby = simulation.getSpatialIndex().queryRadius(
                predator.getX(), predator.getY(), predator.getZ(), searchRadius);

        Individual nearest = null;
        float nearestDist = Float.MAX_VALUE;

        for (Individual ant : nearby) {
            if (predator.canSee(ant)) {
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
