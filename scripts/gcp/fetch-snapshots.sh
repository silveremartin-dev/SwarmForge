#!/bin/bash
# ==============================================================================
# SwarmForge - Export & Download Snapshots from GCP to Local (Bash / Cloud Shell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================
OUTPUT_FILE=${1:-"snapshots_export.sql"}
PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="swarmforge-vm"

echo "=========================================================="
echo "   📥 Exportation des snapshots depuis PostgreSQL GCP...  "
echo "=========================================================="

# 1. Export pg_dump on the remote VM
echo "[1/2] Dump des tables 'checkpoints' et 'worlds' sur la VM..."
DUMP_CMD="cd swarmforge && sudo docker compose exec -T postgres pg_dump -U swarmforge -d swarmforge -t checkpoints -t worlds -t colonies > /tmp/snapshots_export.sql"
gcloud compute ssh $INSTANCE --project=$PROJECT --zone=$ZONE --command="$DUMP_CMD"

# 2. SCP transfer to local machine
echo "[2/2] Téléchargement du fichier vers '$OUTPUT_FILE'..."
gcloud compute scp "$INSTANCE:/tmp/snapshots_export.sql" "$OUTPUT_FILE" --project=$PROJECT --zone=$ZONE

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================================="
    echo "   ✅ Snapshots rapatriés avec succès dans '$OUTPUT_FILE' !"
    echo "=========================================================="
    echo "💡 Ce fichier peut être importé dans votre base locale pour les replays déterministes."
else
    echo "❌ Erreur lors du rapatriement des snapshots."
fi
