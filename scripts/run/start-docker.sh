#!/usr/bin/env bash
# SwarmForge Infrastructure Starter (Linux / macOS)

cd "$(dirname "$0")/../.."

echo "========================================"
echo "  Starting SwarmForge Infrastructure"
echo "========================================"

docker compose up -d 2>/dev/null || docker-compose up -d

echo "Infrastructure started."
echo "PostgreSQL: localhost:5432 (swarmforge/swarmforge)"
echo "Redis:      localhost:6379"
