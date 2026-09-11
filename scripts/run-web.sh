#!/usr/bin/env bash
# SwarmForge Web Client Launcher (Linux / macOS)
# Automatically detects Node.js/Vite or falls back to local HTTP server.

set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DIR"

TARGET_DIR="swarmforge-web"
PORT=5173

while [[ "$#" -gt 0 ]]; do
    case $1 in
        --static) TARGET_DIR="swarmforge-web-client"; PORT=8080 ;;
        --port) PORT="$2"; shift ;;
        *) echo "Unknown parameter passed: $1"; exit 1 ;;
    esac
    shift
done

echo "========================================"
echo "  SwarmForge - Web Client Launcher"
echo "========================================"
echo ""

if command -v node >/dev/null 2>&1 && command -v npm >/dev/null 2>&1 && [ "$TARGET_DIR" = "swarmforge-web" ]; then
    echo "[1/2] Checking dependencies for swarmforge-web..."
    cd swarmforge-web
    if [ ! -d "node_modules" ]; then
        echo "[INFO] Installing NPM dependencies..."
        npm install
    fi
    echo "[2/2] Starting SwarmForge Web Client (Vite Dev Server)..."
    echo "URL: http://localhost:$PORT/"
    npm run dev -- --host --port "$PORT" --open
    exit 0
fi

if command -v python3 >/dev/null 2>&1; then
    echo "[INFO] Starting local Web server on port $PORT using Python 3..."
    echo "URL: http://localhost:$PORT/"
    python3 -m http.server "$PORT" --directory "$TARGET_DIR"
    exit 0
elif command -v python >/dev/null 2>&1; then
    echo "[INFO] Starting local Web server on port $PORT using Python..."
    echo "URL: http://localhost:$PORT/"
    python -m http.server "$PORT" --directory "$TARGET_DIR"
    exit 0
fi

echo "ERROR: Neither Node.js nor Python 3 found on PATH."
exit 1
