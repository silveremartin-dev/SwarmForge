/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * A generic object pool to reduce garbage collection pressure for frequently
 * created objects.
 *
 * <p>Backed by a {@link ConcurrentLinkedQueue} to be fully thread-safe for
 * multi-threaded simulation ticks (parallelStream, virtual threads, etc.).
 * The pool is lock-free for both {@link #borrow()} and {@link #recycle(Object)}.
 *
 * @param <T> The type of object to pool.
 */
public class ObjectPool<T> {

    private final Queue<T> pool;
    private final Supplier<T> factory;
    private final int maxSize;
    // Track approximate pool size without blocking; ConcurrentLinkedQueue.size() is O(n)
    private final AtomicInteger currentSize = new AtomicInteger(0);

    public ObjectPool(Supplier<T> factory, int initialCapacity, int maxSize) {
        this.factory = factory;
        this.maxSize = maxSize;
        this.pool = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < initialCapacity; i++) {
            pool.offer(factory.get());
            currentSize.incrementAndGet();
        }
    }

    /**
     * Borrows an object from the pool. If the pool is empty, allocates a new instance.
     */
    public T borrow() {
        T obj = pool.poll();
        if (obj != null) {
            currentSize.decrementAndGet();
            return obj;
        }
        return factory.get();
    }

    /**
     * Returns an object to the pool. If the pool is at capacity, the object is dropped
     * and collected by the GC (overflow protection).
     */
    public void recycle(T object) {
        if (object != null && currentSize.get() < maxSize) {
            pool.offer(object);
            currentSize.incrementAndGet();
        }
        // If pool is full or object is null, let GC handle it
    }
}
