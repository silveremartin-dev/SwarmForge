/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.genetics;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class HaplodiploidGeneticsTest {

    @Test
    @DisplayName("Test Haplodiploid Ploidy & Trait Expression")
    void testHaplodiploidTraits() {
        Random rng = new Random(42);

        HaplodiploidGenome queen = HaplodiploidGenome.createWildType(HaplodiploidGenome.Ploidy.DIPLOID_FEMALE, rng);
        HaplodiploidGenome drone = HaplodiploidGenome.createWildType(HaplodiploidGenome.Ploidy.HAPLOID_MALE, rng);

        assertEquals(HaplodiploidGenome.Ploidy.DIPLOID_FEMALE, queen.getPloidy());
        assertEquals(HaplodiploidGenome.Ploidy.HAPLOID_MALE, drone.getPloidy());

        assertTrue(queen.getColdResistance() > 0.1f);
        assertTrue(queen.getAggression() > 0.1f);
        assertTrue(queen.getForagingEfficiency() > 0.1f);
        assertTrue(queen.getHeatTolerance() > 0.1f);
        assertTrue(queen.getPathogenResistance() > 0.1f);
    }

    @Test
    @DisplayName("Test Nuptial Flight, Spermatheca & Fertilization")
    void testNuptialFlightAndSpermatheca() {
        Random rng = new Random(100);

        HaplodiploidGenome queenGenome = HaplodiploidGenome.createWildType(HaplodiploidGenome.Ploidy.DIPLOID_FEMALE, rng);
        HaplodiploidGenome drone1 = HaplodiploidGenome.createWildType(HaplodiploidGenome.Ploidy.HAPLOID_MALE, rng);
        HaplodiploidGenome drone2 = HaplodiploidGenome.createWildType(HaplodiploidGenome.Ploidy.HAPLOID_MALE, rng);

        NuptialFlightManager flightManager = new NuptialFlightManager();
        assertTrue(flightManager.isFlightConditionsOptimal(24.0f, 0.70f, 5.0f));
        assertFalse(flightManager.isFlightConditionsOptimal(10.0f, 0.30f, 25.0f));

        List<NuptialFlightManager.MatingPairResult> results = flightManager.executeNuptialFlight(
                List.of(queenGenome),
                List.of(drone1, drone2),
                3,
                rng
        );

        assertEquals(1, results.size());
        Spermatheca spermatheca = results.get(0).spermatheca();
        assertTrue(spermatheca.hasSperm());
        assertTrue(spermatheca.getMateCount() >= 1);

        // Test Fertilization -> Diploid female offspring
        HaplodiploidEvolutionEngine engine = new HaplodiploidEvolutionEngine();
        HaplodiploidGenome workerOffspring = engine.produceOffspringFemale(queenGenome, spermatheca, 0.05f, rng);
        assertEquals(HaplodiploidGenome.Ploidy.DIPLOID_FEMALE, workerOffspring.getPloidy());
        assertEquals(1, workerOffspring.getGeneration());

        // Test Unfertilized egg -> Haploid male offspring (Hymenoptera / Thrips)
        HaplodiploidGenome maleOffspring = engine.produceOffspringMale(queenGenome, 0.05f, rng);
        assertEquals(HaplodiploidGenome.Ploidy.HAPLOID_MALE, maleOffspring.getPloidy());
        assertEquals(1, maleOffspring.getGeneration());

        // Test Diploid Male offspring (Termites / Eusocial Beetles)
        HaplodiploidGenome termiteMale = engine.produceOffspringDiploidMale(queenGenome, spermatheca, 0.05f, rng);
        assertEquals(HaplodiploidGenome.Ploidy.DIPLOID_MALE, termiteMale.getPloidy());
        assertTrue(termiteMale.getColdResistance() > 0.0f);

        // Test Parthenogenetic female offspring (Eusocial Aphids)
        HaplodiploidGenome aphidFemale = engine.produceOffspringParthenogenetic(queenGenome, 0.05f, rng);
        assertEquals(HaplodiploidGenome.Ploidy.DIPLOID_FEMALE, aphidFemale.getPloidy());
        assertEquals(1, aphidFemale.getGeneration());
    }

    @Test
    @DisplayName("Test Environmental Selection & Fitness Evaluation")
    void testSelectionPressure() {
        Random rng = new Random(123);
        HaplodiploidEvolutionEngine engine = new HaplodiploidEvolutionEngine();

        HaplodiploidGenome g1 = HaplodiploidGenome.createWildType(HaplodiploidGenome.Ploidy.DIPLOID_FEMALE, rng);
        HaplodiploidGenome g2 = HaplodiploidGenome.createWildType(HaplodiploidGenome.Ploidy.DIPLOID_FEMALE, rng);

        HaplodiploidEvolutionEngine.EnvironmentalConditions coldSnap = new HaplodiploidEvolutionEngine.EnvironmentalConditions(
                2.0f, 0.1f, 0.8f, 0.2f
        );

        float fit1 = engine.calculateIndividualFitness(g1, coldSnap);
        assertTrue(fit1 > 0.0f);

        HaplodiploidGenome[] pop = new HaplodiploidGenome[]{g1, g2};
        HaplodiploidEvolutionEngine.GenerationFitnessReport report = engine.evaluatePopulation(pop, coldSnap);
        assertTrue(report.overallColonyFitness() > 0.0f);
        assertTrue(report.meanColdResistance() > 0.0f);
    }
}
