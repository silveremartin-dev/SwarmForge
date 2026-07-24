#!/bin/bash
# SwarmForge Editor (Studio) Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Editor (Studio) Launcher"
echo "========================================"

cd "$(dirname "$0")/.."

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

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start SwarmForge Editor."
    exit 1
fi
