#!/usr/bin/env bash
# SwarmForge Test Execution Script (Linux / macOS)

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/../.."

echo "========================================"
echo "  SwarmForge - Test Runner"
echo "========================================"
echo ""

echo "[1/2] Executing build-all..."
"$SCRIPT_DIR/../build/build-all.sh"

echo ""
echo "[2/2] Running all unit & integration tests..."
mvn test "$@"

echo ""
echo "========================================"
echo "All Tests Passed Successfully!"
echo "========================================"
