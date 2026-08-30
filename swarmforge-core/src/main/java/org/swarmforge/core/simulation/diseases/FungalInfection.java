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
 * Fungal infection disease (e.g., Ophiocordyceps-like).
 * Affects behavior and eventually kills the host.
 *
 * <p>
 * Famous example: "Zombie ant fungus" (Ophiocordyceps unilateralis)
 * </p>
 *
 * <p>
 * Progression:
 * </p>
 * <ol>
 * <li>Incubation: No visible symptoms</li>
 * <li>Early: Slight behavior changes, reduced speed</li>
 * <li>Advanced: Erratic behavior, energy drain</li>
 * <li>Terminal: Death and spore release</li>
 * </ol>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class FungalInfection implements Disease {

    private static final String NAME = "Cordyceps Fungal Infection";
    private static final String SCIENTIFIC_NAME = "Ophiocordyceps swarmforgei";

    private static final int INCUBATION_PERIOD = 500; // ~8 minutes
    private static final int EARLY_PHASE = 1000; // Symptoms start
    private static final int ADVANCED_PHASE = 2000; // Severe symptoms
    private static final int TERMINAL_PHASE = 3000; // Death

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getScientificName() {
        return SCIENTIFIC_NAME;
    }

    @Override
    public TransmissionMode getTransmissionMode() {
        return TransmissionMode.CONTACT;
    }

    @Override
    public Severity getSeverity() {
        return Severity.LETHAL;
    }

    @Override
    public float getInfectionRate() {
        return 0.15f; // 15% chance per contact
    }

    @Override
    public float getRecoveryRate() {
        return 0.001f; // Very low natural recovery
    }

    @Override
    public float getMortalityRate() {
        return 0.95f; // 95% mortality
    }

    @Override
    public int getIncubationPeriod() {
        return INCUBATION_PERIOD;
    }

    @Override
    public boolean processTick(Individual individual, Simulation simulation, int ticksInfected) {
        if (ticksInfected < INCUBATION_PERIOD) {
            // Incubation phase - no visible symptoms
            return false;
        }

        Random random = getRng(individual, simulation);
        // Check for natural recovery (rare)
        if (random.nextFloat() < getRecoveryRate()) {
            simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.INFO,
                    simulation.getTickCount(),
                    "Individual recovered from fungal infection"));
            return true;
        }

        if (ticksInfected < EARLY_PHASE) {
            // Early phase - mild symptoms
            processEarlyPhase(individual, simulation);
        } else if (ticksInfected < ADVANCED_PHASE) {
            // Advanced phase - severe symptoms
            processAdvancedPhase(individual, simulation);
        } else if (ticksInfected < TERMINAL_PHASE) {
            // Terminal phase - death approaches
            processTerminalPhase(individual, simulation);
        } else {
            // Death and spore release
            processDeath(individual, simulation);
        }

        return false;
    }

    private Random getRng(Individual individual, Simulation simulation) {
        if (simulation != null && simulation.getRandom() != null) return simulation.getRandom();
        if (individual != null && individual.getRandom() != null) return individual.getRandom();
        return java.util.concurrent.ThreadLocalRandom.current();
    }

    private void processEarlyPhase(Individual individual, Simulation simulation) {
        // Slight energy drain
        individual.setEnergy(individual.getEnergy() - 0.1f);

        Random random = getRng(individual, simulation);
        // Occasional erratic movement
        if (random.nextFloat() < 0.1f) {
            individual.setHeading(individual.getHeading() + (random.nextFloat() - 0.5f) * 2f);
        }
    }

    private void processAdvancedPhase(Individual individual, Simulation simulation) {
        // Significant energy drain
        individual.setEnergy(individual.getEnergy() - 0.3f);

        // Health deterioration
        individual.setHealth(individual.getHealth() - 0.2f);

        Random random = getRng(individual, simulation);
        // Erratic "zombie" behavior - wander aimlessly
        if (random.nextFloat() < 0.3f) {
            individual.setHeading(individual.getHeading() + (random.nextFloat() - 0.5f) * 3f);
            individual.setState(Individual.AiState.WANDER);
        }

        // Characteristic climbing behavior
        if (random.nextFloat() < 0.05f && individual.getZ() < 100) {
            // Try to climb (fungus makes ants climb)
            individual.setPosition(individual.getX(), individual.getY(), individual.getZ() + 1);
        }
    }

    private void processTerminalPhase(Individual individual, Simulation simulation) {
        // Rapid health decline
        individual.takeDamage(0.5f);

        // Locked in place (death grip behavior of cordyceps)
        individual.setState(Individual.AiState.IDLE);

        // Energy depleted
        individual.setEnergy(Math.max(0, individual.getEnergy() - 1f));
    }

    private void processDeath(Individual individual, Simulation simulation) {
        if (individual.isAlive()) {
            individual.takeDamage(100);

            simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.DEATH,
                    simulation.getTickCount(),
                    "Individual died from fungal infection, releasing spores"));

            // Release spores (infect nearby individuals)
            releaseSpores(individual, simulation);
        }
    }

    /**
     * Release spores on death, potentially infecting nearby individuals.
     */
    private void releaseSpores(Individual source, Simulation simulation) {
        float sporeRadius = 15.0f;

        var nearby = simulation.getSpatialIndex().queryRadius(
                source.getX(), source.getY(), source.getZ(), sporeRadius);

        for (Individual target : nearby) {
            if (target.isAlive() && target != source) {
                float dist = distance(source, target);
                // Higher infection chance for spores
                float sporeProbability = 0.4f * (1 - dist / sporeRadius);

                Random random = getRng(source, simulation);
                if (random.nextFloat() < sporeProbability) {
                    // Mark as infected (would need infection tracking)
                    simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.INFO,
                            simulation.getTickCount(),
                            "New fungal infection from spores"));
                }
            }
        }
    }

    @Override
    public boolean attemptInfection(Individual source, Individual target, float distance) {
        if (!target.isAlive() || distance > 3.0f) {
            return false;
        }

        // Contact-based transmission
        float probability = getInfectionRate() * (1 - distance / 3.0f);

        // Reduced infection for queens (better immune system)
        if (target.getCaste() == Individual.Caste.QUEEN) {
            probability *= 0.3f;
        }

        Random random = getRng(source, null);
        return random.nextFloat() < probability;
    }

    @Override
    public boolean canInfect(Individual.Caste caste) {
        // Can infect all castes, but some are more resistant
        return true;
    }

    private float distance(Individual a, Individual b) {
        float dx = a.getX() - b.getX();
        float dy = a.getY() - b.getY();
        float dz = a.getZ() - b.getZ();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
