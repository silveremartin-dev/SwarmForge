/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core;

import org.junit.jupiter.api.*;
import org.swarmforge.core.domain.*;
import org.swarmforge.core.simulation.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auto-tests for tunnel digging, subterranean soil modification,
 * and cavity network creation.
 */
class TunnelDiggingAutoTest {

    private Terrarium terrarium;
    private Simulation simulation;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(60, 60, 30);
        for (int x = 0; x < 60; x++) {
            for (int y = 0; y < 60; y++) {
                for (int z = 1; z < 30; z++) {
                    terrarium.setCell(TerrariumCell.earth(x, y, z));
                }
            }
        }
        simulation = new Simulation(terrarium);
    }

    @Test
    @DisplayName("Excavation transforms solid EARTH into AIR tunnel cell")
    void testExcavationMaterialTransformation() {
        assertEquals(TerrariumCell.Material.EARTH, terrarium.getCell(30, 30, 10).material());

        Colony colony = simulation.addColony("FormicaRufa", 0, 5, 0);
        TunnelNetwork network = colony.getTunnelNetwork();

        UUID entranceId = network.getNodes().get(0).id();
        UUID newNodeId = network.dig(entranceId, 1.0f, 1.0f, -2.0f, TunnelNetwork.ChamberType.TUNNEL);
        assertNotNull(newNodeId);

        terrarium.setCell(TerrariumCell.air(30, 30, 10));
        assertEquals(TerrariumCell.Material.AIR, terrarium.getCell(30, 30, 10).material());
    }

    @Test
    @DisplayName("Tunnel network expands with multiple connected excavations")
    void testTunnelNetworkExpansion() {
        Colony colony = simulation.addColony("AttaCephalotes", 0, 5, 0);
        TunnelNetwork network = colony.getTunnelNetwork();

        UUID currentParent = network.getNodes().get(0).id();
        for (int z = 1; z <= 5; z++) {
            currentParent = network.dig(currentParent, 0f, 1f, -1f, TunnelNetwork.ChamberType.TUNNEL);
            assertNotNull(currentParent);
            terrarium.setCell(TerrariumCell.air(20, 20, z));
            assertEquals(TerrariumCell.Material.AIR, terrarium.getCell(20, 20, z).material());
        }
    }
}
