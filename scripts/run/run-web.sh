#!/usr/bin/env bash
# ==============================================================================
# SwarmForge Web Client Launcher (Linux / macOS) - Client Seul
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
echo "             SwarmForge - Client Web Léger (Client Seul)                      "
echo "=============================================================================="
echo ""

if nc -z 127.0.0.1 8081 2>/dev/null; then
    echo "[INFO] Serveur Java SwarmForge actif sur ws://localhost:8081"
else
    echo "[INFO] Serveur Java SwarmForge non détecté sur le port 8081 (mode autonome)."
    echo "       Pour démarrer le client et le serveur : ./scripts/run/run-web-and-server.sh"
fi
echo ""

if command -v node >/dev/null 2>&1 && command -v npm >/dev/null 2>&1 && [ -d "$ROOT_DIR/swarmforge-web/src" ]; then
    cd "$ROOT_DIR/swarmforge-web"
    if [ ! -d "node_modules" ]; then
        echo "[INFO] Installation des dépendances NPM..."
        npm install
    fi
    echo "[INFO] Lancement du serveur Vite Dev sur le port $PORT..."
    npm run dev -- --host --port "$PORT" --open
    exit 0
fi

if command -v python3 >/dev/null 2>&1; then
    echo "[INFO] Distribution du client Web sur http://localhost:$PORT..."
    python3 "$SCRIPT_DIR/web_server.py" --port "$PORT"
    exit 0
elif command -v python >/dev/null 2>&1; then
    echo "[INFO] Distribution du client Web sur http://localhost:$PORT..."
    python "$SCRIPT_DIR/web_server.py" --port "$PORT"
    exit 0
fi

echo "[ERREUR] Ni Node.js ni Python n'ont été trouvés sur le système."
exit 1
