/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Handles persistence of simulation state to/from disk.
 * Uses GZIP compression to minimize file size.
 *
 * @author Gemini AI Assistant
 */
public class SimulationSerializer {

    public static void saveToFile(Simulation simulation, String filePath) throws IOException {
        SimulationSnapshot snapshot = SimulationSnapshot.capture(simulation);

        try (FileOutputStream fos = new FileOutputStream(filePath);
                GZIPOutputStream gzos = new GZIPOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(gzos)) {

            oos.writeObject(snapshot);
        }
    }

    public static void loadFromFile(Simulation simulation, String filePath) throws IOException, ClassNotFoundException {
        try (FileInputStream fis = new FileInputStream(filePath);
                GZIPInputStream gzis = new GZIPInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(gzis)) {

            SimulationSnapshot snapshot = (SimulationSnapshot) ois.readObject();
            snapshot.restore(simulation);
        }
    }
}
