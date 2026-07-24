/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A grid-based Spatial Hash Map implementation.
 * Better performance than Octree for uniform distributions and dynamic updates.
 * O(1) average case for insertion and lookup.
 *
 * @param <T> Type of element
 */
public class SpatialHashMap<T> implements SpatialPartition<T> {

    private final float cellSize;
    private final Map<Long, List<Entry<T>>> grid;
    private final Map<T, Entry<T>> reverseLookup;

    public SpatialHashMap(float cellSize) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
        this.reverseLookup = new HashMap<>();
    }

    private long hash(int cx, int cy, int cz) {
        return 73856093L * cx ^ 19349663L * cy ^ 83492791L * cz;
    }

    private long getKey(float x, float y, float z) {
        int cx = (int) (x / cellSize);
        int cy = (int) (y / cellSize);
        int cz = (int) (z / cellSize);
        return hash(cx, cy, cz);
    }

    @Override
    public synchronized void insert(T element, float x, float y, float z) {
        if (reverseLookup.containsKey(element)) {
            remove(element);
        }
        long key = getKey(x, y, z);
        Entry<T> entry = new Entry<>(element, x, y, z, key);
        grid.computeIfAbsent(key, k -> new ArrayList<>()).add(entry);
        reverseLookup.put(element, entry);
    }

    @Override
    public synchronized boolean remove(T element) {
        Entry<T> entry = reverseLookup.remove(element);
        if (entry != null) {
            List<Entry<T>> list = grid.get(entry.key);
            if (list != null) {
                boolean removed = list.remove(entry);
                if (list.isEmpty()) {
                    grid.remove(entry.key);
                }
                return removed;
            }
        }
        return false;
    }

    @Override
    public synchronized void update(T element, float oldX, float oldY, float oldZ, float newX, float newY, float newZ) {
        // We can optimize by checking if cell changed
        long oldKey = getKey(oldX, oldY, oldZ);
        long newKey = getKey(newX, newY, newZ);

        if (oldKey == newKey) {
            // Update coordinates in entry but keep in same bucket
            Entry<T> entry = reverseLookup.get(element);
            if (entry != null) {
                entry.x = newX;
                entry.y = newY;
                entry.z = newZ;
            }
            return;
        }

        remove(element);
        insert(element, newX, newY, newZ);
    }

    @Override
    public synchronized List<T> queryRange(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        List<T> results = new ArrayList<>();

        int startX = (int) (minX / cellSize);
        int endX = (int) (maxX / cellSize);
        int startY = (int) (minY / cellSize);
        int endY = (int) (maxY / cellSize);
        int startZ = (int) (minZ / cellSize);
        int endZ = (int) (maxZ / cellSize);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                for (int z = startZ; z <= endZ; z++) {
                    long key = hash(x, y, z);
                    List<Entry<T>> cell = grid.get(key);
                    if (cell != null) {
                        for (Entry<T> entry : cell) {
                            if (entry.x >= minX && entry.x <= maxX &&
                                    entry.y >= minY && entry.y <= maxY &&
                                    entry.z >= minZ && entry.z <= maxZ) {
                                results.add(entry.item);
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    @Override
    public synchronized List<T> queryRadius(float x, float y, float z, float radius) {
        List<T> results = new ArrayList<>();
        float rSq = radius * radius;

        // Bounding box for cells
        float minX = x - radius, maxX = x + radius;
        float minY = y - radius, maxY = y + radius;
        float minZ = z - radius, maxZ = z + radius;

        int startX = (int) (minX / cellSize);
        int endX = (int) (maxX / cellSize);
        int startY = (int) (minY / cellSize);
        int endY = (int) (maxY / cellSize);
        int startZ = (int) (minZ / cellSize);
        int endZ = (int) (maxZ / cellSize);

        for (int ix = startX; ix <= endX; ix++) {
            for (int iy = startY; iy <= endY; iy++) {
                for (int iz = startZ; iz <= endZ; iz++) {
                    long key = hash(ix, iy, iz);
                    List<Entry<T>> cell = grid.get(key);
                    if (cell != null) {
                        for (Entry<T> entry : cell) {
                            float dx = entry.x - x;
                            float dy = entry.y - y;
                            float dz = entry.z - z;
                            if (dx * dx + dy * dy + dz * dz <= rSq) {
                                results.add(entry.item);
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    @Override
    public synchronized void clear() {
        grid.clear();
        reverseLookup.clear();
    }

    @Override
    public int size() {
        return reverseLookup.size();
    }

    private static class Entry<T> {
        final T item;
        float x, y, z;
        final long key;

        Entry(T item, float x, float y, float z, long key) {
            this.item = item;
            this.x = x;
            this.y = y;
            this.z = z;
            this.key = key;
        }
    }
}
