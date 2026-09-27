/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.engine;

import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.spatial.Morton3D;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;
import java.util.UUID;

/**
 * Native Direct Off-Heap Data-Oriented Design (DOD) Entity Buffer for Java 21 LTS.
 *
 * Allocates native off-heap memory outside the JVM Garbage Collector heap via direct {@link ByteBuffer}.
 * Eliminates all GC overhead and memory fragmentation, enabling deterministic, multi-million entity simulations
 * with zero GC pause spikes.
 *
 * Contiguous Structure-of-Arrays (SoA) Direct Off-Heap Layout:
 *  - Positions: posX, posY, posZ (3 * 4 bytes/entity)
 *  - Velocities: velX, velY, velZ (3 * 4 bytes/entity)
 *  - Metabolic: health, energy (2 * 4 bytes/entity)
 *  - Caste & Job: caste, job (2 * 1 byte/entity)
 *  - Spatial & Identity: colonyMsb, colonyLsb, mortonCode (3 * 8 bytes/entity)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public final class OffHeapDodEntityBuffer implements AutoCloseable {

    private static final int INITIAL_CAPACITY = 32_768;
    private static final float GROWTH_FACTOR = 1.75f;

    private int capacity;
    private int count;

    // Direct Off-Heap Native Buffers
    private ByteBuffer bufPosX, bufPosY, bufPosZ;
    private FloatBuffer fbPosX, fbPosY, fbPosZ;

    private ByteBuffer bufVelX, bufVelY, bufVelZ;
    private FloatBuffer fbVelX, fbVelY, fbVelZ;

    private ByteBuffer bufHealth, bufEnergy;
    private FloatBuffer fbHealth, fbEnergy;

    private ByteBuffer bufCaste, bufJob;

    private ByteBuffer bufColonyMsb, bufColonyLsb, bufMorton;
    private LongBuffer lbColonyMsb, lbColonyLsb, lbMorton;

    public OffHeapDodEntityBuffer() {
        this(INITIAL_CAPACITY);
    }

    public OffHeapDodEntityBuffer(int initialCapacity) {
        this.capacity = Math.max(128, initialCapacity);
        this.count = 0;
        allocateBuffers(this.capacity);
    }

    private void allocateBuffers(int cap) {
        bufPosX = ByteBuffer.allocateDirect(cap * Float.BYTES).order(ByteOrder.nativeOrder());
        fbPosX = bufPosX.asFloatBuffer();

        bufPosY = ByteBuffer.allocateDirect(cap * Float.BYTES).order(ByteOrder.nativeOrder());
        fbPosY = bufPosY.asFloatBuffer();

        bufPosZ = ByteBuffer.allocateDirect(cap * Float.BYTES).order(ByteOrder.nativeOrder());
        fbPosZ = bufPosZ.asFloatBuffer();

        bufVelX = ByteBuffer.allocateDirect(cap * Float.BYTES).order(ByteOrder.nativeOrder());
        fbVelX = bufVelX.asFloatBuffer();

        bufVelY = ByteBuffer.allocateDirect(cap * Float.BYTES).order(ByteOrder.nativeOrder());
        fbVelY = bufVelY.asFloatBuffer();

        bufVelZ = ByteBuffer.allocateDirect(cap * Float.BYTES).order(ByteOrder.nativeOrder());
        fbVelZ = bufVelZ.asFloatBuffer();

        bufHealth = ByteBuffer.allocateDirect(cap * Float.BYTES).order(ByteOrder.nativeOrder());
        fbHealth = bufHealth.asFloatBuffer();

        bufEnergy = ByteBuffer.allocateDirect(cap * Float.BYTES).order(ByteOrder.nativeOrder());
        fbEnergy = bufEnergy.asFloatBuffer();

        bufCaste = ByteBuffer.allocateDirect(cap).order(ByteOrder.nativeOrder());
        bufJob = ByteBuffer.allocateDirect(cap).order(ByteOrder.nativeOrder());

        bufColonyMsb = ByteBuffer.allocateDirect(cap * Long.BYTES).order(ByteOrder.nativeOrder());
        lbColonyMsb = bufColonyMsb.asLongBuffer();

        bufColonyLsb = ByteBuffer.allocateDirect(cap * Long.BYTES).order(ByteOrder.nativeOrder());
        lbColonyLsb = bufColonyLsb.asLongBuffer();

        bufMorton = ByteBuffer.allocateDirect(cap * Long.BYTES).order(ByteOrder.nativeOrder());
        lbMorton = bufMorton.asLongBuffer();
    }

    public synchronized void ensureCapacity(int minCapacity) {
        if (minCapacity <= capacity) {
            return;
        }

        int newCap = (int) Math.max(minCapacity, capacity * GROWTH_FACTOR);

        ByteBuffer newBufPosX = ByteBuffer.allocateDirect(newCap * Float.BYTES).order(ByteOrder.nativeOrder());
        ByteBuffer newBufPosY = ByteBuffer.allocateDirect(newCap * Float.BYTES).order(ByteOrder.nativeOrder());
        ByteBuffer newBufPosZ = ByteBuffer.allocateDirect(newCap * Float.BYTES).order(ByteOrder.nativeOrder());

        ByteBuffer newBufVelX = ByteBuffer.allocateDirect(newCap * Float.BYTES).order(ByteOrder.nativeOrder());
        ByteBuffer newBufVelY = ByteBuffer.allocateDirect(newCap * Float.BYTES).order(ByteOrder.nativeOrder());
        ByteBuffer newBufVelZ = ByteBuffer.allocateDirect(newCap * Float.BYTES).order(ByteOrder.nativeOrder());

        ByteBuffer newBufHealth = ByteBuffer.allocateDirect(newCap * Float.BYTES).order(ByteOrder.nativeOrder());
        ByteBuffer newBufEnergy = ByteBuffer.allocateDirect(newCap * Float.BYTES).order(ByteOrder.nativeOrder());

        ByteBuffer newBufCaste = ByteBuffer.allocateDirect(newCap).order(ByteOrder.nativeOrder());
        ByteBuffer newBufJob = ByteBuffer.allocateDirect(newCap).order(ByteOrder.nativeOrder());

        ByteBuffer newBufColonyMsb = ByteBuffer.allocateDirect(newCap * Long.BYTES).order(ByteOrder.nativeOrder());
        ByteBuffer newBufColonyLsb = ByteBuffer.allocateDirect(newCap * Long.BYTES).order(ByteOrder.nativeOrder());
        ByteBuffer newBufMorton = ByteBuffer.allocateDirect(newCap * Long.BYTES).order(ByteOrder.nativeOrder());

        if (count > 0) {
            bufPosX.position(0).limit(count * Float.BYTES);
            newBufPosX.put(bufPosX);
            bufPosY.position(0).limit(count * Float.BYTES);
            newBufPosY.put(bufPosY);
            bufPosZ.position(0).limit(count * Float.BYTES);
            newBufPosZ.put(bufPosZ);

            bufVelX.position(0).limit(count * Float.BYTES);
            newBufVelX.put(bufVelX);
            bufVelY.position(0).limit(count * Float.BYTES);
            newBufVelY.put(bufVelY);
            bufVelZ.position(0).limit(count * Float.BYTES);
            newBufVelZ.put(bufVelZ);

            bufHealth.position(0).limit(count * Float.BYTES);
            newBufHealth.put(bufHealth);
            bufEnergy.position(0).limit(count * Float.BYTES);
            newBufEnergy.put(bufEnergy);

            bufCaste.position(0).limit(count);
            newBufCaste.put(bufCaste);
            bufJob.position(0).limit(count);
            newBufJob.put(bufJob);

            bufColonyMsb.position(0).limit(count * Long.BYTES);
            newBufColonyMsb.put(bufColonyMsb);
            bufColonyLsb.position(0).limit(count * Long.BYTES);
            newBufColonyLsb.put(bufColonyLsb);
            bufMorton.position(0).limit(count * Long.BYTES);
            newBufMorton.put(bufMorton);
        }

        this.bufPosX = newBufPosX; this.fbPosX = newBufPosX.asFloatBuffer();
        this.bufPosY = newBufPosY; this.fbPosY = newBufPosY.asFloatBuffer();
        this.bufPosZ = newBufPosZ; this.fbPosZ = newBufPosZ.asFloatBuffer();

        this.bufVelX = newBufVelX; this.fbVelX = newBufVelX.asFloatBuffer();
        this.bufVelY = newBufVelY; this.fbVelY = newBufVelY.asFloatBuffer();
        this.bufVelZ = newBufVelZ; this.fbVelZ = newBufVelZ.asFloatBuffer();

        this.bufHealth = newBufHealth; this.fbHealth = newBufHealth.asFloatBuffer();
        this.bufEnergy = newBufEnergy; this.fbEnergy = newBufEnergy.asFloatBuffer();

        this.bufCaste = newBufCaste;
        this.bufJob = newBufJob;

        this.bufColonyMsb = newBufColonyMsb; this.lbColonyMsb = newBufColonyMsb.asLongBuffer();
        this.bufColonyLsb = newBufColonyLsb; this.lbColonyLsb = newBufColonyLsb.asLongBuffer();
        this.bufMorton = newBufMorton; this.lbMorton = newBufMorton.asLongBuffer();

        this.capacity = newCap;
    }

    public synchronized int spawn(UUID colonyId, Individual.Caste casteType, Individual.Job jobType,
                                  float x, float y, float z) {
        ensureCapacity(count + 1);
        int idx = count;

        fbPosX.put(idx, x);
        fbPosY.put(idx, y);
        fbPosZ.put(idx, z);

        float randVx = (float) ((Math.sin(idx * 0.137) * 0.2));
        float randVy = (float) ((Math.cos(idx * 0.173) * 0.2));
        float randVz = 0.0f;

        fbVelX.put(idx, randVx);
        fbVelY.put(idx, randVy);
        fbVelZ.put(idx, randVz);

        fbHealth.put(idx, 100.0f);
        fbEnergy.put(idx, 100.0f);

        bufCaste.put(idx, casteType != null ? (byte) casteType.ordinal() : 0);
        bufJob.put(idx, jobType != null ? (byte) jobType.ordinal() : 0);

        if (colonyId != null) {
            lbColonyMsb.put(idx, colonyId.getMostSignificantBits());
            lbColonyLsb.put(idx, colonyId.getLeastSignificantBits());
        } else {
            lbColonyMsb.put(idx, 0L);
            lbColonyLsb.put(idx, 0L);
        }

        long mCode = Morton3D.encode((int) Math.max(0, x), (int) Math.max(0, y), (int) Math.max(0, z));
        lbMorton.put(idx, mCode);

        count++;
        return idx;
    }

    public void step(float dt, float boundX, float boundY, float boundZ) {
        final int n = count;
        final float energyDecay = 0.01f * dt;

        for (int i = 0; i < n; i++) {
            float x = fbPosX.get(i) + fbVelX.get(i) * dt;
            float y = fbPosY.get(i) + fbVelY.get(i) * dt;
            float z = fbPosZ.get(i) + fbVelZ.get(i) * dt;

            // Boundary collision
            if (x < 0.0f) { x = 0.0f; fbVelX.put(i, -fbVelX.get(i)); }
            else if (x > boundX) { x = boundX; fbVelX.put(i, -fbVelX.get(i)); }

            if (y < 0.0f) { y = 0.0f; fbVelY.put(i, -fbVelY.get(i)); }
            else if (y > boundY) { y = boundY; fbVelY.put(i, -fbVelY.get(i)); }

            if (z < 0.0f) { z = 0.0f; fbVelZ.put(i, -fbVelZ.get(i)); }
            else if (z > boundZ) { z = boundZ; fbVelZ.put(i, -fbVelZ.get(i)); }

            fbPosX.put(i, x);
            fbPosY.put(i, y);
            fbPosZ.put(i, z);

            float en = fbEnergy.get(i) - energyDecay;
            fbEnergy.put(i, Math.max(0.0f, en));

            long mCode = Morton3D.encode((int) Math.max(0, x), (int) Math.max(0, y), (int) Math.max(0, z));
            lbMorton.put(i, mCode);
        }
    }

    public int getCount() {
        return count;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public void close() {
        bufPosX = null; bufPosY = null; bufPosZ = null;
        bufVelX = null; bufVelY = null; bufVelZ = null;
        bufHealth = null; bufEnergy = null;
        bufCaste = null; bufJob = null;
        bufColonyMsb = null; bufColonyLsb = null; bufMorton = null;
    }
}
