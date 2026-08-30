/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.diseases;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;

/**
 * Interface for diseases and parasites that can affect individuals.
 * Diseases can spread between individuals, affect health/behavior, and be
 * cured.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public interface Disease {

    /**
     * Disease transmission mode.
     */
    enum TransmissionMode {
        CONTACT, // Direct contact with infected individual
        AIRBORNE, // Proximity-based
        FOOD, // Through contaminated food
        VECTOR, // Through parasites/mites
        ENVIRONMENTAL // From terrain (e.g., contaminated soil)
    }

    /**
     * Disease severity level.
     */
    enum Severity {
        MILD, // Minor symptoms, usually recovers
        MODERATE, // Noticeable symptoms, may spread
        SEVERE, // Serious health impact
        LETHAL // High mortality rate
    }

    /**
     * Get the disease name.
     */
    String getName();

    /**
     * Get the scientific name (if applicable).
     */
    String getScientificName();

    /**
     * Get transmission mode.
     */
    TransmissionMode getTransmissionMode();

    /**
     * Get severity level.
     */
    Severity getSeverity();

    /**
     * Get base infection probability (0-1).
     */
    float getInfectionRate();

    /**
     * Get recovery probability per tick (0-1).
     */
    float getRecoveryRate();

    /**
     * Get mortality rate for this disease (0-1).
     */
    float getMortalityRate();

    /**
     * Get incubation period in ticks before symptoms appear.
     */
    int getIncubationPeriod();

    /**
     * Process disease effects on an infected individual.
     * 
     * @param individual    Infected individual
     * @param simulation    Simulation context
     * @param ticksInfected How long the individual has been infected
     * @return true if individual should be cured this tick
     */
    boolean processTick(Individual individual, Simulation simulation, int ticksInfected);

    /**
     * Process disease effects on an infected individual using step duration deltaSeconds.
     */
    default boolean processTick(Individual individual, Simulation simulation, int ticksInfected, float deltaSeconds) {
        return processTick(individual, simulation, ticksInfected);
    }

    /**
     * Attempt to infect a nearby individual.
     * 
     * @param source   Infected source
     * @param target   Potential victim
     * @param distance Distance between individuals
     * @return true if infection occurred
     */
    boolean attemptInfection(Individual source, Individual target, float distance);

    /**
     * Check if this disease can infect the given caste.
     */
    boolean canInfect(Individual.Caste caste);
}
