/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles demographic census calculations and history telemetry snapshots for a colony.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ColonyCensusTracker implements Serializable {
    private static final long serialVersionUID = 1L;

    public static int countByCaste(List<Individual> individuals, Individual.Caste caste) {
        if (individuals == null || caste == null) return 0;
        return (int) individuals.stream()
                .filter(i -> i.isAlive() && i.getCaste() == caste)
                .count();
    }

    public static float getAverageAgeByCaste(List<Individual> individuals, Individual.Caste caste) {
        if (individuals == null || caste == null) return 0f;
        List<Individual> casteInds = individuals.stream()
                .filter(i -> i.isAlive() && i.getCaste() == caste)
                .toList();
        if (casteInds.isEmpty()) return 0f;
        double sum = casteInds.stream().mapToDouble(Individual::getAge).sum();
        return (float) (sum / casteInds.size());
    }

    public static int getBroodCountByStage(List<Individual> individuals, Individual.LifeStage stage) {
        if (individuals == null || stage == null) return 0;
        return (int) individuals.stream()
                .filter(i -> i.isAlive() && i.getLifeStage() == stage)
                .count();
    }

    public static void recordDetailedSnapshot(Colony colony, ColonyStatistics statistics,
                                             long currentTick, float nestTemp, float nestCo2, float nestO2) {
        if (colony == null || statistics == null) return;

        Map<Individual.Caste, Integer> casteMap = new HashMap<>();
        Map<Individual.Caste, Float> ageMap = new HashMap<>();
        for (Individual.Caste c : Individual.Caste.values()) {
            int count = colony.countByCaste(c);
            casteMap.put(c, count);
            if (count > 0) {
                ageMap.put(c, colony.getAverageAgeByCaste(c));
            } else {
                ageMap.put(c, 0f);
            }
        }

        Map<Individual.LifeStage, Integer> broodMap = new HashMap<>();
        for (Individual.LifeStage s : Individual.LifeStage.values()) {
            broodMap.put(s, colony.getBroodCountByStage(s));
        }

        int chamberCount = (colony.getNest() != null && colony.getNest().getChambers() != null)
                ? colony.getNest().getChambers().size() : 0;

        ColonyStatistics.DetailedDataPoint point = new ColonyStatistics.DetailedDataPoint(
                currentTick,
                colony.getAgeInTicks(),
                colony.getPopulation(),
                casteMap,
                ageMap,
                broodMap,
                colony.getFoodStored(),
                colony.getProteinStored(),
                colony.getCarbohydrateStored(),
                colony.getWaterStored(),
                nestTemp,
                nestCo2,
                nestO2,
                chamberCount,
                colony.getTotalBorn(),
                colony.getTotalDied()
        );

        statistics.recordDetailed(point);
    }
}
