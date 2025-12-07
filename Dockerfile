# SwarmForge Server Dockerfile
# Copyright (c) 2022-2025 Silvère Martin-Michiellot
# AI Assistant: Gemini (Google DeepMind)

FROM eclipse-temurin:21-jre-alpine

LABEL maintainer="Silvère Martin-Michiellot"
LABEL description="SwarmForge Simulation Server"
LABEL version="2.0.0"

WORKDIR /app

# Copy server JAR
COPY swarmforge-server/target/swarmforge-server-*.jar server.jar

# Expose gRPC port
EXPOSE 50051

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s \
  CMD wget --spider -q http://localhost:8080/health || exit 1

# Run server
ENTRYPOINT ["java", "-jar", "server.jar"]
