#!/bin/bash
# ==============================================================================
# SwarmForge - Clean / Reset Database & Redis on GCP VM (Bash / Cloud Shell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="swarmforge-vm"

echo "⚠️  Réinitialisation des données PostgreSQL & Redis sur le Cloud..."

CMD="cd ~/swarmforge && sudo docker compose down -v && sudo docker compose up -d"

gcloud compute ssh "$INSTANCE" --project=$PROJECT --zone=$ZONE --command="$CMD"

echo "✅ Base de données et cache réinitialisés à neuf !"
