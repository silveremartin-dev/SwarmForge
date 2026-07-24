#!/bin/bash
# SwarmForge Server Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Server Launcher"
echo "========================================"

cd "$(dirname "$0")/.."

START_DOCKER=false
DEBUG_OPT=""
PASSED_ARGS=""

for arg in "$@"; do
    case $arg in
        --postgres)
            START_DOCKER=true
            PASSED_ARGS="$PASSED_ARGS --postgres"
            ;;
        --debug)
            echo "[INFO] Debug mode active (JDWP agent on port 5005)"
            DEBUG_OPT="-Dexec.jvmArgs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
            ;;
        *)
            PASSED_ARGS="$PASSED_ARGS $arg"
            ;;
    esac
done

if [ "$START_DOCKER" = true ]; then
    echo "[1/2] Launching Docker Infrastructure (Postgres & Redis)..."
    docker-compose up -d postgres redis 2>/dev/null || docker compose up -d postgres redis 2>/dev/null || echo "WARNING: Docker infrastructure startup attempted"
else
    echo "[1/2] Running in Standalone/Local mode (H2 Database fallback active)..."
fi

echo ""
echo "[2/2] Launching SwarmForge Server..."
mvn compile exec:java -pl swarmforge-server -q $DEBUG_OPT -Dexec.args="$PASSED_ARGS"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start SwarmForge Server."
    exit 1
fi
