/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.persistence;

import java.sql.*;
import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.io.*;
import java.util.zip.*;

/**
 * Repository for simulation checkpoints (save/load).
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class CheckpointRepository {

    private final Connection connection;

    public CheckpointRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Save a checkpoint with compressed data.
     */
    public UUID save(UUID worldId, long tick, String name,
            byte[] cellsData, byte[] coloniesData, byte[] individualsData)
            throws SQLException, IOException {
        UUID id = UUID.randomUUID();
        String sql = """
                INSERT INTO checkpoints (id, world_id, tick, name, cells_data, colonies_data, individuals_data)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.setObject(2, worldId);
            stmt.setLong(3, tick);
            stmt.setString(4, name);
            stmt.setBytes(5, compress(cellsData));
            stmt.setBytes(6, compress(coloniesData));
            stmt.setBytes(7, compress(individualsData));
            stmt.executeUpdate();
        }
        return id;
    }

    /**
     * Load checkpoint by ID.
     */
    public Optional<CheckpointRecord> findById(UUID id) throws SQLException, IOException {
        String sql = "SELECT * FROM checkpoints WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(new CheckpointRecord(
                        rs.getObject("id", UUID.class),
                        rs.getObject("world_id", UUID.class),
                        rs.getLong("tick"),
                        rs.getString("name"),
                        decompress(rs.getBytes("cells_data")),
                        decompress(rs.getBytes("colonies_data")),
                        decompress(rs.getBytes("individuals_data")),
                        rs.getTimestamp("created_at")));
            }
        }
        return Optional.empty();
    }

    /**
     * List checkpoints for a world.
     */
    public List<CheckpointSummary> findByWorld(UUID worldId) throws SQLException {
        List<CheckpointSummary> list = new ArrayList<>();
        String sql = "SELECT id, tick, name, created_at FROM checkpoints WHERE world_id = ? ORDER BY tick DESC";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, worldId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new CheckpointSummary(
                        rs.getObject("id", UUID.class),
                        rs.getLong("tick"),
                        rs.getString("name"),
                        rs.getTimestamp("created_at")));
            }
        }
        return list;
    }

    private byte[] compress(byte[] data) throws IOException {
        if (data == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
            gzip.write(data);
        }
        return baos.toByteArray();
    }

    private byte[] decompress(byte[] data) throws IOException {
        if (data == null)
            return null;
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gzip = new GZIPInputStream(bais)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzip.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
        }
        return baos.toByteArray();
    }

    public record CheckpointRecord(
            UUID id, UUID worldId, long tick, String name,
            byte[] cellsData, byte[] coloniesData, byte[] individualsData,
            Timestamp createdAt) {
    }

    public record CheckpointSummary(UUID id, long tick, String name, Timestamp createdAt) {
    }
}
