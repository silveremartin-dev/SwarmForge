#!/usr/bin/env bash
# ==============================================================================
# SwarmForge — Google Cloud Batch: Infrastructure Setup (Linux / macOS)
# ==============================================================================

set -euo pipefail

PROJECT_ID="${1:-swarmforge-509813}"
REGION="${2:-europe-west1}"
REGISTRY_NAME="${3:-swarmforge-registry}"
GCS_BUCKET="${4:-swarmforge-simulations}"
SA_NAME="${5:-swarmforge-batch-runner}"
SA_EMAIL="${SA_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

echo "=========================================================="
echo "   SWARMFORGE — GCP BATCH INFRASTRUCTURE SETUP            "
echo "=========================================================="
echo "  Project ID   : $PROJECT_ID"
echo "  Region       : $REGION"
echo "  Registry     : $REGISTRY_NAME"
echo "  GCS Bucket   : gs://$GCS_BUCKET"
echo "  Service Acct : $SA_EMAIL"
echo "----------------------------------------------------------"

# 1. Enable APIs
echo "[1/4] Enabling GCP APIs..."
gcloud services enable \
    batch.googleapis.com \
    artifactregistry.googleapis.com \
    storage.googleapis.com \
    logging.googleapis.com \
    --project="$PROJECT_ID"

# 2. Artifact Registry
echo "[2/4] Configuring Artifact Registry repository '$REGISTRY_NAME'..."
if ! gcloud artifacts repositories describe "$REGISTRY_NAME" --location="$REGION" --project="$PROJECT_ID" &>/dev/null; then
    gcloud artifacts repositories create "$REGISTRY_NAME" \
        --repository-format=docker \
        --location="$REGION" \
        --project="$PROJECT_ID" \
        --description="SwarmForge simulation engine Docker images"
else
    echo "      Repository already exists — OK."
fi

# 3. GCS Bucket
echo "[3/4] Configuring GCS bucket 'gs://$GCS_BUCKET'..."
if ! gsutil ls -b "gs://$GCS_BUCKET" &>/dev/null; then
    gsutil mb -p "$PROJECT_ID" -l "$REGION" -b on "gs://$GCS_BUCKET"
    TMP_JSON=$(mktemp)
    echo '{"lifecycle":{"rule":[{"action":{"type":"Delete"},"condition":{"age":90}}]}}' > "$TMP_JSON"
    gsutil lifecycle set "$TMP_JSON" "gs://$GCS_BUCKET"
    rm -f "$TMP_JSON"
    echo "      Bucket created with 90-day auto-delete lifecycle."
else
    echo "      Bucket already exists — OK."
fi

# 4. Service Account & IAM
echo "[4/4] Configuring Service Account '$SA_NAME'..."
if ! gcloud iam service-accounts describe "$SA_EMAIL" --project="$PROJECT_ID" &>/dev/null; then
    gcloud iam service-accounts create "$SA_NAME" \
        --project="$PROJECT_ID" \
        --display-name="SwarmForge Batch Runner" \
        --description="Service account for SwarmForge Cloud Batch simulation jobs"
fi

for role in roles/batch.jobsEditor roles/storage.objectAdmin roles/artifactregistry.reader roles/logging.logWriter; do
    gcloud projects add-iam-policy-binding "$PROJECT_ID" \
        --member="serviceAccount:$SA_EMAIL" \
        --role="$role" --quiet >/dev/null
done

echo ""
echo "=========================================================="
echo "✅ GCP Batch infrastructure is ready!"
echo "=========================================================="
