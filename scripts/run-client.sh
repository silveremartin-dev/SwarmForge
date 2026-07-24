#!/bin/bash
# SwarmForge Client Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Client Launcher"
echo "========================================"

cd "$(dirname "$0")/.."

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

echo "Launching SwarmForge Client..."
mvn compile exec:java -pl swarmforge-client -q $DEBUG_OPT $HEADLESS_OPT -Dexec.args="$PASSED_ARGS"

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start SwarmForge Client."
    exit 1
fi
