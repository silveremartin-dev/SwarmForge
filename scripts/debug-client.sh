#!/bin/bash
# SwarmForge Client Debug Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge Client - DEBUG MODE"
echo "========================================"
echo "Connecting on port 5005..."

cd "$(dirname "$0")/.."

mvn javafx:run -pl swarmforge-client-javafx -Djavafx.options="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
