package org.swarmforge.benchmarks;

import org.openjdk.jmh.annotations.*;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.species.CustomSpecies;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
public class SimulationBenchmark {

    private Simulation simulation;

    @Setup
    public void setup() {
        Terrarium terrarium = new Terrarium(100, 100, 20);
        simulation = new Simulation(terrarium);
        
        CustomSpecies s = new CustomSpecies();
        s.setScientificName("Lasius niger");
        
        Colony colony = new Colony(s, 50, 50, 0);
        for(int i=0; i<100; i++) {
           colony.addIndividual(colony.createWorker()); 
        }
        
        simulation.addColony(colony);
        simulation.start();
    }

    @Benchmark
    public void benchmarkTick() {
        simulation.tick();
    }
}
