#!/bin/bash
# SwarmForge Full Build Script (Linux/Mac)
# Compiles and packages all modules across the repository.

echo "========================================"
echo "  SwarmForge - Full Build All (Linux/Mac)"
echo "========================================"

cd "$(dirname "$0")/.."

echo "Building ALL project modules with Maven..."
mvn package -DskipTests "$@"

if [ $? -ne 0 ]; then
    echo "ERROR: Build Failed!"
    exit 1
fi

echo ""
echo "========================================"
echo "Build Successful for ALL modules!"
echo "========================================"
