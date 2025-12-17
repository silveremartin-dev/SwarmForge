/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.spatial;

import java.util.List;

/**
 * Generic interface for spatial partition data structures.
 * Allows storing elements with 3D coordinates and querying them efficiently.
 *
 * @param <T> Type of element stored
 */
public interface SpatialPartition<T> {

    /**
     * Insert an element into the spatial index.
     * 
     * @param element The element to insert
     * @param x       X coordinate
     * @param y       Y coordinate
     * @param z       Z coordinate
     */
    void insert(T element, float x, float y, float z);

    /**
     * Remove an element from the index.
     * 
     * @param element The element to remove
     * @return true if found and removed
     */
    boolean remove(T element);

    /**
     * Update an element's position.
     * Effectively removes and re-inserts.
     */
    void update(T element, float oldX, float oldY, float oldZ, float newX, float newY, float newZ);

    /**
     * Find all elements within a bounding box.
     */
    List<T> queryRange(float minX, float minY, float minZ, float maxX, float maxY, float maxZ);

    /**
     * Find all elements within a radius of a point.
     */
    List<T> queryRadius(float x, float y, float z, float radius);

    /**
     * Remove all elements.
     */
    void clear();

    /**
     * Get total element count.
     */
    int size();
}
