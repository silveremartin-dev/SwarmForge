/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.util;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * A generic object pool to reduce garbage collection pressure for frequently
 * created objects.
 * Not thread-safe by default for performance; synchronizing overhead often
 * negates pooling benefits
 * in single-threaded context. For multi-threaded use, wrap or use
 * ConcurrentLinkedQueue.
 *
 * @param <T> The type of object to pool.
 */
public class ObjectPool<T> {

    private final Queue<T> pool;
    private final Supplier<T> factory;
    private final int maxSize;

    public ObjectPool(Supplier<T> factory, int initialCapacity, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.pool = new ArrayDeque<>(initialCapacity);

        for (int i = 0; i < initialCapacity; i++) {
            pool.offer(factory.get());
        }
    }

    /**
     * Borrows an object from the pool.
     */
    public T borrow() {
        T obj = pool.poll();
        if (obj == null) {
            return factory.get();
        }
        return obj;
    }

    /**
     * Returns an object to the pool.
     */
    public void recycle(T object) {
        if (pool.size() < maxSize) {
            pool.offer(object);
        }
        // If full, let GC handle it (overflow protection)
    }
}
