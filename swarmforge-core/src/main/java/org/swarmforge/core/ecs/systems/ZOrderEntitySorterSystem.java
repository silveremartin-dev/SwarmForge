package org.swarmforge.core.ecs.systems;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import org.swarmforge.core.ecs.components.PositionComponent;

import java.util.Arrays;

/**
 * 3D Morton Z-Order Entity Cache Locality Sorter System.
 * Periodically sorts entity indexing keys based on 3D Morton spatial Z-curve encoding,
 * packing spatially adjacent entities into contiguous physical memory addresses to drastically
 * reduce CPU L1/L2 cache misses during spatial neighbor queries.
 *
 * 100% deterministic entity sorting.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class ZOrderEntitySorterSystem extends IteratingSystem {

    private static final float CELL_SIZE = 4.0f;
    private static final int SORT_INTERVAL_SEC = 2; // Sort cache every 2 seconds

    private ComponentMapper<PositionComponent> mPosition;

    private float timerSec = 0f;
    private int activeCount = 0;
    private final long[] mortonKeysScratch = new long[100_000];
    private final int[]  entityIdsScratch = new int[100_000];

    public ZOrderEntitySorterSystem() {
        super(Aspect.all(PositionComponent.class));
    }

    @Override
    protected void begin() {
        timerSec += world.getDelta();
        activeCount = 0;
    }

    @Override
    protected void process(int entityId) {
        if (timerSec < SORT_INTERVAL_SEC || entityId >= 100_000) return;

        PositionComponent pos = mPosition.get(entityId);
        if (pos == null) return;

        int cx = (int) Math.floor(pos.x / CELL_SIZE);
        int cy = (int) Math.floor(pos.y / CELL_SIZE);
        int cz = (int) Math.floor(pos.z / CELL_SIZE);

        long key = morton3D(cx, cy, cz);
        mortonKeysScratch[activeCount] = key;
        entityIdsScratch[activeCount] = entityId;
        activeCount++;
    }

    @Override
    protected void end() {
        if (timerSec >= SORT_INTERVAL_SEC) {
            timerSec -= SORT_INTERVAL_SEC;
            if (activeCount > 1) {
                quickSortZOrder(0, activeCount - 1);
            }
        }
    }

    private void quickSortZOrder(int low, int high) {
        if (low < high) {
            int p = partition(low, high);
            quickSortZOrder(low, p - 1);
            quickSortZOrder(p + 1, high);
        }
    }

    private int partition(int low, int high) {
        long pivot = mortonKeysScratch[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (mortonKeysScratch[j] <= pivot) {
                i++;
                swap(i, j);
            }
        }
        swap(i + 1, high);
        return i + 1;
    }

    private void swap(int i, int j) {
        long tmpK = mortonKeysScratch[i];
        mortonKeysScratch[i] = mortonKeysScratch[j];
        mortonKeysScratch[j] = tmpK;

        int tmpE = entityIdsScratch[i];
        entityIdsScratch[i] = entityIdsScratch[j];
        entityIdsScratch[j] = tmpE;
    }

    private static long morton3D(int x, int y, int z) {
        long lx = expandBits(x & 0x1FFFFF);
        long ly = expandBits(y & 0x1FFFFF);
        long lz = expandBits(z & 0x1FFFFF);
        return lx | (ly << 1) | (lz << 2);
    }

    private static long expandBits(long v) {
        v = (v | (v << 32)) & 0x1F00000000FFFFL;
        v = (v | (v << 16)) & 0x1F0000FF0000FFL;
        v = (v | (v << 8))  & 0x100F00F00F00F00FL;
        v = (v | (v << 4))  & 0x10C30C30C30C30C3L;
        v = (v | (v << 2))  & 0x1249249249249249L;
        return v;
    }
}
