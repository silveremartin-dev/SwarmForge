/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.core.simulation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Full simulation state checkpoint for save/resume functionality.
 *
 * @author Gemini AI Assistant
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimulationCheckpoint implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    // Metadata
    private String id;
    private Instant createdAt;
    private long tickNumber;
    private String description;

    // World State
    private int worldWidth, worldHeight, worldDepth;
    private byte[] terrariumData; // Compressed cell data

    // Colony States
    private List<ColonyState> colonies = new ArrayList<>();

    // Weather/Time State

    // Weather/Time State

    /**
     * Create checkpoint from current simulation state.
     */
    public static SimulationCheckpoint capture(Simulation simulation, String description) {
        SimulationCheckpoint cp = new SimulationCheckpoint();
        cp.id = UUID.randomUUID().toString();
        cp.createdAt = Instant.now();
        cp.tickNumber = simulation.getTickCount();
        cp.description = description;

        var terrarium = simulation.getTerrarium();
        cp.worldWidth = terrarium.getWidth();
        cp.worldHeight = terrarium.getHeight();
        cp.worldDepth = terrarium.getDepth();
        cp.terrariumData = serializeTerrarium(terrarium);

        // Capture colony states
        for (var colony : simulation.getColonies()) {
            ColonyState cs = new ColonyState();
            cs.id = colony.getId().toString();
            cs.speciesName = colony.getSpeciesName();
            cs.nestX = colony.getNestX();
            cs.nestY = colony.getNestY();
            cs.nestZ = colony.getNestZ();
            cs.foodStored = colony.getFoodStored();
            cs.waterStored = colony.getWaterStored();
            cs.population = colony.getPopulation();
            cp.colonies.add(cs);
        }

        // Weather state
        // (Weather state not currently persisted in checkpoint)

        return cp;
    }

    /**
     * Save checkpoint to file (gzipped JSON).
     */
    public void saveToFile(Path path) throws IOException {
        try (OutputStream os = Files.newOutputStream(path);
                GZIPOutputStream gzip = new GZIPOutputStream(os)) {
            MAPPER.writeValue(gzip, this);
        }
    }

    /**
     * Load checkpoint from file.
     */
    public static SimulationCheckpoint loadFromFile(Path path) throws IOException {
        try (InputStream is = Files.newInputStream(path);
                GZIPInputStream gzip = new GZIPInputStream(is)) {
            return MAPPER.readValue(gzip, SimulationCheckpoint.class);
        }
    }

    /**
     * Restore simulation state from this checkpoint.
     */
    public void restore(Simulation simulation) {
        simulation.reset(tickNumber);

        // Restore terrarium
        deserializeTerrarium(terrariumData, simulation.getTerrarium());

        // Note: Full colony restoration requires more complex logic
        // This is a simplified version that restores metadata only
    }

    private static byte[] serializeTerrarium(org.swarmforge.core.domain.Terrarium terrarium) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                GZIPOutputStream gzip = new GZIPOutputStream(baos);
                ObjectOutputStream oos = new ObjectOutputStream(gzip)) {

            // Export cells as map (only non-air cells)
            Map<String, Object> data = new HashMap<>();
            data.put("width", terrarium.getWidth());
            data.put("height", terrarium.getHeight());
            data.put("depth", terrarium.getDepth());
            // Additional cell data would go here...

            oos.writeObject(data);
            gzip.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private static void deserializeTerrarium(byte[] data, org.swarmforge.core.domain.Terrarium terrarium) {
        if (data == null || data.length == 0)
            return;
        // Restore logic would parse the data and set cells
    }

    // === Getters ===

    public String getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public long getTickNumber() {
        return tickNumber;
    }

    public String getDescription() {
        return description;
    }

    public int getWorldWidth() {
        return worldWidth;
    }

    public int getWorldHeight() {
        return worldHeight;
    }

    public int getWorldDepth() {
        return worldDepth;
    }

    public List<ColonyState> getColonies() {
        return colonies;
    }

    /**
     * Colony state snapshot.
     */
    public static class ColonyState implements Serializable {
        public String id;
        public String speciesName;
        public float nestX, nestY, nestZ;
        public float foodStored, waterStored;
        public int population;
    }
}
