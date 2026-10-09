#!/bin/bash
# ==============================================================================
# SwarmForge - Automated Deployment Script for Google Cloud Compute (GCP)
# Project: swarmforge-509813
# ==============================================================================

set -e

echo "================================================================="
echo "   🚀 Initializing SwarmForge Cloud Cluster on GCP             "
echo "================================================================="

# 1. Update & Install Prerequisites (Docker, Compose, OpenJDK 21, Git)
echo "[1/4] Installing Docker and Dependencies..."
sudo DEBIAN_FRONTEND=noninteractive apt-get update -y
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y apt-transport-https ca-certificates curl gnupg lsb-release git openjdk-21-jdk maven

if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sudo sh get-docker.sh
    sudo usermod -aG docker $USER
fi

# 2. Build & Start SwarmForge Ecosystem (1 Master Server + 2 Compute Nodes + DB + Redis + Web)
echo "[2/4] Building and launching SwarmForge stack (1 Server + 2 Compute Nodes)..."
sudo docker compose down --remove-orphans || true
sudo docker compose build
sudo docker compose up -d --scale compute=2

# 3. Check Cluster Health
echo "[3/4] Checking Cluster Status..."
sleep 8
sudo docker compose ps

echo "================================================================="
echo "   ✅ SwarmForge Cluster is UP & RUNNING!                       "
echo "================================================================="
echo "  • gRPC Simulation Server : port 50051 (Binary high-throughput stream)"
echo "  • REST API / Metrics     : port 8080"
echo "  • Web 3D Viewer          : port 3000 (http://<EXTERNAL_IP>:3000)"
echo "  • PostgreSQL Persistence : port 5432"
echo "  • Redis State Cache      : port 6379"
echo "  • Compute Nodes Active   : 2 replicas (Clustered)"
echo "================================================================="
