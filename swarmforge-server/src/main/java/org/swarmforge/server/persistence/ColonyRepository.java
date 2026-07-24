/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.persistence;

import org.swarmforge.core.domain.Colony;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Repository for persisting Colony state to PostgreSQL.
 */
public class ColonyRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ColonyRepository.class);
    private final Connection connection;

    public ColonyRepository(Connection connection) {
        this.connection = connection;
        initTable();
    }

    private void initTable() {
        if (connection == null)
            return;
        try (var stmt = connection.createStatement()) {
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS colonies (
                            id VARCHAR(36) PRIMARY KEY,
                            owner_id VARCHAR(255),
                            name VARCHAR(255),
                            biomass FLOAT,
                            age INT,
                            wins INT DEFAULT 0,
                            data BYTEA
                        );
                    """);
        } catch (SQLException e) {
            LOG.error("Failed to initialize colonies table: " + e.getMessage());
        }
    }

    public void save(Colony colony, String ownerId) {
        if (connection == null)
            return;

        String sql = "INSERT INTO colonies (id, owner_id, name, biomass, age, wins, data) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (id) DO UPDATE SET " +
                "owner_id = EXCLUDED.owner_id, " +
                "name = EXCLUDED.name, " +
                "biomass = EXCLUDED.biomass, " +
                "age = EXCLUDED.age, " +
                "wins = EXCLUDED.wins, " +
                "data = EXCLUDED.data";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, colony.getId().toString());
            ps.setString(2, ownerId);
            ps.setString(3, "Colony " + colony.getId().toString().substring(0, 8)); // Mock name if missing
            ps.setFloat(4, colony.getTotalBiomass());
            ps.setInt(5, colony.getAgeInTicks());
            ps.setInt(6, 0); // Wins logic tbd

            // Serialize Data
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(colony);
                ps.setBytes(7, bos.toByteArray());
            }

            ps.executeUpdate();
            LOG.info("Saved colony: {}", colony.getId());
        } catch (SQLException | IOException e) {
            LOG.error("Failed to save colony: {}", e.getMessage(), e);
        }
    }

    public Colony load(String colonyId) {
        if (connection == null)
            return null;

        String sql = "SELECT data FROM colonies WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, colonyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    byte[] data = rs.getBytes("data");
                    try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
                            ObjectInputStream ois = new ObjectInputStream(bis)) {
                        return (Colony) ois.readObject();
                    }
                }
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            LOG.error("Failed to load colony: {}", e.getMessage(), e);
        }
        return null; // Not found or error
    }
}
