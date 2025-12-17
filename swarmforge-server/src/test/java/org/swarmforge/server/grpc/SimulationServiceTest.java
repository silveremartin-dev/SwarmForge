/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.grpc;

import org.junit.jupiter.api.*;

import org.swarmforge.protocol.grpc.*;
import io.grpc.stub.StreamObserver;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimulationServiceImpl.
 */
class SimulationServiceTest {

    private SimulationServiceImpl service;

    @BeforeEach
    void setUp() {
        org.swarmforge.server.simulation.SimulationManager manager = new org.swarmforge.server.simulation.SimulationManager();
        manager.createSimulation("main", "Test World", 100, 100, 50);
        service = new SimulationServiceImpl(manager);
    }

    @Test
    @DisplayName("getState should return correct dimensions")
    void testGetState() {
        GetStateRequest request = GetStateRequest.newBuilder().setSimulationId("main").build();
        TestStreamObserver<SimulationState> observer = new TestStreamObserver<>();
        service.getState(request, observer);

        SimulationState state = observer.getValue();
        assertEquals(100, state.getWidth());
        assertEquals(100, state.getHeight());
        assertEquals(50, state.getDepth());
    }

    @Test
    @DisplayName("Control start should change state")
    void testControlStart() {
        ControlRequest request = ControlRequest.newBuilder()
                .setAction(ControlAction.CTRL_START)
                .setSimulationId("main")
                .build();
        TestStreamObserver<ControlResponse> observer = new TestStreamObserver<>();
        service.control(request, observer);

        assertTrue(observer.getValue().getSuccess());
    }

    // Simple test observer
    static class TestStreamObserver<T> implements StreamObserver<T> {
        private T value;

        @Override
        public void onNext(T v) {
            this.value = v;
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onCompleted() {
        }

        T getValue() {
            return value;
        }
    }
}
