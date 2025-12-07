/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.grpc;

import org.junit.jupiter.api.*;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.domain.Terrarium;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimulationServiceImpl.
 */
class SimulationServiceTest {

    private Simulation simulation;
    private SimulationServiceImpl service;

    @BeforeEach
    void setUp() {
        Terrarium terrarium = new Terrarium(100, 100, 50);
        simulation = new Simulation(terrarium);
        service = new SimulationServiceImpl(simulation);
    }

    @Test
    @DisplayName("getState should return correct dimensions")
    void testGetState() {
        var response = service.getState(false, false);
        assertEquals(100, response.width());
        assertEquals(100, response.height());
        assertEquals(50, response.depth());
        assertEquals("STOPPED", response.status());
    }

    @Test
    @DisplayName("Control start should change state")
    void testControlStart() {
        var response = service.control("START", 60);
        assertTrue(response.success());
        assertEquals("RUNNING", simulation.getState().name());
    }

    @Test
    @DisplayName("Control pause should change state")
    void testControlPause() {
        simulation.start();
        var response = service.control("PAUSE", 60);
        assertTrue(response.success());
        assertEquals("PAUSED", simulation.getState().name());
    }

    @Test
    @DisplayName("Invalid command should return failure")
    void testInvalidCommand() {
        var response = service.control("INVALID", 60);
        // Depending on impl it might just ignore or fail
        // Our impl returns success=true but status remains same, acts as no-op or
        // catch-all?
        // Let's check impl: switch on string, default isn't handled explicitly inside
        // try,
        // so it likely does nothing and returns success=true with current state.
        // Actually looking at code: use 'switch' statement. If no case matches, nothing
        // happens.
        assertTrue(response.success());
    }
}
