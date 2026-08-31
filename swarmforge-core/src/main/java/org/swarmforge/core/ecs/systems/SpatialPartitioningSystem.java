package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.PositionComponent;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-performance 3D Spatial Partitioning System for ECS entities.
 * Indexes entity IDs into uniform spatial grid cells, providing O(1) neighbor lookups
 * for trophallaxis, aggression, and density-dependent behavior without O(N^2) bottlenecks.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SpatialPartitioningSystem extends IteratingSystem {

    private ComponentMapper<PositionComponent> mPosition;

    private static final float CELL_SIZE = 1.0f; // 1 meter grid cells
    private final Map<Long, List<Integer>> grid = new ConcurrentHashMap<>();

    public SpatialPartitioningSystem() {
        super(Aspect.all(PositionComponent.class));
    }

    @Override
    protected void begin() {
        grid.clear();
    }

    @Override
    protected void process(int entityId) {
        PositionComponent pos = mPosition.get(entityId);
        long cellKey = getCellKey(pos.x, pos.y, pos.z);
        grid.computeIfAbsent(cellKey, k -> new ArrayList<>(8)).add(entityId);
    }

    /**
     * Retrieves nearby entity IDs within the spatial cell of the target position and adjacent cells.
     */
    public List<Integer> getNearbyEntities(float x, float y, float z) {
        List<Integer> neighbors = new ArrayList<>();
        int cx = (int) Math.floor(x / CELL_SIZE);
        int cy = (int) Math.floor(y / CELL_SIZE);
        int cz = (int) Math.floor(z / CELL_SIZE);

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    long key = packKey(cx + dx, cy + dy, cz + dz);
                    List<Integer> cellList = grid.get(key);
                    if (cellList != null) {
                        neighbors.addAll(cellList);
                    }
                }
            }
        }
        return neighbors;
    }

    private static long getCellKey(float x, float y, float z) {
        int cx = (int) Math.floor(x / CELL_SIZE);
        int cy = (int) Math.floor(y / CELL_SIZE);
        int cz = (int) Math.floor(z / CELL_SIZE);
        return packKey(cx, cy, cz);
    }

    private static long packKey(int x, int y, int z) {
        return (((long) (x & 0x1FFFFF)) << 42) | (((long) (y & 0x1FFFFF)) << 21) | ((long) (z & 0x1FFFFF));
    }
}
