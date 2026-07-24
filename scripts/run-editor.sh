#!/bin/bash
# SwarmForge Editor (Studio) Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Editor (Studio) Launcher"
echo "========================================"

cd "$(dirname "$0")/.."

echo "[1/2] Ensuring Infrastructure is UP..."
docker-compose up -d postgres redis 2>/dev/null || echo "WARNING: Docker infrastructure may not be running"

echo ""
echo "[2/2] Launching SwarmForge Editor..."
mvn compile exec:java -pl swarmforge-editor -q "$@"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start SwarmForge Editor."
    exit 1
fi
