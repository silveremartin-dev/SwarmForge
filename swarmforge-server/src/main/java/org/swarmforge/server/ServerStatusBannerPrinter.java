/*
 * SwarmForge - Eusocial Insect Simulation
 * Copyright (c) 2022-2026 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
package org.swarmforge.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swarmforge.server.persistence.DatabaseManager;
import org.swarmforge.server.persistence.RedisCache;

/**
 * Utility to print diagnostic status banners for SwarmForge Server.
 */
public final class ServerStatusBannerPrinter {

    private static final Logger LOG = LoggerFactory.getLogger(ServerStatusBannerPrinter.class);

    private ServerStatusBannerPrinter() {
    }

    /**
     * Prints an ASCII / ANSI status banner to standard log or console.
     *
     * @param grpcPort Port on which gRPC is listening
     * @param database Database manager instance
     * @param cache Redis cache instance
     */
    public static void printStatusBanner(int grpcPort, DatabaseManager database, RedisCache cache) {
        String dbStatus = database.isConnected()
                ? (database.isH2Fallback() ? "[OK] H2 (MEM)" : "[OK] POSTGRES")
                : "[X] OFFLINE";
        String redisStatus = cache.isConnected() ? "[OK] ONLINE " : "[X] OFFLINE";

        LOG.info("==================== SWARMFORGE SERVER STATUS ====================");
        LOG.info("gRPC Server  : [OK] RUNNING on port {}", grpcPort);
        LOG.info("Database     : {}", dbStatus);
        LOG.info("Redis Cache  : {}", redisStatus);
        if (database.isH2Fallback()) {
            LOG.info("[!] Running in Standalone / H2 In-Memory Fallback");
        }
        if (!cache.isConnected()) {
            LOG.info("[!] Caching disabled - start Redis for caching");
        }
        if (database.isConnected() && cache.isConnected() && !database.isH2Fallback()) {
            LOG.info("[OK] All production services operational");
        }
        LOG.info("==================================================================");
    }
}
