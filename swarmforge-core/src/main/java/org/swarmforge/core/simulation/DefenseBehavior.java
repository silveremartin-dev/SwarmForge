/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.Colony;
import java.util.Random;

/**
 * Defense behavior for nest entrance guards.
 * Guards nest entrance and challenges intruders.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class DefenseBehavior implements BehaviorStrategy {

    private static final float GUARD_RANGE = 3f;

    @Override
    public void execute(Individual ind, Terrarium terrarium, Colony colony, BehaviorContext ctx) {
        if (!ind.isAlive())
            return;

        float dx = ind.getX() - colony.getNestX();
        float dy = ind.getY() - colony.getNestY();
        float dz = ind.getZ() - colony.getNestZ();
        float distFromNest = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        // If enemy nearby, engage
        if (ctx.enemyNearby()) {
            engageEnemy(ind, ctx);
            return;
        }

        // If alarm pheromone, investigate
        if (ctx.alarmPheromoneStrength() > 0.2f) {
            investigate(ind);
            return;
        }

        var rand = java.util.concurrent.ThreadLocalRandom.current();

        // Stay near nest entrance
        if (distFromNest > GUARD_RANGE) {
            // Return to post
            float angleToNest = (float) Math.atan2(-dy, -dx);
            ind.turnTowards(angleToNest, 0.2f);
            ind.move(0.3f);
        } else {
            // Guard stance - small movements, face outward
            float angleFromNest = (float) Math.atan2(dy, dx);
            ind.turnTowards(angleFromNest, 0.1f);

            // Occasional small adjustments
            if (rand.nextFloat() < 0.05f) {
                ind.setHeading(ind.getHeading() + (rand.nextFloat() - 0.5f) * 0.5f);
            }
            ind.move(0.05f);
        }

        ind.setEnergy(ind.getEnergy() - 0.0001f);
    }

    private void engageEnemy(Individual ind, BehaviorContext ctx) {
        // Turn towards threat and advance
        ind.move(0.6f);
        ind.setEnergy(ind.getEnergy() - 0.001f);
        // Combat would reduce health of both parties
    }

    private void investigate(Individual ind) {
        // Move towards source of alarm
        ind.move(0.4f);
        // Add random component to spread out guards
        ind.setHeading(ind.getHeading() + (java.util.concurrent.ThreadLocalRandom.current().nextFloat() - 0.5f) * 0.3f);
    }
}
