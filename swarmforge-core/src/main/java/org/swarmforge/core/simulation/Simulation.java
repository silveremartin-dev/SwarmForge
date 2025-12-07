/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Core simulation engine for SwarmForge.
 * Manages the simulation loop, tick processing, and world state.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class Simulation {

    public enum State {
        STOPPED, RUNNING, PAUSED
    }

    private final Terrarium terrarium;
    private final CopyOnWriteArrayList<Colony> colonies;
    private final AtomicLong tickCount;
    private final AtomicReference<State> state;

    // Simulation settings
    private int ticksPerSecond = 60;
    private long tickDurationNanos;

    // Thread
    private Thread simulationThread;

    public Simulation(Terrarium terrarium) {
        this.terrarium = terrarium;
        this.colonies = new CopyOnWriteArrayList<>();
        this.tickCount = new AtomicLong(0);
        this.state = new AtomicReference<>(State.STOPPED);
        this.tickDurationNanos = 1_000_000_000L / ticksPerSecond;
    }

    /**
     * Add a colony to the simulation.
     */
    public void addColony(Colony colony) {
        colonies.add(colony);
    }

    /**
     * Start the simulation in a virtual thread.
     */
    public void start() {
        if (state.compareAndSet(State.STOPPED, State.RUNNING) ||
                state.compareAndSet(State.PAUSED, State.RUNNING)) {
            simulationThread = Thread.ofVirtual().name("simulation-loop").start(this::runLoop);
        }
    }

    /**
     * Pause the simulation.
     */
    public void pause() {
        state.set(State.PAUSED);
    }

    /**
     * Stop the simulation.
     */
    public void stop() {
        state.set(State.STOPPED);
        if (simulationThread != null) {
            simulationThread.interrupt();
        }
    }

    /**
     * Main simulation loop using virtual threads.
     */
    private void runLoop() {
        while (state.get() == State.RUNNING) {
            long startTime = System.nanoTime();

            tick();

            long elapsed = System.nanoTime() - startTime;
            long sleepNanos = tickDurationNanos - elapsed;
            if (sleepNanos > 0) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000, (int) (sleepNanos % 1_000_000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /**
     * Execute a single simulation tick.
     */
    public void tick() {
        tickCount.incrementAndGet();

        // Phase 1: Update environment (pheromone diffusion/evaporation)
        updateEnvironment();

        // Phase 2: Update all individuals
        for (Colony colony : colonies) {
            for (Individual individual : colony.getLivingIndividuals()) {
                individual.tick();
                // TODO: Execute behavior strategy
            }
            colony.removeDeadIndividuals();
        }

        // Phase 3: Check for events (births, deaths, discoveries)
        processEvents();
    }

    private void updateEnvironment() {
        // TODO: GPU-accelerated pheromone diffusion
    }

    private void processEvents() {
        // TODO: Event system for births, deaths, food discoveries
    }

    // Getters
    public long getTickCount() {
        return tickCount.get();
    }

    public State getState() {
        return state.get();
    }

    public Terrarium getTerrarium() {
        return terrarium;
    }

    public List<Colony> getColonies() {
        return List.copyOf(colonies);
    }

    public int getTicksPerSecond() {
        return ticksPerSecond;
    }

    // Setters
    public void setTicksPerSecond(int tps) {
        this.ticksPerSecond = tps;
        this.tickDurationNanos = 1_000_000_000L / tps;
    }
}
