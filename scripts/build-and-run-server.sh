#!/bin/bash
# SwarmForge Server Build & Run (Linux/Mac)

echo "========================================"
echo "  SwarmForge Server - Build & Run"
echo "========================================"
echo

cd "$(dirname "$0")/.."

echo "[Build Phase] Compiling project..."
mvn install -pl swarmforge-server -am -DskipTests -q
if [ $? -ne 0 ]; then
    echo
    echo "ERROR: Build failed."
    exit 1
fi

echo
# Delegate to the run script which handles Docker startup
./scripts/run-server.sh
