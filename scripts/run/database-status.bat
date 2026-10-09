@echo off
REM ==============================================================================
REM SwarmForge - Verification de l'etat de la base de donnees et du cache Redis
REM ==============================================================================

cd /d "%~dp0..\.."

echo ==============================================================================
echo    SwarmForge - Statut de l'infrastructure Docker (PostgreSQL ^& Redis)
echo ==============================================================================
echo.

docker compose ps

echo.
echo Test de connectivite des ports :
powershell -Command "Test-NetConnection -ComputerName localhost -Port 5432 -InformationLevel Quiet" >nul 2>nul
if errorlevel 1 (
    echo  • PostgreSQL (port 5432) : [INACTIF]
) else (
    echo  • PostgreSQL (port 5432) : [ACTIF / JOIGNABLE]
)

powershell -Command "Test-NetConnection -ComputerName localhost -Port 6379 -InformationLevel Quiet" >nul 2>nul
if errorlevel 1 (
    echo  • Redis      (port 6379) : [INACTIF]
) else (
    echo  • Redis      (port 6379) : [ACTIF / JOIGNABLE]
)
echo.
