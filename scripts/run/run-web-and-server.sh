#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Lanceur Combiné Serveur Java & Client Web (Linux / macOS)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

PORT=5173

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --port) PORT="$2"; shift ;;
        *) echo "Paramètre inconnu: $1"; exit 1 ;;
    esac
    shift
done

echo "=============================================================================="
echo "        SwarmForge - Démarrage Combiné Serveur Java & Client Web              "
echo "=============================================================================="
echo ""

if ! nc -z 127.0.0.1 8081 2>/dev/null; then
    if command -v mvn >/dev/null 2>&1; then
        echo "[1/2] Lancement du Serveur Java SwarmForge en arrière-plan..."
        mvn exec:java -pl swarmforge-server -Dexec.args="--local" &
        SERVER_PID=$!
        echo "      PID Serveur: $SERVER_PID. Attente de l'initialisation..."
        sleep 3
    fi
else
    echo "[1/2] Serveur Java SwarmForge déjà actif sur :8081 / :50051."
fi

echo "[2/2] Lancement du Client Web..."
"$SCRIPT_DIR/run-web.sh" --port "$PORT"
