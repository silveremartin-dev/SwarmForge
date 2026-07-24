/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation.gpu;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Off-Heap Direct Memory Manager for 3D Pheromone Grids.
 * Allocates native off-heap memory outside the Java Garbage Collector heap,
 * preventing GC pauses when simulating dense multi-million cell terrariums.
 */
public class PheromoneOffHeapGrid implements AutoCloseable {

    private final int width;
    private final int height;
    private final int depth;
    private final int pheromoneTypes;
    private final int totalElements;

    private final ByteBuffer byteBuffer;
    private final FloatBuffer floatBuffer;

    public PheromoneOffHeapGrid(int width, int height, int depth, int pheromoneTypes) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.pheromoneTypes = pheromoneTypes;
        this.totalElements = width * height * depth * pheromoneTypes;

        int byteCapacity = totalElements * Float.BYTES;
        this.byteBuffer = ByteBuffer.allocateDirect(byteCapacity).order(ByteOrder.nativeOrder());
        this.floatBuffer = byteBuffer.asFloatBuffer();
    }

    /**
     * Copy input Java float array into off-heap native memory.
     */
    public void copyFrom(float[] source) {
        floatBuffer.clear();
        floatBuffer.put(source, 0, Math.min(source.length, totalElements));
    }

    /**
     * Copy off-heap native memory into destination Java float array.
     */
    public void copyTo(float[] destination) {
        floatBuffer.clear();
        floatBuffer.get(destination, 0, Math.min(destination.length, totalElements));
    }

    public float get(int x, int y, int z, int type) {
        int index = ((x + y * width + z * width * height) * pheromoneTypes) + type;
        return floatBuffer.get(index);
    }

    public void set(int x, int y, int z, int type, float value) {
        int index = ((x + y * width + z * width * height) * pheromoneTypes) + type;
        floatBuffer.put(index, value);
    }

    public ByteBuffer getByteBuffer() {
        return byteBuffer;
    }

    public FloatBuffer getFloatBuffer() {
        return floatBuffer;
    }

    public int getTotalElements() {
        return totalElements;
    }

    @Override
    public void close() {
        floatBuffer.clear();
        byteBuffer.clear();
    }
}
