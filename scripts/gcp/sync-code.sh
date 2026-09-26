#!/bin/bash
# ==============================================================================
# SwarmForge - Sync Local Code to GCP VM (Bash / Cloud Shell / macOS / Linux)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================

PROJECT="swarmforge-509813"
ZONE="europe-west1-b"
INSTANCE="swarmforge-vm"
ARCHIVE="code_sync.tar.gz"

echo "=========================================================="
echo "   📦 Synchronisation du code local vers la VM GCP...     "
echo "=========================================================="

# 1. Créer une archive locale légère
echo "[1/3] Empaquetage du code source..."
tar --exclude="*/target" \
    --exclude="target" \
    --exclude="*/node_modules" \
    --exclude="node_modules" \
    --exclude="*/dist" \
    --exclude="dist" \
    --exclude=".git" \
    --exclude="javadoc" \
    --exclude="captures" \
    --exclude="logs" \
    --exclude="*.sql" \
    --exclude="*.dump" \
    --exclude="*.zip" \
    --exclude="*.exe" \
    --exclude="*.jar" \
    -czf "$ARCHIVE" \
    swarmforge-core swarmforge-server swarmforge-compute swarmforge-benchmarks swarmforge-plugins swarmforge-editor swarmforge-web pom.xml docker-compose.yml deploy-gcp.sh scripts docs

# 2. Transférer l'archive
echo "[2/3] Envoi vers la VM..."
gcloud compute scp --quiet "$ARCHIVE" "$INSTANCE:~/$ARCHIVE" --project=$PROJECT --zone=$ZONE

# 3. Extraire l'archive sur la VM
echo "[3/3] Décompression..."
UNPACK_CMD="sudo rm -rf ~/swarmforge && mkdir -p ~/swarmforge && tar -xzf ~/$ARCHIVE -C ~/swarmforge && rm -f ~/$ARCHIVE"
gcloud compute ssh --quiet "$INSTANCE" --project=$PROJECT --zone=$ZONE --command="$UNPACK_CMD"

# Nettoyage
rm -f "$ARCHIVE"

echo ""
echo "=========================================================="
echo "   ✅ Code synchronisé avec succès sur la VM GCP !       "
echo "=========================================================="
