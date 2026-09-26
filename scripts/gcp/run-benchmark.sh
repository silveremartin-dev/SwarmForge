#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Run Comprehensive Benchmark on GCP VM (Linux / macOS)
# ==============================================================================

set -euo pipefail

PROJECT="${1:-swarmforge-509813}"
ZONE="${2:-europe-west1-b}"
INSTANCE="${3:-swarmforge-vm}"

echo "=========================================================="
echo "   Execution de la suite de Benchmarks sur GCP VM         "
echo "=========================================================="

gcloud compute ssh "$INSTANCE" --project="$PROJECT" --zone="$ZONE" --command="bash ~/swarmforge/scripts/gcp/remote-run-benchmark.sh"
