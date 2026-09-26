#!/bin/bash
# ==============================================================================
# SwarmForge - Live Logs Stream from GCP VM (Bash / Cloud Shell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================
SERVICE=${1:-"server"}
PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="swarmforge-vm"

echo "📜 Suivi des logs en temps réel pour '$SERVICE' sur GCP..."
echo "(Appuyez sur Ctrl+C pour quitter le flux de logs)"

CMD="cd ~/swarmforge && sudo docker compose logs -f --tail=100 $SERVICE"

gcloud compute ssh "$INSTANCE" --project=$PROJECT --zone=$ZONE --command="$CMD"
