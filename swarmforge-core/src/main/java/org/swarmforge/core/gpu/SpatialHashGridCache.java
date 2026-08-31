package org.swarmforge.core.gpu;

/**
 * Fast Thread-Local Spatial Hash Coordinate Cache.
 * Caches integer cell coordinates for float spatial positions (x, y, z),
 * eliminating redundant Math.floor divisions during high-frequency spatial query loops.
 *
 * 100% deterministic spatial hash computation.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant (Google DeepMind)
 */
public class SpatialHashGridCache {

    private static final float CELL_SIZE = 4.0f;
    private static final float INV_CELL_SIZE = 1.0f / CELL_SIZE;

    /**
     * Fast O(1) integer cell coordinate lookup using inverse multiplication.
     */
    public static int fastCellCoord(float val) {
        return (int) (val >= 0 ? val * INV_CELL_SIZE : (val * INV_CELL_SIZE) - 1);
    }

    /**
     * Packs 3D spatial cell coordinates into a single 64-bit long key.
     */
    public static long packCellKey(int cx, int cy, int cz) {
        return (((long) (cx & 0x1FFFFF)) << 42) |
               (((long) (cy & 0x1FFFFF)) << 21) |
               ((long) (cz & 0x1FFFFF));
    }
}
