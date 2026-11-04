#!/usr/bin/env bash
# SwarmForge Infrastructure Starter (Linux / macOS)

cd "$(dirname "$0")/../.."

echo "========================================"
echo "  Starting SwarmForge Infrastructure"
echo "========================================"

if ! command -v docker &>/dev/null && ! command -v docker-compose &>/dev/null; then
    echo "[INFO] Docker / Docker Compose non detecte sur le systeme."
    echo "[INFO] SwarmForge fonctionne de maniere autonome avec base H2 en memoire."
    exit 0
fi

docker compose up -d 2>/dev/null || docker-compose up -d 2>/dev/null || {
    echo "[AVERTISSEMENT] Echec du demarrage Docker. SwarmForge utilisera la base locale H2."
    exit 0
}

echo "Infrastructure started."
echo "PostgreSQL: localhost:5432 (swarmforge/swarmforge)"
echo "Redis:      localhost:6379"
