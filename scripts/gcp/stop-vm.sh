#!/bin/bash
# ==============================================================================
# SwarmForge - Stop GCP VM (Bash / Cloud Shell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="swarmforge-vm"

echo "🛑 Arrêt de l'instance Google Cloud Compute '$INSTANCE'..."

gcloud compute instances stop $INSTANCE --project=$PROJECT --zone=$ZONE

if [ $? -eq 0 ]; then
    echo "✅ Instance '$INSTANCE' arrêtée avec succès !"
    echo "💡 La facturation du processeur (vCPU) et de la mémoire (RAM) est maintenant suspendue."
else
    echo "❌ Erreur lors de l'arrêt de l'instance."
fi
