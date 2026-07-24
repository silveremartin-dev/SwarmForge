#!/bin/bash
# SwarmForge Full Build Script (Linux/Mac)
# Compiles and packages all modules across the repository.

echo "========================================"
echo "  SwarmForge - Full Build All (Linux/Mac)"
echo "========================================"

cd "$(dirname "$0")/.."

SKIP_TESTS="-DskipTests"
MVN_OPTS=""

for arg in "$@"; do
    case $arg in
        --debug)
            echo "[INFO] Debug mode enabled for build"
            MVN_OPTS="$MVN_OPTS -Dmaven.compiler.debug=true -Dmaven.compiler.debuglevel=lines,vars,source"
            ;;
        --with-tests)
            echo "[INFO] Running tests during build"
            SKIP_TESTS=""
            ;;
        *)
            ;;
    esac
done

echo "Building ALL project modules with Maven..."
mvn package $SKIP_TESTS $MVN_OPTS "$@"

if [ $? -ne 0 ]; then
    echo "ERROR: Build Failed!"
    exit 1
fi

echo ""
echo "========================================"
echo "Build Successful for ALL modules!"
echo "========================================"
