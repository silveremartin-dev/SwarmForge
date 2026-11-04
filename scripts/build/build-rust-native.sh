#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Build Rust Native Acceleration Kernels (Linux / macOS)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

echo "=================================================="
echo "  SwarmForge - Rust Native Engine Compilation"
echo "=================================================="
echo ""

if ! command -v cargo &>/dev/null; then
    echo "[AVERTISSEMENT] Cargo (Rust) non détecté. Le moteur utilisera le fallback pur Java."
    exit 0
fi

if [ -d "swarmforge-core/src/main/rust" ]; then
    echo "[INFO] Compilation des modules Rust natifs..."
    cd swarmforge-core/src/main/rust
    cargo build --release
    echo "[SUCCÈS] Bibliothèques natives compilées."
fi
