#!/usr/bin/env bash
# ==============================================================================
# SwarmForge — Monitor a Cloud Batch Job (Linux / macOS)
# ==============================================================================

set -euo pipefail

JOB_NAME="${1:?Usage: monitor-batch-job.sh <job-name> [project-id] [region]}"
PROJECT_ID="${2:-swarmforge-509813}"
REGION="${3:-europe-west1}"

echo "=========================================================="
echo "   SWARMFORGE — CLOUD BATCH JOB MONITOR                   "
echo "=========================================================="
echo "  Job Name   : $JOB_NAME"
echo "  Project ID : $PROJECT_ID"
echo "  Region     : $REGION"
echo "----------------------------------------------------------"

echo ""
echo "── Job Status ────────────────────────────────────────────"
gcloud batch jobs describe "$JOB_NAME" \
    --project="$PROJECT_ID" \
    --location="$REGION" \
    --format="yaml(status,createTime,updateTime)"

echo ""
echo "── Recent Logs (last 50 lines) ───────────────────────────"
gcloud logging read \
    "resource.type=batch.googleapis.com/Job AND resource.labels.job_id=$JOB_NAME" \
    --project="$PROJECT_ID" \
    --limit=50 \
    --format="value(timestamp, textPayload)" \
    --order=asc || echo "  (No logs yet — job may still be starting)"
