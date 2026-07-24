#!/bin/bash
# SwarmForge Test Runner (Linux/Mac)
# Automatically builds all modules first, then runs unit & integration tests.

echo "========================================"
echo "  SwarmForge - Test Runner (Linux/Mac)"
echo "========================================"

cd "$(dirname "$0")/.."

echo "[1/2] Executing build-all..."
./scripts/build-all.sh
if [ $? -ne 0 ]; then
    echo "ERROR: Build failed prior to running tests."
    exit 1
fi

echo ""
echo "[2/2] Running all project unit & integration tests..."
mvn test "$@"

if [ $? -ne 0 ]; then
    echo "ERROR: Some tests failed!"
    exit 1
fi

echo ""
echo "All tests passed successfully!"
