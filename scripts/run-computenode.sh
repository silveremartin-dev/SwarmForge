#!/bin/bash
# SwarmForge Compute Node Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Compute Node Launcher"
echo "========================================"

cd "$(dirname "$0")/.."

echo "Starting SwarmForge Compute Node..."
mvn compile exec:java -pl swarmforge-compute -q "$@"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start Compute Node."
    exit 1
fi
