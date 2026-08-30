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
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import org.swarmforge.core.world.NestGenerator;
import org.swarmforge.core.world.NestGenerator.NestType;

import java.util.LinkedList;
import java.util.Queue;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Auto-Test Suite verifying Nest Generation, Structural Integrity, Chamber/Tunnel Models,
 * Spatial Boundary Safety, Subterranean Material Placements, and Flood-Fill Reachability.
 */
public class NestStructureAutoTest {

    private Terrarium terrarium;
    private NestGenerator generator;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(80, 80, 40);
        generator = new NestGenerator(terrarium, 12345L);
    }

    @Test
    @DisplayName("Verify NestGenerator executes safely for all NestType variants")
    void testNestGeneratorAllTypes() {
        int startX = 40;
        int startY = 40;
        int startZ = 20;

        for (NestType type : NestType.values()) {
            Terrarium testTerrarium = new Terrarium(80, 80, 40);
            NestGenerator gen = new NestGenerator(testTerrarium, 999L);

            int chambersCarved = gen.generate(startX, startY, startZ, type, 1.0f);

            assertTrue(chambersCarved > 0, "Nest type " + type + " should carve at least 1 chamber/cell");
            assertDoesNotThrow(() -> testTerrarium.getCell(startX, startY, startZ),
                    "Terrarium cell access should be safe after generating " + type);
        }
    }

    @Test
    @DisplayName("Verify boundary safety when generating nests near terrarium edges")
    void testTerrariumBoundarySafety() {
        // Near min bounds
        assertDoesNotThrow(() -> generator.generate(1, 1, 1, NestType.SIMPLE, 1.0f),
                "Generation near minimum bounds should not throw IndexOutOfBoundsException");

        // Near max bounds
        assertDoesNotThrow(() -> generator.generate(78, 78, 38, NestType.MATURE, 1.0f),
                "Generation near maximum bounds should not throw IndexOutOfBoundsException");
    }

    @Test
    @DisplayName("Verify Nest, Chamber, and Tunnel spatial and capacity model operations")
    void testChamberAndTunnelModel() {
        Nest nest = new Nest();
        assertTrue(nest.getChambers().isEmpty(), "New nest should have no chambers");
        assertTrue(nest.getTunnels().isEmpty(), "New nest should have no tunnels");

        Chamber queenChamber = new Chamber("qc-1", Chamber.Type.QUEEN_QUARTERS, 40, 40, 10, 50.0f);
        Chamber nursery = new Chamber("nur-1", Chamber.Type.NURSERY, 45, 40, 10, 100.0f);
        Chamber foodVault = new Chamber("food-1", Chamber.Type.FOOD_STORAGE, 40, 45, 10, 200.0f);

        nest.addChamber(queenChamber);
        nest.addChamber(nursery);
        nest.addChamber(foodVault);

        assertEquals(3, nest.getChambers().size(), "Nest should contain 3 chambers");

        Tunnel tunnel1 = new Tunnel(queenChamber, nursery);
        Tunnel tunnel2 = new Tunnel(queenChamber, foodVault);

        nest.addTunnel(tunnel1);
        nest.addTunnel(tunnel2);

        assertEquals(2, nest.getTunnels().size(), "Nest should contain 2 tunnels");
        assertEquals(5.0f, tunnel1.getLength(), 0.01f, "Tunnel length between (40,40,10) and (45,40,10) should be 5.0");

        // Test nearest chamber lookup
        Chamber nearest = nest.findNearestChamber(44, 40, 10);
        assertEquals(nursery, nearest, "Nearest chamber to (44,40,10) should be nursery");

        // Test capacity and load management
        assertEquals(0.0f, nursery.getCurrentLoad(), 0.01f);
        nursery.addLoad(30.0f);
        assertEquals(30.0f, nursery.getCurrentLoad(), 0.01f);

        nursery.addLoad(90.0f);
        assertEquals(100.0f, nursery.getCurrentLoad(), 0.01f, "Load should be capped at capacity (100.0)");

        nursery.removeLoad(40.0f);
        assertEquals(60.0f, nursery.getCurrentLoad(), 0.01f);

        // Test stability factor & contamination level
        nursery.setStabilityFactor(0.95f);
        nursery.setContaminationLevel(0.12f);
        assertEquals(0.95f, nursery.getStabilityFactor(), 0.01f);
        assertEquals(0.12f, nursery.getContaminationLevel(), 0.01f);
    }

    @Test
    @DisplayName("Verify species-specific nest materials placement in subterranean/mound structures")
    void testSubterraneanDepthAndMaterialIntegrity() {
        // Honeybee hexagonal wax comb
        generator.generate(40, 40, 20, NestType.WAX_COMB_HEXAGONAL, 1.0f);
        boolean foundBeeswax = false;
        for (int x = 20; x < 60; x++) {
            for (int y = 20; y < 60; y++) {
                for (int z = 10; z < 30; z++) {
                    if (terrarium.getCell(x, y, z).material() == TerrariumCell.Material.BEESWAX) {
                        foundBeeswax = true;
                        break;
                    }
                }
            }
        }
        assertTrue(foundBeeswax, "Wax comb nest must generate BEESWAX cells");

        // Leafcutter fungus vault
        Terrarium t2 = new Terrarium(80, 80, 60);
        NestGenerator g2 = new NestGenerator(t2, 42L);
        g2.generate(40, 40, 40, NestType.SUBTERRANEAN_FUNGI_VAULT, 1.0f);
        boolean foundFungusGarden = false;
        for (int x = 20; x < 60; x++) {
            for (int y = 20; y < 60; y++) {
                for (int z = 0; z < 40; z++) {
                    if (t2.getCell(x, y, z).material() == TerrariumCell.Material.FUNGUS_GARDEN) {
                        foundFungusGarden = true;
                        break;
                    }
                }
            }
        }
        assertTrue(foundFungusGarden, "Subterranean fungi vault nest must generate FUNGUS_GARDEN cells");

        // Termite cathedral mound
        Terrarium t3 = new Terrarium(80, 80, 60);
        NestGenerator g3 = new NestGenerator(t3, 100L);
        g3.generate(40, 40, 10, NestType.CATHEDRAL_MOUND, 1.0f);
        boolean foundStercoralCement = false;
        for (int x = 20; x < 60; x++) {
            for (int y = 20; y < 60; y++) {
                for (int z = 10; z < 50; z++) {
                    if (t3.getCell(x, y, z).material() == TerrariumCell.Material.STERCORAL_CEMENT) {
                        foundStercoralCement = true;
                        break;
                    }
                }
            }
        }
        assertTrue(foundStercoralCement, "Cathedral mound nest must generate STERCORAL_CEMENT cells");
    }

    @Test
    @DisplayName("Verify flood-fill reachability from entrance to internal chambers")
    void testNestChamberReachabilityAndConnectivity() {
        Terrarium t = new Terrarium(60, 60, 40);
        NestGenerator gen = new NestGenerator(t, 777L);
        int startX = 30, startY = 30, startZ = 20;

        gen.generate(startX, startY, startZ, NestType.SIMPLE, 1.0f);

        // Perform 3D Flood Fill starting from entrance (startX, startY, startZ)
        boolean[][][] visited = new boolean[60][60][40];
        Queue<int[]> queue = new LinkedList<>();

        queue.add(new int[]{startX, startY, startZ});
        visited[startX][startY][startZ] = true;

        int reachableCarvedCells = 0;
        int[][] directions = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            reachableCarvedCells++;

            for (int[] dir : directions) {
                int nx = curr[0] + dir[0];
                int ny = curr[1] + dir[1];
                int nz = curr[2] + dir[2];

                if (nx >= 0 && nx < 60 && ny >= 0 && ny < 60 && nz >= 0 && nz < 40) {
                    if (!visited[nx][ny][nz]) {
                        TerrariumCell.Material mat = t.getCell(nx, ny, nz).material();
                        // Air or Chamber indicates carved passable void
                        if (mat == TerrariumCell.Material.AIR || mat == TerrariumCell.Material.CHAMBER) {
                            visited[nx][ny][nz] = true;
                            queue.add(new int[]{nx, ny, nz});
                        }
                    }
                }
            }
        }

        assertTrue(reachableCarvedCells > 5, "Flood fill should reach multiple carved tunnel and chamber cells from entrance");
    }
}
