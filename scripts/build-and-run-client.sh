#!/bin/bash
# SwarmForge Build & Run Editor (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Build & Run Editor"
echo "========================================"

cd "$(dirname "$0")/.."

echo "[Build Phase] Compiling project..."
mvn install -DskipTests -pl swarmforge-editor -am -q
if [ $? -ne 0 ]; then
    echo "ERROR: Build failed."
    exit 1
fi

echo ""
# Delegate to the run script
./scripts/run-client.sh
