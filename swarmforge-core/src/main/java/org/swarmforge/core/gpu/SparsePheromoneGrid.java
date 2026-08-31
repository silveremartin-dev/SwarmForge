/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.gpu;

import org.swarmforge.core.spatial.Morton3D;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.BiPredicate;

/**
 * Sparse pheromone grid with lazy decay and terrain-aware diffusion.
 * 
 * Optimizations:
 * 1. Only stores cells with non-zero pheromone values (sparse)
 * 2. Uses timestamps for lazy decay calculation (compute on read)
 * 3. Periodically prunes entries that have decayed below threshold
 * 4. Uses Morton encoding for spatial locality in cache
 * 5. Only diffuses through passable cells (air, chamber) - not rock/water
 * 6. Optional max height limit above ground for efficiency
 * 
 * Complexity: O(n) where n = active pheromone deposits, not O(W*H*D).
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class SparsePheromoneGrid {

    private static final float PRUNE_THRESHOLD = 0.001f;
    public static final int PHEROMONE_TYPES = 8;

    // Default Pheromone Properties in real-world seconds (Index mapping: 0=HOME, 1=FOOD, 2=DANGER/ALARM,
    // 3=RECRUIT, 4=MARK, 5=GATHER, 6=ATTACK, 7=UNKNOWN)
    private static final float[] DEFAULT_HALF_LIFE_SECONDS = {
            1.6667f, // HOME_TRAIL (1.67s)
            3.3333f, // FOOD_TRAIL (3.33s)
            0.5000f, // DANGER (0.5s)
            1.3333f, // RECRUIT (1.33s)
            8.3333f, // MARK (8.33s)
            5.0000f, // GATHER (5.0s)
            0.3333f, // ATTACK (0.33s)
            2.5000f  // UNKNOWN (2.5s)
    };

    private static final float[] DEFAULT_DIFFUSION_RATE = {
            0.05f, // HOME_TRAIL
            0.08f, // FOOD_TRAIL
            0.15f, // DANGER
            0.06f, // RECRUIT
            0.02f, // MARK
            0.03f, // GATHER
            0.10f, // ATTACK
            0.04f  // UNKNOWN
    };

    private final float[] halfLifeSeconds;
    private final float[] diffusionRate;
    private final ConcurrentHashMap<Long, PheromoneEntry> grid;
    private volatile long currentTick = 0;
    private volatile float simulationStepSeconds = 0.016666667f; // Default 60 TPS dt
    private volatile float evaporationMultiplier = 1.0f;
    private final int width, height, depth;

    public float getSimulationStepSeconds() { return simulationStepSeconds; }
    public void setSimulationStepSeconds(float stepSeconds) { this.simulationStepSeconds = Math.max(0.0001f, stepSeconds); }

    public void setEvaporationMultiplier(float multiplier) {
        this.evaporationMultiplier = Math.max(0.1f, Math.min(5.0f, multiplier));
    }

    // Terrain awareness
    private Terrarium terrarium;
    private int maxHeightAboveGround = 10;
    private BiPredicate<Integer, TerrariumCell> passabilityChecker;

    private static class PheromoneEntry {
        final float[] concentrations = new float[PHEROMONE_TYPES];
        final long[] lastUpdatedTick = new long[PHEROMONE_TYPES];

        synchronized void update(int type, float newConc, long tick) {
            concentrations[type] = newConc;
            lastUpdatedTick[type] = tick;
        }

        synchronized float getConcentration(int type) {
            return concentrations[type];
        }

        synchronized long getLastUpdatedTick(int type) {
            return lastUpdatedTick[type];
        }
    }

    public SparsePheromoneGrid(int width, int height, int depth) {
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.grid = new ConcurrentHashMap<>();

        this.halfLifeSeconds = DEFAULT_HALF_LIFE_SECONDS.clone();
        this.diffusionRate = DEFAULT_DIFFUSION_RATE.clone();
        this.passabilityChecker = (type, cell) -> cell.isPassable();
    }

    public void setTerrarium(Terrarium terrarium) {
        this.terrarium = terrarium;
    }

    public void setMaxHeightAboveGround(int layers) {
        this.maxHeightAboveGround = Math.max(1, layers);
    }

    public void setPassabilityChecker(BiPredicate<Integer, TerrariumCell> checker) {
        this.passabilityChecker = checker;
    }

    private boolean isPassable(int x, int y, int z, int pheromoneType) {
        if (terrarium == null)
            return true;
        if (!inBounds(x, y, z))
            return false;

        TerrariumCell cell = terrarium.getCell(x, y, z);
        if (!passabilityChecker.test(pheromoneType, cell))
            return false;

        // Check height limit above ground
        if (maxHeightAboveGround > 0) {
            int groundY = findGroundLevel(x, y, z);
            if (y - groundY > maxHeightAboveGround)
                return false;
        }
        return true;
    }

    private int findGroundLevel(int x, int startY, int z) {
        for (int y = startY; y >= 0; y--) {
            TerrariumCell cell = terrarium.getCell(x, y, z);
            if (!cell.isPassable())
                return y;
        }
        return 0;
    }

    public record PheromoneDeposit(int x, int y, int z, int type, float amount) {}

    public void deposit(int x, int y, int z, int type, float amount) {
        if (!inBounds(x, y, z) || type < 0 || type >= PHEROMONE_TYPES)
            return;
        if (!isPassable(x, y, z, type))
            return;

        long key = Morton3D.encode(x, y, z);
        grid.compute(key, (k, entry) -> {
            if (entry == null)
                entry = new PheromoneEntry();
            float decayed = computeDecay(entry.getConcentration(type), entry.getLastUpdatedTick(type), type);
            entry.update(type, Math.min(1.0f, decayed + amount), currentTick);
            return entry;
        });

        if (terrarium != null) {
            TerrariumCell cell = terrarium.getCell(x, y, z);
            if (cell != null) {
                float[] p = cell.pheromones();
                if (p != null && type >= 0 && type < p.length) {
                    p[type] = Math.min(1.0f, p[type] + amount);
                    terrarium.setCell(cell);
                }
            }
        }
    }

    public void depositBatch(java.util.Collection<PheromoneDeposit> deposits) {
        if (deposits == null || deposits.isEmpty())
            return;

        java.util.Map<Long, java.util.Map<Integer, Float>> grouped = new java.util.HashMap<>();
        for (PheromoneDeposit d : deposits) {
            if (!inBounds(d.x(), d.y(), d.z()) || d.type() < 0 || d.type() >= PHEROMONE_TYPES)
                continue;
            if (!isPassable(d.x(), d.y(), d.z(), d.type()))
                continue;

            long key = Morton3D.encode(d.x(), d.y(), d.z());
            grouped.computeIfAbsent(key, k -> new java.util.HashMap<>())
                    .merge(d.type(), d.amount(), Float::sum);
        }

        grouped.forEach((key, typeMap) -> {
            grid.compute(key, (k, entry) -> {
                if (entry == null)
                    entry = new PheromoneEntry();
                for (java.util.Map.Entry<Integer, Float> e : typeMap.entrySet()) {
                    int type = e.getKey();
                    float decayed = computeDecay(entry.getConcentration(type), entry.getLastUpdatedTick(type), type);
                    entry.update(type, Math.min(1.0f, decayed + e.getValue()), currentTick);
                }
                return entry;
            });
        });
    }

    public float read(int x, int y, int z, int type) {
        if (!inBounds(x, y, z) || type < 0 || type >= PHEROMONE_TYPES)
            return 0f;
        long key = Morton3D.encode(x, y, z);
        PheromoneEntry entry = grid.get(key);
        if (entry == null)
            return 0f;
        return computeDecay(entry.getConcentration(type), entry.getLastUpdatedTick(type), type);
    }

    public void clearAt(int x, int y, int z) {
        if (!inBounds(x, y, z)) return;
        long key = Morton3D.encode(x, y, z);
        grid.remove(key);
    }

    public float getIntensity(int x, int y, int z, int type) {
        return read(x, y, z, type);
    }

    public float[] readAll(int x, int y, int z) {
        if (!inBounds(x, y, z))
            return null;
        long key = Morton3D.encode(x, y, z);
        PheromoneEntry entry = grid.get(key);
        if (entry == null)
            return null;

        float[] result = new float[PHEROMONE_TYPES];
        for (int t = 0; t < PHEROMONE_TYPES; t++) {
            result[t] = computeDecay(entry.getConcentration(t), entry.getLastUpdatedTick(t), t);
        }
        return result;
    }

    private float computeDecay(float original, long depositTick, int type) {
        if (original <= 0)
            return 0f;
        long elapsedTicks = currentTick - depositTick;
        if (elapsedTicks <= 0)
            return original;
        double elapsedSeconds = elapsedTicks * (double) simulationStepSeconds;
        double effectiveHalfLifeSec = (double) halfLifeSeconds[type] / (double) evaporationMultiplier;
        return (float) (original * Math.pow(0.5, elapsedSeconds / effectiveHalfLifeSec));
    }

    public void tick() {
        currentTick++;
        if (currentTick % 100 == 0)
            prune();
        syncToTerrarium();
    }

    public void syncToTerrarium() {
        if (terrarium == null) return;
        grid.forEach((key, entry) -> {
            int[] pos = Morton3D.decode(key);
            int x = pos[0], y = pos[1], z = pos[2];
            TerrariumCell cell = terrarium.getCell(x, y, z);
            if (cell != null) {
                float[] p = cell.pheromones();
                if (p != null) {
                    for (int t = 0; t < PHEROMONE_TYPES; t++) {
                        p[t] = computeDecay(entry.getConcentration(t), entry.getLastUpdatedTick(t), t);
                    }
                    terrarium.setCell(cell);
                }
            }
        });
    }

    public void prune() {
        // Optimized pruning using removeIf (internal parallelization if supported by
        // map, or at least optimized)
        grid.entrySet().removeIf(entry -> {
            PheromoneEntry val = entry.getValue();
            for (int t = 0; t < PHEROMONE_TYPES; t++) {
                if (computeDecay(val.concentrations[t], val.lastUpdatedTick[t], t) >= PRUNE_THRESHOLD) {
                    return false; // Keep if any type has significant concentration
                }
            }
            return true; // Remove if all decayed
        });
    }

    /**
     * Terrain-aware diffusion: only spreads to passable neighbors.
     * Uses parallel two-pass approach to avoid bias and maximize performance.
     */
    /**
     * Terrain-aware diffusion: only spreads to passable neighbors.
     * Uses parallel two-pass approach to avoid bias and maximize performance.
     * Optimized to reduce GC pressure by accumulating deltas instead of creating
     * objects.
     */
    public void diffuse() {
        // Pass 1: Calculate spread updates in parallel and accumulate them
        ConcurrentHashMap<Long, float[]> deltas = new ConcurrentHashMap<>();

        grid.entrySet().parallelStream().forEach(e -> {
            Long key = e.getKey();
            PheromoneEntry entry = e.getValue();
            int[] coords = Morton3D.decode(key);
            int x = coords[0], y = coords[1], z = coords[2];

            for (int t = 0; t < PHEROMONE_TYPES; t++) {
                float conc = computeDecay(entry.concentrations[t], entry.lastUpdatedTick[t], t);
                if (conc < PRUNE_THRESHOLD)
                    continue;

                int passable = countPassableNeighbors(x, y, z, t);
                if (passable == 0)
                    continue;

                float spreadAmount = conc * diffusionRate[t];
                float amountPerNeighbor = spreadAmount / passable;

                if (amountPerNeighbor >= PRUNE_THRESHOLD) {
                    accumulateSpread(deltas, x - 1, y, z, t, amountPerNeighbor);
                    accumulateSpread(deltas, x + 1, y, z, t, amountPerNeighbor);
                    accumulateSpread(deltas, x, y - 1, z, t, amountPerNeighbor);
                    accumulateSpread(deltas, x, y + 1, z, t, amountPerNeighbor);
                    accumulateSpread(deltas, x, y, z - 1, t, amountPerNeighbor);
                    accumulateSpread(deltas, x, y, z + 1, t, amountPerNeighbor);
                }
            }
        });

        // Pass 2: Apply updates
        deltas.entrySet().parallelStream().forEach(e -> {
            long key = e.getKey();
            float[] amounts = e.getValue();
            int[] pos = Morton3D.decode(key);
            for (int t = 0; t < PHEROMONE_TYPES; t++) {
                if (amounts[t] > 0) {
                    deposit(pos[0], pos[1], pos[2], t, amounts[t]);
                }
            }
        });
    }

    private void accumulateSpread(ConcurrentHashMap<Long, float[]> deltas, int x, int y, int z, int type,
            float amount) {
        if (isPassable(x, y, z, type)) {
            long key = Morton3D.encode(x, y, z);
            deltas.compute(key, (k, v) -> {
                if (v == null)
                    v = new float[PHEROMONE_TYPES];
                v[type] += amount;
                return v;
            });
        }
    }

    private int countPassableNeighbors(int x, int y, int z, int type) {
        int count = 0;
        if (isPassable(x - 1, y, z, type))
            count++;
        if (isPassable(x + 1, y, z, type))
            count++;
        if (isPassable(x, y - 1, z, type))
            count++;
        if (isPassable(x, y + 1, z, type))
            count++;
        if (isPassable(x, y, z - 1, type))
            count++;
        if (isPassable(x, y, z + 1, type))
            count++;
        return count;
    }

    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    public int getActiveEntryCount() {
        return grid.size();
    }

    public void clear() {
        grid.clear();
    }

    public long getCurrentTick() {
        return currentTick;
    }
    public void setHalfLifeSeconds(int type, float seconds) {
        if (type >= 0 && type < PHEROMONE_TYPES)
            halfLifeSeconds[type] = Math.max(0.01f, seconds);
    }

    public void setDiffusionRate(int type, float rate) {
        if (type >= 0 && type < PHEROMONE_TYPES)
            diffusionRate[type] = Math.max(0, Math.min(1, rate));
    }

    // === Serialization Support ===

    public Map<Long, float[]> getAllEntries() {
        Map<Long, float[]> snapshot = new java.util.HashMap<>();
        for (Map.Entry<Long, PheromoneEntry> e : grid.entrySet()) {
            int[] pos = Morton3D.decode(e.getKey());
            snapshot.put(e.getKey(), readAll(pos[0], pos[1], pos[2]));
        }
        return snapshot;
    }

    public void putEntry(long key, float[] values) {
        PheromoneEntry entry = new PheromoneEntry();
        System.arraycopy(values, 0, entry.concentrations, 0, PHEROMONE_TYPES);
        for (int t = 0; t < PHEROMONE_TYPES; t++)
            entry.lastUpdatedTick[t] = currentTick;
        grid.put(key, entry);
    }

    public byte[] serialize() {
        // Simple serialization of internal grid state
        // Needed for SimulationSnapshot
        // Format: [Version(int), Count(int), [Key(long), Intensities(float[8])...]]
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);

            dos.writeInt(1); // Version
            dos.writeInt(grid.size());

            for (Map.Entry<Long, PheromoneEntry> entry : grid.entrySet()) {
                dos.writeLong(entry.getKey());
                // Compute current values with decay
                float[] current = decay(entry.getValue(), currentTick);
                for (float val : current) {
                    dos.writeFloat(val);
                }
            }
            return baos.toByteArray();
        } catch (java.io.IOException e) {
            return new byte[0];
        }
    }

    // Helper to compute decayed values without mutating state (for serialization)
    private float[] decay(PheromoneEntry entry, long time) {
        float[] result = new float[PHEROMONE_TYPES];
        for (int i = 0; i < PHEROMONE_TYPES; i++) {
            result[i] = computeDecay(entry.concentrations[i], entry.lastUpdatedTick[i], i);
            if (result[i] < PRUNE_THRESHOLD)
                result[i] = 0f;
        }
        return result;
    }
}
