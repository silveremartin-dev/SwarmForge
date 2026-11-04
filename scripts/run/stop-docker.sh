#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Docker Stack Teardown (PostgreSQL & Redis)
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

if ! command -v docker &>/dev/null && ! command -v docker-compose &>/dev/null; then
    echo "[INFO] Docker non installe. Aucun conteneur actif a arreter."
    exit 0
fi

echo "[INFO] Stopping Docker Compose services (PostgreSQL & Redis)..."
docker compose down 2>/dev/null || docker-compose down 2>/dev/null || true

echo "[OK] Docker containers stopped successfully."
