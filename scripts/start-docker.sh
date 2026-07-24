#!/bin/bash
# SwarmForge Infrastructure Startup (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Infrastructure Startup"
echo "========================================"

cd "$(dirname "$0")/.."

echo "Starting Docker containers (Postgres, Redis)..."
docker-compose up -d postgres redis

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to start Docker infrastructure. Ensure Docker daemon is running."
    exit 1
fi

echo ""
echo "Infrastructure started successfully."
