#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Lanceur de Test d'Integration Tout-en-Un (Linux / macOS)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

SCENARIO_ID="${1:-4}"
WEB_COUNT="${2:-2}"
LAUNCH_EDITOR="${3:-N}"
LAUNCH_COMPUTE="${4:-N}"

echo "=============================================================================="
echo "       SwarmForge - Banc d'Essai & Test d'Integration Tout-en-Un             "
echo "=============================================================================="
echo ""
echo "[1/4] Demarrage du Serveur Java SwarmForge (Scenario #$SCENARIO_ID)..."
mvn exec:java -pl swarmforge-server -Dexec.args="--scenario $SCENARIO_ID" &
SERVER_PID=$!

echo "[2/4] Demarrage du Serveur HTTP Statique (Port 5173)..."
python3 "$SCRIPT_DIR/serve_web_static.py" 5173 &
HTTP_PID=$!

trap "kill $SERVER_PID $HTTP_PID 2>/dev/null" EXIT

if [ "$LAUNCH_COMPUTE" = "O" ] || [ "$LAUNCH_COMPUTE" = "o" ] || [ "$LAUNCH_COMPUTE" = "Y" ] || [ "$LAUNCH_COMPUTE" = "y" ]; then
    echo "[Option] Lancement du Noeud de Calcul Headless..."
    mvn exec:java -pl swarmforge-compute &
fi

echo "[3/4] Attente de 4 secondes pour initialisation..."
sleep 4

if [ "$WEB_COUNT" -gt 0 ]; then
    echo "[4/4] Ouverture de $WEB_COUNT client(s) Web..."
    for ((i=1; i<=WEB_COUNT; i++)); do
        if command -v xdg-open >/dev/null 2>&1; then
            xdg-open http://localhost:5173 >/dev/null 2>&1 &
        elif command -v open >/dev/null 2>&1; then
            open http://localhost:5173 &
        fi
    done
fi

if [ "$LAUNCH_EDITOR" = "O" ] || [ "$LAUNCH_EDITOR" = "o" ] || [ "$LAUNCH_EDITOR" = "Y" ] || [ "$LAUNCH_EDITOR" = "y" ]; then
    echo "[Option] Lancement du Studio SwarmForge..."
    mvn exec:java -pl swarmforge-editor
else
    wait $SERVER_PID
fi
