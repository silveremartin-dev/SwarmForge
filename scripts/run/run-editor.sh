#!/usr/bin/env bash
# SwarmForge Editor (Studio) Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Editor (Studio) Launcher"
echo "========================================"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/../.."

DEBUG_OPT=""
HEADLESS_OPT=""
PASSED_ARGS=""

for arg in "$@"; do
    case $arg in
        --debug)
            echo "[INFO] Debug mode active (JDWP agent on port 5006)"
            DEBUG_OPT="-Dexec.jvmArgs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006"
            ;;
        --nogui)
            echo "[INFO] Running editor in No-GUI/Headless mode"
            HEADLESS_OPT="-Djava.awt.headless=true"
            PASSED_ARGS="$PASSED_ARGS --nogui"
            ;;
        *)
            PASSED_ARGS="$PASSED_ARGS $arg"
            ;;
    esac
done

echo "Launching SwarmForge Editor..."
mvn compile exec:java -pl swarmforge-editor -q $DEBUG_OPT $HEADLESS_OPT -Dexec.args="$PASSED_ARGS"
