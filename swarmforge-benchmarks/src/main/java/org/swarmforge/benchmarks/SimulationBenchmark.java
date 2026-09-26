package org.swarmforge.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.ResourceType;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.LasiusNiger;

import java.util.concurrent.TimeUnit;

/**
 * JMH Micro-Benchmark for SwarmForge Simulation Engine tick throughput.
 *
 * <p>Measures the raw throughput of {@link Simulation#tick()} under a realistic
 * Lasius niger colony with standard caste proportions (20% nurses, 35% foragers,
 * 15% soldiers, 30% workers) on a 100×100×20 terrarium grid with active
 * food sources and a predator. This gives a reproducible baseline for
 * CI regression detection and hardware comparison.</p>
 *
 * <h3>Caste Demographics (colony size = {@value #COLONY_SIZE})</h3>
 * <ul>
 *   <li>Nurses   : 20% — FSM brain, {@link Individual.Job#NURSE}</li>
 *   <li>Foragers : 35% — BDI brain, {@link Individual.Job#FORAGER}</li>
 *   <li>Soldiers : 15% — {@link Individual.Job#GUARD}</li>
 *   <li>Workers  : 30% — {@link Individual.Job#BUILDER}</li>
 * </ul>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
public class SimulationBenchmark {

    /** Colony population used during JMH micro-benchmarks. */
    private static final int COLONY_SIZE = 1_000;

    private Simulation simulation;

    @Setup(Level.Trial)
    public void setup() {
        // 1. Build realistic 3D Terrarium (100×100×20, surface at z<5 = AIR, z≥5 = EARTH)
        Terrarium terrarium = ScenarioPopulator.createTerrarium(100, 100, 20);
        simulation = new Simulation(terrarium);

        // 2. Instantiate colony with Lasius niger species parameters
        LasiusNiger species = new LasiusNiger();
        Colony colony = new Colony(species, 50.0f, 50.0f, 5.0f);
        colony.addProtein(5_000.0f);
        colony.addCarbohydrate(5_000.0f);
        colony.setWaterStored(5_000.0f);
        colony.createQueens(1);

        // 3. Populate with realistic caste proportions
        int nurseCount    = (int) (COLONY_SIZE * 0.20);
        int foragerCount  = (int) (COLONY_SIZE * 0.35);
        int soldierCount  = (int) (COLONY_SIZE * 0.15);
        int workerCount   = COLONY_SIZE - nurseCount - foragerCount - soldierCount - 1; // -1 for queen

        for (int i = 0; i < nurseCount; i++) {
            Individual ind = new Individual(colony.getId(), Individual.Caste.NURSE, 50f, 50f, 5f);
            ind.setSpecies(species);
            ind.setJob(Individual.Job.NURSE);
            ind.setBrain(new org.swarmforge.core.behavior.FSMArchitecture());
            colony.addIndividual(ind);
        }
        for (int i = 0; i < foragerCount; i++) {
            Individual ind = new Individual(colony.getId(), Individual.Caste.FORAGER,
                    50f + (float)(Math.random() * 20 - 10),
                    50f + (float)(Math.random() * 20 - 10), 5f);
            ind.setSpecies(species);
            ind.setJob(Individual.Job.FORAGER);
            ind.setBrain(new org.swarmforge.core.behavior.BDIArchitecture());
            colony.addIndividual(ind);
        }
        for (int i = 0; i < soldierCount; i++) {
            Individual ind = colony.createSoldier();
            ind.setPosition(50f + (float)(Math.random() * 15 - 7.5f),
                            50f + (float)(Math.random() * 15 - 7.5f), 5f);
            ind.setJob(Individual.Job.GUARD);
        }
        for (int i = 0; i < workerCount; i++) {
            Individual ind = colony.createWorker();
            ind.setPosition(50f + (float)(Math.random() * 10 - 5),
                            50f + (float)(Math.random() * 10 - 5), 5f);
            ind.setJob(Individual.Job.BUILDER);
        }

        simulation.addColony(colony);

        // 4. Spawn food sources and one predator for realistic load
        simulation.spawnFood(30f, 30f, 5f, 1_500f, ResourceType.SUGAR);
        simulation.spawnFood(70f, 70f, 5f, 1_500f, ResourceType.PROTEIN);
        simulation.getPredatorManager().spawnPredator(
                org.swarmforge.core.domain.PredatorType.BEETLE, 20f, 20f, 5f);

        // 5. JVM warmup — run a few ticks before JMH measures
        simulation.start();
        for (int i = 0; i < 10; i++) simulation.tick();
    }

    /**
     * Benchmark target: measures raw throughput of one full simulation tick
     * with all subsystems active (pheromones, BDI/FSM brains, weather, predators).
     *
     * @return opaque result to prevent JIT dead-code elimination
     */
    @Benchmark
    public Object benchmarkTick() {
        simulation.tick();
        return simulation;
    }
}
