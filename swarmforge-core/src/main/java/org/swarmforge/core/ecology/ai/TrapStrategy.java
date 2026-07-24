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

/**
 * Strategy for pit-building hunters (Antlion).
 * Pulls prey into the center of the trap.
 */
public class TrapStrategy implements HuntingStrategy {

    @Override
    public void update(Predator predator, Simulation simulation) {
        if (!predator.isTrapBuilt()) {
            predator.buildTrap();
            predator.setState(HuntingState.IDLE);
            return;
        }

        // Pull ants toward trap center
        List<Individual> nearby = simulation.getSpatialIndex().queryRadius(
                predator.getX(), predator.getY(), predator.getZ(), 5.0f);

        boolean hunting = false;

        for (Individual ant : nearby) {
            if (predator.isInTrap(ant)) {
                hunting = true;
                // Pull ant toward predator
                float pullStrength = 0.5f;
                float dx = predator.getX() - ant.getX();
                float dy = predator.getY() - ant.getY();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist > 0.1f) {
                    float newX = ant.getX() + (dx / dist) * pullStrength;
                    float newY = ant.getY() + (dy / dist) * pullStrength;
                    ant.setPosition(newX, newY, ant.getZ());
                }

                // Attack if close enough
                if (dist < 1.0f) {
                    predator.setState(HuntingState.ATTACKING);
                    predator.attack(ant);
                }
            }
        }

        if (!hunting) {
            predator.setState(HuntingState.IDLE);
        }
    }
}
