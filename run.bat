@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
title SwarmForge - Eusocial Insect Society Simulation

echo ==============================================================================
echo    🐝 SwarmForge - Simulation Platform Launcher (JavaFX / 3D Engine)
echo ==============================================================================
echo.

:: 1. Verify Java 21+ Installation
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERREUR] Java 21+ est requis mais introuvable dans le PATH systeme.
    echo.
    echo Ouverture de la page de telechargement OpenJDK 21 Temurin dans votre navigateur...
    start https://adoptium.net/temurin/releases/?version=21
    echo.
    echo Une fois Java installe, relancez ce script en double-cliquant dessus.
    echo.
    pause
    exit /b 1
)

:: 2. Launch using packaged launcher or Maven
if exist "dist\staging\windows-bundle\run-editor.bat" (
    call "dist\staging\windows-bundle\run-editor.bat" %*
) else (
    call "scripts\run\run-editor.bat" %*
)
