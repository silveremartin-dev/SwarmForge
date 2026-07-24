/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server;

/**
 * Server configuration record.
 */
public record ServerConfig(
        int grpcPort,
        int worldWidth, int worldHeight, int worldDepth, int groundLevel,
        double latitude, double longitude, long seed,
        String dbHost, int dbPort, String dbName, String dbUser, String dbPassword,
        String redisHost, int redisPort) {

    /**
     * Local / standalone mode (default for development).
     * Attempts to connect to a local PostgreSQL instance.
     * Falls back to H2 in-memory automatically if PostgreSQL is unavailable.
     * Redis is optional — silently disabled if unreachable.
     */
    public static ServerConfig local() {
        return new ServerConfig(
                50051,
                256, 256, 128, 64,
                48.8566, 2.3522, System.currentTimeMillis(),
                "localhost", 5432, "swarmforge", "swarmforge", "swarmforge",
                "localhost", 6379);
    }

    /** @deprecated use {@link #local()} instead */
    public static ServerConfig defaults() {
        return local();
    }

    /**
     * Fully offline mode — no database or Redis connections attempted.
     * Use this only when neither PostgreSQL nor H2 is needed.
     */
    public static ServerConfig offline() {
        return new ServerConfig(
                50051,
                256, 256, 128, 64,
                48.8566, 2.3522, System.currentTimeMillis(),
                "", 0, "", "", "",
                "", 0);
    }

    public static ServerConfig fromEnvironment() {
        return new ServerConfig(
                getInt("GRPC_PORT", 50051),
                getInt("WORLD_WIDTH", 256),
                getInt("WORLD_HEIGHT", 256),
                getInt("WORLD_DEPTH", 128),
                getInt("GROUND_LEVEL", 64),
                getDouble("LATITUDE", 48.8566),
                getDouble("LONGITUDE", 2.3522),
                getLong("SEED", System.currentTimeMillis()),
                getString("DB_HOST", "localhost"),
                getInt("DB_PORT", 5432),
                getString("DB_NAME", "swarmforge"),
                getString("DB_USER", "swarmforge"),
                getString("DB_PASSWORD", "swarmforge"),
                getString("REDIS_HOST", "localhost"),
                getInt("REDIS_PORT", 6379));
    }

    private static String getString(String key, String def) {
        String val = System.getenv(key);
        return val != null ? val : def;
    }

    private static int getInt(String key, int def) {
        String val = System.getenv(key);
        if (val != null) {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                return def;
            }
        }
        return def;
    }

    private static long getLong(String key, long def) {
        String val = System.getenv(key);
        if (val != null) {
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException e) {
                return def;
            }
        }
        return def;
    }

    private static double getDouble(String key, double def) {
        String val = System.getenv(key);
        if (val != null) {
            try {
                return Double.parseDouble(val);
            } catch (NumberFormatException e) {
                return def;
            }
        }
        return def;
    }
}
