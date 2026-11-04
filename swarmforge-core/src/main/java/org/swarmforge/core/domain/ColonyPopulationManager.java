/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Handles individual registration, bulk operations, death reaping, and population queries for {@link Colony}.
 */
public final class ColonyPopulationManager {

    private ColonyPopulationManager() {}

    public static void addIndividual(Colony colony, List<Individual> individuals, Individual individual, List<ColonyListener> listeners) {
        if (individual == null || individuals.contains(individual)) return;
        if (individual.getSpecies() == null && colony.getSpecies() != null) {
            individual.setSpecies(colony.getSpecies());
        }
        individual.setRandom(new Random(colony.getRandom().nextLong()));
        individuals.add(individual);
        colony.incrementTotalBorn();
        for (ColonyListener l : listeners) {
            l.onBirth(colony, individual);
        }
    }

    public static void addIndividualsBulk(Colony colony, List<Individual> individuals, Collection<Individual> batch) {
        if (batch == null || batch.isEmpty()) return;
        List<Individual> toAdd;
        if (individuals.isEmpty()) {
            toAdd = batch.stream().filter(Objects::nonNull).toList();
        } else {
            Set<Individual> existing = new HashSet<>(individuals);
            toAdd = batch.stream().filter(ind -> ind != null && !existing.contains(ind)).toList();
        }
        if (toAdd.isEmpty()) return;
        toAdd.forEach(ind -> {
            if (colony.getSpecies() != null && ind.getSpecies() == null) {
                ind.setSpecies(colony.getSpecies());
            }
            ind.setRandom(new Random(colony.getRandom().nextLong()));
        });
        individuals.addAll(toAdd);
        colony.addTotalBorn(toAdd.size());
    }

    public static int removeDeadIndividuals(Colony colony, List<Individual> individuals, List<ColonyListener> listeners) {
        List<Individual> dead = new ArrayList<>();
        for (Individual ind : individuals) {
            if (!ind.isAlive()) {
                dead.add(ind);
            }
        }
        if (dead.isEmpty()) return 0;

        individuals.removeAll(dead);
        colony.addTotalDied(dead.size());
        for (Individual ind : dead) {
            for (ColonyListener l : listeners) {
                l.onDeath(colony, ind);
            }
        }
        return dead.size();
    }
}
