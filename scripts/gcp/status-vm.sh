#!/bin/bash
# ==============================================================================
# SwarmForge - Check VM & Cluster Status (Bash / Cloud Shell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="swarmforge-vm"

echo "🔍 Vérification de l'état de l'instance '$INSTANCE'..."

STATUS=$(gcloud compute instances describe $INSTANCE --project=$PROJECT --zone=$ZONE --format="get(status)" 2>/dev/null)

if [ -z "$STATUS" ]; then
    echo "❌ L'instance '$INSTANCE' n'existe pas ou n'est pas accessible."
    exit 1
fi

echo "• État de la VM : $STATUS"

if [ "$STATUS" = "RUNNING" ]; then
    NAT_IP=$(gcloud compute instances describe $INSTANCE --project=$PROJECT --zone=$ZONE --format="get(networkInterfaces[0].accessConfigs[0].natIP)")
    echo "• IP Publique   : $NAT_IP"
    echo ""
    echo "Services SwarmForge disponibles :"
    echo "  - gRPC Server        : $NAT_IP:50051"
    echo "  - Web Dashboard 3D   : http://$NAT_IP:3000"
    echo "  - REST API / Metrics : http://$NAT_IP:8080"
else
    echo "💡 La VM est arrêtée. Utilisez ./scripts/gcp/start-vm.sh pour la réveiller."
fi
