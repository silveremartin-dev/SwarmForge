#!/bin/bash
# SwarmForge Server Debug Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge Server - DEBUG MODE"
echo "========================================"
echo "Connecting on port 5006..."

cd "$(dirname "$0")/.."

mvn exec:java -pl swarmforge-server -Dexec.args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006"
