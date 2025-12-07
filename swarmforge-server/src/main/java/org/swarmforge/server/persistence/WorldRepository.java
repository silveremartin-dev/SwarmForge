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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Repository for world/terrarium persistence.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class WorldRepository {

    private static final Logger LOG = Logger.getLogger(WorldRepository.class.getName());
    private final Connection connection;

    public WorldRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Save a new world.
     */
    public UUID save(String name, int width, int height, int depth,
            double latitude, double longitude, double altitude) throws SQLException {
        UUID id = UUID.randomUUID();
        String sql = """
                INSERT INTO worlds (id, name, width, height, depth, latitude, longitude, altitude)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            stmt.setString(2, name);
            stmt.setInt(3, width);
            stmt.setInt(4, height);
            stmt.setInt(5, depth);
            stmt.setDouble(6, latitude);
            stmt.setDouble(7, longitude);
            stmt.setDouble(8, altitude);
            stmt.executeUpdate();
        }
        LOG.info("Saved world: " + name + " (" + id + ")");
        return id;
    }

    /**
     * Find world by ID.
     */
    public Optional<WorldRecord> findById(UUID id) throws SQLException {
        String sql = "SELECT * FROM worlds WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    /**
     * List all worlds.
     */
    public List<WorldRecord> findAll() throws SQLException {
        List<WorldRecord> worlds = new ArrayList<>();
        String sql = "SELECT * FROM worlds ORDER BY created_at DESC";
        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                worlds.add(mapRow(rs));
            }
        }
        return worlds;
    }

    /**
     * Delete world by ID.
     */
    public boolean delete(UUID id) throws SQLException {
        String sql = "DELETE FROM worlds WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setObject(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    private WorldRecord mapRow(ResultSet rs) throws SQLException {
        return new WorldRecord(
                rs.getObject("id", UUID.class),
                rs.getString("name"),
                rs.getInt("width"),
                rs.getInt("height"),
                rs.getInt("depth"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getDouble("altitude"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("updated_at"));
    }

    public record WorldRecord(
            UUID id, String name,
            int width, int height, int depth,
            double latitude, double longitude, double altitude,
            Timestamp createdAt, Timestamp updatedAt) {
    }
}
