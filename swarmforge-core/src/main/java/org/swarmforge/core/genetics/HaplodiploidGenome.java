/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.genetics;

import java.io.Serializable;
import java.util.Random;
import java.util.UUID;

/**
 * Implements an explicit Haplodiploid Digital Genome for Hymenoptera.
 * Females (Queens & Workers) are Diploid (2n), carrying maternal and paternal allele sets.
 * Males (Drones) are Haploid (1n), carrying a single maternal allele set.
 *
 * Expressed traits influence colony adaptation:
 * - Cold Resistance
 * - Aggression Level
 * - Foraging Efficiency
 * - Heat Tolerance
 * - Pathogen Resistance
 */
public class HaplodiploidGenome implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Ploidy {
        HAPLOID_MALE,   // 1n (Hymenoptera & Thrips drones)
        DIPLOID_FEMALE, // 2n (Queens, Workers, Soldiers)
        DIPLOID_MALE    // 2n (Termite Kings & Male Workers, Eusocial Beetle Males)
    }

    public record GeneLocus(float maternalAllele, float paternalAllele) implements Serializable {
        public float getExpressedValue(boolean isHaploid) {
            if (isHaploid) {
                return maternalAllele;
            }
            // Co-dominant additive expression with minor dominance effect
            return (maternalAllele + paternalAllele) / 2.0f;
        }

        public GeneLocus mutate(float mutationRate, float mutationMagnitude, Random rng) {
            float newMat = maternalAllele;
            float newPat = paternalAllele;

            if (rng.nextFloat() < mutationRate) {
                newMat += (float) (rng.nextGaussian() * mutationMagnitude);
                newMat = Math.max(0.1f, Math.min(2.5f, newMat));
            }
            if (rng.nextFloat() < mutationRate) {
                newPat += (float) (rng.nextGaussian() * mutationMagnitude);
                newPat = Math.max(0.1f, Math.min(2.5f, newPat));
            }
            return new GeneLocus(newMat, newPat);
        }
    }

    private final UUID genomeId;
    private final Ploidy ploidy;
    private final int generation;

    // Genetic Loci for Adaptive Traits
    private final GeneLocus coldResistanceLocus;
    private final GeneLocus aggressionLocus;
    private final GeneLocus foragingEfficiencyLocus;
    private final GeneLocus heatToleranceLocus;
    private final GeneLocus pathogenResistanceLocus;

    public HaplodiploidGenome(Ploidy ploidy, int generation,
                              GeneLocus coldResistanceLocus,
                              GeneLocus aggressionLocus,
                              GeneLocus foragingEfficiencyLocus,
                              GeneLocus heatToleranceLocus,
                              GeneLocus pathogenResistanceLocus) {
        this.genomeId = UUID.randomUUID();
        this.ploidy = ploidy;
        this.generation = generation;
        this.coldResistanceLocus = coldResistanceLocus;
        this.aggressionLocus = aggressionLocus;
        this.foragingEfficiencyLocus = foragingEfficiencyLocus;
        this.heatToleranceLocus = heatToleranceLocus;
        this.pathogenResistanceLocus = pathogenResistanceLocus;
    }

    /**
     * Create a wild-type initial genome.
     */
    public static HaplodiploidGenome createWildType(Ploidy ploidy, Random rng) {
        float var = 0.1f;
        GeneLocus cold = new GeneLocus(1.0f + (float) rng.nextGaussian() * var, 1.0f + (float) rng.nextGaussian() * var);
        GeneLocus aggr = new GeneLocus(1.0f + (float) rng.nextGaussian() * var, 1.0f + (float) rng.nextGaussian() * var);
        GeneLocus forage = new GeneLocus(1.0f + (float) rng.nextGaussian() * var, 1.0f + (float) rng.nextGaussian() * var);
        GeneLocus heat = new GeneLocus(1.0f + (float) rng.nextGaussian() * var, 1.0f + (float) rng.nextGaussian() * var);
        GeneLocus path = new GeneLocus(1.0f + (float) rng.nextGaussian() * var, 1.0f + (float) rng.nextGaussian() * var);

        return new HaplodiploidGenome(ploidy, 0, cold, aggr, forage, heat, path);
    }

    /**
     * Meiotic recombination to produce a haploid gamete (egg from queen or sperm from drone).
     */
    public HaplodiploidGenome produceGamete(Random rng) {
        boolean isHaploidMale = (ploidy == Ploidy.HAPLOID_MALE);

        float coldG = selectAllele(coldResistanceLocus, isHaploidMale, rng);
        float aggrG = selectAllele(aggressionLocus, isHaploidMale, rng);
        float forageG = selectAllele(foragingEfficiencyLocus, isHaploidMale, rng);
        float heatG = selectAllele(heatToleranceLocus, isHaploidMale, rng);
        float pathG = selectAllele(pathogenResistanceLocus, isHaploidMale, rng);

        GeneLocus coldL = new GeneLocus(coldG, coldG);
        GeneLocus aggrL = new GeneLocus(aggrG, aggrG);
        GeneLocus forageL = new GeneLocus(forageG, forageG);
        GeneLocus heatL = new GeneLocus(heatG, heatG);
        GeneLocus pathL = new GeneLocus(pathG, pathG);

        return new HaplodiploidGenome(Ploidy.HAPLOID_MALE, generation, coldL, aggrL, forageL, heatL, pathL);
    }

    private float selectAllele(GeneLocus locus, boolean isHaploidMale, Random rng) {
        if (isHaploidMale) {
            return locus.maternalAllele();
        }
        // Independent assortment / crossing-over equivalent
        return rng.nextBoolean() ? locus.maternalAllele() : locus.paternalAllele();
    }

    /**
     * Combine maternal egg gamete and paternal sperm gamete to produce a diploid female.
     */
    public static HaplodiploidGenome combineGametes(HaplodiploidGenome maternalEgg, HaplodiploidGenome paternalSperm,
                                                    float mutationRate, Random rng) {
        int nextGen = Math.max(maternalEgg.generation, paternalSperm.generation) + 1;

        GeneLocus cold = combineLocus(maternalEgg.coldResistanceLocus, paternalSperm.coldResistanceLocus, mutationRate, rng);
        GeneLocus aggr = combineLocus(maternalEgg.aggressionLocus, paternalSperm.aggressionLocus, mutationRate, rng);
        GeneLocus forage = combineLocus(maternalEgg.foragingEfficiencyLocus, paternalSperm.foragingEfficiencyLocus, mutationRate, rng);
        GeneLocus heat = combineLocus(maternalEgg.heatToleranceLocus, paternalSperm.heatToleranceLocus, mutationRate, rng);
        GeneLocus path = combineLocus(maternalEgg.pathogenResistanceLocus, paternalSperm.pathogenResistanceLocus, mutationRate, rng);

        return new HaplodiploidGenome(Ploidy.DIPLOID_FEMALE, nextGen, cold, aggr, forage, heat, path);
    }

    /**
     * Combine maternal egg gamete and paternal sperm gamete to produce a diploid male (for Termites & Beetles).
     */
    public static HaplodiploidGenome combineGametesMale(HaplodiploidGenome maternalEgg, HaplodiploidGenome paternalSperm,
                                                        float mutationRate, Random rng) {
        int nextGen = Math.max(maternalEgg.generation, paternalSperm.generation) + 1;

        GeneLocus cold = combineLocus(maternalEgg.coldResistanceLocus, paternalSperm.coldResistanceLocus, mutationRate, rng);
        GeneLocus aggr = combineLocus(maternalEgg.aggressionLocus, paternalSperm.aggressionLocus, mutationRate, rng);
        GeneLocus forage = combineLocus(maternalEgg.foragingEfficiencyLocus, paternalSperm.foragingEfficiencyLocus, mutationRate, rng);
        GeneLocus heat = combineLocus(maternalEgg.heatToleranceLocus, paternalSperm.heatToleranceLocus, mutationRate, rng);
        GeneLocus path = combineLocus(maternalEgg.pathogenResistanceLocus, paternalSperm.pathogenResistanceLocus, mutationRate, rng);

        return new HaplodiploidGenome(Ploidy.DIPLOID_MALE, nextGen, cold, aggr, forage, heat, path);
    }

    /**
     * Parthenogenetic clonal reproduction producing diploid female offspring (for Eusocial Aphids).
     */
    public static HaplodiploidGenome developParthenogeneticEgg(HaplodiploidGenome maternalGenome, float mutationRate, Random rng) {
        int nextGen = maternalGenome.generation + 1;

        GeneLocus cold = maternalGenome.coldResistanceLocus.mutate(mutationRate, 0.05f, rng);
        GeneLocus aggr = maternalGenome.aggressionLocus.mutate(mutationRate, 0.05f, rng);
        GeneLocus forage = maternalGenome.foragingEfficiencyLocus.mutate(mutationRate, 0.05f, rng);
        GeneLocus heat = maternalGenome.heatToleranceLocus.mutate(mutationRate, 0.05f, rng);
        GeneLocus path = maternalGenome.pathogenResistanceLocus.mutate(mutationRate, 0.05f, rng);

        return new HaplodiploidGenome(Ploidy.DIPLOID_FEMALE, nextGen, cold, aggr, forage, heat, path);
    }

    /**
     * Develop unfertilized egg into a haploid male.
     */
    public static HaplodiploidGenome developUnfertilizedEgg(HaplodiploidGenome maternalEgg, float mutationRate, Random rng) {
        int nextGen = maternalEgg.generation + 1;

        GeneLocus cold = maternalEgg.coldResistanceLocus.mutate(mutationRate, 0.05f, rng);
        GeneLocus aggr = maternalEgg.aggressionLocus.mutate(mutationRate, 0.05f, rng);
        GeneLocus forage = maternalEgg.foragingEfficiencyLocus.mutate(mutationRate, 0.05f, rng);
        GeneLocus heat = maternalEgg.heatToleranceLocus.mutate(mutationRate, 0.05f, rng);
        GeneLocus path = maternalEgg.pathogenResistanceLocus.mutate(mutationRate, 0.05f, rng);

        return new HaplodiploidGenome(Ploidy.HAPLOID_MALE, nextGen, cold, aggr, forage, heat, path);
    }

    private static GeneLocus combineLocus(GeneLocus mat, GeneLocus pat, float mutationRate, Random rng) {
        GeneLocus locus = new GeneLocus(mat.maternalAllele(), pat.maternalAllele());
        return locus.mutate(mutationRate, 0.05f, rng);
    }

    // Expressed Phenotype Trait Getters
    public float getColdResistance() {
        return coldResistanceLocus.getExpressedValue(ploidy == Ploidy.HAPLOID_MALE);
    }

    public float getAggression() {
        return aggressionLocus.getExpressedValue(ploidy == Ploidy.HAPLOID_MALE);
    }

    public float getForagingEfficiency() {
        return foragingEfficiencyLocus.getExpressedValue(ploidy == Ploidy.HAPLOID_MALE);
    }

    public float getHeatTolerance() {
        return heatToleranceLocus.getExpressedValue(ploidy == Ploidy.HAPLOID_MALE);
    }

    public float getPathogenResistance() {
        return pathogenResistanceLocus.getExpressedValue(ploidy == Ploidy.HAPLOID_MALE);
    }

    public UUID getGenomeId() {
        return genomeId;
    }

    public Ploidy getPloidy() {
        return ploidy;
    }

    public int getGeneration() {
        return generation;
    }
}
