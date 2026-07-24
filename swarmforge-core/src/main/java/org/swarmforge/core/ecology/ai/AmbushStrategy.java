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
 * Strategy for trap-building hunters (Antlion, Spider).
 * Waits for prey to enter trap radius.
 */
public class AmbushStrategy implements HuntingStrategy {

    @Override
    public void update(Predator predator, Simulation simulation) {
        // Build trap if needed
        if (!predator.isTrapBuilt()) {
            predator.buildTrap();
            predator.setState(HuntingState.IDLE);
            return;
        }

        // Check for prey in trap
        Individual trapped = findTrappedAnt(predator, simulation);

        if (trapped != null) {
            predator.setCurrentTarget(trapped);
            predator.setState(HuntingState.ATTACKING);
            predator.attack(trapped);
        } else {
            predator.setState(HuntingState.IDLE);
        }
    }

    private Individual findTrappedAnt(Predator predator, Simulation simulation) {
        // Only verify very close ants
        List<Individual> nearby = simulation.getSpatialIndex().queryRadius(
                predator.getX(), predator.getY(), predator.getZ(), 3.0f);

        for (Individual ant : nearby) {
            if (predator.isInTrap(ant)) {
                return ant;
            }
        }
        return null;
    }
}
