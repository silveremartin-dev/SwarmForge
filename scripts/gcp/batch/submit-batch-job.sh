#!/usr/bin/env bash
# ==============================================================================
# SwarmForge — Submit a Google Cloud Batch Simulation Campaign (Linux / macOS)
# ==============================================================================

set -euo pipefail

SCENARIO="${1:-1}"
TICKS="${2:-2000}"
WORKERS="${3:-1}"
MACHINE_TYPE="${4:-e2-standard-4}"
USE_SPOT="${5:-true}"
PROJECT_ID="${6:-swarmforge-509813}"
REGION="${7:-europe-west1}"
IMAGE_TAG="${8:-latest}"

GCS_BUCKET="swarmforge-simulations"
IMAGE="${REGION}-docker.pkg.dev/${PROJECT_ID}/swarmforge-registry/swarmforge-server:${IMAGE_TAG}"
SA_EMAIL="swarmforge-batch-runner@${PROJECT_ID}.iam.gserviceaccount.com"

TIMESTAMP=$(date +"%Y%m%d-%H%M%S")
SCENARIO_SLUG=$(echo "$SCENARIO" | tr '[:upper:]' '[:lower:]' | tr -cd 'a-z0-9' | cut -c1-15)
JOB_NAME="sf-${SCENARIO_SLUG}-${TIMESTAMP}"

PROVISIONING_MODEL="STANDARD"
if [ "$USE_SPOT" = "true" ]; then
    PROVISIONING_MODEL="SPOT"
fi

echo "=========================================================="
echo "   SWARMFORGE — SUBMITTING CLOUD BATCH JOB                "
echo "=========================================================="
echo "  Job Name     : $JOB_NAME"
echo "  Scenario     : $SCENARIO"
echo "  Ticks        : $TICKS"
echo "  Task replicas: $WORKERS"
echo "  Machine Type : $MACHINE_TYPE"
echo "  Spot/Preempt : $USE_SPOT"
echo "  Image        : $IMAGE"
echo "  Results GCS  : gs://$GCS_BUCKET/$JOB_NAME/"
echo "----------------------------------------------------------"

TMP_SPEC=$(mktemp --suffix=.json)
cat <<EOF > "$TMP_SPEC"
{
  "taskGroups": [
    {
      "taskSpec": {
        "runnables": [
          {
            "container": {
              "imageUri": "$IMAGE",
              "entrypoint": "/bin/sh",
              "commands": ["-c",
                "java -Xms2g -Xmx12g -XX:+UseG1GC -jar app.jar --headless --batch --scenario=\${SCENARIO} --ticks=\${TICKS} --export-dir=/app/saves/batch && gsutil -m rsync -r /app/saves/batch/ gs://$GCS_BUCKET/$JOB_NAME/task-\${BATCH_TASK_INDEX}/"
              ]
            },
            "environment": {
              "variables": {
                "MODE": "batch",
                "SCENARIO": "$SCENARIO",
                "TICKS": "$TICKS"
              }
            }
          }
        ],
        "computeResource": {
          "cpuMilli": 4000,
          "memoryMib": 14336
        },
        "maxRetryCount": 1,
        "maxRunDuration": "86400s"
      },
      "taskCount": $WORKERS,
      "parallelism": $WORKERS
    }
  ],
  "allocationPolicy": {
    "instances": [
      {
        "policy": {
          "machineType": "$MACHINE_TYPE",
          "provisioningModel": "$PROVISIONING_MODEL"
        }
      }
    ],
    "serviceAccount": { "email": "$SA_EMAIL" },
    "location": { "allowedLocations": ["regions/$REGION"] }
  },
  "logsPolicy": { "destination": "CLOUD_LOGGING" }
}
EOF

gcloud batch jobs submit "$JOB_NAME" \
    --project="$PROJECT_ID" \
    --location="$REGION" \
    --config="$TMP_SPEC"

rm -f "$TMP_SPEC"

echo ""
echo "=========================================================="
echo "✅ Cloud Batch job submitted successfully!"
echo "=========================================================="
