/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
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
        Random rng = (predator != null && predator.getRandom() != null) ? predator.getRandom() : new Random(1337L);
        if (rng.nextFloat() < 0.05f) { // 5% chance to change direction
            float wanderX = predator.getX() + (rng.nextFloat() - 0.5f) * 20f;
            float wanderY = predator.getY() + (rng.nextFloat() - 0.5f) * 20f;
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
