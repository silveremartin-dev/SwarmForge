#!/usr/bin/env bash
# ==============================================================================
# SwarmForge — Fetch Batch Simulation Results from GCS (Linux / macOS)
# ==============================================================================

set -euo pipefail

JOB_NAME="${1:?Usage: fetch-batch-results.sh <job-name> [local-dir]}"
LOCAL_DIR="${2:-./saves/batch/$JOB_NAME}"
GCS_PATH="gs://swarmforge-simulations/$JOB_NAME/"

echo "=========================================================="
echo "   SWARMFORGE — FETCH BATCH RESULTS FROM GCS              "
echo "=========================================================="
echo "  Job Name   : $JOB_NAME"
echo "  GCS Source : $GCS_PATH"
echo "  Local Dest : $LOCAL_DIR"
echo "----------------------------------------------------------"

mkdir -p "$LOCAL_DIR"
echo "Downloading results..."
gsutil -m rsync -r "$GCS_PATH" "$LOCAL_DIR/"

echo ""
echo "=========================================================="
echo "✅ Results downloaded to: $LOCAL_DIR"
ls -la "$LOCAL_DIR"
echo "=========================================================="
