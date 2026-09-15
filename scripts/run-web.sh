#!/usr/bin/env bash
# SwarmForge Web Client & Simulation Launcher (Linux / macOS)
# Starts the 3D Web Simulation Client + Integrated Live Simulation Server.

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$ROOT_DIR"

PORT=5173

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --port) PORT="$2"; shift ;;
        *) echo "Parametre inconnu: $1"; exit 1 ;;
    esac
    shift
done

echo "======================================================"
echo "      SwarmForge - Web Client & Serveur Simulation    "
echo "======================================================"
echo ""

if command -v python3 >/dev/null 2>&1; then
    echo "[INFO] Lancement du serveur complet [Web sur :$PORT + WebSocket sur :8081] avec Python 3..."
    python3 "$SCRIPT_DIR/mock_server.py"
    exit 0
elif command -v python >/dev/null 2>&1; then
    echo "[INFO] Lancement du serveur complet [Web sur :$PORT + WebSocket sur :8081] avec Python..."
    python "$SCRIPT_DIR/mock_server.py"
    exit 0
fi

if command -v node >/dev/null 2>&1 && command -v npm >/dev/null 2>&1; then
    cd "$ROOT_DIR/swarmforge-web"
    if [ ! -d "node_modules" ]; then
        echo "[INFO] Installation des dependances NPM..."
        npm install
    fi
    echo "[INFO] Lancement du serveur Vite Dev..."
    npm run dev -- --host --port "$PORT" --open
    exit 0
fi

echo "[ERREUR] Ni Python ni Node.js n'ont ete trouves sur le systeme."
echo "Veuillez verifier votre installation de Python ou Node.js."
exit 1
