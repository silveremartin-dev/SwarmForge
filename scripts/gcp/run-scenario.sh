#!/bin/bash
# ==============================================================================
# SwarmForge - Run Headless Scenario on Cloud Cluster (Bash / Cloud Shell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================
SCENARIO=${1:-1}
PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="swarmforge-vm"

echo "=========================================================="
echo "   🐜 Lancement du Scénario #$SCENARIO sur le Cloud       "
echo "=========================================================="

CMD="cd ~/swarmforge && sudo SCENARIO=$SCENARIO docker compose up -d --no-deps server"

gcloud compute ssh --quiet "$INSTANCE" --project=$PROJECT --zone=$ZONE --command="$CMD"

echo "✅ Scénario #$SCENARIO démarré et en cours de calcul !"
