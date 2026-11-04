#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Package Client Artifacts (Linux / macOS)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

echo "=========================================="
echo " Packaging SwarmForge Client Artifacts"
echo "=========================================="

mvn package -pl swarmforge-client -am -DskipTests

echo "[SUCCÈS] Client packagé avec succès."
