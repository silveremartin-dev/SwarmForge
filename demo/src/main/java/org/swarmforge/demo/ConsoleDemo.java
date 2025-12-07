/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.demo;

import org.swarmforge.core.domain.*;
import org.swarmforge.core.simulation.*;
import org.swarmforge.core.world.*;
import org.swarmforge.core.gpu.GpuExecutor;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Console demo showcasing SwarmForge simulation.
 * Demonstrates core functionality without GUI.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class ConsoleDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔═══════════════════════════════════════════════════════════╗");
        System.out.println("║           SwarmForge - Console Demo                       ║");
        System.out.println("║     Eusocial Insect Simulation Platform                   ║");
        System.out.println("╚═══════════════════════════════════════════════════════════╝");
        System.out.println();

        // Load i18n
        ResourceBundle messages = ResourceBundle.getBundle("i18n.messages", Locale.ENGLISH);
        System.out.println("Language: English");
        System.out.println();

        // Create world
        System.out.println("► Creating world (128x128x64)...");
        Terrarium terrarium = new Terrarium(128, 128, 64);
        terrarium.setLatitude(48.8566);
        terrarium.setLongitude(2.3522);
        System.out.println("  Location: Paris, France");

        // Generate terrain
        System.out.println("► Generating terrain with Perlin noise...");
        TerrainGenerator terrainGen = new TerrainGenerator(42);
        terrainGen.generate(terrarium, 40, 8f, 0.03f);
        System.out.println("  Cells created: " + terrarium.getCellCount());

        // Generate nest
        System.out.println("► Generating ant nest (L-system)...");
        NestGenerator nestGen = new NestGenerator(terrarium, 42);
        int chambers = nestGen.generate(64, 64, 40, NestGenerator.NestType.MATURE, 1.0f);
        System.out.println("  Chambers created: " + chambers);

        // Create colony
        System.out.println("► Creating colony: Lasius niger (Black Garden Ant)...");
        Colony colony = new Colony("Lasius niger", 64, 64, 38);

        // Add queen
        colony.addIndividual(new Individual(colony.getId(), Individual.Caste.QUEEN, 64, 64, 35));
        System.out.println("  Queen added");

        // Add workers
        for (int i = 0; i < 50; i++) {
            float x = 64 + (float) (Math.random() * 6 - 3);
            float y = 64 + (float) (Math.random() * 6 - 3);
            colony.addIndividual(new Individual(colony.getId(), Individual.Caste.WORKER, x, y, 36));
        }
        System.out.println("  Workers added: 50");
        System.out.println("  Total population: " + colony.getPopulation());
        System.out.println();

        // Initialize simulation
        System.out.println("► Initializing simulation engine...");
        Simulation simulation = new Simulation(terrarium);
        simulation.addColony(colony);
        simulation.setTicksPerSecond(10);

        // GPU executor
        GpuExecutor gpu = new GpuExecutor();
        System.out.println("  Compute device: " + gpu.getDeviceName());

        // Weather
        WeatherSystem weather = new WeatherSystem(48.8566, 2.3522);
        System.out.println("  Temperature: " + String.format("%.1f", weather.getTemperature()) + "°C");
        System.out.println("  Humidity: " + String.format("%.0f", weather.getHumidity()) + "%");
        System.out.println("  Daytime: " + weather.isDaytime());
        System.out.println();

        // Run simulation
        System.out.println("► Running simulation for 100 ticks...");
        System.out.println("  [");

        for (int i = 0; i < 100; i++) {
            simulation.tick();

            if (i % 10 == 0) {
                System.out.print("  ▓");
            }
        }
        System.out.println("]");
        System.out.println();

        // Report
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("  Simulation Report (Tick " + simulation.getTickCount() + ")");
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("  Colony: " + colony.getSpeciesName());
        System.out.println("  Population: " + colony.getPopulation());
        System.out.println("  Has Queen: " + colony.hasQueen());
        System.out.println("  Workers: " + colony.countByCaste(Individual.Caste.WORKER));
        System.out.println("  Food stored: " + String.format("%.1f", colony.getFoodStored()));
        System.out.println("  Total born: " + colony.getTotalBorn());
        System.out.println("  Total died: " + colony.getTotalDied());
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println();
        System.out.println("Demo complete. SwarmForge is ready!");
    }
}
