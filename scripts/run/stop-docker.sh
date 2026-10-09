#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Docker Stack Teardown (PostgreSQL & Redis)
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

echo "[INFO] Stopping Docker Compose services (PostgreSQL & Redis)..."
docker compose down

echo "[OK] Docker containers stopped successfully."
