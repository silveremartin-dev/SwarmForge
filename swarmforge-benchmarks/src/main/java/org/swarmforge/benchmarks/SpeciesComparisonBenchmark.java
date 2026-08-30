package org.swarmforge.benchmarks;

import org.swarmforge.core.domain.*;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Species Comparative Benchmark Suite.
 * Measures tick throughput (TPS), tick latency, and memory scaling across
 * diverse biological species models in SwarmForge.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpeciesComparisonBenchmark {

    public record SpeciesBenchmarkResult(
            String speciesName,
            String category,
            int colonySize,
            double tps,
            double avgLatencyMs,
            double minLatencyMs,
            double p95LatencyMs,
            double maxLatencyMs,
            long memoryUsedMb
    ) {}

    public static List<SpeciesBenchmarkResult> runSpeciesBenchmarks(int[] sizes, int measuredTicks) {
        List<SpeciesBenchmarkResult> results = new ArrayList<>();

        List<Species> speciesList = List.of(
                new LasiusNiger(),
                new FormicaRufa(),
                new AttaCephalotes(),
                new SolenopsisInvicta(),
                new CamponotusPennsylvanicus(),
                new ApisMellifera()
        );

        for (Species species : speciesList) {
            for (int size : sizes) {
                SpeciesBenchmarkResult res = benchmarkSingleSpecies(species, size, measuredTicks);
                results.add(res);
            }
        }

        return results;
    }

    private static SpeciesBenchmarkResult benchmarkSingleSpecies(Species species, int size, int measuredTicks) {
        Terrarium terrarium = ScenarioPopulator.createTerrarium(100, 100, 20);
        Simulation sim = new Simulation(terrarium);

        Colony colony = new Colony(species, 50.0f, 50.0f, 5.0f);
        colony.addProtein(5000.0f);
        colony.addCarbohydrate(5000.0f);
        colony.createQueens(1);

        int nurseCount = (int) (size * 0.20);
        int soldierCount = (int) (size * 0.15);
        int foragerCount = (int) (size * 0.35);
        int workerCount = size - (nurseCount + soldierCount + foragerCount + 1);

        for (int i = 0; i < nurseCount; i++) {
            Individual ind = new Individual(colony.getId(), Individual.Caste.NURSE, 50.0f, 50.0f, 5.0f);
            ind.setSpecies(species);
            ind.setJob(Individual.Job.NURSE);
            ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
            colony.addIndividual(ind);
        }
        for (int i = 0; i < soldierCount; i++) {
            Individual ind = colony.createSoldier();
            ind.setSpecies(species);
        }
        for (int i = 0; i < foragerCount; i++) {
            Individual ind = new Individual(colony.getId(), Individual.Caste.FORAGER, 50.0f, 50.0f, 5.0f);
            ind.setSpecies(species);
            ind.setJob(Individual.Job.FORAGER);
            ind.setBrain(new org.swarmforge.core.behavior.BDIArchitecture());
            colony.addIndividual(ind);
        }
        for (int i = 0; i < workerCount; i++) {
            Individual ind = colony.createWorker();
            ind.setSpecies(species);
        }

        sim.addColony(colony);

        // Food & Predators
        sim.spawnFood(30, 30, 5, 2000, ResourceType.SUGAR);
        sim.spawnFood(70, 70, 5, 2000, ResourceType.PROTEIN);
        sim.getPredatorManager().spawnPredator(PredatorType.BEETLE, 20, 20, 5);

        // Warmup
        for (int w = 0; w < 15; w++) {
            sim.tick();
        }

        long[] elapsedNanos = new long[measuredTicks];
        long startTotal = System.nanoTime();

        for (int t = 0; t < measuredTicks; t++) {
            long tickStart = System.nanoTime();
            sim.tick();
            elapsedNanos[t] = System.nanoTime() - tickStart;
        }

        long totalNanos = System.nanoTime() - startTotal;
        double tps = (measuredTicks * 1_000_000_000.0) / totalNanos;
        double[] msDurations = Arrays.stream(elapsedNanos).mapToDouble(n -> n / 1_000_000.0).sorted().toArray();

        double avgMs = Arrays.stream(msDurations).average().orElse(0.0);
        double minMs = msDurations[0];
        double maxMs = msDurations[msDurations.length - 1];
        double p95Ms = msDurations[(int) (msDurations.length * 0.95)];

        long memoryUsedMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);

        System.gc();

        return new SpeciesBenchmarkResult(
                species.getCommonName() != null ? species.getCommonName() : species.getClass().getSimpleName(),
                species.getScientificName() != null ? species.getScientificName() : "Inconnu",
                size,
                tps,
                avgMs,
                minMs,
                p95Ms,
                maxMs,
                memoryUsedMb
        );
    }
}
