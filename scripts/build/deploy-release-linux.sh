#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Immediate Linux Release Deployment & Packaging Script
# Generates standalone Linux release bundles (.tar.gz and .zip) with executable launchers.
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
echo "   🚀 SwarmForge v${VERSION} - Linux Release Packager & Deployer             "
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
STAGING_DIR="$DIST_DIR/staging/linux-bundle"

rm -rf "$STAGING_DIR"
mkdir -p "$STAGING_DIR/lib"
mkdir -p "$RELEASE_DIR"

echo ""
echo "[3/4] Assembling Linux distribution bundle..."

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

# Generate Linux Launcher Scripts
cat > "$STAGING_DIR/run-editor.sh" << 'EOF'
#!/usr/bin/env bash
cd "$(dirname "$0")"
java -Xmx4g \
     --enable-native-access=ALL-UNNAMED \
     --add-modules jdk.incubator.vector \
     -cp "swarmforge-editor.jar:swarmforge-core.jar:swarmforge-server.jar:lib/*" \
     org.swarmforge.client.Launcher "$@"
EOF

cat > "$STAGING_DIR/run-server.sh" << 'EOF'
#!/usr/bin/env bash
cd "$(dirname "$0")"
java -Xmx4g \
     --enable-native-access=ALL-UNNAMED \
     --add-modules jdk.incubator.vector \
     -cp "swarmforge-server.jar:swarmforge-core.jar:lib/*" \
     org.swarmforge.server.SimulationServer "$@"
EOF

cat > "$STAGING_DIR/run-client.sh" << 'EOF'
#!/usr/bin/env bash
cd "$(dirname "$0")"
java -Xmx2g \
     --enable-native-access=ALL-UNNAMED \
     -cp "swarmforge-client.jar:swarmforge-core.jar:lib/*" \
     org.swarmforge.client.ClientApp "$@"
EOF

cat > "$STAGING_DIR/run-computenode.sh" << 'EOF'
#!/usr/bin/env bash
cd "$(dirname "$0")"
java -Xmx4g \
     --enable-native-access=ALL-UNNAMED \
     --add-modules jdk.incubator.vector \
     -cp "swarmforge-compute.jar:swarmforge-core.jar:lib/*" \
     org.swarmforge.compute.ComputeNodeApp "$@"
EOF

chmod +x "$STAGING_DIR"/*.sh

echo ""
echo "[4/4] Generating Linux release archives (.tar.gz and .zip)..."

TAR_NAME="SwarmForge-v${VERSION}-Linux-x64.tar.gz"
ZIP_NAME="SwarmForge-v${VERSION}-Linux-x64.zip"
TAR_PATH="$RELEASE_DIR/$TAR_NAME"
ZIP_PATH="$RELEASE_DIR/$ZIP_NAME"

tar -czf "$TAR_PATH" -C "$STAGING_DIR" .
if command -v zip &>/dev/null; then
  (cd "$STAGING_DIR" && zip -r -9 -q "$ZIP_PATH" .)
fi

# Checksums
CHECKSUM_FILE="$RELEASE_DIR/SHA256SUMS.txt"
cd "$RELEASE_DIR"
if command -v sha256sum &>/dev/null; then
  sha256sum "$TAR_NAME" >> "$CHECKSUM_FILE"
  [ -f "$ZIP_NAME" ] && sha256sum "$ZIP_NAME" >> "$CHECKSUM_FILE"
elif command -v shasum &>/dev/null; then
  shasum -a 256 "$TAR_NAME" >> "$CHECKSUM_FILE"
  [ -f "$ZIP_NAME" ] && shasum -a 256 "$ZIP_NAME" >> "$CHECKSUM_FILE"
fi

echo "=============================================================================="
echo "   ✅ Linux Release Packages Generated Successfully!                         "
echo "   • Archive (tar.gz) : $TAR_PATH"
[ -f "$ZIP_PATH" ] && echo "   • Archive (zip)    : $ZIP_PATH"
echo "=============================================================================="
