/*
 * SwarmForge Server Module
 * Copyright (c) 2022-2025 Silvère Martin-Michiellot
 * AI Assistant: Gemini (Google DeepMind)
 * MIT License
 */
module org.swarmforge.server {
    requires java.base;
    requires java.logging;
    requires java.sql;
    requires org.swarmforge.core;

    exports org.swarmforge.server;
    exports org.swarmforge.server.persistence;
    exports org.swarmforge.server.grpc;
}
