#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Javadoc Generation Script (Linux / macOS)
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

echo "=========================================="
echo " Building SwarmForge Javadoc Documentation "
echo "=========================================="

echo "[1/4] Installing modules in local repository..."
mvn install -DskipTests -q

echo "[2/4] Running Maven javadoc:javadoc..."
mvn javadoc:javadoc -Dquiet=true -Ddoclint=none -DfailOnError=false

echo "[3/4] Running Maven javadoc:aggregate..."
mvn javadoc:aggregate -Dquiet=true -Ddoclint=none -DfailOnError=false

echo "[4/4] Structuring output in javadoc directory..."

MODULES=("swarmforge-core" "swarmforge-server" "swarmforge-editor" "swarmforge-client" "swarmforge-compute" "swarmforge-benchmarks")
TEMP_MODULES_DIR="$ROOT_DIR/target/temp_module_javadocs"
TEMP_AGGREGATE_DIR="$ROOT_DIR/target/temp_aggregate_javadoc"

rm -rf "$TEMP_MODULES_DIR" "$TEMP_AGGREGATE_DIR"
mkdir -p "$TEMP_MODULES_DIR" "$TEMP_AGGREGATE_DIR"

for mod in "${MODULES[@]}"; do
    MOD_APIDOCS="$ROOT_DIR/$mod/javadoc/apidocs"
    if [ ! -d "$MOD_APIDOCS" ]; then
        MOD_APIDOCS="$ROOT_DIR/$mod/target/site/apidocs"
    fi
    if [ ! -d "$MOD_APIDOCS" ] && [ -f "$ROOT_DIR/$mod/javadoc/index.html" ]; then
        MOD_APIDOCS="$ROOT_DIR/$mod/javadoc"
    fi
    if [ -d "$MOD_APIDOCS" ]; then
        MOD_DEST="$TEMP_MODULES_DIR/$mod"
        echo "Collecting Javadoc for module '$mod'..."
        mkdir -p "$MOD_DEST"
        cp -r "$MOD_APIDOCS"/* "$MOD_DEST/"
    fi
done

AGG_APIDOCS="$ROOT_DIR/javadoc/apidocs"
if [ ! -d "$AGG_APIDOCS" ]; then
    AGG_APIDOCS="$ROOT_DIR/target/site/apidocs"
fi
if [ ! -d "$AGG_APIDOCS" ] && [ -f "$ROOT_DIR/javadoc/index.html" ]; then
    AGG_APIDOCS="$ROOT_DIR/javadoc"
fi
if [ -d "$AGG_APIDOCS" ]; then
    echo "Collecting Aggregate Javadoc..."
    cp -r "$AGG_APIDOCS"/* "$TEMP_AGGREGATE_DIR/"
fi

FINAL_JAVADOC_DIR="$ROOT_DIR/javadoc"
echo "[3/3] Structuring output in $FINAL_JAVADOC_DIR (removing apidocs sub-level)..."

rm -rf "$FINAL_JAVADOC_DIR"
mkdir -p "$FINAL_JAVADOC_DIR"

for mod in "${MODULES[@]}"; do
    rm -rf "$ROOT_DIR/$mod/javadoc"
done

if [ -f "$TEMP_AGGREGATE_DIR/index.html" ]; then
    cp -r "$TEMP_AGGREGATE_DIR"/* "$FINAL_JAVADOC_DIR/"
fi

for mod in "${MODULES[@]}"; do
    SRC_MOD="$TEMP_MODULES_DIR/$mod"
    if [ -f "$SRC_MOD/index.html" ]; then
        DEST_MOD="$FINAL_JAVADOC_DIR/$mod"
        echo "Placing module Javadoc: $DEST_MOD/index.html"
        mkdir -p "$DEST_MOD"
        cp -r "$SRC_MOD"/* "$DEST_MOD/"
    fi
done

rm -rf "$TEMP_MODULES_DIR" "$TEMP_AGGREGATE_DIR"

echo "=========================================="
echo " Javadoc successfully generated! "
echo " Aggregate: $FINAL_JAVADOC_DIR/index.html"
for mod in "${MODULES[@]}"; do
    if [ -f "$FINAL_JAVADOC_DIR/$mod/index.html" ]; then
        echo " Module $mod: $FINAL_JAVADOC_DIR/$mod/index.html"
    fi
done
echo "=========================================="
