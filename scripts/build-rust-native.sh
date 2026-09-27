#!/usr/bin/env bash
# ==============================================================================
# SwarmForge - Native Rust Simulation Engine Multiplatform Build Script (Bash)
# Compiles Windows (.dll), Linux (.so), and macOS (.dylib) release binaries.
# ==============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
CRATE_DIR="${ROOT_DIR}/crates/swarmforge-core-rust"
LIBS_DIR="${ROOT_DIR}/libs"
RESOURCES_DIR="${ROOT_DIR}/swarmforge-core/src/main/resources/native"

echo "======================================================================"
echo "  SwarmForge Native Rust Simulation Engine Multiplatform Builder      "
echo "======================================================================"
echo "Crate Location: ${CRATE_DIR}"
echo "Target Output : ${LIBS_DIR}"

mkdir -p "${LIBS_DIR}" "${RESOURCES_DIR}/windows" "${RESOURCES_DIR}/linux" "${RESOURCES_DIR}/macos"

if ! command -v cargo &> /dev/null; then
    echo "[WARN] Cargo / Rust toolchain not detected in PATH. Please install via https://rustup.rs/."
    echo "Pure Java 21 Artemis ECS mode remains 100% operational with automatic fallback."
    exit 0
fi

cd "${CRATE_DIR}"

echo "==> Compiling native release binary for host platform..."
cargo build --release

# Copy produced artifacts
if [ -f "target/release/swarmforge_core_rust.dll" ]; then
    cp "target/release/swarmforge_core_rust.dll" "${LIBS_DIR}/swarmforge_core_rust.dll"
    cp "target/release/swarmforge_core_rust.dll" "${RESOURCES_DIR}/windows/swarmforge_core_rust.dll"
    echo "✓ Windows Native Library compiled: ${LIBS_DIR}/swarmforge_core_rust.dll"
fi

if [ -f "target/release/libswarmforge_core_rust.so" ]; then
    cp "target/release/libswarmforge_core_rust.so" "${LIBS_DIR}/libswarmforge_core_rust.so"
    cp "target/release/libswarmforge_core_rust.so" "${RESOURCES_DIR}/linux/libswarmforge_core_rust.so"
    echo "✓ Linux Native Library compiled: ${LIBS_DIR}/libswarmforge_core_rust.so"
fi

if [ -f "target/release/libswarmforge_core_rust.dylib" ]; then
    cp "target/release/libswarmforge_core_rust.dylib" "${LIBS_DIR}/libswarmforge_core_rust.dylib"
    cp "target/release/libswarmforge_core_rust.dylib" "${RESOURCES_DIR}/macos/libswarmforge_core_rust.dylib"
    echo "✓ macOS Native Library compiled: ${LIBS_DIR}/libswarmforge_core_rust.dylib"
fi

echo "==> Running native unit tests..."
cargo test --release

echo "[SUCCESS] Rust native engine build and synchronization completed."
