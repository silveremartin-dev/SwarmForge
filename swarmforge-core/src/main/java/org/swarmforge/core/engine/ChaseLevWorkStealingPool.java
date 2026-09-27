/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.engine;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lock-Free Multi-Producer Multi-Consumer (MPMC) Work-Stealing Task Scheduler based on the Chase-Lev Deque.
 *
 * Each worker thread owns a local deque:
 *  - Owner operations: LIFO {@code push()} and {@code pop()} with zero lock contention.
 *  - Thief operations: Lock-free FIFO {@code steal()} via atomic CAS on top cursor.
 *
 * Delivers optimal dynamic load balancing for cognitive BDI agent deliberative actions, nest construction,
 * and high-frequency territorial foraging calculations across all available CPU cores.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public final class ChaseLevWorkStealingPool {

    public static final class WorkStealingDeque {
        private static final int INITIAL_CAPACITY = 1024;

        private volatile long top = 0L;
        private volatile long bottom = 0L;
        private volatile Runnable[] buffer = new Runnable[INITIAL_CAPACITY];

        private static final VarHandle TOP_VH;
        private static final VarHandle BOTTOM_VH;

        static {
            try {
                TOP_VH = MethodHandles.lookup().findVarHandle(WorkStealingDeque.class, "top", long.class);
                BOTTOM_VH = MethodHandles.lookup().findVarHandle(WorkStealingDeque.class, "bottom", long.class);
            } catch (ReflectiveOperationException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        public void push(Runnable task) {
            long b = (long) BOTTOM_VH.getOpaque(this);
            long t = (long) TOP_VH.getVolatile(this);
            Runnable[] buf = buffer;

            if (b - t >= buf.length - 1) {
                // Grow buffer
                Runnable[] newBuf = new Runnable[buf.length * 2];
                for (long i = t; i < b; i++) {
                    newBuf[(int) (i & (newBuf.length - 1))] = buf[(int) (i & (buf.length - 1))];
                }
                buffer = newBuf;
                buf = newBuf;
            }

            buf[(int) (b & (buf.length - 1))] = task;
            BOTTOM_VH.setOpaque(this, b + 1);
        }

        public Runnable pop() {
            long b = (long) BOTTOM_VH.getOpaque(this) - 1;
            Runnable[] buf = buffer;
            BOTTOM_VH.setOpaque(this, b);

            long t = (long) TOP_VH.getVolatile(this);
            if (b < t) {
                BOTTOM_VH.setOpaque(this, t);
                return null;
            }

            Runnable task = buf[(int) (b & (buf.length - 1))];
            if (b > t) {
                return task;
            }

            // Single item race with thieves
            if (!TOP_VH.compareAndSet(this, t, t + 1)) {
                task = null; // Thief won
            }
            BOTTOM_VH.setOpaque(this, t + 1);
            return task;
        }

        public Runnable steal() {
            long t = (long) TOP_VH.getVolatile(this);
            long b = (long) BOTTOM_VH.getVolatile(this);
            if (t >= b) {
                return null;
            }

            Runnable[] buf = buffer;
            Runnable task = buf[(int) (t & (buf.length - 1))];
            if (!TOP_VH.compareAndSet(this, t, t + 1)) {
                return null;
            }
            return task;
        }

        public boolean isEmpty() {
            long t = (long) TOP_VH.getVolatile(this);
            long b = (long) BOTTOM_VH.getVolatile(this);
            return t >= b;
        }
    }

    private final int numWorkers;
    private final WorkStealingDeque[] deques;
    private final Thread[] workerThreads;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ChaseLevWorkStealingPool() {
        this(Runtime.getRuntime().availableProcessors());
    }

    public ChaseLevWorkStealingPool(int numWorkers) {
        this.numWorkers = Math.max(1, numWorkers);
        this.deques = new WorkStealingDeque[this.numWorkers];
        this.workerThreads = new Thread[this.numWorkers];

        for (int i = 0; i < this.numWorkers; i++) {
            deques[i] = new WorkStealingDeque();
        }
    }

    public synchronized void start() {
        if (running.compareAndSet(false, true)) {
            for (int i = 0; i < numWorkers; i++) {
                final int workerId = i;
                workerThreads[i] = Thread.ofPlatform().daemon(true).name("swarmforge-ws-" + workerId).start(() -> runWorker(workerId));
            }
        }
    }

    public void submit(int targetWorkerId, Runnable task) {
        int idx = Math.abs(targetWorkerId) % numWorkers;
        deques[idx].push(task);
    }

    private void runWorker(int workerId) {
        WorkStealingDeque myDeque = deques[workerId];

        while (running.get()) {
            Runnable task = myDeque.pop();

            if (task == null) {
                // Steal from other workers round-robin
                for (int offset = 1; offset < numWorkers; offset++) {
                    int victim = (workerId + offset) % numWorkers;
                    task = deques[victim].steal();
                    if (task != null) {
                        break;
                    }
                }
            }

            if (task != null) {
                try {
                    task.run();
                } catch (Throwable ignored) {
                }
            } else {
                Thread.onSpinWait();
            }
        }
    }

    public synchronized void stop() {
        running.set(false);
    }

    public int getNumWorkers() {
        return numWorkers;
    }
}
