#!/usr/bin/env bash
# ==============================================================================
# SwarmForge Client Launcher (Linux / macOS)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/../.."

echo "========================================"
echo "  SwarmForge - Client Launcher"
echo "========================================"

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

echo "[1/2] Compiling SwarmForge Core & Client..."
mvn compile -pl swarmforge-client -am -q
if [ $? -ne 0 ]; then
    echo "ERROR: Compilation failed."
    exit 1
fi

echo "[2/2] Launching SwarmForge Client..."
mvn exec:java -pl swarmforge-client -q $DEBUG_OPT $HEADLESS_OPT -Dexec.args="$PASSED_ARGS"
