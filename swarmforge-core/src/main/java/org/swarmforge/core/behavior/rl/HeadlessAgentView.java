/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.behavior.rl;

import org.swarmforge.core.behavior.AgentView;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.species.Species;
import org.swarmforge.core.species.SpeciesRegistry;

import java.util.UUID;

/**
 * Synthetic AgentView adapter for rapid multi-caste training data generation.
 */
public class HeadlessAgentView implements AgentView {
    public float x = 0f, y = 0f, z = 0f;
    public float heading = 0f;
    public float energy = 1.0f;
    public boolean carryingFood = false;
    public boolean atNest = false;
    public Individual.Caste caste = Individual.Caste.WORKER;
    public String insectType = "ANT";
    public boolean hasFoodNearby = false;
    public boolean hasEnemyNearby = false;
    public float alarmPheromone = 0.0f;
    public float foodPheromoneGradientX = 0.0f;
    public float foodPheromoneGradientY = 0.0f;

    @Override public float getX() { return x; }
    @Override public float getY() { return y; }
    @Override public float getZ() { return z; }
    @Override public float getHeading() { return heading; }
    @Override public float getHomeX() { return 0f; }
    @Override public float getHomeY() { return 0f; }
    @Override public boolean isCarryingFood() { return carryingFood; }
    @Override public boolean isAtNest() { return atNest; }
    @Override public float getEnergyLevel() { return energy; }
    @Override public float getHunger() { return 1.0f - energy; }
    @Override public UUID getColonyId() { return UUID.randomUUID(); }
    @Override public boolean isSoldier() { return caste == Individual.Caste.SOLDIER; }
    @Override public boolean isQueen() { return caste == Individual.Caste.QUEEN; }
    @Override public boolean isNurse() { return caste == Individual.Caste.NURSE; }
    @Override public boolean isDrone() { return caste == Individual.Caste.MALE; }
    @Override public Species getSpecies() {
        return SpeciesRegistry.getInstance().getSpecies("lasius-niger");
    }
}
