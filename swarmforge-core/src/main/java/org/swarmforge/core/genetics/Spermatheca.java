/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.genetics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Organ storing drone sperm inside a queen.
 * Enables polyandry (mating with multiple drones) and lifetime egg fertilization.
 */
public class Spermatheca implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<HaplodiploidGenome> storedDroneSperm = new ArrayList<>();
    private int totalCapacity;
    private int remainingSpermUnits;

    public Spermatheca(int capacity) {
        this.totalCapacity = capacity;
        this.remainingSpermUnits = 0;
    }

    /**
     * Store drone sperm during nuptial flight.
     */
    public boolean storeSperm(HaplodiploidGenome droneSperm) {
        if (storedDroneSperm.size() >= totalCapacity) {
            return false;
        }
        storedDroneSperm.add(droneSperm);
        remainingSpermUnits += 100; // Each drone provides 100 fertilization units
        return true;
    }

    public boolean hasSperm() {
        return remainingSpermUnits > 0 && !storedDroneSperm.isEmpty();
    }

    /**
     * Fertilize a maternal egg gamete to yield a diploid female genome (Worker or Queen).
     */
    public HaplodiploidGenome fertilize(HaplodiploidGenome maternalEgg, float mutationRate, Random rng) {
        if (!hasSperm()) {
            throw new IllegalStateException("Spermatheca empty! Cannot fertilize egg.");
        }
        remainingSpermUnits--;
        HaplodiploidGenome fatherSperm = storedDroneSperm.get(rng.nextInt(storedDroneSperm.size()));
        return HaplodiploidGenome.combineGametes(maternalEgg, fatherSperm, mutationRate, rng);
    }

    public HaplodiploidGenome getRandomSperm(Random rng) {
        if (!hasSperm()) {
            return null;
        }
        return storedDroneSperm.get(rng.nextInt(storedDroneSperm.size()));
    }

    public List<HaplodiploidGenome> getStoredDroneSperm() {
        return Collections.unmodifiableList(storedDroneSperm);
    }

    public int getRemainingSpermUnits() {
        return remainingSpermUnits;
    }

    public int getMateCount() {
        return storedDroneSperm.size();
    }
}
