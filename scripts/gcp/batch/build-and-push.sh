#!/usr/bin/env bash
# ==============================================================================
# SwarmForge — Build & Push Batch Simulation Engine Image (Linux / macOS)
# ==============================================================================

set -euo pipefail

PROJECT_ID="${1:-swarmforge-509813}"
REGION="${2:-europe-west1}"
REGISTRY_NAME="${3:-swarmforge-registry}"
IMAGE_TAG="${4:-latest}"
IMAGE_URI="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REGISTRY_NAME}/swarmforge-server:${IMAGE_TAG}"

echo "=========================================================="
echo "   SWARMFORGE — BUILD & PUSH BATCH DOCKER IMAGE           "
echo "=========================================================="
echo "  Target Image : $IMAGE_URI"
echo "----------------------------------------------------------"

gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet
docker build -f swarmforge-server/Dockerfile -t "$IMAGE_URI" .
docker push "$IMAGE_URI"

echo ""
echo "=========================================================="
echo "✅ Image pushed successfully: $IMAGE_URI"
echo "=========================================================="
