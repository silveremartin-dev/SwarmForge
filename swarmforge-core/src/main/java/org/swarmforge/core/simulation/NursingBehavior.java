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
 * Nursing behavior for nurse ants.
 * Cares for brood, feeds larvae, moves eggs to optimal conditions.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class NursingBehavior implements BehaviorStrategy {

    private final Random random = new Random();

    private enum NurseState {
        SEEKING_BROOD, TENDING, SEEKING_FOOD, FEEDING
    }

    private NurseState state = NurseState.SEEKING_BROOD;

    @Override
    public void execute(Individual ind, Terrarium terrarium, Colony colony, BehaviorContext ctx) {
        if (!ind.isAlive())
            return;

        switch (state) {
            case SEEKING_BROOD -> {
                // Follow brood pheromone to find larvae/eggs
                if (ctx.atNest()) {
                    // Found brood area
                    state = NurseState.TENDING;
                } else {
                    // Move towards nest center (brood is typically there)
                    moveTowardsNest(ind, colony);
                }
            }
            case TENDING -> {
                // Stay near brood, occasionally check temperature/humidity
                if (random.nextFloat() < 0.1f) {
                    // Check if brood needs food
                    state = NurseState.SEEKING_FOOD;
                }
                // Small random movements while tending
                ind.setHeading(ind.getHeading() + (random.nextFloat() - 0.5f) * 0.3f);
                ind.move(0.1f);
            }
            case SEEKING_FOOD -> {
                // Go get food from storage
                if (colony.getFoodStored() > 0) {
                    colony.setFoodStored(colony.getFoodStored() - 0.5f);
                    ind.setCarriedItem(Individual.CarriedItem.FOOD);
                    state = NurseState.FEEDING;
                } else {
                    // No food available, go back to tending
                    state = NurseState.TENDING;
                }
            }
            case FEEDING -> {
                // Bring food back to brood
                if (ctx.atNest()) {
                    // Feed larvae (food disappears)
                    ind.setCarriedItem(Individual.CarriedItem.NONE);
                    state = NurseState.TENDING;
                } else {
                    moveTowardsNest(ind, colony);
                }
            }
        }

        ind.setEnergy(ind.getEnergy() - 0.02f);
    }

    private void moveTowardsNest(Individual ind, Colony colony) {
        float dx = colony.getNestX() - ind.getX();
        float dy = colony.getNestY() - ind.getY();
        float targetAngle = (float) Math.atan2(dy, dx);
        ind.turnTowards(targetAngle, 0.15f);
        ind.move(0.3f);
    }
}
