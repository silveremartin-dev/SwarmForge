#!/bin/bash
# SwarmForge Benchmarks Launcher (Linux/macOS)

echo "========================================"
echo "  SwarmForge - Benchmarks Launcher"
echo "========================================"
echo ""

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR/.."

echo "Building and Running SwarmForge Benchmarks..."
mvn clean package -pl swarmforge-benchmarks -DskipTests
if [ $? -ne 0 ]; then
    echo "ERROR: Failed to build SwarmForge Benchmarks."
    exit 1
fi

echo "Executing Benchmarks..."
java -jar swarmforge-benchmarks/target/swarmforge-benchmarks-2.0.0-SNAPSHOT.jar "$@"
