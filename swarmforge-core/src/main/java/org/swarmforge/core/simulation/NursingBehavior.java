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
 * Nursing behavior for nurse ants.
 * Cares for brood, feeds larvae, moves eggs to optimal conditions.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NursingBehavior implements BehaviorStrategy {

    private final Random random = new Random();

    @Override
    public void execute(Individual ind, Terrarium terrarium, Colony colony, BehaviorContext ctx) {
        if (!ind.isAlive())
            return;

        // If carrying food for brood
        if (ind.getCarriedItem() == Individual.CarriedItem.FOOD) {
            if (ctx.atNest()) {
                // Feed larvae (food transferred to brood)
                ind.setCarriedItem(Individual.CarriedItem.NONE);
                ind.setState(Individual.AiState.TEND_BROOD);
            } else {
                moveTowardsNest(ind, colony);
            }
        } else {
            // Not carrying food
            if (!ctx.atNest()) {
                // Move back to brood chamber in nest
                moveTowardsNest(ind, colony);
            } else {
                // At nest: either seek food from storage or tend brood directly
                if (random.nextFloat() < 0.1f && colony.getFoodStored() > 0) {
                    colony.setFoodStored(Math.max(0.0f, colony.getFoodStored() - 0.5f));
                    ind.setCarriedItem(Individual.CarriedItem.FOOD);
                } else {
                    // Tend brood with micro-movements
                    ind.setState(Individual.AiState.TEND_BROOD);
                    ind.setHeading(ind.getHeading() + (random.nextFloat() - 0.5f) * 0.3f);
                    ind.move(0.1f);
                }
            }
        }

        ind.setEnergy(Math.max(0.0f, ind.getEnergy() - 0.0001f));
    }

    private void moveTowardsNest(Individual ind, Colony colony) {
        float dx = colony.getNestX() - ind.getX();
        float dy = colony.getNestY() - ind.getY();
        float targetAngle = (float) Math.atan2(dy, dx);
        ind.turnTowards(targetAngle, 0.15f);
        ind.move(0.3f);
    }
}
