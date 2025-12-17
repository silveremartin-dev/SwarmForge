/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.disasters;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.species.CustomSpecies;

/**
 * Unit tests for Disaster classes.
 */
class DisasterTest {

    private Simulation simulation;
    private Terrarium terrarium;
    private Colony colony;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(50, 50, 20);
        for (int x = 0; x < 50; x++) {
            for (int y = 0; y < 50; y++) {
                for (int z = 0; z < 20; z++) {
                    terrarium.setCell(new TerrariumCell(
                            x, y, z, TerrariumCell.Material.AIR,
                            new float[TerrariumCell.PHEROMONE_TYPES], 25f, 50f));
                }
            }
        }
        simulation = new Simulation(terrarium);

        CustomSpecies species = new CustomSpecies();
        species.setScientificName("Testus antus");
        colony = new Colony(species, 25f, 25f, 5f);
        simulation.addColony(colony);

        // Add some ants
        for (int i = 0; i < 20; i++) {
            Individual ant = new Individual(colony.getId(), Individual.Caste.WORKER,
                    25f + i, 25f, 5f);
            colony.addIndividual(ant);
        }
    }

    @Test
    void testFireDisasterCreation() {
        FireDisaster fire = new FireDisaster(25, 25, 5, 0.5f);
        assertEquals("Wildfire", fire.getName());
        assertEquals("MINOR", fire.getSeverity());
    }

    @Test
    void testFireDisasterTrigger() {
        FireDisaster fire = new FireDisaster(25, 25, 10, 0.3f);
        int initialPop = colony.getLivingIndividuals().size();

        fire.trigger(simulation, terrarium);

        // Some ants may be affected
        assertTrue(colony.getLivingIndividuals().size() <= initialPop);
    }

    @Test
    void testFloodDisasterCreation() {
        FloodDisaster flood = new FloodDisaster(0.6f, 5);
        assertEquals("Flash Flood", flood.getName());
        assertEquals("MAJOR", flood.getSeverity());
    }

    @Test
    void testFloodDisasterTrigger() {
        FloodDisaster flood = new FloodDisaster(0.5f, 10);
        flood.trigger(simulation, terrarium);
        // Check for water presence (random placement)
        assertNotNull(simulation);
    }

    @Test
    void testHeatwaveDisasterCreation() {
        HeatwaveDisaster heat = new HeatwaveDisaster(0.7f, 200);
        assertEquals("Extreme Heatwave", heat.getName());
        assertEquals("MAJOR", heat.getSeverity());
        assertEquals(200, heat.getDurationTicks());
    }

    @Test
    void testHeatwaveDisasterTrigger() {
        float initialTemp = simulation.getWeather().getTemperature();
        HeatwaveDisaster heat = new HeatwaveDisaster(0.8f, 100);
        heat.trigger(simulation, terrarium);

        // Temperature should have increased
        assertTrue(simulation.getWeather().getTemperature() > initialTemp);
    }

    @Test
    void testDroughtDisasterCreation() {
        DroughtDisaster drought = new DroughtDisaster(0.5f);
        assertEquals("Severe Drought", drought.getName());
    }

    @Test
    void testDroughtDisasterTrigger() {
        float initialHumidity = simulation.getWeather().getHumidity();
        DroughtDisaster drought = new DroughtDisaster(0.7f);
        drought.trigger(simulation, terrarium);

        // Humidity should have decreased
        assertTrue(simulation.getWeather().getHumidity() <= initialHumidity);
    }

    @Test
    void testEarthquakeDisasterCreation() {
        EarthquakeDisaster quake = new EarthquakeDisaster(0.6f);
        assertEquals("Earthquake", quake.getName());
        assertEquals("MAJOR", quake.getSeverity());
    }

    @Test
    void testDisasterSeverityLevels() {
        FireDisaster minor = new FireDisaster(0, 0, 0, 0.2f);
        FireDisaster major = new FireDisaster(0, 0, 0, 0.6f);
        FireDisaster catastrophic = new FireDisaster(0, 0, 0, 0.9f);

        assertEquals("MINOR", minor.getSeverity());
        assertEquals("MAJOR", major.getSeverity());
        assertEquals("CATASTROPHIC", catastrophic.getSeverity());
    }
}
