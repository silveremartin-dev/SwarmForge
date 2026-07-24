/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.swarmforge.core.domain.Colony;
import org.swarmforge.core.domain.Individual;
import org.swarmforge.core.domain.Terrarium;
import org.swarmforge.core.domain.TerrariumCell;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Helper class to serialize/deserialize simulation components.
 * Uses Jackson ObjectMapper.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class SimulationSerializer {

    private final ObjectMapper mapper;

    public SimulationSerializer() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        // this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
        // false);
    }

    public byte[] serializeCells(Terrarium terrarium) throws IOException {
        // Only serialize non-air cells to save space (handled by getCell logic usually,
        // but here we take values)
        // Note: TerrariumCell is a record, so it serializes fine.
        return mapper.writeValueAsBytes(terrarium.getAllCells());
    }

    public byte[] serializeColonies(Collection<Colony> colonies) throws IOException {
        return mapper.writeValueAsBytes(colonies);
    }

    public Collection<TerrariumCell> deserializeCells(byte[] data) throws IOException {
        if (data == null || data.length == 0)
            return new ArrayList<>();
        return mapper.readValue(data, new com.fasterxml.jackson.core.type.TypeReference<List<TerrariumCell>>() {
        });
    }

    public Collection<Colony> deserializeColonies(byte[] data) throws IOException {
        if (data == null || data.length == 0)
            return new ArrayList<>();
        return mapper.readValue(data, new com.fasterxml.jackson.core.type.TypeReference<List<Colony>>() {
        });
    }

    // Individuals are inside colonies usually, so serializeColonies might cover it.
    // But CheckpointRepository has separate individuals_data column?
    // Let's assume individuals are serialized WITHIN colonies for now.
    public byte[] serializeIndividuals(Collection<Individual> individuals) throws IOException {
        return mapper.writeValueAsBytes(individuals);
    }
}
