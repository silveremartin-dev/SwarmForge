/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import java.util.ArrayList;
import java.util.List;

/**
 * A sparse, point-region Octree implementation.
 * Thread-safe for read/write operations if synchronized externally or used with
 * appropriate locking.
 * Current implementation uses simple synchronized methods for basic thread
 * safety.
 *
 * @param <T> Type of element
 */
public class Octree<T> implements SpatialPartition<T> {

    private final float minX, minY, minZ;
    private final float size;
    private final Node<T> root;
    private final int maxDepth;
    private final int capacity; // Capacity per node before splitting

    public Octree(float minX, float minY, float minZ, float size, int capacity, int maxDepth) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.size = size;
        this.capacity = capacity;
        this.maxDepth = maxDepth;
        this.root = new Node<>(minX, minY, minZ, size, 0);
    }

    public float getMinX() {
        return minX;
    }

    public float getMinY() {
        return minY;
    }

    public float getMinZ() {
        return minZ;
    }

    public float getSize() {
        return size;
    }

    @Override
    public synchronized void insert(T element, float x, float y, float z) {
        root.insert(Entry.obtain(element, x, y, z), capacity, maxDepth);
    }

    @Override
    public synchronized boolean remove(T element) {
        return root.remove(element);
    }

    @Override
    public synchronized void update(T element, float oldX, float oldY, float oldZ, float newX, float newY, float newZ) {
        remove(element);
        insert(element, newX, newY, newZ);
    }

    @Override
    public synchronized List<T> queryRange(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        List<T> results = new ArrayList<>();
        root.queryRange(minX, minY, minZ, maxX, maxY, maxZ, results);
        return results;
    }

    @Override
    public synchronized List<T> queryRadius(float x, float y, float z, float radius) {
        float r = radius;
        return queryRange(x - r, y - r, z - r, x + r, y + r, z + r);
    }

    @Override
    public synchronized void clear() {
        root.clear();
    }

    @Override
    public int size() {
        return root.count;
    }

    // --- Inner Classes ---

    private static class Entry<T> {
        T element;
        float x, y, z;

        @SuppressWarnings("rawtypes")
        private static final org.swarmforge.core.util.ObjectPool<Entry> POOL = new org.swarmforge.core.util.ObjectPool<>(
                Entry::new, 1000, 100000);

        private Entry() {
        }

        @SuppressWarnings("unchecked")
        static <T> Entry<T> obtain(T element, float x, float y, float z) {
            Entry<T> e = POOL.borrow();
            e.element = element;
            e.x = x;
            e.y = y;
            e.z = z;
            return e;
        }

        void recycle() {
            this.element = null;
            POOL.recycle(this);
        }
    }

    private static class Node<T> {
        float x, y, z, size;
        int depth;
        List<Entry<T>> entries;
        Node<T>[] children; // 0..7
        int count; // Subtree count

        Node(float x, float y, float z, float size, int depth) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.size = size;
            this.depth = depth;
            this.entries = new ArrayList<>();
        }

        void insert(Entry<T> entry, int capacity, int maxDepth) {
            if (children != null) {
                // Interior node
                int index = getOctant(entry.x, entry.y, entry.z);
                if (children[index] == null) {
                    float half = size / 2;
                    float nx = x + (index & 1) * half;
                    float ny = y + ((index & 2) >> 1) * half;
                    float nz = z + ((index & 4) >> 2) * half;
                    children[index] = new Node<>(nx, ny, nz, half, depth + 1);
                }
                children[index].insert(entry, capacity, maxDepth);
                count++;
            } else {
                // Leaf node
                entries.add(entry);
                count++;
                if (entries.size() > capacity && depth < maxDepth) {
                    split(capacity, maxDepth);
                }
            }
        }

        @SuppressWarnings("unchecked")
        void split(int capacity, int maxDepth) {
            // Safe cast because array is only populated with Node<T>
            children = (Node<T>[]) new Node[8];

            List<Entry<T>> oldEntries = new ArrayList<>(entries);
            entries = null; // Mark as interior

            // We need to adjust count because we are re-inserting.
            // But we can just reset count and let insert() handle it.
            int entriesToMove = oldEntries.size();
            count -= entriesToMove;

            for (Entry<T> entry : oldEntries) {
                int index = getOctant(entry.x, entry.y, entry.z);
                if (children[index] == null) {
                    float half = size / 2;
                    float nx = x + (index & 1) * half;
                    float ny = y + ((index & 2) >> 1) * half;
                    float nz = z + ((index & 4) >> 2) * half;
                    children[index] = new Node<>(nx, ny, nz, half, depth + 1);
                }
                children[index].insert(entry, capacity, maxDepth);
                count++;
            }
        }

        boolean remove(T limit) {
            if (children != null) {
                for (Node<T> child : children) {
                    if (child != null && child.remove(limit)) {
                        count--;
                        return true;
                    }
                }
            } else {
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).element.equals(limit)) {
                        Entry<T> removed = entries.remove(i);
                        removed.recycle();
                        count--;
                        return true;
                    }
                }
            }
            return false;
        }

        void clear() {
            if (entries != null) {
                for (Entry<T> e : entries) {
                    e.recycle();
                }
                entries.clear();
            }
            if (children != null) {
                for (Node<T> child : children) {
                    if (child != null)
                        child.clear();
                }
                children = null;
            }
            entries = new ArrayList<>();
            count = 0;
        }

        boolean intersects(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
            return x < maxX && x + size > minX &&
                    y < maxY && y + size > minY &&
                    z < maxZ && z + size > minZ;
        }

        int getOctant(float ox, float oy, float oz) {
            int oct = 0;
            if (ox >= x + size / 2)
                oct |= 1;
            if (oy >= y + size / 2)
                oct |= 2;
            if (oz >= z + size / 2)
                oct |= 4;
            return oct;
        }

        void queryRange(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, List<T> result) {
            if (!intersects(minX, minY, minZ, maxX, maxY, maxZ)) {
                return;
            }

            if (children != null) {
                for (Node<T> child : children) {
                    if (child != null) {
                        child.queryRange(minX, minY, minZ, maxX, maxY, maxZ, result);
                    }
                }
            } else {
                for (Entry<T> entry : entries) {
                    if (entry.x >= minX && entry.x <= maxX &&
                            entry.y >= minY && entry.y <= maxY &&
                            entry.z >= minZ && entry.z <= maxZ) {
                        result.add(entry.element);
                    }
                }
            }
        }
    }
}
