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

        // Check for prey in trap or within direct strike reach
        Individual target = findTargetAnt(predator, simulation);

        if (target != null) {
            predator.setCurrentTarget(target);
            predator.setState(HuntingState.ATTACKING);
            predator.attack(target);
        } else {
            predator.setState(HuntingState.IDLE);
        }
    }

    private Individual findTargetAnt(Predator predator, Simulation simulation) {
        float attackReach = Math.max(1.8f, predator.getType().getAttackRadius() * 1.5f);
        float queryRadius = Math.max(predator.getTrapRadius(), attackReach);
        List<Individual> nearby = simulation.getSpatialIndex().queryRadius(
                predator.getX(), predator.getY(), predator.getZ(), queryRadius);

        for (Individual ant : nearby) {
            if (ant == null || !ant.isAlive()) continue;
            // Strike if trapped in web/pit OR if within immediate physical reach
            if (predator.isInTrap(ant) || predator.distanceTo(ant) <= attackReach) {
                return ant;
            }
        }
        return null;
    }
}
