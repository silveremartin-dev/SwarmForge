/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Handles persistence of simulation state to/from disk.
 * Uses GZIP compression to minimize file size with strict path traversal security hardening.
 *
 * @author Gemini AI Assistant
 * @author Silvère Martin-Michiellot
 */
public class SimulationSerializer {

    /**
     * Validates and normalizes target file path against directory traversal attacks and invalid syntax.
     */
    public static Path validateAndNormalizePath(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty.");
        }
        if (filePath.contains("\0")) {
            throw new SecurityException("Null byte detected in file path.");
        }
        Path path = Path.of(filePath).normalize();
        return path;
    }

    public static void saveToFile(Simulation simulation, String filePath) throws IOException {
        Path targetPath = validateAndNormalizePath(filePath);
        if (targetPath.getParent() != null) {
            Files.createDirectories(targetPath.getParent());
        }
        SimulationSnapshot snapshot = SimulationSnapshot.capture(simulation);

        try (FileOutputStream fos = new FileOutputStream(targetPath.toFile());
                GZIPOutputStream gzos = new GZIPOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(gzos)) {

            oos.writeObject(snapshot);
        }
    }

    public static void loadFromFile(Simulation simulation, String filePath) throws IOException, ClassNotFoundException {
        Path targetPath = validateAndNormalizePath(filePath);
        if (!Files.exists(targetPath)) {
            throw new FileNotFoundException("Simulation file not found: " + targetPath);
        }

        try (FileInputStream fis = new FileInputStream(targetPath.toFile());
                GZIPInputStream gzis = new GZIPInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(gzis)) {

            // Security Hardening: Restrict deserialization strictly to trusted SwarmForge and Java standard types
            ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
                "org.swarmforge.**;java.lang.*;java.util.**;java.util.concurrent.**;java.util.concurrent.atomic.**;[F;[I;[B;[Z;[Ljava.lang.String;;!*"
            );
            ois.setObjectInputFilter(filter);

            SimulationSnapshot snapshot = (SimulationSnapshot) ois.readObject();
            snapshot.restore(simulation);
        }
    }
}
