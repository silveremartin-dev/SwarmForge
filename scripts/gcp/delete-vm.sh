#!/bin/bash
# ==============================================================================
# SwarmForge - Delete GCP VM & Firewall Rules (Bash / Cloud Shell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="swarmforge-vm"

echo "⚠️  Suppression de l'instance '$INSTANCE' et des règles pare-feu..."

# Supprimer la VM
gcloud compute instances delete $INSTANCE --project=$PROJECT --zone=$ZONE --quiet

# Supprimer la règle de pare-feu
gcloud compute firewall-rules delete allow-swarmforge --project=$PROJECT --quiet

echo "🧹 Nettoyage complet terminé !"
