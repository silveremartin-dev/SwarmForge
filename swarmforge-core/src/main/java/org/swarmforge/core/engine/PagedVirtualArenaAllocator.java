/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.engine;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Paged Virtual Arena Memory Allocator for Unbounded Terrarium Scaling.
 *
 * Employs a 2 MB paged virtual memory architecture:
 *  - Divides massive world entity states into fixed-size contiguous direct pages (2 MB each).
 *  - Allows dynamically spawning millions of entities without large contiguous array reallocations or copy overheads.
 *  - Page index indexing: {@code pageIdx = entityId / PAGE_CAPACITY}, {@code offset = entityId % PAGE_CAPACITY}.
 *  - 0 GC memory allocations on page traversal and direct access.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public final class PagedVirtualArenaAllocator implements AutoCloseable {

    public static final int PAGE_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB Page Size
    public static final int ENTITY_STRIDE_BYTES = 64;           // 64-byte Cache line aligned entity stride
    public static final int ENTITIES_PER_PAGE = PAGE_SIZE_BYTES / ENTITY_STRIDE_BYTES; // 32,768 entities/page

    private final List<ByteBuffer> pages = new ArrayList<>();
    private int totalEntityCount = 0;
    private int allocatedCapacity = 0;

    public PagedVirtualArenaAllocator() {
        this(1); // 1 initial page (32,768 entities)
    }

    public PagedVirtualArenaAllocator(int initialPages) {
        int p = Math.max(1, initialPages);
        for (int i = 0; i < p; i++) {
            allocateNewPage();
        }
    }

    private synchronized void allocateNewPage() {
        ByteBuffer page = ByteBuffer.allocateDirect(PAGE_SIZE_BYTES).order(ByteOrder.nativeOrder());
        pages.add(page);
        allocatedCapacity += ENTITIES_PER_PAGE;
    }

    public synchronized int allocateEntity() {
        if (totalEntityCount >= allocatedCapacity) {
            allocateNewPage();
        }
        return totalEntityCount++;
    }

    public void setPosition(int entityId, float x, float y, float z) {
        int pageIdx = entityId / ENTITIES_PER_PAGE;
        int entityOffset = (entityId % ENTITIES_PER_PAGE) * ENTITY_STRIDE_BYTES;

        ByteBuffer page = pages.get(pageIdx);
        page.putFloat(entityOffset, x);
        page.putFloat(entityOffset + 4, y);
        page.putFloat(entityOffset + 8, z);
    }

    public float getPositionX(int entityId) {
        int pageIdx = entityId / ENTITIES_PER_PAGE;
        int entityOffset = (entityId % ENTITIES_PER_PAGE) * ENTITY_STRIDE_BYTES;
        return pages.get(pageIdx).getFloat(entityOffset);
    }

    public float getPositionY(int entityId) {
        int pageIdx = entityId / ENTITIES_PER_PAGE;
        int entityOffset = (entityId % ENTITIES_PER_PAGE) * ENTITY_STRIDE_BYTES;
        return pages.get(pageIdx).getFloat(entityOffset + 4);
    }

    public float getPositionZ(int entityId) {
        int pageIdx = entityId / ENTITIES_PER_PAGE;
        int entityOffset = (entityId % ENTITIES_PER_PAGE) * ENTITY_STRIDE_BYTES;
        return pages.get(pageIdx).getFloat(entityOffset + 8);
    }

    public int getTotalEntityCount() {
        return totalEntityCount;
    }

    public int getPageCount() {
        return pages.size();
    }

    public int getAllocatedCapacity() {
        return allocatedCapacity;
    }

    @Override
    public synchronized void close() {
        pages.clear();
        totalEntityCount = 0;
        allocatedCapacity = 0;
    }
}
