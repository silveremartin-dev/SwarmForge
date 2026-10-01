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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Repository for persisting {@link Colony} state to the active database backend.
 *
 * <p>Supports two SQL dialects transparently:
 * <ul>
 *   <li><b>PostgreSQL</b> – uses native {@code INSERT ... ON CONFLICT (id) DO UPDATE SET ... EXCLUDED.*} upsert.</li>
 *   <li><b>H2</b> (in-memory offline fallback) – uses standard SQL {@code MERGE INTO} because H2 does not
 *       implement the {@code EXCLUDED} pseudo-table extension even in {@code MODE=PostgreSQL}.</li>
 * </ul>
 *
 * <p>The dialect is detected once at construction time via {@link DatabaseMetaData#getDatabaseProductName()}.
 */
public class ColonyRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ColonyRepository.class);

    private final Connection connection;
    /** {@code true} when the active connection targets H2 rather than PostgreSQL. */
    private final boolean isH2;

    // ---------------------------------------------------------------------------
    // PostgreSQL upsert  (INSERT … ON CONFLICT)
    // ---------------------------------------------------------------------------
    private static final String SQL_UPSERT_PG =
            "INSERT INTO colonies (id, owner_id, name, biomass, age, wins, data) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (id) DO UPDATE SET " +
            "  owner_id = EXCLUDED.owner_id, " +
            "  name     = EXCLUDED.name, " +
            "  biomass  = EXCLUDED.biomass, " +
            "  age      = EXCLUDED.age, " +
            "  wins     = EXCLUDED.wins, " +
            "  data     = EXCLUDED.data";

    // ---------------------------------------------------------------------------
    // H2 upsert  (MERGE INTO … USING VALUES)
    // H2 supports MERGE INTO target USING (SELECT …) AS src ON … WHEN MATCHED …
    // ---------------------------------------------------------------------------
    private static final String SQL_UPSERT_H2 =
            "MERGE INTO colonies (id, owner_id, name, biomass, age, wins, data) " +
            "KEY (id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    public ColonyRepository(Connection connection) {
        this.connection = connection;
        this.isH2 = detectH2(connection);
        initTable();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Detects whether the underlying JDBC connection targets an H2 database.
     *
     * @param conn active JDBC connection (may be {@code null})
     * @return {@code true} if H2, {@code false} for PostgreSQL or unknown
     */
    private static boolean detectH2(Connection conn) {
        if (conn == null) return false;
        try {
            DatabaseMetaData meta = conn.getMetaData();
            String product = meta.getDatabaseProductName();
            return product != null && product.toUpperCase().contains("H2");
        } catch (SQLException e) {
            LOG.warn("Could not detect database dialect, defaulting to PostgreSQL syntax: {}", e.getMessage());
            return false;
        }
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
            LOG.error("Failed to initialize colonies table: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Public API
    // ------------------------------------------------------------------

    /**
     * Persists or updates a colony record using a dialect-aware upsert.
     *
     * @param colony  the colony whose state is to be persisted
     * @param ownerId identifier of the player / session owning this colony
     */
    public void save(Colony colony, String ownerId) {
        if (connection == null)
            return;

        String sql = isH2 ? SQL_UPSERT_H2 : SQL_UPSERT_PG;
        LOG.debug("Using {} upsert dialect for colony {}", isH2 ? "H2" : "PostgreSQL", colony.getId());

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, colony.getId().toString());
            ps.setString(2, ownerId);
            ps.setString(3, "Colony " + colony.getId().toString().substring(0, 8));
            ps.setFloat(4, colony.getTotalBiomass());
            ps.setInt(5, colony.getAgeInTicks());
            ps.setInt(6, 0); // Wins logic tbd

            // Serialize colony state to a compact binary blob
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(colony);
                ps.setBytes(7, bos.toByteArray());
            }

            ps.executeUpdate();
            LOG.info("Saved colony: {}", colony.getId());
        } catch (SQLException | IOException e) {
            LOG.error("Failed to save colony {}: {}", colony.getId(), e.getMessage(), e);
        }
    }

    /**
     * Loads and deserializes a colony by its UUID string.
     *
     * @param colonyId UUID string of the target colony
     * @return the deserialized {@link Colony}, or {@code null} if not found or on error
     */
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
                        // Security hardening: restrict deserialization to trusted SwarmForge and standard Java types only
                        ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
                            "org.swarmforge.**;java.lang.*;java.util.**;java.util.concurrent.**;" +
                            "java.util.concurrent.atomic.**;[F;[I;[B;[Z;[Ljava.lang.String;;!*"
                        );
                        ois.setObjectInputFilter(filter);
                        return (Colony) ois.readObject();
                    }
                }
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            LOG.error("Failed to load colony {}: {}", colonyId, e.getMessage(), e);
        }
        return null;
    }
}

