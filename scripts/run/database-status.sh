#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Database & Redis Infrastructure Status Check
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

echo "=============================================================================="
echo "   SwarmForge - Docker Infrastructure Status (PostgreSQL & Redis)            "
echo "=============================================================================="
echo ""

docker compose ps

echo ""
echo "Port Connectivity Check:"
if nc -z localhost 5432 2>/dev/null || (echo > /dev/tcp/localhost/5432) 2>/dev/null; then
  echo " • PostgreSQL (port 5432) : [ACTIVE / REACHABLE]"
else
  echo " • PostgreSQL (port 5432) : [INACTIVE]"
fi

if nc -z localhost 6379 2>/dev/null || (echo > /dev/tcp/localhost/6379) 2>/dev/null; then
  echo " • Redis      (port 6379) : [ACTIVE / REACHABLE]"
else
  echo " • Redis      (port 6379) : [INACTIVE]"
fi
echo ""
