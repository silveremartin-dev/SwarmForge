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
        this.mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public byte[] serializeCells(Terrarium terrarium) throws IOException {
        if (terrarium == null) return new byte[0];
        List<TerrariumCell> nonAir = terrarium.getAllCells().stream()
                .filter(c -> c.material() != TerrariumCell.Material.AIR)
                .limit(20000)
                .toList();
        return mapper.writeValueAsBytes(nonAir);
    }

    public byte[] serializeColonies(Collection<Colony> colonies) throws IOException {
        if (colonies == null || colonies.isEmpty()) return new byte[0];
        try (java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
             java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(new ArrayList<>(colonies));
            return bos.toByteArray();
        }
    }

    private static final int MAX_PAYLOAD_BYTES = 64 * 1024 * 1024; // 64 MB security limit

    private static void checkPayloadSize(byte[] data) {
        if (data != null && data.length > MAX_PAYLOAD_BYTES) {
            throw new IllegalArgumentException("Payload size exceeds maximum allowed limit of " + MAX_PAYLOAD_BYTES + " bytes.");
        }
    }

    public Collection<TerrariumCell> deserializeCells(byte[] data) throws IOException {
        if (data == null || data.length == 0)
            return new ArrayList<>();
        checkPayloadSize(data);
        return mapper.readValue(data, new com.fasterxml.jackson.core.type.TypeReference<List<TerrariumCell>>() {
        });
    }

    @SuppressWarnings("unchecked")
    public Collection<Colony> deserializeColonies(byte[] data) throws IOException {
        if (data == null || data.length == 0)
            return new ArrayList<>();
        checkPayloadSize(data);
        try (java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(data);
             java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis)) {
            java.io.ObjectInputFilter filter = java.io.ObjectInputFilter.Config.createFilter(
                "org.swarmforge.**;java.lang.*;java.util.**;java.util.concurrent.**;java.util.concurrent.atomic.**;[F;[I;[B;[Z;[Ljava.lang.String;;!*"
            );
            ois.setObjectInputFilter(filter);
            return (Collection<Colony>) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    public byte[] serializeIndividuals(Collection<Individual> individuals) throws IOException {
        return new byte[0];
    }
}
