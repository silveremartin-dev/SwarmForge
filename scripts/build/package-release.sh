#!/usr/bin/env bash
# ==============================================================================
# SwarmForge Release & Standalone Packaging Entry Script (Linux / macOS)
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
exec "$SCRIPT_DIR/deploy-release-all.sh" "$@"
