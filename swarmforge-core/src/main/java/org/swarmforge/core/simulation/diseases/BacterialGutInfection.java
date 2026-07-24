/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.diseases;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.event.SimulationEvent;

import java.util.Random;

/**
 * Bacterial infection affecting the digestive system.
 * Spreads through contaminated food or water.
 *
 * <p>
 * Effects:
 * </p>
 * <ul>
 * <li>Causes hunger to increase rapidly</li>
 * <li>Energy inefficiency (food doesn't help as much)</li>
 * <li>Moderate mortality</li>
 * <li>Spreads through trophallaxis (food sharing)</li>
 * </ul>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class BacterialGutInfection implements Disease {

    private static final Random random = new Random();

    @Override
    public String getName() {
        return "Bacterial Gut Infection";
    }

    @Override
    public String getScientificName() {
        return "Serratia marcescens simulata";
    }

    @Override
    public TransmissionMode getTransmissionMode() {
        return TransmissionMode.FOOD;
    }

    @Override
    public Severity getSeverity() {
        return Severity.MODERATE;
    }

    @Override
    public float getInfectionRate() {
        return 0.1f; // Through food sharing
    }

    @Override
    public float getRecoveryRate() {
        return 0.02f; // Can recover naturally
    }

    @Override
    public float getMortalityRate() {
        return 0.4f;
    }

    @Override
    public int getIncubationPeriod() {
        return 120; // ~2 minutes
    }

    @Override
    public boolean processTick(Individual individual, Simulation simulation, int ticksInfected) {
        if (ticksInfected < getIncubationPeriod()) {
            return false;
        }

        // Check for recovery
        if (random.nextFloat() < getRecoveryRate()) {
            simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.INFO,
                    simulation.getTickCount(),
                    "Individual recovered from gut infection"));
            return true;
        }

        // Increased hunger due to nutrient malabsorption
        individual.setHunger(Math.min(100, individual.getHunger() + 0.3f));

        // Energy drain from fighting infection
        individual.setEnergy(individual.getEnergy() - 0.08f);

        // Occasional weakness episodes
        if (random.nextFloat() < 0.05f) {
            individual.setState(Individual.AiState.IDLE);
        }

        // Check for death from starvation/weakness
        if (ticksInfected > 2000 && individual.getEnergy() < 10) {
            if (random.nextFloat() < 0.01f) {
                individual.takeDamage(100);
            }
        }

        return false;
    }

    @Override
    public boolean attemptInfection(Individual source, Individual target, float distance) {
        // Food-borne - only spread during trophallaxis (very close contact)
        if (distance > 1.0f || !target.isAlive()) {
            return false;
        }

        // Only spread if source was recently feeding
        if (source.getHunger() > 50) {
            return false;
        }

        return random.nextFloat() < getInfectionRate();
    }

    @Override
    public boolean canInfect(Individual.Caste caste) {
        return true; // All castes can be infected
    }
}
