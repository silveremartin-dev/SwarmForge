package org.swarmforge.core.ecs.storage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;

/**
 * High-performance Off-Heap / Flat Array Direct Native Memory Component Buffer.
 * Allocates contiguous direct memory for up to 1,000,000 entities, completely bypassing
 * the Java Garbage Collector to eliminate GC pauses during large scale simulations.
 *
 * Direct Data Layout per Entity (32 Bytes):
 *  - Float x, y, z (12 bytes)
 *  - Float energy, health (8 bytes)
 *  - Long caps0, caps1 (12 bytes packed)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class OffHeapEntityBuffer {

    private static final int BYTES_PER_ENTITY = 32;
    private final int capacity;
    private final ByteBuffer directBuffer;
    private final FloatBuffer floatView;
    private final LongBuffer longView;

    public OffHeapEntityBuffer(int maxEntities) {
        this.capacity = maxEntities;
        int totalBytes = maxEntities * BYTES_PER_ENTITY;
        this.directBuffer = ByteBuffer.allocateDirect(totalBytes).order(ByteOrder.nativeOrder());
        this.floatView = directBuffer.asFloatBuffer();
        this.longView = directBuffer.asLongBuffer();
    }

    public void setPosition(int entityId, float x, float y, float z) {
        int baseFloat = (entityId * BYTES_PER_ENTITY) / 4;
        floatView.put(baseFloat, x);
        floatView.put(baseFloat + 1, y);
        floatView.put(baseFloat + 2, z);
    }

    public float getX(int entityId) {
        return floatView.get((entityId * BYTES_PER_ENTITY) / 4);
    }

    public float getY(int entityId) {
        return floatView.get((entityId * BYTES_PER_ENTITY) / 4 + 1);
    }

    public float getZ(int entityId) {
        return floatView.get((entityId * BYTES_PER_ENTITY) / 4 + 2);
    }

    public void setVitality(int entityId, float energy, float health) {
        int baseFloat = (entityId * BYTES_PER_ENTITY) / 4;
        floatView.put(baseFloat + 3, energy);
        floatView.put(baseFloat + 4, health);
    }

    public float getEnergy(int entityId) {
        return floatView.get((entityId * BYTES_PER_ENTITY) / 4 + 3);
    }

    public void setBitmasks(int entityId, long caps0, long caps1) {
        int baseLong = (entityId * BYTES_PER_ENTITY) / 8;
        longView.put(baseLong + 3, caps0);
    }

    public long getCaps0(int entityId) {
        return longView.get((entityId * BYTES_PER_ENTITY) / 8 + 3);
    }

    public void clear() {
        directBuffer.clear();
    }

    public int getCapacity() {
        return capacity;
    }
}
