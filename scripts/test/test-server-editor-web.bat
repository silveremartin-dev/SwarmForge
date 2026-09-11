@echo off
REM SwarmForge - Test Hybride : Serveur Java + Client Web + Client Studio JavaFX
cd /d "%~dp0..\.."

echo ==================================================================
echo   SwarmForge - Test Hybride Serveur + Web + Studio Desktop
echo ==================================================================

set SCENARIO_ID=4
if not "%~1"=="" set SCENARIO_ID=%~1

echo [1/4] Demarrage du Serveur Java SwarmForge (Scenario #%SCENARIO_ID%)...
start "SwarmForge Server" cmd /k "mvn exec:java -pl swarmforge-server -Dexec.args=\"--scenario %SCENARIO_ID%\""

echo [2/4] Demarrage du serveur Web HTTP (:5173)...
start "SwarmForge Web Static" cmd /k "py scripts\test\serve_web_static.py || python scripts\test\serve_web_static.py"

echo [3/4] Attente d'initialisation...
timeout /t 4 /nobreak >nul

echo [4/4] Ouverture du Web et du Studio JavaFX...
start http://localhost:5173
start "SwarmForge Studio Client" cmd /k "mvn exec:java -pl swarmforge-editor"

echo.
echo Test Hybride lance avec succes !
