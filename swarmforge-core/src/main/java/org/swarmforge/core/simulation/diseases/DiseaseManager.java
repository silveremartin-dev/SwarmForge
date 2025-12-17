/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.diseases;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.event.SimulationEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Disease management system for tracking and processing infections.
 * Handles infection tracking, transmission, recovery, and statistics.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class DiseaseManager {

    private final Simulation simulation;
    private final Random random = new Random();

    // Track infections: Individual ID -> (Disease, ticks infected)
    private final Map<String, InfectionRecord> infections = new ConcurrentHashMap<>();

    // Available diseases
    private final List<Disease> diseases = new ArrayList<>();

    // Statistics
    private int totalInfections = 0;
    private int totalRecoveries = 0;
    private int totalDeaths = 0;

    /**
     * Infection record.
     */
    public record InfectionRecord(Disease disease, int ticksInfected, long startTick) {
    }

    public DiseaseManager(Simulation simulation) {
        this.simulation = simulation;

        // Register default diseases
        diseases.add(new FungalInfection());
    }

    /**
     * Process all infections for one tick.
     */
    public void tick() {
        List<String> cured = new ArrayList<>();
        List<String> died = new ArrayList<>();

        for (var entry : infections.entrySet()) {
            String individualId = entry.getKey();
            InfectionRecord record = entry.getValue();

            // Find the individual
            Individual individual = findIndividual(individualId);

            if (individual == null || !individual.isAlive()) {
                died.add(individualId);
                continue;
            }

            // Process disease effects
            boolean recovered = record.disease.processTick(
                    individual, simulation, record.ticksInfected);

            if (recovered) {
                cured.add(individualId);
            } else if (!individual.isAlive()) {
                died.add(individualId);
            } else {
                // Update tick count
                infections.put(individualId, new InfectionRecord(
                        record.disease, record.ticksInfected + 1, record.startTick));
            }
        }

        // Remove cured/dead
        for (String id : cured) {
            infections.remove(id);
            totalRecoveries++;
        }
        for (String id : died) {
            infections.remove(id);
            totalDeaths++;
        }

        // Check for new transmissions
        checkTransmissions();
    }

    /**
     * Check for disease transmission between individuals.
     */
    private void checkTransmissions() {
        float transmissionRadius = 5.0f;

        for (var entry : new ArrayList<>(infections.entrySet())) {
            String sourceId = entry.getKey();
            InfectionRecord record = entry.getValue();

            // Only transmit after incubation
            if (record.ticksInfected < record.disease.getIncubationPeriod()) {
                continue;
            }

            Individual source = findIndividual(sourceId);
            if (source == null || !source.isAlive())
                continue;

            // Find nearby individuals
            var nearby = simulation.getSpatialIndex().queryRadius(
                    source.getX(), source.getY(), source.getZ(), transmissionRadius);

            for (Individual target : nearby) {
                if (target == source)
                    continue;
                if (!target.isAlive())
                    continue;
                if (isInfected(target.getId().toString()))
                    continue;
                if (!record.disease.canInfect(target.getCaste()))
                    continue;

                float dist = distance(source, target);

                if (record.disease.attemptInfection(source, target, dist)) {
                    infect(target, record.disease);
                }
            }
        }
    }

    /**
     * Infect an individual with a disease.
     */
    public void infect(Individual individual, Disease disease) {
        if (individual == null || !individual.isAlive())
            return;
        if (isInfected(individual.getId().toString()))
            return;

        infections.put(individual.getId().toString(), new InfectionRecord(
                disease, 0, simulation.getTickCount()));
        totalInfections++;

        simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.INFO,
                simulation.getTickCount(),
                individual.getCaste() + " infected with " + disease.getName()));
    }

    /**
     * Start an outbreak of a disease.
     * 
     * @param disease           Disease to spread
     * @param initialInfections Number of initial infected
     */
    public void startOutbreak(Disease disease, int initialInfections) {
        List<Individual> allIndividuals = getAllIndividuals();

        if (allIndividuals.isEmpty())
            return;

        int infected = 0;
        int attempts = 0;

        while (infected < initialInfections && attempts < initialInfections * 10) {
            Individual target = allIndividuals.get(random.nextInt(allIndividuals.size()));

            if (target.isAlive() && !isInfected(target.getId().toString()) &&
                    disease.canInfect(target.getCaste())) {
                infect(target, disease);
                infected++;
            }
            attempts++;
        }

        simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.INFO,
                simulation.getTickCount(),
                "Disease outbreak: " + disease.getName() + " (" + infected + " initial cases)"));
    }

    /**
     * Cure an individual.
     */
    public boolean cure(Individual individual) {
        if (infections.remove(individual.getId().toString()) != null) {
            totalRecoveries++;
            simulation.queueEvent(new SimulationEvent(SimulationEvent.EventType.INFO,
                    simulation.getTickCount(),
                    individual.getCaste() + " was cured"));
            return true;
        }
        return false;
    }

    /**
     * Check if an individual is infected.
     */
    public boolean isInfected(String individualId) {
        return infections.containsKey(individualId);
    }

    /**
     * Get the infection record for an individual.
     */
    public InfectionRecord getInfection(String individualId) {
        return infections.get(individualId);
    }

    /**
     * Get current number of active infections.
     */
    public int getActiveInfections() {
        return infections.size();
    }

    /**
     * Get breakdown of infections by disease.
     */
    public Map<String, Integer> getInfectionBreakdown() {
        Map<String, Integer> breakdown = new ConcurrentHashMap<>();
        for (InfectionRecord record : infections.values()) {
            breakdown.merge(record.disease.getName(), 1, (a, b) -> a + b);
        }
        return breakdown;
    }

    // Statistics
    public int getTotalInfections() {
        return totalInfections;
    }

    public int getTotalRecoveries() {
        return totalRecoveries;
    }

    public int getTotalDeaths() {
        return totalDeaths;
    }

    /**
     * Register a new disease type.
     */
    public void registerDisease(Disease disease) {
        diseases.add(disease);
    }

    /**
     * Get all registered diseases.
     */
    public List<Disease> getDiseases() {
        return new ArrayList<>(diseases);
    }

    // Helper methods

    private Individual findIndividual(String id) {
        for (Colony colony : simulation.getColonies()) {
            for (Individual ind : colony.getLivingIndividuals()) {
                if (ind.getId().toString().equals(id)) {
                    return ind;
                }
            }
        }
        return null;
    }

    private List<Individual> getAllIndividuals() {
        List<Individual> all = new ArrayList<>();
        for (Colony colony : simulation.getColonies()) {
            all.addAll(colony.getLivingIndividuals());
        }
        return all;
    }

    private float distance(Individual a, Individual b) {
        float dx = a.getX() - b.getX();
        float dy = a.getY() - b.getY();
        float dz = a.getZ() - b.getZ();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
