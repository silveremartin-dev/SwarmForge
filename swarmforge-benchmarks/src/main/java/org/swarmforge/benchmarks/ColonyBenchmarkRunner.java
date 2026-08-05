package org.swarmforge.benchmarks;

import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.species.LasiusNiger;
import org.swarmforge.core.species.FormicaRufa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * High-fidelity benchmark runner measuring simulation throughput (Ticks Per Second)
 * and tick latency across various colony sizes with all biological and physical
 * simulation subsystems fully activated.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class ColonyBenchmarkRunner {

    public static void main(String[] args) {
        System.out.println("===============================================================================");
        System.out.println("            SWARMFORGE SIMULATION ENGINE BENCHMARK SUITE                       ");
        System.out.println("===============================================================================");
        System.out.println("Java Version  : " + System.getProperty("java.version"));
        System.out.println("OS Architecture: " + System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
        System.out.println("Available Cores: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Max Memory     : " + (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + " MB");
        System.out.println("-------------------------------------------------------------------------------");
        System.out.println("All Subsystems Active: Terrarium, SpatialHashMap, Multi-Channel PheromoneGrid, ");
        System.out.println("WeatherSystem, DayNightCycle, SeasonManager, WaterGrid, SoilStructureSystem,   ");
        System.out.println("PheromoneClimateSystem, SymbiosisSystem, NuptialFlight, Diapause, Diseases,   ");
        System.out.println("PredatorManager, TerritoryManager, BDI/FSM Cognitive Brains, Construction.      ");
        System.out.println("-------------------------------------------------------------------------------\n");

        int[] colonySizes = { 100, 500, 1000, 2500, 5000, 10000, 25000, 50000, 100000, 250000, 500000, 1000000 };
        int warmupTicks = 20;

        System.out.printf("%-12s | %-12s | %-14s | %-12s | %-12s | %-12s%n",
                "Colony Size", "TPS (ticks/s)", "Avg Latency (ms)", "Min (ms)", "p95 (ms)", "Max (ms)");
        System.out.println("---------------------------------------------------------------------------------");

        for (int size : colonySizes) {
            int ticksToMeasure = (size >= 250_000) ? 30 : ((size >= 50_000) ? 50 : 150);
            runBenchmarkForSize(size, warmupTicks, ticksToMeasure);
        }

        System.out.println("===============================================================================");
        System.out.println("Benchmark completed successfully.");
    }

    private static void runBenchmarkForSize(int size, int warmupTicks, int measuredTicks) {
        // 1. Create 3D Terrarium World (100x100x20 cells)
        int width = 100;
        int height = 100;
        int depth = 20;
        Terrarium terrarium = new Terrarium(width, height, depth);

        // Initialize terrarium cells with soil and surface air
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    TerrariumCell.Material mat = (z < 5) ? TerrariumCell.Material.AIR : TerrariumCell.Material.EARTH;
                    terrarium.setCell(new TerrariumCell(
                            x, y, z, mat,
                            new float[TerrariumCell.PHEROMONE_TYPES], 22.0f, 60.0f));
                }
            }
        }

        // 2. Instantiate Simulation Engine with all subsystems enabled
        Simulation simulation = new Simulation(terrarium);

        // 3. Create Primary Colony
        LasiusNiger species = new LasiusNiger();
        Colony colony = new Colony(species, 50.0f, 50.0f, 5.0f);
        colony.addProtein(5000.0f);
        colony.addCarbohydrate(5000.0f);
        colony.setWaterStored(5000.0f);

        // 4. Create Queen
        colony.createQueen();

        // 5. Populate Colony with realistic caste proportions
        int nurseCount = (int) (size * 0.20);
        int soldierCount = (int) (size * 0.15);
        int foragerCount = (int) (size * 0.35);
        int workerCount = size - (nurseCount + soldierCount + foragerCount + 1);

        for (int i = 0; i < nurseCount; i++) {
            Individual ind = new Individual(colony.getId(), Individual.Caste.NURSE, 50.0f + (float)(Math.random()*10-5), 50.0f + (float)(Math.random()*10-5), 5.0f);
            ind.setSpecies(species);
            ind.setJob(Individual.Job.NURSE);
            ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
            colony.addIndividual(ind);
        }

        for (int i = 0; i < soldierCount; i++) {
            Individual ind = colony.createSoldier();
            ind.setPosition(50.0f + (float)(Math.random()*20-10), 50.0f + (float)(Math.random()*20-10), 5.0f);
            ind.setJob(Individual.Job.GUARD);
        }

        for (int i = 0; i < foragerCount; i++) {
            Individual ind = new Individual(colony.getId(), Individual.Caste.FORAGER, 50.0f + (float)(Math.random()*30-15), 50.0f + (float)(Math.random()*30-15), 5.0f);
            ind.setSpecies(species);
            ind.setJob(Individual.Job.FORAGER);
            ind.setBrain(new org.swarmforge.core.behavior.BDIArchitecture());
            colony.addIndividual(ind);
        }

        for (int i = 0; i < workerCount; i++) {
            Individual ind = colony.createWorker();
            ind.setPosition(50.0f + (float)(Math.random()*20-10), 50.0f + (float)(Math.random()*20-10), 5.0f);
            ind.setJob(Individual.Job.BUILDER);
        }

        simulation.addColony(colony);

        // 6. Spawn Food Sources
        for (int f = 0; f < 10; f++) {
            float fx = (float) (Math.random() * 80 + 10);
            float fy = (float) (Math.random() * 80 + 10);
            simulation.spawnFood(fx, fy, 5.0f, 500.0f, ResourceType.SUGAR);
            simulation.spawnFood(fx + 2, fy + 2, 5.0f, 300.0f, ResourceType.PROTEIN);
        }

        // 7. Spawn Active Predators
        for (int p = 0; p < 5; p++) {
            simulation.getPredatorManager().spawnPredator(
                    org.swarmforge.core.domain.PredatorType.BEETLE,
                    (float) (Math.random() * 80 + 10),
                    (float) (Math.random() * 80 + 10),
                    5.0f);
        }

        // 8. Warmup Phase
        for (int w = 0; w < warmupTicks; w++) {
            simulation.tick();
        }

        // 9. Benchmark Execution Phase
        long[] elapsedNanos = new long[measuredTicks];
        long startTotal = System.nanoTime();

        for (int t = 0; t < measuredTicks; t++) {
            long tickStart = System.nanoTime();
            simulation.tick();
            elapsedNanos[t] = System.nanoTime() - tickStart;
        }

        long totalNanos = System.nanoTime() - startTotal;

        // 10. Compute Benchmark Statistics
        double tps = (measuredTicks * 1_000_000_000.0) / totalNanos;
        double[] msDurations = Arrays.stream(elapsedNanos).mapToDouble(n -> n / 1_000_000.0).sorted().toArray();

        double avgMs = Arrays.stream(msDurations).average().orElse(0.0);
        double minMs = msDurations[0];
        double maxMs = msDurations[msDurations.length - 1];
        double p95Ms = msDurations[(int) (msDurations.length * 0.95)];

        System.out.printf("%-12d | %-12.2f | %-14.4f | %-12.4f | %-12.4f | %-12.4f%n",
                size, tps, avgMs, minMs, p95Ms, maxMs);

        // Force GC between benchmarks to prevent memory accumulation affecting next test
        System.gc();
    }
}
