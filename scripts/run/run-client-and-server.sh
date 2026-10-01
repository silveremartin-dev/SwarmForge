#!/usr/bin/env bash
# ==============================================================================
# SwarmForge Client + Server Launcher (Linux / macOS)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/../.."

echo "================================================="
echo "  SwarmForge - Client Lourd + Serveur Launcher"
echo "================================================="

DEBUG_OPT=""
HEADLESS_OPT=""
PASSED_ARGS=""

for arg in "$@"; do
    case $arg in
        --debug)
            echo "[INFO] Debug mode active (JDWP agent on port 5007)"
            DEBUG_OPT="-Dexec.jvmArgs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5007"
            ;;
        --nogui)
            echo "[INFO] Running client in No-GUI/Headless mode"
            HEADLESS_OPT="-Djava.awt.headless=true"
            PASSED_ARGS="$PASSED_ARGS --nogui"
            ;;
        *)
            PASSED_ARGS="$PASSED_ARGS $arg"
            ;;
    esac
done

echo "[1/3] Compiling Server & Client..."
mvn compile -pl swarmforge-server,swarmforge-client -am -q

echo "[2/3] Starting SwarmForge Server in background..."
mvn exec:java -pl swarmforge-server -Dexec.args="--local" &
SERVER_PID=$!
trap "kill $SERVER_PID 2>/dev/null" EXIT

echo "[INFO] Waiting 3 seconds for server initialization..."
sleep 3

echo "[3/3] Launching SwarmForge Client..."
mvn exec:java -pl swarmforge-client -q $DEBUG_OPT $HEADLESS_OPT -Dexec.args="$PASSED_ARGS"
