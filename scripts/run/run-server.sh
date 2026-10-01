#!/usr/bin/env bash
# ==============================================================================
# SwarmForge Server Launcher (Linux / macOS)
# Default: Starts with graphical administration GUI.
# Options:
#   --nogui / --headless : Console mode without GUI
#   --scenario <1-16>    : Preload a specific academic scenario
#   --postgres           : Launch Docker Postgres + Redis infrastructure
#   --debug              : Enable JDWP debug agent on port 5005
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/../.."

export MAVEN_OPTS="${MAVEN_OPTS:--Xms2g -Xmx8g -XX:+UseG1GC --add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED -XX:+AlwaysPreTouch}"

START_DOCKER=false
DEBUG_OPT=""
USE_GUI=true
PASSED_ARGS=""

for arg in "$@"; do
    case $arg in
        --postgres)
            START_DOCKER=true
            PASSED_ARGS="$PASSED_ARGS --postgres"
            ;;
        --nogui|--headless)
            USE_GUI=false
            PASSED_ARGS="$PASSED_ARGS --nogui"
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
    echo "[1/3] Launching Docker Infrastructure (Postgres & Redis)..."
    docker compose up -d postgres redis 2>/dev/null || docker-compose up -d postgres redis 2>/dev/null || echo "[WARNING] Docker startup failed."
else
    echo "[1/3] Running in Local Standalone mode (H2 In-Memory Database)..."
fi

echo ""
echo "[2/3] Compiling SwarmForge Core & Server..."
mvn compile -pl swarmforge-server -am -q

echo ""
if [ "$USE_GUI" = true ]; then
    echo "[3/3] Launching SwarmForge Server with GUI Management Console..."
    mvn exec:java -pl swarmforge-server -q $DEBUG_OPT "-Dexec.mainClass=org.swarmforge.server.ServerGuiLauncher" -Dexec.args="$PASSED_ARGS"
else
    echo "[3/3] Launching SwarmForge Server in Console (Headless / No-GUI) mode..."
    mvn exec:java -pl swarmforge-server -q $DEBUG_OPT "-Dexec.mainClass=org.swarmforge.server.SwarmForgeServer" -Dexec.args="$PASSED_ARGS"
fi
