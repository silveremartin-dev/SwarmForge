#!/usr/bin/env bash
# SwarmForge Release & Server Packaging Script (Linux / macOS)
# Copyright (c) 2022-2026 Silvère Martin-Michiellot / SwarmForge
#
# This script produces the cross-platform server release artifact:
#
#   SwarmForge-vX.Y.Z-Server-CrossPlatform.zip  (~480 MB)
#      A "bring your own JRE" headless server bundle — no JRE embedded.
#      Requires Java 21+ to be installed on the target machine (Linux/macOS/Windows).
#      Intended for Docker, Kubernetes, GCP VM, or bare-metal server deployments.
#      Includes: server JAR, all dependency JARs (/lib), Dockerfile, docker-compose.yml,
#                run-server.sh (Linux/macOS), run-server.bat (Windows), samplenests.
#      Platform: Cross-platform (pure JARs).
#
# NOTE: The Windows Standalone (Windows-x64-Standalone.zip, ~650 MB) can ONLY be built
#       on Windows using package-release.ps1, because jpackage generates platform-native
#       application images (it embeds a stripped JRE from the host OS, ~200-250 MB).
#       The Windows Standalone is larger solely because of the embedded JRE.
#
# Usage:
#   ./scripts/package-release.sh [VERSION] [--skip-build]
#   ./scripts/package-release.sh 1.0.0-beta.1
#   ./scripts/package-release.sh 1.0.0-beta.1 --skip-build

set -euo pipefail

# ─── Parameters ───────────────────────────────────────────────────────────────
VERSION="${1:-1.0.0-beta.1}"
SKIP_BUILD=false
for arg in "$@"; do
  [[ "$arg" == "--skip-build" ]] && SKIP_BUILD=true
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

echo "================================================================"
echo "   SwarmForge v${VERSION} - Release Package Generator (Linux/macOS)"
echo "================================================================"

# ─── Check Java ───────────────────────────────────────────────────────────────
if ! command -v java &>/dev/null; then
  echo "ERROR: java not found. Please install JDK 21+ and ensure it is on PATH." >&2
  exit 1
fi
JAVA_VER=$(java -version 2>&1 | head -1)
echo " [OK] Java: $JAVA_VER"

if ! command -v mvn &>/dev/null; then
  echo "ERROR: mvn not found. Please install Apache Maven 3.9+ and ensure it is on PATH." >&2
  exit 1
fi
echo " [OK] Maven: $(mvn --version | head -1)"

# ─── 1. Build Maven modules ───────────────────────────────────────────────────
if [ "$SKIP_BUILD" = false ]; then
  echo ""
  echo "[1/5] Building Maven modules (Core, Server, Editor)..."
  mvn clean install -pl swarmforge-editor -am -DskipTests
  echo ""
  echo "[2/5] Copying runtime dependencies..."
  mvn dependency:copy-dependencies \
    -pl swarmforge-editor -am \
    -DincludeScope=runtime \
    -DoutputDirectory=target/libs
else
  echo ""
  echo "[SKIP] Skipping Maven build (--skip-build specified)."
fi

# ─── 2. Staging directories ───────────────────────────────────────────────────
DIST_DIR="$REPO_ROOT/dist"
RELEASE_DIR="$DIST_DIR/release"
STAGING_DIR="$DIST_DIR/staging"
SERVER_STAGE="$STAGING_DIR/server-bundle"

rm -rf "$STAGING_DIR"
mkdir -p "$SERVER_STAGE/lib"
mkdir -p "$RELEASE_DIR"

echo ""
echo "[3/5] Staging server JARs..."

# Server JAR
SERVER_JAR=$(find swarmforge-server/target -maxdepth 1 -name "swarmforge-server-*.jar" \
  ! -name "*original*" 2>/dev/null | head -1)
if [ -z "$SERVER_JAR" ]; then
  echo "ERROR: Could not find swarmforge-server JAR in swarmforge-server/target." >&2
  exit 1
fi
cp "$SERVER_JAR" "$SERVER_STAGE/swarmforge-server.jar"

# Core JAR
CORE_JAR=$(find swarmforge-core/target -maxdepth 1 -name "swarmforge-core-*.jar" \
  ! -name "*original*" 2>/dev/null | head -1)
[ -n "$CORE_JAR" ] && cp "$CORE_JAR" "$SERVER_STAGE/lib/"

# Runtime dependency JARs (from editor, which has the full dependency tree)
cp swarmforge-editor/target/libs/*.jar "$SERVER_STAGE/lib/" 2>/dev/null || true

# Docker / compose / license / samplenests
[ -f "Dockerfile" ]          && cp "Dockerfile"          "$SERVER_STAGE/"
[ -f "docker-compose.yml" ]  && cp "docker-compose.yml"  "$SERVER_STAGE/"
[ -f "LICENSE" ]             && cp "LICENSE"             "$SERVER_STAGE/"
[ -d "samplenests" ]         && cp -r "samplenests"      "$SERVER_STAGE/"

# ─── 3. Launchers ─────────────────────────────────────────────────────────────
echo "[4/5] Generating launcher scripts..."

cat > "$SERVER_STAGE/run-server.sh" << 'EOF'
#!/usr/bin/env bash
# SwarmForge Dedicated Server Launcher (Linux / macOS)
# Requires: Java 21+ installed on the host
cd "$(dirname "$0")"
java -Xmx4g \
     --enable-native-access=ALL-UNNAMED \
     -cp "swarmforge-server.jar:lib/*" \
     org.swarmforge.server.SimulationServer "$@"
EOF
chmod +x "$SERVER_STAGE/run-server.sh"

cat > "$SERVER_STAGE/run-server.bat" << 'EOF'
@echo off
title SwarmForge Dedicated Server
cd /d "%~dp0"
java -Xmx4g --enable-native-access=ALL-UNNAMED -cp "swarmforge-server.jar;lib/*" org.swarmforge.server.SimulationServer %*
pause
EOF

cat > "$SERVER_STAGE/README.txt" << EOF
SwarmForge v${VERSION} - Server CrossPlatform Bundle
=====================================================

REQUIREMENTS
  - Java 21+ (JRE or JDK) installed on the host machine.
    Download: https://adoptium.net/

QUICK START (Linux / macOS)
  chmod +x run-server.sh
  ./run-server.sh

QUICK START (Windows)
  run-server.bat

DOCKER
  docker compose up -d

NOTE: This package does NOT embed a JRE. See the Windows Standalone
      package (Windows-x64-Standalone.zip) for a fully self-contained
      build that requires no Java installation.
EOF

# ─── 4. Archive & Checksums ───────────────────────────────────────────────────
echo ""
echo "[5/5] Generating release archive and checksum..."

ZIP_NAME="SwarmForge-v${VERSION}-Server-CrossPlatform.zip"
ZIP_PATH="$RELEASE_DIR/$ZIP_NAME"

echo " -> Compressing $ZIP_NAME..."
(cd "$SERVER_STAGE" && zip -r -9 "$ZIP_PATH" .)

echo " -> Calculating SHA256 checksum..."
CHECKSUM_FILE="$RELEASE_DIR/SHA256SUMS.txt"
if command -v sha256sum &>/dev/null; then
  (cd "$RELEASE_DIR" && sha256sum "$ZIP_NAME" | tee -a "$CHECKSUM_FILE")
elif command -v shasum &>/dev/null; then
  (cd "$RELEASE_DIR" && shasum -a 256 "$ZIP_NAME" | tee -a "$CHECKSUM_FILE")
else
  echo "WARNING: Neither sha256sum nor shasum found — skipping checksum." >&2
fi

echo ""
echo "================================================================"
echo " [COMPLETED] Release bundle created in:"
echo "  $RELEASE_DIR"
echo "================================================================"
