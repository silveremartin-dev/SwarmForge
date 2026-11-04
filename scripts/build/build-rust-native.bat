@echo off
REM ==============================================================================
REM SwarmForge - Build Rust Native Acceleration Kernels (Windows Batch)
REM ==============================================================================

cd /d "%~dp0..\.."

echo ==================================================
echo   SwarmForge - Rust Native Engine Compilation
echo ==================================================
echo.

where cargo >nul 2>&1
if errorlevel 1 (
    echo [AVERTISSEMENT] Cargo ^(Rust^) non detecte dans le PATH.
    echo Le moteur SwarmForge utilisera le fallback haute performance pur Java.
    exit /b 0
)

if exist "swarmforge-core\src\main\rust\Cargo.toml" (
    echo [INFO] Compilation des modules Rust natifs en mode Release...
    cd swarmforge-core\src\main\rust
    cargo build --release
    if errorlevel 1 (
        echo [ERREUR] Echec de la compilation Rust.
        cd /d "%~dp0..\.."
        pause
        exit /b 1
    )
    cd /d "%~dp0..\.."
    echo [SUCCES] Bibliotheques Rust natives compilees avec succes.
) else (
    echo [INFO] Aucun module Rust detecte dans swarmforge-core\src\main\rust.
)
