#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - 1-Click Root Launcher (Linux / macOS)
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo "=============================================================================="
echo "   🐝 SwarmForge - Simulation Platform Launcher (JavaFX / 3D Engine)          "
echo "=============================================================================="

if ! command -v java &>/dev/null; then
  echo "[ERREUR] Java 21+ est requis mais introuvable dans le PATH."
  echo "Veuillez installer OpenJDK 21 ou supérieur (ex: sdk install java 21.0.2-tem)."
  exit 1
fi

if [ -f "dist/staging/linux-bundle/run-editor.sh" ]; then
  exec "dist/staging/linux-bundle/run-editor.sh" "$@"
elif [ -f "dist/staging/macos-bundle/run-editor.sh" ]; then
  exec "dist/staging/macos-bundle/run-editor.sh" "$@"
else
  exec "./scripts/run/run-editor.sh" "$@"
fi
