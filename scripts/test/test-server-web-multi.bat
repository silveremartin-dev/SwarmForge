@echo off
REM SwarmForge - Test Rapide : Serveur Java + 2 Clients Web
cd /d "%~dp0..\.."

echo ======================================================
echo    SwarmForge - Test Serveur Java + 2 Clients Web
echo ======================================================

set SCENARIO_ID=4
if not "%~1"=="" set SCENARIO_ID=%~1

echo [1/3] Demarrage du Serveur Java SwarmForge (Scenario #%SCENARIO_ID%)...
start "SwarmForge Server" cmd /k "mvn exec:java -pl swarmforge-server -Dexec.args=\"--scenario %SCENARIO_ID%\""

echo [2/3] Demarrage du serveur Web HTTP (:5173)...
start "SwarmForge Web Static" cmd /k "py scripts\test\serve_web_static.py || python scripts\test\serve_web_static.py"

echo [3/3] Attente d'initialisation...
timeout /t 4 /nobreak >nul

start http://localhost:5173
timeout /t 1 /nobreak >nul
start http://localhost:5173

echo.
echo Test actif ! Deux fenetres/onglets Web connectes sur http://localhost:5173
