/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for A* Pathfinder.
 */
class AStarPathfinderTest {

    private Terrarium terrarium;
    private AStarPathfinder pathfinder;

    @BeforeEach
    void setUp() {
        terrarium = new Terrarium(32, 32, 16);

        // Fill with air (passable)
        for (int x = 0; x < 32; x++) {
            for (int y = 0; y < 32; y++) {
                for (int z = 0; z < 16; z++) {
                    terrarium.setCell(TerrariumCell.air(x, y, z));
                }
            }
        }

        pathfinder = new AStarPathfinder(terrarium);
    }

    @Test
    void testSimplePath() {
        List<int[]> path = pathfinder.findPath(0, 0, 0, 5, 5, 0);

        assertFalse(path.isEmpty(), "Path should not be empty");

        // Check start and end
        assertArrayEquals(new int[] { 0, 0, 0 }, path.get(0), "Path should start at origin");
        assertArrayEquals(new int[] { 5, 5, 0 }, path.get(path.size() - 1), "Path should end at goal");
    }

    @Test
    void testPathWithObstacle() {
        // Create a wall
        for (int y = 2; y < 10; y++) {
            terrarium.setCell(TerrariumCell.earth(5, y, 0));
        }

        List<int[]> path = pathfinder.findPath(0, 5, 0, 10, 5, 0);

        assertFalse(path.isEmpty(), "Path should exist around obstacle");

        // Verify path doesn't go through obstacle
        for (int[] point : path) {
            if (point[0] == 5 && point[2] == 0) {
                assertTrue(point[1] < 2 || point[1] >= 10,
                        "Path should not go through wall at y=" + point[1]);
            }
        }
    }

    @Test
    void testNoPath() {
        // Create a complete wall blocking passage
        for (int y = 0; y < 32; y++) {
            for (int z = 0; z < 16; z++) {
                terrarium.setCell(TerrariumCell.rock(15, y, z));
            }
        }

        List<int[]> path = pathfinder.findPath(0, 0, 0, 30, 0, 0);

        assertTrue(path.isEmpty(), "No path should exist through solid wall");
    }

    @Test
    void testPathCaching() {
        // First call
        List<int[]> path1 = pathfinder.findPath(0, 0, 0, 10, 10, 0);

        // Second call (should be cached)
        List<int[]> path2 = pathfinder.findPath(0, 0, 0, 10, 10, 0);

        assertEquals(path1.size(), path2.size(), "Cached path should have same size");
    }

    @Test
    void testDifferentHeuristics() {
        pathfinder.setHeuristic(AStarPathfinder.Heuristic.MANHATTAN);
        List<int[]> manhattanPath = pathfinder.findPath(0, 0, 0, 10, 10, 0);

        pathfinder.clearCache();
        pathfinder.setHeuristic(AStarPathfinder.Heuristic.EUCLIDEAN);
        List<int[]> euclideanPath = pathfinder.findPath(0, 0, 0, 10, 10, 0);

        pathfinder.clearCache();
        pathfinder.setHeuristic(AStarPathfinder.Heuristic.CHEBYSHEV);
        List<int[]> chebyshevPath = pathfinder.findPath(0, 0, 0, 10, 10, 0);

        // All should reach the goal
        assertFalse(manhattanPath.isEmpty());
        assertFalse(euclideanPath.isEmpty());
        assertFalse(chebyshevPath.isEmpty());
    }

    @Test
    void testNoDiagonals() {
        pathfinder.setAllowDiagonals(false);
        List<int[]> path = pathfinder.findPath(0, 0, 0, 5, 5, 0);

        assertFalse(path.isEmpty());

        // Check that moves are only cardinal (not diagonal)
        for (int i = 1; i < path.size(); i++) {
            int dx = Math.abs(path.get(i)[0] - path.get(i - 1)[0]);
            int dy = Math.abs(path.get(i)[1] - path.get(i - 1)[1]);
            int dz = Math.abs(path.get(i)[2] - path.get(i - 1)[2]);

            int moves = dx + dy + dz;
            assertEquals(1, moves, "Without diagonals, only one axis should change per step");
        }
    }

    @Test
    void test3DPath() {
        // Create a path that needs to go through 3D space
        List<int[]> path = pathfinder.findPath(0, 0, 0, 10, 10, 10);

        assertFalse(path.isEmpty(), "3D path should exist");
        assertArrayEquals(new int[] { 10, 10, 10 }, path.get(path.size() - 1));
    }
}
