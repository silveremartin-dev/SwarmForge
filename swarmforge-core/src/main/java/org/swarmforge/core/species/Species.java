/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.species;

/**
 * Interface for species definitions.
 * Each species defines physical characteristics, lifecycle, and behavior
 * parameters.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
@com.fasterxml.jackson.annotation.JsonTypeInfo(use = com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CLASS, include = com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY, property = "@class")
public interface Species {

    /**
     * @return Scientific name of the species
     */
    String getScientificName();

    /**
     * @return Common name of the species
     */
    String getCommonName();

    /**
     * @return Average lifespan in simulation ticks for workers
     */
    int getWorkerLifespan();

    /**
     * @return Average lifespan in simulation ticks for queens
     */
    int getQueenLifespan();

    /**
     * @return Movement speed for workers
     */
    float getWorkerSpeed();

    /**
     * @return View distance for detecting food/threats
     */
    float getViewDistance();

    /**
     * @return Typical colony size at maturity
     */
    int getTypicalColonySize();

    /**
     * @return Whether this species forms mega-colonies
     */
    default boolean formsMegaColonies() {
        return false;
    }

    /**
     * @return Aggression level (0.0 to 1.0). High aggression leads to more combat.
     */
    default float getAggression() {
        return 0.3f; // Default low aggression
    }

    /**
     * @return Metabolism rate (multiplier). Higher means faster hunger/fatigue.
     */
    default float getMetabolism() {
        return 1.0f;
    }

    /**
     * @return Combat strength (damage per hit).
     */
    default float getStrength() {
        return 5.0f;
    }

    /**
     * @return Set of resource types this species forages for to bring to the
     * 
     *         colony.
     */
    default java.util.Set<org.swarmforge.core.domain.ResourceType> getForagingTypes() {
        // Default to common ants
        return java.util.Set.of(
                org.swarmforge.core.domain.ResourceType.SEED,
                org.swarmforge.core.domain.ResourceType.NECTAR);
    }

    /**
     * @return List of available caste templates for this species.
     */
    default java.util.List<org.swarmforge.core.domain.CasteTemplate> getCastes() {
        return java.util.Collections.emptyList();
    }

    /**
     * Configure an individual of this species (set stats, brain, etc).
     */
    default void configureIndividual(org.swarmforge.core.domain.Individual individual) {
        // Default: do nothing or basic setup
    }
}
