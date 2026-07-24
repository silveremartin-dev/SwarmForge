#!/bin/bash
# SwarmForge Server GUI Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Server GUI Launcher"
echo "========================================"

cd "$(dirname "$0")/.."

echo "[1/2] Ensuring Infrastructure is UP..."
docker-compose up -d postgres redis 2>/dev/null || echo "WARNING: Docker infrastructure may not be running"

echo ""
echo "[2/2] Launching Server GUI..."
mvn compile exec:java -pl swarmforge-server "-Dexec.mainClass=org.swarmforge.server.ServerGuiLauncher" "$@"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start Server GUI."
    exit 1
fi
