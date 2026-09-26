#!/usr/bin/env bash
# SwarmForge Client Native Package Script (Linux / macOS)
# Copyright (c) 2022-2026 Silvère Martin-Michiellot / SwarmForge
#
# Equivalent of package_client.ps1 for Linux / macOS.
# Uses jpackage to generate a native app-image for the current platform.
#
# NOTE: jpackage generates platform-native binaries from the host OS only.
#       Run on Linux → Linux app-image. Run on macOS → macOS .app bundle.
#       For Windows .exe, use package_client.ps1 on a Windows machine.
#
# Usage:
#   ./scripts/package_client.sh [VERSION]
#   ./scripts/package_client.sh 1.0.0-beta.1

set -euo pipefail

VERSION="${1:-1.0.0-beta.1}"
APP_NAME="SwarmForgeClient"
MAIN_JAR="swarmforge-client-${VERSION}-SNAPSHOT.jar"
INPUT_DIR="target"
OUTPUT_DIR="dist"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

# ─── Checks ───────────────────────────────────────────────────────────────────
if ! command -v jpackage &>/dev/null; then
  echo "ERROR: jpackage not found. Please install JDK 21+ and ensure it is on PATH." >&2
  exit 1
fi
if ! command -v mvn &>/dev/null; then
  echo "ERROR: mvn not found. Please install Apache Maven 3.9+ and ensure it is on PATH." >&2
  exit 1
fi

echo "🚧 Building SwarmForge Client v${VERSION}..."
mvn clean package -pl swarmforge-client -am -DskipTests

CLIENT_DIR="$REPO_ROOT/swarmforge-client"
cd "$CLIENT_DIR"

# Ensure output directory is clean
if [ -d "$OUTPUT_DIR" ]; then
  rm -rf "$OUTPUT_DIR"
fi
mkdir -p "$OUTPUT_DIR"

echo "📦 Creating native package with jpackage..."
jpackage \
  --name          "$APP_NAME" \
  --app-version   "$VERSION" \
  --input         "$INPUT_DIR" \
  --main-jar      "$MAIN_JAR" \
  --type          app-image \
  --dest          "$OUTPUT_DIR" \
  --java-options  "-Djava.library.path=libs" \
  --description   "SwarmForge Eusocial Insect Simulation" \
  --vendor        "Silvere Martin-Michiellot"

echo "✅ Package created successfully in swarmforge-client/$OUTPUT_DIR"
