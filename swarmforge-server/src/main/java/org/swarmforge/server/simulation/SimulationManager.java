/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.simulation;

import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.domain.Terrarium;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

/**
 * Manages multiple active simulations.
 * 
 * @author Gemini AI Assistant
 */
public class SimulationManager {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationManager.class);

    private final Map<String, Simulation> simulations = new ConcurrentHashMap<>();
    private final Map<String, SimulationInfo> simulationMetadata = new ConcurrentHashMap<>();

    public SimulationManager() {
    }

    public void createSimulation(String id, String name, int width, int height, int depth) {
        if (simulations.containsKey(id)) {
            LOG.warn("Simulation {} already exists", id);
            return;
        }

        LOG.info("Creating simulation '{}' ({})", name, id);
        Terrarium terrarium = new Terrarium(width, height, depth);
        Simulation simulation = new Simulation(terrarium);

        simulations.put(id, simulation);
        simulationMetadata.put(id, new SimulationInfo(id, name));

        // Auto-start or wait? Wait for user control usually.
    }

    public Optional<Simulation> getSimulation(String id) {
        return Optional.ofNullable(simulations.get(id));
    }

    public Optional<String> getSimulationName(String id) {
        return Optional.ofNullable(simulationMetadata.get(id)).map(SimulationInfo::name);
    }

    public void removeSimulation(String id) {
        Simulation sim = simulations.remove(id);
        if (sim != null) {
            sim.stop();
        }
        simulationMetadata.remove(id);
    }

    public Map<String, Simulation> getAllSimulations() {
        return Map.copyOf(simulations);
    }

    public void tickAll() {
        // This could be run in parallel or sequentially.
        // Simulation itself runs in its own thread/loop?
        // No, Simulation.java usually has a 'tick()' and a game loop.
        // Let's check Simulation.java. It usually has start()/stop() and runs its own
        // loop.
        // If Simulation runs its own loop, we just need to manage lifecycle.
    }

    public void stopAll() {
        simulations.values().forEach(Simulation::stop);
    }

    public record SimulationInfo(String id, String name) {
    }
}
