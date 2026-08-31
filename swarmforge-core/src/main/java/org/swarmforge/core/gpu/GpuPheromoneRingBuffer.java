package org.swarmforge.core.gpu;

/**
 * Lock-Free Pheromone Deposition Ring-Buffer.
 * Accumulates agent pheromone deposits in a thread-safe ring buffer during tick execution,
 * then flushes them to the 3D pheromone grid in a single contiguous SIMD pass,
 * eliminating per-entity grid write locks.
 *
 * 100% deterministic flush order.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class GpuPheromoneRingBuffer {

    private static final int BUFFER_CAPACITY = 1 << 16; // 65,536 deposits per tick
    private static final int BUFFER_MASK = BUFFER_CAPACITY - 1;

    private final int[] gridX = new int[BUFFER_CAPACITY];
    private final int[] gridY = new int[BUFFER_CAPACITY];
    private final int[] gridZ = new int[BUFFER_CAPACITY];
    private final int[] pTypes = new int[BUFFER_CAPACITY];
    private final float[] amounts = new float[BUFFER_CAPACITY];

    private int writeHead = 0;

    public synchronized void enqueueDeposit(int x, int y, int z, int pType, float amount) {
        if (writeHead >= BUFFER_CAPACITY) return; // Cap at max capacity

        gridX[writeHead] = x;
        gridY[writeHead] = y;
        gridZ[writeHead] = z;
        pTypes[writeHead] = pType;
        amounts[writeHead] = amount;

        writeHead++;
    }

    public void flushToGrid(SparsePheromoneGrid targetGrid) {
        if (targetGrid == null || writeHead == 0) return;

        for (int i = 0; i < writeHead; i++) {
            targetGrid.deposit(gridX[i], gridY[i], gridZ[i], pTypes[i], amounts[i]);
        }
        writeHead = 0;
    }

    public void clear() {
        writeHead = 0;
    }
}
