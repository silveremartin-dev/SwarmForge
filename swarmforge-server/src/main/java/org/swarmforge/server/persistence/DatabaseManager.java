/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Database connection manager for PostgreSQL.
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class DatabaseManager {

    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private Connection connection;

    public DatabaseManager(String host, int port, String database, String username, String password) {
        this.jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        this.username = username;
        this.password = password;
    }

    /**
     * Connect to the database.
     */
    public void connect() throws SQLException {
        connection = DriverManager.getConnection(jdbcUrl, username, password);
        LOG.info("Connected to PostgreSQL: " + jdbcUrl);
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
                LOG.info("Disconnected from PostgreSQL");
            } catch (SQLException e) {
                LOG.warning("Error closing connection: " + e.getMessage());
            }
        }
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
}
