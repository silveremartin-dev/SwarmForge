/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a colony of eusocial insects.
 * Manages all individuals and colony-level resources.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class Colony {

    private final UUID id;
    private final String speciesName;
    private final CopyOnWriteArrayList<Individual> individuals;

    // Colony location (nest entrance)
    private float nestX, nestY, nestZ;

    // Resources
    private float foodStored;
    private float waterStored;

    // Statistics
    private int totalBorn;
    private int totalDied;

    public Colony(String speciesName, float nestX, float nestY, float nestZ) {
        this.id = UUID.randomUUID();
        this.speciesName = speciesName;
        this.individuals = new CopyOnWriteArrayList<>();
        this.nestX = nestX;
        this.nestY = nestY;
        this.nestZ = nestZ;
    }

    /**
     * Add an individual to this colony.
     */
    public void addIndividual(Individual individual) {
        individuals.add(individual);
        totalBorn++;
    }

    /**
     * Remove dead individuals from the colony.
     */
    public int removeDeadIndividuals() {
        int removed = 0;
        for (Individual ind : individuals) {
            if (!ind.isAlive()) {
                individuals.remove(ind);
                totalDied++;
                removed++;
            }
        }
        return removed;
    }

    /**
     * Get count of individuals by caste.
     */
    public int countByCaste(Individual.Caste caste) {
        return (int) individuals.stream()
                .filter(i -> i.isAlive() && i.getCaste() == caste)
                .count();
    }

    /**
     * Get all living individuals.
     */
    public List<Individual> getLivingIndividuals() {
        return individuals.stream()
                .filter(Individual::isAlive)
                .toList();
    }

    /**
     * Get total population (living only).
     */
    public int getPopulation() {
        return (int) individuals.stream().filter(Individual::isAlive).count();
    }

    /**
     * Check if colony has a living queen.
     */
    public boolean hasQueen() {
        return individuals.stream()
                .anyMatch(i -> i.isAlive() && i.getCaste() == Individual.Caste.QUEEN);
    }

    // Getters
    public UUID getId() {
        return id;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public float getNestX() {
        return nestX;
    }

    public float getNestY() {
        return nestY;
    }

    public float getNestZ() {
        return nestZ;
    }

    public float getFoodStored() {
        return foodStored;
    }

    public float getWaterStored() {
        return waterStored;
    }

    public int getTotalBorn() {
        return totalBorn;
    }

    public int getTotalDied() {
        return totalDied;
    }

    // Setters
    public void setFoodStored(float food) {
        this.foodStored = Math.max(0, food);
    }

    public void setWaterStored(float water) {
        this.waterStored = Math.max(0, water);
    }
}
