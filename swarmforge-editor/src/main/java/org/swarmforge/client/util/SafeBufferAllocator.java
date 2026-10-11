/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.client.util;

import com.jme3.util.BufferAllocator;

import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * High-performance, crash-free direct buffer allocator for SwarmForge and jMonkeyEngine.
 * <p>
 * Grounded in Java standard NIO memory management, this allocator eliminates the JVM crash
 * (EXCEPTION_ACCESS_VIOLATION 0xc0000005) and unsightly {@link InterruptedException} stack traces
 * triggered on Windows by jME3's default {@code LWJGLBufferAllocator} cleaner daemon thread during
 * simulation shutdown.
 * </p>
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SafeBufferAllocator implements BufferAllocator {

    private static final Logger LOG = Logger.getLogger(SafeBufferAllocator.class.getName());
    private static Method cleanerMethod;
    private static Method cleanMethod;

    static {
        try {
            // Attempt to resolve direct buffer cleaner reflectively if permitted by JVM module access
            ByteBuffer probe = ByteBuffer.allocateDirect(1);
            Method mCleaner = probe.getClass().getMethod("cleaner");
            mCleaner.setAccessible(true);
            Object cleaner = mCleaner.invoke(probe);
            if (cleaner != null) {
                Method mClean = cleaner.getClass().getMethod("clean");
                mClean.setAccessible(true);
                cleanerMethod = mCleaner;
                cleanMethod = mClean;
                cleanMethod.invoke(cleaner);
            }
        } catch (Throwable ignored) {
            cleanerMethod = null;
            cleanMethod = null;
        }
    }

    /**
     * Default public no-args constructor required by {@link com.jme3.util.BufferAllocatorFactory}.
     */
    public SafeBufferAllocator() {
    }

    @Override
    public ByteBuffer allocate(int size) {
        return ByteBuffer.allocateDirect(size);
    }

    @Override
    public void destroyDirectBuffer(Buffer buffer) {
        if (buffer == null) {
            return;
        }
        if (cleanerMethod != null && cleanMethod != null) {
            try {
                Object cleaner = cleanerMethod.invoke(buffer);
                if (cleaner != null) {
                    cleanMethod.invoke(cleaner);
                    return;
                }
            } catch (Throwable ignored) {
            }
        }
        // Direct ByteBuffers are safely reclaimed by the JVM Garbage Collector
        // and internal NIO cleaners without native teardown race conditions.
    }
}
