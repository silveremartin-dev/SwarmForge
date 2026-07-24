#!/bin/bash
# SwarmForge Client (Viewer) Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Client (Viewer) Launcher"
echo "========================================"

cd "$(dirname "$0")/.."

echo "[1/2] Ensuring Infrastructure is UP..."
docker-compose up -d postgres redis 2>/dev/null || echo "WARNING: Docker infrastructure may not be running"

echo ""
echo "[2/2] Launching SwarmForge Client..."
mvn compile exec:java -pl swarmforge-client -q "$@"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start SwarmForge Client."
    exit 1
fi
