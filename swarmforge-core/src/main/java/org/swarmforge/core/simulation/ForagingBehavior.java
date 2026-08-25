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
import org.swarmforge.core.domain.TerrariumCell;
import java.util.Random;

/**
 * Simple foraging behavior using pheromone following.
 * Demonstrates the behavior strategy pattern with realistic ant-like behavior.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ForagingBehavior implements BehaviorStrategy {

    private static final float RANDOM_TURN_CHANCE = 0.1f;
    private static final float PHEROMONE_DEPOSIT = 0.5f;
    private static final float MOVE_SPEED = 0.5f;

    private final Random random = new Random();

    @Override
    public void execute(Individual ind, Terrarium terrarium, Colony colony, BehaviorContext ctx) {
        if (!ind.isAlive())
            return;

        // If carrying food and at nest, deposit it
        if (ind.getCarriedItem() == Individual.CarriedItem.FOOD && ctx.atNest()) {
            ind.setCarriedItem(Individual.CarriedItem.NONE);
            colony.setFoodStored(colony.getFoodStored() + 1);
            // Turn around to go back out
            ind.setHeading(ind.getHeading() + (float) Math.PI);
            return;
        }

        // If carrying food, follow home pheromone
        if (ind.getCarriedItem() == Individual.CarriedItem.FOOD) {
            followPheromone(ind, terrarium, TerrariumCell.PHEROMONE_HOME);
            depositPheromone(terrarium, ind, TerrariumCell.PHEROMONE_FOOD);
        }
        // Otherwise, follow food pheromone or explore
        else {
            if (ctx.nearestFoodDistance() < 1.0f) {
                // Found food!
                ind.setCarriedItem(Individual.CarriedItem.FOOD);
                ind.setHeading(ind.getHeading() + (float) Math.PI);
            } else if (ctx.foodPheromoneStrength() > 0.1f) {
                followPheromone(ind, terrarium, TerrariumCell.PHEROMONE_FOOD);
            } else {
                // Random exploration with slight left bias
                explore(ind);
            }
            depositPheromone(terrarium, ind, TerrariumCell.PHEROMONE_HOME);
        }

        // Move forward
        ind.move(MOVE_SPEED);

        // Consume energy
        ind.setEnergy(ind.getEnergy() - 0.0001f);
    }

    private void followPheromone(Individual ind, Terrarium terrarium, int pheromoneType) {
        // Sample pheromones in a cone ahead
        float bestStrength = 0;
        float bestAngle = ind.getHeading();

        for (float angle = -0.5f; angle <= 0.5f; angle += 0.25f) {
            float testAngle = ind.getHeading() + angle;
            float testX = ind.getX() + (float) Math.cos(testAngle) * 2;
            float testY = ind.getY() + (float) Math.sin(testAngle) * 2;

            TerrariumCell cell = terrarium.getCell((int) testX, (int) testY, (int) ind.getZ());
            float strength = cell.getPheromone(pheromoneType);

            if (strength > bestStrength) {
                bestStrength = strength;
                bestAngle = testAngle;
            }
        }

        ind.turnTowards(bestAngle, 0.2f);
    }

    private void explore(Individual ind) {
        if (random.nextFloat() < RANDOM_TURN_CHANCE) {
            ind.setHeading(ind.getHeading() + (random.nextFloat() - 0.5f) * 0.5f);
        }
    }

    private void depositPheromone(Terrarium terrarium, Individual ind, int pheromoneType) {
        int x = (int) ind.getX();
        int y = (int) ind.getY();
        int z = (int) ind.getZ();

        TerrariumCell current = terrarium.getCell(x, y, z);
        float[] newPheromones = current.pheromones().clone();
        newPheromones[pheromoneType] = Math.min(1.0f, newPheromones[pheromoneType] + PHEROMONE_DEPOSIT);

        TerrariumCell updated = new TerrariumCell(
                x, y, z, current.material(), newPheromones, current.temperature(), current.humidity());
        terrarium.setCell(updated);
    }
}
