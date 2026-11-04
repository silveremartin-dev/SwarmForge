@echo off
REM ==============================================================================
REM SwarmForge - Arret des conteneurs Docker (PostgreSQL & Redis)
REM ==============================================================================

cd /d "%~dp0..\.."

where docker >nul 2>&1
if errorlevel 1 (
    where docker-compose >nul 2>&1
    if errorlevel 1 (
        echo [INFO] Docker non installe. Aucun conteneur actif a arreter.
        exit /b 0
    )
)

echo [INFO] Arret de la stack Docker Compose (PostgreSQL ^& Redis)...
docker compose down 2>nul || docker-compose down 2>nul

echo [OK] Arret Docker termine.
