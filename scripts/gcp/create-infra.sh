#!/bin/bash
# ==============================================================================
# SwarmForge - Create GCP Infrastructure (Bash / Cloud Shell)
# Project: swarmforge-509813 | Zone: europe-west1-b | Machine: c2-standard-4 / c3-standard-4
# ==============================================================================

PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="${1:-swarmforge-vm}"
MACHINE_TYPE="${2:-c2-standard-4}"

echo "=========================================================="
echo "   🚀 Initialisation de l'infrastructure Google Cloud     "
echo "=========================================================="

# 1. Règle Pare-feu (Firewall)
echo "[1/2] Configuration des règles de pare-feu..."
gcloud compute firewall-rules create allow-swarmforge \
    --project=$PROJECT \
    --allow=tcp:50051,tcp:8080,tcp:3000,tcp:8081 \
    --target-tags=swarmforge-node \
    --description="Autoriser le trafic SwarmForge gRPC, Web et REST"

# 2. Création de la VM
echo "[2/2] Création de la machine virtuelle ($MACHINE_TYPE)..."
gcloud compute instances create $INSTANCE \
    --project=$PROJECT \
    --zone=$ZONE \
    --machine-type=$MACHINE_TYPE \
    --tags=swarmforge-node,http-server \
    --image-family=ubuntu-2204-lts \
    --image-project=ubuntu-os-cloud \
    --boot-disk-size=30GB

if [ $? -eq 0 ]; then
    NAT_IP=$(gcloud compute instances describe $INSTANCE --project=$PROJECT --zone=$ZONE --format="get(networkInterfaces[0].accessConfigs[0].natIP)")
    echo ""
    echo "=========================================================="
    echo "   ✅ VM Créée avec succès ! IP Publique : $NAT_IP"
    echo "=========================================================="
    echo "Pour vous connecter en SSH :"
    echo "  gcloud compute ssh $INSTANCE --project=$PROJECT --zone=$ZONE"
else
    echo "❌ Erreur lors de la création de la VM."
fi
