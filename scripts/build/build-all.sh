#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Full Project Build (Linux / macOS)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/../.."

echo "========================================"
echo "  SwarmForge - Full Maven Build"
echo "========================================"
echo ""

mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "ERROR: Maven build failed."
    exit 1
fi

echo "SUCCESS: All modules built successfully."
