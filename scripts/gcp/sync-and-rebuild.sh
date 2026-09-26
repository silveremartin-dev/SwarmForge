#!/bin/bash
# ==============================================================================
# SwarmForge - One-Click Sync & Hot-Reload Rebuild on GCP (Bash / Cloud Shell)
# Project: swarmforge-509813 | VM: swarmforge-vm | Zone: europe-west1-b
# ==============================================================================
SERVICE=${1:-""}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=========================================================="
echo "   ⚡ SWARMFORGE : SYNC LOCAL CODE + CLUSTER HOT REBUILD  "
echo "=========================================================="

# 1. Sync
"$SCRIPT_DIR/sync-code.sh"
if [ $? -ne 0 ]; then
    echo "❌ Échec de la synchronisation."
    exit 1
fi

# 2. Rebuild
"$SCRIPT_DIR/rebuild-cloud.sh" "$SERVICE"
if [ $? -ne 0 ]; then
    echo "❌ Échec du rebuild."
    exit 1
fi

echo ""
echo "=========================================================="
echo "   🎉 Déploiement et Rebuild réussis avec succès !        "
echo "=========================================================="
