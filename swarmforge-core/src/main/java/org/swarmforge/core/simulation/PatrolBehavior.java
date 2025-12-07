/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.Colony;
import java.util.Random;

/**
 * Patrol behavior for soldier ants.
 * Patrols nest perimeter and responds to alarm pheromones.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class PatrolBehavior implements BehaviorStrategy {

    private static final float PATROL_RADIUS = 10f;
    private static final float TURN_RATE = 0.1f;
    private final Random random = new Random();

    @Override
    public void execute(Individual ind, Terrarium terrarium, Colony colony, BehaviorContext ctx) {
        if (!ind.isAlive())
            return;

        // Respond to alarm pheromone - rush towards it
        if (ctx.alarmPheromoneStrength() > 0.3f) {
            rushToAlarm(ind, ctx);
            return;
        }

        // If enemy nearby, release alarm pheromone and attack
        if (ctx.enemyNearby()) {
            releaseAlarmPheromone(terrarium, ind);
            attackEnemy(ind);
            return;
        }

        // Normal patrol - circle around nest
        patrol(ind, colony);

        // Move
        ind.move(0.4f);
        ind.setEnergy(ind.getEnergy() - 0.03f);
    }

    private void rushToAlarm(Individual ind, BehaviorContext ctx) {
        // Turn towards alarm source (simplified)
        ind.setHeading(ind.getHeading() + (random.nextFloat() - 0.5f) * 0.5f);
        ind.move(0.8f); // Move fast
    }

    private void releaseAlarmPheromone(Terrarium terrarium, Individual ind) {
        // Would deposit alarm pheromone at current location
    }

    private void attackEnemy(Individual ind) {
        // Combat logic would go here
        ind.setEnergy(ind.getEnergy() - 0.2f); // Fighting is exhausting
    }

    private void patrol(Individual ind, Colony colony) {
        // Calculate angle to nest center
        float dx = colony.getNestX() - ind.getX();
        float dy = colony.getNestY() - ind.getY();
        float distToNest = (float) Math.sqrt(dx * dx + dy * dy);
        float angleToNest = (float) Math.atan2(dy, dx);

        if (distToNest > PATROL_RADIUS * 1.2f) {
            // Too far, head back towards nest
            ind.turnTowards(angleToNest, TURN_RATE);
        } else if (distToNest < PATROL_RADIUS * 0.8f) {
            // Too close, head away
            ind.turnTowards(angleToNest + (float) Math.PI, TURN_RATE);
        } else {
            // Good distance, circle around
            ind.turnTowards(angleToNest + (float) Math.PI / 2, TURN_RATE);
        }
    }
}
