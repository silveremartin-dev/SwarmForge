#!/bin/bash
# SwarmForge Server Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Server Launcher"
echo "========================================"

cd "$(dirname "$0")/.."

echo "[1/2] Ensuring Infrastructure is UP..."
docker-compose up -d postgres redis 2>/dev/null || echo "WARNING: Docker infrastructure may not be running"

echo ""
echo "[2/2] Launching SwarmForge Server..."
mvn compile exec:java -pl swarmforge-server -q "$@"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start SwarmForge Server."
    exit 1
fi
