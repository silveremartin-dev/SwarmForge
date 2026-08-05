/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.genetics;

import java.io.Serializable;
import java.util.Random;

/**
 * Population Genetics & Adaptive Evolution Engine.
 * Evaluates colony survival fitness under environmental selection pressures:
 * - Cold Snaps (Selection on Cold Resistance)
 * - Heat Waves (Selection on Heat Tolerance)
 * - Pathogen Epizootics (Selection on Pathogen Resistance)
 * - Resource Scarcity (Selection on Foraging Efficiency)
 * - Inter-Colony Territorial Conflicts (Selection on Aggression)
 */
public class HaplodiploidEvolutionEngine implements Serializable {
    private static final long serialVersionUID = 1L;

    public record EnvironmentalConditions(
            float temperatureC,
            float pathogenSeverity, // 0.0 - 1.0
            float resourceAbundance, // 0.0 - 1.0
            float rivalColonyThreat  // 0.0 - 1.0
    ) implements Serializable {}

    public record GenerationFitnessReport(
            float meanColdResistance,
            float meanAggression,
            float meanForagingEfficiency,
            float meanHeatTolerance,
            float meanPathogenResistance,
            float overallColonyFitness
    ) implements Serializable {}

    /**
     * Compute survival probability multiplier for an individual genome under specific environmental stress.
     */
    public float calculateIndividualFitness(HaplodiploidGenome genome, EnvironmentalConditions env) {
        float coldFitness = 1.0f;
        if (env.temperatureC() < 10.0f) {
            float deficit = (10.0f - env.temperatureC()) / 10.0f;
            coldFitness = Math.max(0.05f, 1.0f - (deficit / Math.max(0.1f, genome.getColdResistance())));
        }

        float heatFitness = 1.0f;
        if (env.temperatureC() > 30.0f) {
            float excess = (env.temperatureC() - 30.0f) / 10.0f;
            heatFitness = Math.max(0.05f, 1.0f - (excess / Math.max(0.1f, genome.getHeatTolerance())));
        }

        float pathogenFitness = 1.0f - (env.pathogenSeverity() / (1.0f + genome.getPathogenResistance()));

        float foragingBonus = genome.getForagingEfficiency() * env.resourceAbundance();
        float combatBonus = genome.getAggression() * env.rivalColonyThreat();

        float baseFitness = (coldFitness + heatFitness + pathogenFitness) / 3.0f;
        return Math.max(0.01f, baseFitness * (0.8f + 0.2f * (foragingBonus + combatBonus)));
    }

    /**
     * Generates a new offspring worker or queen genome from a mated queen.
     */
    public HaplodiploidGenome produceOffspringFemale(HaplodiploidGenome queenGenome, Spermatheca spermatheca,
                                                     float mutationRate, Random rng) {
        HaplodiploidGenome eggGamete = queenGenome.produceGamete(rng);
        return spermatheca.fertilize(eggGamete, mutationRate, rng);
    }

    /**
     * Generates an unfertilized egg into a male drone (Hymenoptera & Thrips).
     */
    public HaplodiploidGenome produceOffspringMale(HaplodiploidGenome queenGenome, float mutationRate, Random rng) {
        HaplodiploidGenome eggGamete = queenGenome.produceGamete(rng);
        return HaplodiploidGenome.developUnfertilizedEgg(eggGamete, mutationRate, rng);
    }

    /**
     * Generates a diploid male offspring (Termites Isoptera & Beetles Coleoptera).
     */
    public HaplodiploidGenome produceOffspringDiploidMale(HaplodiploidGenome queenGenome, Spermatheca spermatheca,
                                                          float mutationRate, Random rng) {
        HaplodiploidGenome eggGamete = queenGenome.produceGamete(rng);
        HaplodiploidGenome spermGamete = spermatheca.getRandomSperm(rng);
        if (spermGamete == null) {
            return produceOffspringMale(queenGenome, mutationRate, rng);
        }
        return HaplodiploidGenome.combineGametesMale(eggGamete, spermGamete, mutationRate, rng);
    }

    /**
     * Generates a parthenogenetic diploid female offspring (Eusocial Aphids Hemiptera).
     */
    public HaplodiploidGenome produceOffspringParthenogenetic(HaplodiploidGenome queenGenome, float mutationRate, Random rng) {
        return HaplodiploidGenome.developParthenogeneticEgg(queenGenome, mutationRate, rng);
    }

    /**
     * Aggregates population fitness stats for an array of genomes.
     */
    public GenerationFitnessReport evaluatePopulation(HaplodiploidGenome[] population, EnvironmentalConditions env) {
        if (population == null || population.length == 0) {
            return new GenerationFitnessReport(0, 0, 0, 0, 0, 0);
        }

        float sumCold = 0f, sumAggr = 0f, sumForage = 0f, sumHeat = 0f, sumPath = 0f, sumFitness = 0f;
        for (HaplodiploidGenome g : population) {
            sumCold += g.getColdResistance();
            sumAggr += g.getAggression();
            sumForage += g.getForagingEfficiency();
            sumHeat += g.getHeatTolerance();
            sumPath += g.getPathogenResistance();
            sumFitness += calculateIndividualFitness(g, env);
        }

        int n = population.length;
        return new GenerationFitnessReport(
                sumCold / n,
                sumAggr / n,
                sumForage / n,
                sumHeat / n,
                sumPath / n,
                sumFitness / n
        );
    }
}
