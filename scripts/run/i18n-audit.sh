#!/usr/bin/env bash
# ==============================================================================
# SwarmForge Universal 5-Language I18n, Tooltip & Hardcoded String Audit Launcher
# ==============================================================================

echo "=============================================================================="
echo "      SwarmForge - Universal 5-Language I18n & Tooltip Audit Suite"
echo "=============================================================================="
echo ""

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/../.."

if command -v python3 &> /dev/null; then
    python3 scripts/i18n/i18n_validator_and_translator.py "$@"
elif command -v python &> /dev/null; then
    python scripts/i18n/i18n_validator_and_translator.py "$@"
elif command -v py &> /dev/null; then
    py scripts/i18n/i18n_validator_and_translator.py "$@"
else
    echo "[ERREUR] Python 3 est introuvable dans le PATH système."
    exit 1
fi
