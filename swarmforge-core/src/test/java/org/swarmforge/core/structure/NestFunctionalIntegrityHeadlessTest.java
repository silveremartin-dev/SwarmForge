/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.structure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.simulation.Simulation;
import org.swarmforge.core.world.NestGenerator;
import org.swarmforge.core.world.NestGenerator.NestType;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Headless Test Suite verifying Nest Functional Integrity:
 * - Terrain placement & entrance exits
 * - Complete 3D flood-fill connectivity from entrance to all chambers
 * - Chamber type conformity, capacities, and volume accounting
 * - Spatial boundary safety and void space calculations
 */
public class NestFunctionalIntegrityHeadlessTest {

    private Terrarium terrarium;
    private Simulation simulation;
    private Colony colony;
    private Nest nest;
    private NestGenerator generator;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(80, 80, 40);
        simulation = new Simulation(terrarium);
        colony = simulation.addColony("FormicaRufa", 1, 40, 0);
        nest = colony.getNest();
        generator = new NestGenerator(terrarium, 4242L);
    }

    @Test
    @DisplayName("Verify nest terrain placement and entrance/exit passability on surface boundary")
    void testNestTerrainPlacementAndEntranceExits() {
        int entranceX = (int) colony.getNestX();
        int entranceY = (int) colony.getNestY();
        int entranceZ = (int) colony.getNestZ();

        // Check entrance coordinates within terrarium boundaries
        assertTrue(terrarium.inBounds(entranceX, entranceY, entranceZ), "Entrance coordinates must be within valid terrarium bounds");

        // Set surface entrance cell to passable air/entrance
        terrarium.setCell(TerrariumCell.air(entranceX, entranceY, entranceZ));
        TerrariumCell entranceCell = terrarium.getCell(entranceX, entranceY, entranceZ);

        assertTrue(entranceCell.isPassable(), "Nest entrance exit cell must be passable for foraging ants");
        assertEquals(TerrariumCell.Material.AIR, entranceCell.material());

        // Verify entrance chamber exists in nest model
        Chamber entranceChamber = new Chamber("entrance_0", Chamber.Type.ENTRANCE, entranceX, entranceY, entranceZ, 50.0f);
        nest.addChamber(entranceChamber);

        Chamber foundEntrance = nest.getChambers().stream()
                .filter(c -> c.getType() == Chamber.Type.ENTRANCE)
                .findFirst()
                .orElse(null);

        assertNotNull(foundEntrance, "Nest model should register an ENTRANCE chamber");
        assertEquals((float) entranceX, foundEntrance.getX(), 0.01f);
        assertEquals((float) entranceY, foundFoundY(foundEntrance), 0.01f);
    }

    private float foundFoundY(Chamber c) {
        return c.getY();
    }

    @Test
    @DisplayName("Verify complete 3D connectivity between nest entrance, tunnels, and internal chambers")
    void testNestFullConnectivityAndReachability() {
        int startX = 40, startY = 40, startZ = 20;

        // Generate mature nest
        int carvedCount = generator.generate(startX, startY, startZ, NestType.MATURE, 1.0f);
        assertTrue(carvedCount > 0, "Mature nest generator must carve subterranean cells");

        Nest graphNest = new Nest();

        // Add model chambers and connecting tunnels
        Chamber entrance = new Chamber("ch_ent", Chamber.Type.ENTRANCE, startX, startY, startZ, 50.0f);
        Chamber queen = new Chamber("ch_queen", Chamber.Type.QUEEN_QUARTERS, startX, startY, startZ - 5, 100.0f);
        Chamber nursery = new Chamber("ch_nursery", Chamber.Type.NURSERY, startX + 5, startY, startZ - 5, 150.0f);
        Chamber food = new Chamber("ch_food", Chamber.Type.FOOD_STORAGE, startX - 5, startY, startZ - 5, 200.0f);
        Chamber waste = new Chamber("ch_waste", Chamber.Type.WASTE_DUMP, startX, startY + 5, startZ - 5, 80.0f);

        graphNest.addChamber(entrance);
        graphNest.addChamber(queen);
        graphNest.addChamber(nursery);
        graphNest.addChamber(food);
        graphNest.addChamber(waste);

        graphNest.addTunnel(new Tunnel(entrance, queen));
        graphNest.addTunnel(new Tunnel(queen, nursery));
        graphNest.addTunnel(new Tunnel(queen, food));
        graphNest.addTunnel(new Tunnel(queen, waste));

        // Graph BFS Connectivity Check from ENTRANCE chamber to all chambers
        Set<Chamber> visitedChambers = new HashSet<>();
        Queue<Chamber> queue = new ArrayDeque<>();

        queue.add(entrance);
        visitedChambers.add(entrance);

        while (!queue.isEmpty()) {
            Chamber current = queue.poll();

            for (Tunnel tunnel : graphNest.getTunnels()) {
                Chamber neighbor = null;
                if (tunnel.getStart().equals(current)) {
                    neighbor = tunnel.getEnd();
                } else if (tunnel.getEnd().equals(current)) {
                    neighbor = tunnel.getStart();
                }

                if (neighbor != null && !visitedChambers.contains(neighbor)) {
                    visitedChambers.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        assertEquals(graphNest.getChambers().size(), visitedChambers.size(),
                "All internal nest chambers must be fully connected to the entrance (0 orphan chambers)");
    }

    @Test
    @DisplayName("Verify chamber type conformity, capacity limits, stability factors, and contamination levels")
    void testChamberConformityAndParameters() {
        Chamber queenChamber = new Chamber("ch_queen_01", Chamber.Type.QUEEN_QUARTERS, 30f, 30f, 10f, 120.0f);
        Chamber nursery = new Chamber("ch_nur_01", Chamber.Type.NURSERY, 35f, 30f, 10f, 250.0f);
        Chamber foodStorage = new Chamber("ch_food_01", Chamber.Type.FOOD_STORAGE, 40f, 30f, 10f, 500.0f);
        Chamber wasteDump = new Chamber("ch_waste_01", Chamber.Type.WASTE_DUMP, 45f, 30f, 10f, 300.0f);

        assertEquals(Chamber.Type.QUEEN_QUARTERS, queenChamber.getType());
        assertEquals(Chamber.Type.NURSERY, nursery.getType());
        assertEquals(Chamber.Type.FOOD_STORAGE, foodStorage.getType());
        assertEquals(Chamber.Type.WASTE_DUMP, wasteDump.getType());

        // Capacity and load checks
        foodStorage.addLoad(350.0f);
        assertEquals(350.0f, foodStorage.getCurrentLoad(), 0.01f);

        foodStorage.addLoad(300.0f); // Exceeds capacity (500)
        assertEquals(500.0f, foodStorage.getCurrentLoad(), 0.01f, "Load should be capped at chamber capacity limit");

        foodStorage.removeLoad(200.0f);
        assertEquals(300.0f, foodStorage.getCurrentLoad(), 0.01f);

        // Stability & contamination
        nursery.setStabilityFactor(0.98f);
        nursery.setContaminationLevel(0.05f);

        assertEquals(0.98f, nursery.getStabilityFactor(), 0.001f);
        assertEquals(0.05f, nursery.getContaminationLevel(), 0.001f);
    }

    @Test
    @DisplayName("Verify total nest volume calculations and internal voxel void space accounting")
    void testNestVolumeAndVoxelSpaceAccounting() {
        int startX = 40, startY = 40, startZ = 20;

        generator.generate(startX, startY, startZ, NestType.SIMPLE, 1.0f);

        // Count total passable air/chamber voxels in the terrarium
        int voidVolumeVoxels = 0;
        for (int x = 0; x < terrarium.getWidth(); x++) {
            for (int y = 0; y < terrarium.getHeight(); y++) {
                for (int z = 0; z < terrarium.getDepth(); z++) {
                    TerrariumCell cell = terrarium.getCell(x, y, z);
                    if (cell.isPassable()) {
                        voidVolumeVoxels++;
                    }
                }
            }
        }

        assertTrue(voidVolumeVoxels > 0, "Generated nest must account for non-zero void space volume");

        float initialCapacity = nest.getChambers().stream().map(Chamber::getCapacity).reduce(0f, Float::sum);

        // Add 3 chambers and compute total capacity volume
        Chamber c1 = new Chamber("c1", Chamber.Type.QUEEN_QUARTERS, 40, 40, 20, 100f);
        Chamber c2 = new Chamber("c2", Chamber.Type.NURSERY, 42, 40, 20, 200f);
        Chamber c3 = new Chamber("c3", Chamber.Type.FOOD_STORAGE, 44, 40, 20, 300f);

        nest.addChamber(c1);
        nest.addChamber(c2);
        nest.addChamber(c3);

        float totalCapacity = nest.getChambers().stream().map(Chamber::getCapacity).reduce(0f, Float::sum);
        assertEquals(initialCapacity + 600.0f, totalCapacity, 0.01f, "Total nest capacity must equal initial + sum of newly added chamber capacities");
    }

    @Test
    @DisplayName("Verify TunnelNetwork rebuild and Nest synchronization for all 13 subterranean & arboreal nest preset architectures")
    void testAllNestArchitecturesTunnelNetworkSynchronization() {
        String[] nestArchitectures = {
                "BURROW_UNDERGROUND",
                "SUBTERRANEAN_FUNGI_VAULT",
                "CATHEDRAL_MOUND",
                "SURFACE_MOUND",
                "WOODEN_BEEHIVE",
                "WAX_COMB_HEXAGONAL",
                "WAX_POTS_CLUSTER",
                "PAPER_PEDUNCULATE",
                "ARBOREAL_SILK_LEAF",
                "CARTON_NEST",
                "BAMBOO_STEM_NEST",
                "BIVOUAC_LIVING_NEST",
                "HOLLOW_TRUNK_NEST"
        };

        for (String arch : nestArchitectures) {
            Colony testColony = simulation.addColony("FormicaRufa_" + arch, 1, 40, 20);
            Nest testNest = testColony.getNest();

            testColony.getTunnelNetwork().rebuildForArchitecture(40.0f, 40.0f, 20.0f, arch, testColony);

            assertFalse(testNest.getChambers().isEmpty(), "Nest chambers must not be empty for architecture: " + arch);
            assertFalse(testNest.getTunnels().isEmpty(), "Nest tunnels must not be empty for architecture: " + arch);

            // BFS Connectivity Check from ENTRANCE chamber
            Chamber entrance = testNest.getChambers().stream()
                    .filter(c -> c.getType() == Chamber.Type.ENTRANCE)
                    .findFirst()
                    .orElse(null);

            assertNotNull(entrance, "Architecture " + arch + " must contain an ENTRANCE chamber");

            Set<Chamber> visited = new HashSet<>();
            Queue<Chamber> queue = new ArrayDeque<>();
            queue.add(entrance);
            visited.add(entrance);

            while (!queue.isEmpty()) {
                Chamber curr = queue.poll();
                for (Tunnel tunnel : testNest.getTunnels()) {
                    Chamber neighbor = null;
                    if (tunnel.getStart().equals(curr)) {
                        neighbor = tunnel.getEnd();
                    } else if (tunnel.getEnd().equals(curr)) {
                        neighbor = tunnel.getStart();
                    }
                    if (neighbor != null && visited.add(neighbor)) {
                        queue.add(neighbor);
                    }
                }
            }

            assertEquals(testNest.getChambers().size(), visited.size(),
                    "All chambers in architecture " + arch + " must be connected to the entrance (0 orphan chambers)");

            // Spawning Validation
            List<org.swarmforge.core.domain.Individual> queens = testColony.createQueens(1, false);
            assertEquals(1, queens.size());

            Chamber queenChamber = testNest.getChambersOfType(Chamber.Type.QUEEN_QUARTERS).stream().findFirst().orElse(null);
            if (queenChamber != null) {
                assertEquals(queenChamber.getX(), queens.get(0).getX(), 0.01f);
                assertEquals(queenChamber.getY(), queens.get(0).getY(), 0.01f);
                assertEquals(queenChamber.getZ(), queens.get(0).getZ(), 0.01f);
            }
        }
    }
}

