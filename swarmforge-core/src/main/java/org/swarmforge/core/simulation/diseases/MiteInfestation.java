/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.diseases;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;

import java.util.Random;

/**
 * Parasitic mite infestation (e.g., Varroa-like).
 * Common in honeybees but adapted for ant simulation.
 * Weakens individuals and spreads through contact.
 *
 * <p>
 * Effects:
 * </p>
 * <ul>
 * <li>Slowly drains energy and health</li>
 * <li>Reduces work efficiency</li>
 * <li>Spreads easily through colony contact</li>
 * <li>Can be groomed off by nestmates</li>
 * </ul>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class MiteInfestation implements Disease {

    private static final Random random = new Random();

    @Override
    public String getName() {
        return "Parasitic Mite Infestation";
    }

    @Override
    public String getScientificName() {
        return "Varroa simulata";
    }

    @Override
    public TransmissionMode getTransmissionMode() {
        return TransmissionMode.CONTACT;
    }

    @Override
    public Severity getSeverity() {
        return Severity.MODERATE;
    }

    @Override
    public float getInfectionRate() {
        return 0.25f; // Highly contagious
    }

    @Override
    public float getRecoveryRate() {
        return 0.01f; // Can be groomed off
    }

    @Override
    public float getMortalityRate() {
        return 0.3f; // Moderate mortality
    }

    @Override
    public int getIncubationPeriod() {
        return 60; // ~1 minute
    }

    private Random getRng(Individual individual, Simulation simulation) {
        if (simulation != null && simulation.getRandom() != null) return simulation.getRandom();
        if (individual != null && individual.getRandom() != null) return individual.getRandom();
        return java.util.concurrent.ThreadLocalRandom.current();
    }

    @Override
    public boolean processTick(Individual individual, Simulation simulation, int ticksInfected) {
        Random random = getRng(individual, simulation);
        // Check for grooming/natural recovery
        if (random.nextFloat() < getRecoveryRate()) {
            return true; // Cured
        }

        // Energy drain - mites feed on the host
        individual.setEnergy(individual.getEnergy() - 0.05f);

        // Occasional health damage
        if (ticksInfected > 200 && random.nextFloat() < 0.01f) {
            individual.takeDamage(0.5f);
        }

        // Long-term infestation is more severe
        if (ticksInfected > 1000) {
            individual.setEnergy(individual.getEnergy() - 0.1f);

            if (random.nextFloat() < 0.001f) {
                individual.takeDamage(100); // Death
                return false;
            }
        }

        return false;
    }

    @Override
    public boolean attemptInfection(Individual source, Individual target, float distance) {
        if (!target.isAlive() || distance > 2.0f) {
            return false;
        }

        // Higher transmission during close contact
        float probability = getInfectionRate() * (1 - distance / 2.0f);

        // Nurses are more exposed (tending brood)
        if (target.getCaste() == Individual.Caste.NURSE) {
            probability *= 1.5f;
        }

        Random random = target.getRandom() != null ? target.getRandom() : java.util.concurrent.ThreadLocalRandom.current();
        return random.nextFloat() < probability;
    }

    @Override
    public boolean canInfect(Individual.Caste caste) {
        return caste != Individual.Caste.MALE; // Males less susceptible
    }
}
