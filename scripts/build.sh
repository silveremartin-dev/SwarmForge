#!/bin/bash
# SwarmForge Build Script (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Build Script (Linux/Mac)"
echo "========================================"

cd "$(dirname "$0")/.."

echo "Building project with Maven..."
mvn clean install -DskipTests "$@"

if [ $? -ne 0 ]; then
    echo "ERROR: Build Failed!"
    exit 1
fi

echo ""
echo "Build Successful!"
