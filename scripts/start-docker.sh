#!/bin/bash
# SwarmForge Infrastructure Startup

echo "Starting Docker containers (Postgres, Redis)..."
cd "$(dirname "$0")/.."
docker-compose up -d
echo
echo "Infrastructure started."
