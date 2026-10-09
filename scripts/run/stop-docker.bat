@echo off
REM ==============================================================================
REM SwarmForge - Arret des conteneurs Docker (PostgreSQL & Redis)
REM ==============================================================================

cd /d "%~dp0..\.."

echo [INFO] Arret de la stack Docker Compose (PostgreSQL ^& Redis)...
docker compose down

if errorlevel 1 (
    echo [ERREUR] Impossible d'arreter les conteneurs Docker.
    exit /b 1
)

echo [OK] Conteneurs Docker arretes avec succes.
