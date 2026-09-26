#!/bin/bash
# ==============================================================================
# SwarmForge - Start GCP VM and get IP (Bash / Cloud Shell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="swarmforge-vm"

echo "🚀 Démarrage de l'instance Google Cloud '$INSTANCE'..."

gcloud compute instances start $INSTANCE --project=$PROJECT --zone=$ZONE

if [ $? -eq 0 ]; then
    echo "✅ Instance démarrée !"
    NAT_IP=$(gcloud compute instances describe $INSTANCE --project=$PROJECT --zone=$ZONE --format="get(networkInterfaces[0].accessConfigs[0].natIP)")
    
    echo ""
    echo "======================================================"
    echo "   🌐 IP Publique SwarmForge : $NAT_IP"
    echo "======================================================"
    echo "  • gRPC Studio Stream : $NAT_IP:50051"
    echo "  • Web 3D Viewer      : http://$NAT_IP:3000"
    echo "  • REST API / Metrics : http://$NAT_IP:8080"
    echo "======================================================"
else
    echo "❌ Erreur lors du démarrage de l'instance."
fi
