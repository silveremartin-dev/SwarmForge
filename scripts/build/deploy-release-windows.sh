#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Windows Release Deployment & Packaging Script (Bash / Cross-Platform)
# Generates standalone Windows release bundles (.zip) with .bat launchers from Linux/macOS.
# ==============================================================================

set -euo pipefail

VERSION="1.0.0-beta.3"
SKIP_BUILD=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-build)
      SKIP_BUILD=true
      shift
      ;;
    --version)
      VERSION="$2"
      shift 2
      ;;
    -v|--ver)
      VERSION="$2"
      shift 2
      ;;
    *)
      if [[ "$1" =~ ^[0-9] ]]; then
        VERSION="$1"
      fi
      shift
      ;;
  esac
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$REPO_ROOT"

echo "=============================================================================="
echo "   🚀 SwarmForge v${VERSION} - Windows Release Packager (Cross-Platform)     "
echo "=============================================================================="

if ! command -v java &>/dev/null; then
  echo "[ERROR] 'java' command not found. Please install JDK 21+ and ensure it is on PATH." >&2
  exit 1
fi
if ! command -v mvn &>/dev/null; then
  echo "[ERROR] 'mvn' command not found. Please install Apache Maven 3.9+ and ensure it is on PATH." >&2
  exit 1
fi

if [ "$SKIP_BUILD" = false ]; then
  echo ""
  echo "[1/4] Building Maven modules (Core, Server, Editor, Client, Compute, Plugins)..."
  mvn clean install -pl swarmforge-editor,swarmforge-server,swarmforge-client,swarmforge-compute,swarmforge-plugins -am -DskipTests
  echo ""
  echo "[2/4] Resolving runtime dependencies..."
  mvn dependency:copy-dependencies \
    -pl swarmforge-editor,swarmforge-server,swarmforge-client,swarmforge-compute \
    -am \
    -DincludeScope=runtime \
    -DoutputDirectory=target/libs
else
  echo ""
  echo "[SKIP] Skipping Maven build (--skip-build specified)."
fi

DIST_DIR="$REPO_ROOT/dist"
RELEASE_DIR="$DIST_DIR/release"
STAGING_DIR="$DIST_DIR/staging/windows-bundle"

rm -rf "$STAGING_DIR"
mkdir -p "$STAGING_DIR/lib"
mkdir -p "$RELEASE_DIR"

echo ""
echo "[3/4] Assembling Windows distribution bundle..."

# Copy Module JARs
for mod in server editor client compute core plugins; do
  JAR_FILE=$(find "swarmforge-$mod/target" -maxdepth 1 -name "swarmforge-$mod-*.jar" ! -name "*original*" ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -1 || true)
  if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
    cp "$JAR_FILE" "$STAGING_DIR/swarmforge-$mod.jar"
  fi
done

# Copy third-party runtime libraries
find . -type d -name "libs" -path "*/target/libs" | while read -r libdir; do
  cp -u "$libdir"/*.jar "$STAGING_DIR/lib/" 2>/dev/null || true
done

# Copy sample nests, configurations and licenses
[ -d "samplenests" ]        && cp -r "samplenests"        "$STAGING_DIR/"
[ -f "Dockerfile" ]         && cp "Dockerfile"            "$STAGING_DIR/"
[ -f "docker-compose.yml" ] && cp "docker-compose.yml"    "$STAGING_DIR/"
[ -f "envoy.yaml" ]         && cp "envoy.yaml"            "$STAGING_DIR/"
[ -f "prometheus.yml" ]     && cp "prometheus.yml"        "$STAGING_DIR/"
[ -f "LICENSE" ]            && cp "LICENSE"               "$STAGING_DIR/"
[ -f "README.md" ]          && cp "README.md"             "$STAGING_DIR/"

# Generate Windows Launcher Scripts (.bat with CRLF)
printf "@echo off\r\ntitle SwarmForge Studio Editor\r\ncd /d \"%%%%~dp0\"\r\njava -Xmx4g --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector -cp \"swarmforge-editor.jar;swarmforge-core.jar;swarmforge-server.jar;lib/*\" org.swarmforge.client.Launcher %%%%*\r\npause\r\n" > "$STAGING_DIR/run-editor.bat"

printf "@echo off\r\ntitle SwarmForge Dedicated Server\r\ncd /d \"%%%%~dp0\"\r\njava -Xmx4g --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector -cp \"swarmforge-server.jar;swarmforge-core.jar;lib/*\" org.swarmforge.server.SimulationServer %%%%*\r\npause\r\n" > "$STAGING_DIR/run-server.bat"

printf "@echo off\r\ntitle SwarmForge 3D Client Viewer\r\ncd /d \"%%%%~dp0\"\r\njava -Xmx2g --enable-native-access=ALL-UNNAMED -cp \"swarmforge-client.jar;swarmforge-core.jar;lib/*\" org.swarmforge.client.ClientApp %%%%*\r\npause\r\n" > "$STAGING_DIR/run-client.bat"

printf "@echo off\r\ntitle SwarmForge Compute Node\r\ncd /d \"%%%%~dp0\"\r\njava -Xmx4g --enable-native-access=ALL-UNNAMED --add-modules jdk.incubator.vector -cp \"swarmforge-compute.jar;swarmforge-core.jar;lib/*\" org.swarmforge.compute.ComputeNodeApp %%%%*\r\npause\r\n" > "$STAGING_DIR/run-computenode.bat"

echo ""
echo "[4/4] Generating Windows release archive (.zip)..."

ZIP_NAME="SwarmForge-v${VERSION}-Windows-x64.zip"
ZIP_PATH="$RELEASE_DIR/$ZIP_NAME"

if command -v zip &>/dev/null; then
  (cd "$STAGING_DIR" && zip -r -9 -q "$ZIP_PATH" .)
elif command -v tar &>/dev/null; then
  tar -a -c -f "$ZIP_PATH" -C "$STAGING_DIR" *
fi

# Checksums
CHECKSUM_FILE="$RELEASE_DIR/SHA256SUMS.txt"
cd "$RELEASE_DIR"
if command -v sha256sum &>/dev/null; then
  sha256sum "$ZIP_NAME" >> "$CHECKSUM_FILE"
elif command -v shasum &>/dev/null; then
  shasum -a 256 "$ZIP_NAME" >> "$CHECKSUM_FILE"
fi

echo "=============================================================================="
echo "   ✅ Windows Release Package Generated Successfully!                        "
echo "   • Archive (zip) : $ZIP_PATH"
echo "=============================================================================="
