#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - All-in-One Multi-Platform Release Deployer (Linux, macOS, Windows)
# Builds and deploys release packages for all three target operating systems in one step.
# ==============================================================================

set -euo pipefail

VERSION="1.0.0-beta.3"
SKIP_BUILD=false
PUBLISH_GH=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-build)
      SKIP_BUILD=true
      shift
      ;;
    --version|-v)
      VERSION="$2"
      shift 2
      ;;
    --publish|-p)
      PUBLISH_GH=true
      shift
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
echo "   🌍 SwarmForge v${VERSION} - Multi-Platform Release Deployer (All OS)        "
echo "   Platforms: Linux (x64), macOS (Universal), Windows (x64)                   "
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
  echo "[1/5] Building all Maven modules..."
  mvn clean install -pl swarmforge-editor,swarmforge-server,swarmforge-client,swarmforge-compute,swarmforge-plugins -am -DskipTests
  echo ""
  echo "[2/5] Copying all runtime dependencies..."
  mvn dependency:copy-dependencies \
    -pl swarmforge-editor,swarmforge-server,swarmforge-client,swarmforge-compute \
    -am \
    -DincludeScope=runtime \
    -DoutputDirectory=target/libs
else
  echo ""
  echo "[SKIP] Skipping Maven compilation (--skip-build specified)."
fi

DIST_DIR="$REPO_ROOT/dist"
RELEASE_DIR="$DIST_DIR/release"
mkdir -p "$RELEASE_DIR"
rm -f "$RELEASE_DIR/SHA256SUMS.txt"

echo ""
echo "[3/5] Packaging platform-specific release distributions..."

echo " -> [Linux] Generating Linux release bundle..."
"$SCRIPT_DIR/deploy-release-linux.sh" --skip-build --version "$VERSION"

echo ""
echo " -> [macOS] Generating macOS release bundle..."
"$SCRIPT_DIR/deploy-release-macos.sh" --skip-build --version "$VERSION"

echo ""
echo " -> [Windows] Generating Windows release bundle..."
"$SCRIPT_DIR/deploy-release-windows.sh" --skip-build --version "$VERSION"

echo ""
echo "[4/5] Packaging Universal Multi-Platform Bundle (All-in-One)..."

ALL_STAGE="$DIST_DIR/staging/all-platforms"
rm -rf "$ALL_STAGE"
mkdir -p "$ALL_STAGE/lib"

# Copy jars
for mod in server editor client compute core plugins; do
  JAR_FILE=$(find "swarmforge-$mod/target" -maxdepth 1 -name "swarmforge-$mod-*.jar" ! -name "*original*" ! -name "*sources*" ! -name "*javadoc*" 2>/dev/null | head -1 || true)
  if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
    cp "$JAR_FILE" "$ALL_STAGE/swarmforge-$mod.jar"
  fi
done

# Copy libs
find . -type d -name "libs" -path "*/target/libs" | while read -r libdir; do
  cp -u "$libdir"/*.jar "$ALL_STAGE/lib/" 2>/dev/null || true
done

# Copy shared resources
[ -d "samplenests" ]        && cp -r "samplenests"        "$ALL_STAGE/"
[ -f "Dockerfile" ]         && cp "Dockerfile"            "$ALL_STAGE/"
[ -f "docker-compose.yml" ] && cp "docker-compose.yml"    "$ALL_STAGE/"
[ -f "envoy.yaml" ]         && cp "envoy.yaml"            "$ALL_STAGE/"
[ -f "prometheus.yml" ]     && cp "prometheus.yml"        "$ALL_STAGE/"
[ -f "LICENSE" ]            && cp "LICENSE"               "$ALL_STAGE/"
[ -f "README.md" ]          && cp "README.md"             "$ALL_STAGE/"

# Copy all launchers (Windows .bat, Linux/macOS .sh, macOS .command)
cp "$DIST_DIR/staging/windows-bundle"/*.bat "$ALL_STAGE/" 2>/dev/null || true
cp "$DIST_DIR/staging/linux-bundle"/*.sh "$ALL_STAGE/" 2>/dev/null || true
cp "$DIST_DIR/staging/macos-bundle"/*.command "$ALL_STAGE/" 2>/dev/null || true
chmod +x "$ALL_STAGE"/*.sh "$ALL_STAGE"/*.command 2>/dev/null || true

UNIVERSAL_ZIP="SwarmForge-v${VERSION}-AllPlatforms.zip"
(cd "$ALL_STAGE" && zip -r -9 -q "$RELEASE_DIR/$UNIVERSAL_ZIP" .)

if command -v sha256sum &>/dev/null; then
  (cd "$RELEASE_DIR" && sha256sum "$UNIVERSAL_ZIP" >> "$RELEASE_DIR/SHA256SUMS.txt")
elif command -v shasum &>/dev/null; then
  (cd "$RELEASE_DIR" && shasum -a 256 "$UNIVERSAL_ZIP" >> "$RELEASE_DIR/SHA256SUMS.txt")
fi

echo ""
echo "[5/5] Release Summary & Verification..."
echo "------------------------------------------------------------------------------"
ls -lh "$RELEASE_DIR"
echo "------------------------------------------------------------------------------"

if [ "$PUBLISH_GH" = true ]; then
  if command -v gh &>/dev/null; then
    echo "Publishing release v${VERSION} to GitHub..."
    gh release create "v${VERSION}" "$RELEASE_DIR"/* --title "SwarmForge v${VERSION}" --generate-notes
    echo "✅ Published to GitHub Releases!"
  else
    echo "[WARN] GitHub CLI ('gh') is not installed. Skipping automatic GitHub publishing."
  fi
fi

echo ""
echo "=============================================================================="
echo "   🎉 All releases deployed successfully in: $RELEASE_DIR                     "
echo "=============================================================================="
