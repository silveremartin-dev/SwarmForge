#!/bin/bash
# SwarmForge Compute Node Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Compute Node Launcher"
echo "========================================"

cd "$(dirname "$0")/.."

DEBUG_OPT=""
HEADLESS_OPT=""
PASSED_ARGS=""

for arg in "$@"; do
    case $arg in
        --debug)
            echo "[INFO] Debug mode active (JDWP agent on port 5008)"
            DEBUG_OPT="-Dexec.jvmArgs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008"
            ;;
        --nogui)
            echo "[INFO] Running compute node in No-GUI mode"
            HEADLESS_OPT="-Djava.awt.headless=true"
            PASSED_ARGS="$PASSED_ARGS --nogui"
            ;;
        *)
            PASSED_ARGS="$PASSED_ARGS $arg"
            ;;
    esac
done

echo "[1/2] Compiling SwarmForge Core & Compute Node..."
mvn compile -pl swarmforge-compute -am -q
if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed."
    exit 1
fi

echo "[2/2] Starting SwarmForge Compute Node..."
mvn exec:java -pl swarmforge-compute -q $DEBUG_OPT $HEADLESS_OPT -Dexec.args="$PASSED_ARGS"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start Compute Node."
    exit 1
fi
