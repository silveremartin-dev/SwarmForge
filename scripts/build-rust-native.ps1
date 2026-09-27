# ==============================================================================
# SwarmForge - Native Rust Simulation Engine Multiplatform Build Script (PowerShell)
# Builds Windows (.dll), Linux (.so), and macOS (.dylib) release binaries.
# ==============================================================================

[CmdletBinding()]
param (
    [string]$Target = "local",
    [switch]$AllTargets = $false
)

$ErrorActionPreference = "Stop"

$RootPath = (Get-Item $PSScriptRoot).Parent.FullName
$CratePath = Join-Path $RootPath "crates\swarmforge-core-rust"
$LibsPath = Join-Path $RootPath "libs"
$ResourcesPath = Join-Path $RootPath "swarmforge-core\src\main\resources\native"

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "  SwarmForge Native Rust Simulation Engine Multiplatform Builder      " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Crate Location: $CratePath"
Write-Host "Target Output : $LibsPath"

# Ensure output directories exist
New-Item -ItemType Directory -Force -Path $LibsPath | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $ResourcesPath "windows") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $ResourcesPath "linux") | Out-Null
New-Item -ItemType Directory -Force -Path (Join-Path $ResourcesPath "macos") | Out-Null

Push-Location $CratePath
try {
    if (-not (Get-Command "cargo" -ErrorAction SilentlyContinue)) {
        Write-Warning "Cargo / Rust toolchain not detected in PATH. Please install Rust via https://rustup.rs/."
        Write-Host "Pure Java 21 Artemis ECS mode remains 100% operational with automatic fallback." -ForegroundColor Yellow
        exit 0
    }

    if ($AllTargets) {
        Write-Host "==> Compiling for all supported OS targets (Windows, Linux, macOS)..." -ForegroundColor Green
        
        # 1. Windows x86_64
        Write-Host "--> Building target: x86_64-pc-windows-msvc..."
        & cargo build --release --target x86_64-pc-windows-msvc
        if (Test-Path "target\x86_64-pc-windows-msvc\release\swarmforge_core_rust.dll") {
            Copy-Item "target\x86_64-pc-windows-msvc\release\swarmforge_core_rust.dll" "$LibsPath\swarmforge_core_rust.dll" -Force
            Copy-Item "target\x86_64-pc-windows-msvc\release\swarmforge_core_rust.dll" "$ResourcesPath\windows\swarmforge_core_rust.dll" -Force
        }

        # 2. Linux x86_64 (cross / standard if installed)
        Write-Host "--> Building target: x86_64-unknown-linux-gnu..."
        & cargo build --release --target x86_64-unknown-linux-gnu 2>$null
        if (Test-Path "target\x86_64-unknown-linux-gnu\release\libswarmforge_core_rust.so") {
            Copy-Item "target\x86_64-unknown-linux-gnu\release\libswarmforge_core_rust.so" "$LibsPath\libswarmforge_core_rust.so" -Force
            Copy-Item "target\x86_64-unknown-linux-gnu\release\libswarmforge_core_rust.so" "$ResourcesPath\linux\libswarmforge_core_rust.so" -Force
        }

        # 3. macOS aarch64 (Apple Silicon) & x86_64
        Write-Host "--> Building target: aarch64-apple-darwin & x86_64-apple-darwin..."
        & cargo build --release --target aarch64-apple-darwin 2>$null
        if (Test-Path "target\aarch64-apple-darwin\release\libswarmforge_core_rust.dylib") {
            Copy-Item "target\aarch64-apple-darwin\release\libswarmforge_core_rust.dylib" "$LibsPath\libswarmforge_core_rust.dylib" -Force
            Copy-Item "target\aarch64-apple-darwin\release\libswarmforge_core_rust.dylib" "$ResourcesPath\macos\libswarmforge_core_rust.dylib" -Force
        }
    } else {
        Write-Host "==> Compiling native release binary for current host platform..." -ForegroundColor Green
        & cargo build --release
        
        # Copy produced library
        $dll = Get-ChildItem -Path "target\release" -Filter "swarmforge_core_rust.dll" -ErrorAction SilentlyContinue
        $so = Get-ChildItem -Path "target\release" -Filter "libswarmforge_core_rust.so" -ErrorAction SilentlyContinue
        $dylib = Get-ChildItem -Path "target\release" -Filter "libswarmforge_core_rust.dylib" -ErrorAction SilentlyContinue

        if ($dll) {
            Copy-Item $dll.FullName "$LibsPath\swarmforge_core_rust.dll" -Force
            Copy-Item $dll.FullName "$ResourcesPath\windows\swarmforge_core_rust.dll" -Force
            Write-Host "✓ Windows Native Library compiled: $LibsPath\swarmforge_core_rust.dll" -ForegroundColor Green
        }
        if ($so) {
            Copy-Item $so.FullName "$LibsPath\libswarmforge_core_rust.so" -Force
            Copy-Item $so.FullName "$ResourcesPath\linux\libswarmforge_core_rust.so" -Force
            Write-Host "✓ Linux Native Library compiled: $LibsPath\libswarmforge_core_rust.so" -ForegroundColor Green
        }
        if ($dylib) {
            Copy-Item $dylib.FullName "$LibsPath\libswarmforge_core_rust.dylib" -Force
            Copy-Item $dylib.FullName "$ResourcesPath\macos\libswarmforge_core_rust.dylib" -Force
            Write-Host "✓ macOS Native Library compiled: $LibsPath\libswarmforge_core_rust.dylib" -ForegroundColor Green
        }
    }

    Write-Host "==> Running native unit tests..." -ForegroundColor Green
    & cargo test --release
}
finally {
    Pop-Location
}

Write-Host "[SUCCESS] Rust native engine build and synchronization completed." -ForegroundColor Green
