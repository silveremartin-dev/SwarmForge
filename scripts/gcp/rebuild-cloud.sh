#!/bin/bash
# ==============================================================================
# SwarmForge - Rebuild Docker Stack on GCP VM (Bash / Cloud Shell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================
SERVICE=${1:-""}
PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="swarmforge-vm"

echo "=========================================================="
echo "   🔨 Rebuild du cluster SwarmForge sur le Cloud...       "
echo "=========================================================="

if [ -n "$SERVICE" ]; then
    echo "Rebuild ciblé pour le service : $SERVICE"
    CMD="cd ~/swarmforge && sudo docker compose build $SERVICE && sudo docker compose up -d --no-deps $SERVICE"
else
    echo "Rebuild complet (Server + 2 Workers + Web + BD)..."
    CMD="cd ~/swarmforge && sudo docker compose build && sudo docker compose up -d --scale compute=2"
fi

gcloud compute ssh "$INSTANCE" --project=$PROJECT --zone=$ZONE --command="$CMD"

echo ""
echo "=========================================================="
echo "   ✅ Rebuild et redémarrage terminés !                   "
echo "=========================================================="
