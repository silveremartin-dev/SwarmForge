#!/usr/bin/env bash
# SwarmForge Benchmarks Launcher (Linux / macOS)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/../.."

echo "========================================"
echo "  SwarmForge - Benchmarks Launcher"
echo "========================================"
echo ""

echo "Building SwarmForge Benchmarks..."
mvn clean package -pl swarmforge-benchmarks -DskipTests

echo "Executing Benchmarks..."
JAR_FILE=$(find swarmforge-benchmarks/target -name "swarmforge-benchmarks-*.jar" ! -name "*original*" | head -1)
java -jar "$JAR_FILE" "$@"
