/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Automated Periodic Checkpointing & FIFO Disk Retention Manager.
 * Rotates and prunes automated snapshots while preserving pinned/named manual checkpoints.
 * Enforces hard storage quotas to prevent disk exhaustion.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class CheckpointRetentionManager {

    private static final Logger LOGGER = Logger.getLogger(CheckpointRetentionManager.class.getName());

    public static final int DEFAULT_MAX_AUTO_CHECKPOINTS = 10;
    public static final long DEFAULT_AUTO_CHECKPOINT_INTERVAL_TICKS = 1000L;
    public static final long DEFAULT_MAX_STORAGE_BYTES = 500L * 1024L * 1024L; // 500 MB quota

    private final Path baseStorageDirectory;
    private int maxAutoCheckpoints = DEFAULT_MAX_AUTO_CHECKPOINTS;
    private long autoCheckpointIntervalTicks = DEFAULT_AUTO_CHECKPOINT_INTERVAL_TICKS;
    private long maxStorageQuotaBytes = DEFAULT_MAX_STORAGE_BYTES;
    private long lastCheckpointTick = 0L;

    public CheckpointRetentionManager() {
        this(Paths.get(System.getProperty("user.home"), ".swarmforge", "checkpoints"));
    }

    public CheckpointRetentionManager(Path storageDirectory) {
        this.baseStorageDirectory = storageDirectory;
        try {
            Files.createDirectories(this.baseStorageDirectory);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to create checkpoint directory: " + storageDirectory, e);
        }
    }

    public Path getBaseStorageDirectory() {
        return baseStorageDirectory;
    }

    public int getMaxAutoCheckpoints() {
        return maxAutoCheckpoints;
    }

    public void setMaxAutoCheckpoints(int maxAutoCheckpoints) {
        this.maxAutoCheckpoints = Math.max(1, maxAutoCheckpoints);
    }

    public long getAutoCheckpointIntervalTicks() {
        return autoCheckpointIntervalTicks;
    }

    public void setAutoCheckpointIntervalTicks(long intervalTicks) {
        this.autoCheckpointIntervalTicks = Math.max(10L, intervalTicks);
    }

    public long getMaxStorageQuotaBytes() {
        return maxStorageQuotaBytes;
    }

    public void setMaxStorageQuotaBytes(long maxBytes) {
        this.maxStorageQuotaBytes = Math.max(1024L * 1024L, maxBytes);
    }

    /**
     * Checks if a tick requires an automatic checkpoint, and executes it asynchronously.
     */
    public boolean checkAndExecuteAutoCheckpoint(Simulation simulation, long currentTick) {
        if (simulation == null || currentTick <= 0) return false;
        if (currentTick - lastCheckpointTick < autoCheckpointIntervalTicks) {
            return false;
        }

        lastCheckpointTick = currentTick;
        Thread.startVirtualThread(() -> {
            try {
                performAutoCheckpoint(simulation, currentTick);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed executing auto-checkpoint at tick " + currentTick, e);
            }
        });
        return true;
    }

    /**
     * Creates an automated checkpoint and prunes old automated files.
     */
    public File performAutoCheckpoint(Simulation simulation, long currentTick) throws IOException {
        String simId = "sim_main";
        Path simDir = baseStorageDirectory.resolve(simId);
        Files.createDirectories(simDir);

        String filename = String.format("auto_checkpoint_tick_%08d_%d.sfcp.gz", currentTick, System.currentTimeMillis());
        Path targetPath = simDir.resolve(filename);

        SimulationCheckpoint checkpoint = simulation.createCheckpoint("auto_tick_" + currentTick);
        byte[] data = checkpoint.toCompressedBytes();
        Files.write(targetPath, data);

        pruneOldCheckpoints(simDir);
        return targetPath.toFile();
    }

    /**
     * FIFO pruning algorithm: Removes oldest auto checkpoints if count > maxAutoCheckpoints or quota exceeded.
     */
    public synchronized void pruneOldCheckpoints(Path dir) {
        if (!Files.exists(dir)) return;

        File[] files = dir.toFile().listFiles((d, name) -> name.startsWith("auto_checkpoint_") && name.endsWith(".sfcp.gz"));
        if (files == null || files.length == 0) return;

        // Sort by last modified timestamp ascending (oldest first)
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));

        // 1. Retention count limit
        int excess = files.length - maxAutoCheckpoints;
        for (int i = 0; i < excess; i++) {
            if (files[i].delete()) {
                LOGGER.info("Pruned old auto-checkpoint: " + files[i].getName());
            }
        }

        // 2. Storage quota enforcement
        long totalSize = 0;
        File[] remaining = dir.toFile().listFiles((d, name) -> name.endsWith(".sfcp.gz"));
        if (remaining != null) {
            for (File f : remaining) {
                totalSize += f.length();
            }
            if (totalSize > maxStorageQuotaBytes) {
                // Delete oldest auto-checkpoints until within quota
                for (File f : remaining) {
                    if (totalSize <= maxStorageQuotaBytes) break;
                    if (f.getName().startsWith("auto_checkpoint_")) {
                        long sz = f.length();
                        if (f.delete()) {
                            totalSize -= sz;
                            LOGGER.info("Quota enforcement pruned: " + f.getName());
                        }
                    }
                }
            }
        }
    }
}
