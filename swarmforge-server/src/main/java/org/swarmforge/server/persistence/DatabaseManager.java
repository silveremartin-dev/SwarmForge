/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database connection manager for PostgreSQL with automatic H2 In-Memory fallback.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class DatabaseManager {

    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());

    private final String pgJdbcUrl;
    private final String username;
    private final String password;
    private Connection connection;
    private boolean h2Fallback = false;

    public DatabaseManager(String host, int port, String database, String username, String password) {
        this.pgJdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;
    }

    /**
     * Connect to the database. Attempts PostgreSQL first; if unreachable, falls back to H2 in-memory.
     */
    public void connect() throws SQLException {
        try {
            DriverManager.setLoginTimeout(2); // Short timeout for PostgreSQL connection attempt
            connection = DriverManager.getConnection(pgJdbcUrl, username, password);
            h2Fallback = false;
            LOG.info("✓ Connected to PostgreSQL: " + pgJdbcUrl);
        } catch (SQLException e) {
            LOG.warning("⚠️ PostgreSQL unreachable (" + e.getMessage() + "). Falling back to H2 In-Memory database...");
            connectH2InMemory();
        }
        initSchema();
    }

    private void connectH2InMemory() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "H2 Driver not found on classpath", e);
        }
        String h2Url = "jdbc:h2:mem:swarmforge_db;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE";
        connection = DriverManager.getConnection(h2Url, "sa", "");
        h2Fallback = true;
        LOG.info("✓ Connected to H2 In-Memory Database (Offline / Standalone Mode)");
    }

    /**
     * Initializes core database tables if they do not exist.
     */
    private void initSchema() {
        if (connection == null) return;
        String ddl = """
            CREATE TABLE IF NOT EXISTS worlds (
                id UUID PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                width INT NOT NULL,
                height INT NOT NULL,
                depth INT NOT NULL,
                latitude DOUBLE PRECISION,
                longitude DOUBLE PRECISION,
                altitude DOUBLE PRECISION,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS colonies (
                id UUID PRIMARY KEY,
                world_id UUID REFERENCES worlds(id) ON DELETE CASCADE,
                species VARCHAR(255) NOT NULL,
                nest_x REAL NOT NULL,
                nest_y REAL NOT NULL,
                nest_z REAL NOT NULL,
                food_stored REAL DEFAULT 0,
                water_stored REAL DEFAULT 0,
                total_born INT DEFAULT 0,
                total_died INT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            
            CREATE TABLE IF NOT EXISTS checkpoints (
                id UUID PRIMARY KEY,
                world_id UUID REFERENCES worlds(id) ON DELETE CASCADE,
                tick BIGINT NOT NULL,
                name VARCHAR(255),
                cells_data BYTEA,
                colonies_data BYTEA,
                individuals_data BYTEA,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(ddl);
            LOG.fine("Database schema initialized successfully.");
        } catch (SQLException e) {
            LOG.warning("Error initializing database schema: " + e.getMessage());
        }
    }

    /**
     * Get the active connection.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Close the connection.
     */
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                LOG.info("Disconnected from database (" + (h2Fallback ? "H2" : "PostgreSQL") + ")");
            } catch (SQLException e) {
                LOG.warning("Error closing connection: " + e.getMessage());
            }
        }
    }

    /**
     * Check if database is connected.
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isH2Fallback() {
        return h2Fallback;
    }

    /**
     * Create repository instances.
     */
    public WorldRepository worldRepository() {
        return new WorldRepository(connection);
    }

    public CheckpointRepository checkpointRepository() {
        return new CheckpointRepository(connection);
    }

    public ColonyRepository colonyRepository() {
        return new ColonyRepository(connection);
    }
}
