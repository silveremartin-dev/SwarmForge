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

        // 1. If carrying food for brood
        if (ind.getCarriedItem() == Individual.CarriedItem.FOOD) {
            if (ctx.atNest()) {
                // Feed larvae (food transferred to brood)
                ind.setCarriedItem(Individual.CarriedItem.NONE);
                ind.setState(Individual.AiState.TEND_BROOD);
            } else {
                moveTowardsNest(ind, colony);
            }
        } 
        // 2. If carrying brood during thermal shuttling
        else if (ind.getCarriedItem() == Individual.CarriedItem.BROOD) {
            float optimalZ = colony != null ? colony.getDynamicQueenChamberDepth() : -2.0f;
            float currentZ = ind.getZ();
            if (Math.abs(currentZ - optimalZ) < 0.5f) {
                // Deposited brood in thermally regulated chamber
                ind.setCarriedItem(Individual.CarriedItem.NONE);
                ind.setState(Individual.AiState.TEND_BROOD);
            } else {
                // Move towards optimal chamber depth
                float stepZ = (optimalZ > currentZ) ? 0.2f : -0.2f;
                ind.setPosition(ind.getX(), ind.getY(), ind.getZ() + stepZ);
            }
        } 
        else {
            // Not carrying anything
            if (!ctx.atNest()) {
                // Move back to brood chamber in nest
                moveTowardsNest(ind, colony);
            } else {
                // At nest: check thermal gradient for brood shuttling needs
                float ambientTemp = ind.getAmbientTemperatureC();
                if (ambientTemp < 20.0f || ambientTemp > 31.0f) {
                    // Temperature stress: initiate brood translocation to deeper buffered chamber
                    if (random.nextFloat() < 0.2f) {
                        ind.setCarriedItem(Individual.CarriedItem.BROOD);
                    }
                } else if (random.nextFloat() < 0.1f && (colony.getFoodStored() > 0 || colony.getProteinStored() > 0)) {
                    if (colony.getProteinStored() > 0.5f) {
                        colony.setProteinStored(colony.getProteinStored() - 0.5f);
                    } else {
                        colony.setFoodStored(Math.max(0.0f, colony.getFoodStored() - 0.5f));
                    }
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
