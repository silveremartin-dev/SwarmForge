/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.genetics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Simulates the Nuptial Flight (Vol Nuptial) of virgin queens and drones.
 * Handles mating swarms, polyandrous sperm accumulation in spermathecas,
 * and foundress queen initialization for new colonies.
 */
public class NuptialFlightManager {

    public record MatingPairResult(HaplodiploidGenome queenGenome, Spermatheca spermatheca) {}

    /**
     * Check if atmospheric conditions allow a nuptial flight trigger.
     */
    public boolean isFlightConditionsOptimal(float temperatureC, float humidity, float windSpeed) {
        return (temperatureC >= 20.0f && temperatureC <= 32.0f) &&
               (humidity >= 0.60f) &&
               (windSpeed <= 12.0f);
    }

    /**
     * Conducts a nuptial flight mating event.
     * Virgin queens mate with 1 to N males depending on species polyandry.
     */
    public List<MatingPairResult> executeNuptialFlight(
            List<HaplodiploidGenome> virginQueens,
            List<HaplodiploidGenome> drones,
            int maxMatesPerQueen,
            Random rng) {

        List<MatingPairResult> matedQueens = new ArrayList<>();

        if (virginQueens.isEmpty() || drones.isEmpty()) {
            return matedQueens;
        }

        for (HaplodiploidGenome queen : virginQueens) {
            Spermatheca spermatheca = new Spermatheca(maxMatesPerQueen);
            int matesCount = 1 + rng.nextInt(Math.min(maxMatesPerQueen, drones.size()));

            for (int i = 0; i < matesCount; i++) {
                HaplodiploidGenome selectedDrone = drones.get(rng.nextInt(drones.size()));
                HaplodiploidGenome droneGamete = selectedDrone.produceGamete(rng);
                spermatheca.storeSperm(droneGamete);
            }

            matedQueens.add(new MatingPairResult(queen, spermatheca));
        }

        return matedQueens;
    }
}
