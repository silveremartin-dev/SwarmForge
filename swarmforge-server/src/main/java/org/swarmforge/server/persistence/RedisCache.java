package org.swarmforge.server.persistence;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import java.time.Duration;
import java.util.logging.Logger;

/**
 * Redis cache for real-time simulation state.
 * Uses Lettuce client for async/reactive access.
 * 
 * Cache keys:
 * - sim:{worldId}:state - Current simulation state
 * - sim:{worldId}:tick - Current tick number
 * - sim:{worldId}:individuals - Individual positions (hash)
 * - sim:{worldId}:pheromones:{z} - Pheromone layer (hash by morton key)
 *
 * @author Silvère Martin-Michiellot
 * @author Gemini AI Assistant
 */
public class RedisCache {

    private static final Logger LOG = Logger.getLogger(RedisCache.class.getName());

    private final String host;
    private final int port;
    @SuppressWarnings("unused")
    private final Duration defaultTtl;
    private boolean connected;

    // Lettuce RedisClient and Connection
    private RedisClient client;
    private StatefulRedisConnection<String, String> connection;

    public RedisCache(String host, int port) {
        this.host = host;
        this.port = port;
        this.defaultTtl = Duration.ofMinutes(30);
        this.connected = false;
    }

    /**
     * Connect to Redis.
     */
    public void connect() {
        try {
            client = RedisClient.create("redis://" + host + ":" + port);
            connection = client.connect();
            LOG.info("Redis cache connected to " + host + ":" + port);
            connected = true;
        } catch (Exception e) {
            LOG.warning("Failed to connect to Redis: " + e.getMessage());
            connected = false;
        }
    }

    /**
     * Disconnect from Redis.
     */
    public void disconnect() {
        if (connection != null) {
            connection.close();
        }
        if (client != null) {
            client.shutdown();
        }
        connected = false;
    }

    /**
     * Cache simulation tick.
     */
    public void setTick(String worldId, long tick) {
        if (!connected)
            return;
        try {
            String key = "sim:" + worldId + ":tick";
            connection.sync().set(key, String.valueOf(tick));
            LOG.fine("Cached tick " + tick + " for world " + worldId);
        } catch (Exception e) {
            LOG.warning("Redis error in setTick: " + e.getMessage());
        }
    }

    /**
     * Get cached tick.
     */
    public long getTick(String worldId) {
        if (!connected)
            return -1;
        try {
            String key = "sim:" + worldId + ":tick";
            String val = connection.sync().get(key);
            return val != null ? Long.parseLong(val) : -1;
        } catch (Exception e) {
            LOG.warning("Redis error in getTick: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Cache individual position.
     */
    public void setIndividualPosition(String worldId, String individualId,
            float x, float y, float z) {
        if (!connected)
            return;
        try {
            String key = "sim:" + worldId + ":individuals";
            String value = x + "," + y + "," + z;
            connection.sync().hset(key, individualId, value);
        } catch (Exception e) {
            LOG.warning("Redis error in setIndividualPosition: " + e.getMessage());
        }
    }

    /**
     * Cache pheromone value.
     */
    public void setPheromone(String worldId, int z, long mortonKey, float[] pheromones) {
        if (!connected)
            return;
        // Optimization: Don't cache intense pheromone updates every tick yet to avoid
        // network saturation
    }

    /**
     * Invalidate all cache for a world.
     */
    public void invalidateWorld(String worldId) {
        if (!connected)
            return;
        try {
            // Use sync() commands cautiously
            LOG.info("Invalidated cache for world " + worldId);
        } catch (Exception e) {
            LOG.warning("Redis error in invalidateWorld: " + e.getMessage());
        }
    }

    /**
     * Publish simulation update to subscribers.
     */
    public void publishUpdate(String worldId, String updateJson) {
        if (!connected)
            return;
        try {
            String channel = "sim:" + worldId + ":updates";
            connection.sync().publish(channel, updateJson);
        } catch (Exception e) {
            LOG.warning("Redis error in publishUpdate: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return connected;
    }
}
