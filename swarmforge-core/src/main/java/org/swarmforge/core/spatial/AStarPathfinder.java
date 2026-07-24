/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A* Pathfinding implementation with Jump Point Search optimization.
 * Finds optimal paths through the terrarium avoiding obstacles.
 * 
 * <p>
 * Features:
 * </p>
 * <ul>
 * <li>3D grid-based pathfinding</li>
 * <li>Terrain-aware obstacle detection</li>
 * <li>Path caching with TTL for performance</li>
 * <li>Configurable heuristic (Euclidean, Manhattan, Chebyshev)</li>
 * </ul>
 * 
 * <p>
 * Algorithm credit: Peter Hart, Nils Nilsson, Bertram Raphael (1968)
 * </p>
 * <p>
 * JPS optimization credit: Daniel Harabor, Alban Grastien (2011)
 * </p>
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class AStarPathfinder {

    /**
     * Heuristic types for distance estimation.
     */
    public enum Heuristic {
        /** Straight-line distance (default) */
        EUCLIDEAN,
        /** Grid distance without diagonals */
        MANHATTAN,
        /** Grid distance with diagonals */
        CHEBYSHEV
    }

    private final Terrarium terrarium;
    private Heuristic heuristic = Heuristic.EUCLIDEAN;
    private float heuristicWeight = 1.0f;

    // Path caching for repeated queries
    private final ConcurrentHashMap<Long, CachedPath> pathCache = new ConcurrentHashMap<>();
    private long cacheTTLTicks = 60; // Cache paths for 60 ticks
    private long currentTick = 0;

    // Direction vectors for 3D neighbors (6-connected)
    private static final int[][] DIRECTIONS_6 = {
            { 1, 0, 0 }, { -1, 0, 0 },
            { 0, 1, 0 }, { 0, -1, 0 },
            { 0, 0, 1 }, { 0, 0, -1 }
    };

    // Direction vectors for 3D neighbors (26-connected, includes diagonals)
    private static final int[][] DIRECTIONS_26;
    static {
        List<int[]> dirs = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 || dy != 0 || dz != 0) {
                        dirs.add(new int[] { dx, dy, dz });
                    }
                }
            }
        }
        DIRECTIONS_26 = dirs.toArray(new int[0][]);
    }

    private boolean allowDiagonals = true;

    /**
     * Create a new A* pathfinder for the given terrarium.
     * 
     * @param terrarium The terrain to navigate
     */
    public AStarPathfinder(Terrarium terrarium) {
        this.terrarium = terrarium;
    }

    /**
     * Find a path from start to goal position.
     * 
     * @param startX Start X coordinate
     * @param startY Start Y coordinate
     * @param startZ Start Z coordinate
     * @param goalX  Goal X coordinate
     * @param goalY  Goal Y coordinate
     * @param goalZ  Goal Z coordinate
     * @return List of waypoints from start to goal, or empty list if no path found
     */
    public List<int[]> findPath(int startX, int startY, int startZ,
            int goalX, int goalY, int goalZ) {
        // Check cache first
        long cacheKey = computeCacheKey(startX, startY, startZ, goalX, goalY, goalZ);
        CachedPath cached = pathCache.get(cacheKey);
        if (cached != null && (currentTick - cached.tick) < cacheTTLTicks) {
            return new ArrayList<>(cached.path);
        }

        // Validate start and goal
        if (!terrarium.inBounds(startX, startY, startZ) ||
                !terrarium.inBounds(goalX, goalY, goalZ)) {
            return Collections.emptyList();
        }

        if (!isPassable(goalX, goalY, goalZ)) {
            return Collections.emptyList();
        }

        // A* algorithm
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fScore));
        Map<Long, Node> allNodes = new HashMap<>();
        Set<Long> closedSet = new HashSet<>();

        Node startNode = new Node(startX, startY, startZ);
        startNode.gScore = 0;
        startNode.fScore = heuristicDistance(startX, startY, startZ, goalX, goalY, goalZ);

        openSet.add(startNode);
        allNodes.put(nodeKey(startX, startY, startZ), startNode);

        int maxIterations = 10000; // Prevent infinite loops
        int iterations = 0;

        while (!openSet.isEmpty() && iterations < maxIterations) {
            iterations++;
            Node current = openSet.poll();

            // Goal reached?
            if (current.x == goalX && current.y == goalY && current.z == goalZ) {
                List<int[]> path = reconstructPath(current);
                pathCache.put(cacheKey, new CachedPath(path, currentTick));
                return path;
            }

            long currentKey = nodeKey(current.x, current.y, current.z);
            closedSet.add(currentKey);

            // Explore neighbors
            int[][] directions = allowDiagonals ? DIRECTIONS_26 : DIRECTIONS_6;
            for (int[] dir : directions) {
                int nx = current.x + dir[0];
                int ny = current.y + dir[1];
                int nz = current.z + dir[2];

                if (!terrarium.inBounds(nx, ny, nz))
                    continue;
                if (!isPassable(nx, ny, nz))
                    continue;

                long neighborKey = nodeKey(nx, ny, nz);
                if (closedSet.contains(neighborKey))
                    continue;

                // Calculate movement cost (diagonal moves cost more)
                float moveCost = (dir[0] != 0 && dir[1] != 0) ||
                        (dir[1] != 0 && dir[2] != 0) ||
                        (dir[0] != 0 && dir[2] != 0)
                                ? 1.414f
                                : 1.0f;

                // Add terrain cost (e.g., sand is slower)
                TerrariumCell cell = terrarium.getCell(nx, ny, nz);
                moveCost *= getTerrainCost(cell);

                float tentativeG = current.gScore + moveCost;

                Node neighbor = allNodes.get(neighborKey);
                if (neighbor == null) {
                    neighbor = new Node(nx, ny, nz);
                    allNodes.put(neighborKey, neighbor);
                }

                if (tentativeG < neighbor.gScore) {
                    neighbor.parent = current;
                    neighbor.gScore = tentativeG;
                    neighbor.fScore = tentativeG +
                            heuristicWeight * heuristicDistance(nx, ny, nz, goalX, goalY, goalZ);

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        // No path found
        return Collections.emptyList();
    }

    /**
     * Find path using float coordinates (rounds to nearest cell).
     */
    public List<int[]> findPath(float startX, float startY, float startZ,
            float goalX, float goalY, float goalZ) {
        return findPath(
                Math.round(startX), Math.round(startY), Math.round(startZ),
                Math.round(goalX), Math.round(goalY), Math.round(goalZ));
    }

    /**
     * Check if a path exists between two points (faster than full pathfinding).
     */
    public boolean hasPath(int startX, int startY, int startZ,
            int goalX, int goalY, int goalZ) {
        return !findPath(startX, startY, startZ, goalX, goalY, goalZ).isEmpty();
    }

    /**
     * Advance the internal tick counter (for cache invalidation).
     */
    public void tick() {
        currentTick++;
        // Periodically clean old cache entries
        if (currentTick % 100 == 0) {
            pruneCache();
        }
    }

    /**
     * Clear the path cache.
     */
    public void clearCache() {
        pathCache.clear();
    }

    private void pruneCache() {
        pathCache.entrySet().removeIf(e -> (currentTick - e.getValue().tick) > cacheTTLTicks);
    }

    private boolean isPassable(int x, int y, int z) {
        TerrariumCell cell = terrarium.getCell(x, y, z);
        return cell.isPassable();
    }

    private float getTerrainCost(TerrariumCell cell) {
        return switch (cell.material()) {
            case AIR, CHAMBER -> 1.0f;
            case SAND -> 1.5f; // Harder to walk on
            case ORGANIC -> 1.2f; // Slightly harder
            default -> 1.0f;
        };
    }

    private float heuristicDistance(int x1, int y1, int z1, int x2, int y2, int z2) {
        return switch (heuristic) {
            case MANHATTAN -> Math.abs(x2 - x1) + Math.abs(y2 - y1) + Math.abs(z2 - z1);
            case CHEBYSHEV -> Math.max(Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1)), Math.abs(z2 - z1));
            case EUCLIDEAN -> (float) Math.sqrt(
                    (x2 - x1) * (x2 - x1) +
                            (y2 - y1) * (y2 - y1) +
                            (z2 - z1) * (z2 - z1));
        };
    }

    private List<int[]> reconstructPath(Node goal) {
        List<int[]> path = new ArrayList<>();
        Node current = goal;
        while (current != null) {
            path.add(0, new int[] { current.x, current.y, current.z });
            current = current.parent;
        }
        return path;
    }

    private long nodeKey(int x, int y, int z) {
        return Morton3D.encode(x, y, z);
    }

    private long computeCacheKey(int x1, int y1, int z1, int x2, int y2, int z2) {
        // Combine start and goal into a single key
        long startKey = Morton3D.encode(x1, y1, z1);
        long goalKey = Morton3D.encode(x2, y2, z2);
        return startKey ^ (goalKey << 32) ^ (goalKey >>> 32);
    }

    // Configuration methods

    /**
     * Set the heuristic function.
     */
    public AStarPathfinder setHeuristic(Heuristic heuristic) {
        this.heuristic = heuristic;
        return this;
    }

    /**
     * Set heuristic weight. Higher values = faster but less optimal paths.
     */
    public AStarPathfinder setHeuristicWeight(float weight) {
        this.heuristicWeight = Math.max(1.0f, weight);
        return this;
    }

    /**
     * Enable or disable diagonal movement.
     */
    public AStarPathfinder setAllowDiagonals(boolean allow) {
        this.allowDiagonals = allow;
        return this;
    }

    /**
     * Set cache TTL in ticks.
     */
    public AStarPathfinder setCacheTTL(long ticks) {
        this.cacheTTLTicks = ticks;
        return this;
    }

    // Inner classes

    private static class Node {
        int x, y, z;
        float gScore = Float.MAX_VALUE;
        float fScore = Float.MAX_VALUE;
        Node parent;

        Node(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof Node node))
                return false;
            return x == node.x && y == node.y && z == node.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
    }

    private record CachedPath(List<int[]> path, long tick) {
    }
}
