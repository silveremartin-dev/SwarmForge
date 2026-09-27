/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.compute;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Direct Zero-Copy Halo Memory Buffer for Multi-Node Cluster Synchronization.
 *
 * Implements direct memory region sharing for pheromone halo borders and cross-node ant migration.
 * Bypasses JVM object deserialization and intermediary copies:
 *  - Direct off-heap byte buffer mapped to socket NIO / RDMA channels.
 *  - Header layout: [SourceNode (4B) | TargetNode (4B) | HaloWidth (4B) | HaloHeight (4B) | Type (4B) | Payload (NB)].
 *  - Zero GC allocations on distributed tick exchanges.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public final class ZeroCopyHaloExchange implements AutoCloseable {

    private final int bufferCapacity;
    private final ByteBuffer directBuffer;

    public ZeroCopyHaloExchange(int maxHaloElements) {
        // 20 bytes header + float elements
        this.bufferCapacity = 20 + (maxHaloElements * Float.BYTES);
        this.directBuffer = ByteBuffer.allocateDirect(bufferCapacity).order(ByteOrder.nativeOrder());
    }

    /**
     * Packs halo slice into direct off-heap buffer with zero allocations.
     */
    public synchronized int packHaloSlice(int sourceNode, int targetNode, int width, int height, int pheromoneType,
                                          float[] pheromoneData, int dataOffset, int dataLength) {
        directBuffer.clear();
        directBuffer.putInt(sourceNode);
        directBuffer.putInt(targetNode);
        directBuffer.putInt(width);
        directBuffer.putInt(height);
        directBuffer.putInt(pheromoneType);

        directBuffer.asFloatBuffer().put(pheromoneData, dataOffset, dataLength);
        directBuffer.position(20 + (dataLength * Float.BYTES));
        return directBuffer.position();
    }

    /**
     * Unpacks halo slice directly from off-heap buffer into local destination grid.
     */
    public synchronized void unpackHaloSlice(float[] destGrid, int destOffset, int dataLength) {
        directBuffer.position(20);
        directBuffer.asFloatBuffer().get(destGrid, destOffset, dataLength);
    }

    public ByteBuffer getDirectBuffer() {
        return directBuffer;
    }

    public int getBufferCapacity() {
        return bufferCapacity;
    }

    @Override
    public void close() {
        directBuffer.clear();
    }
}
