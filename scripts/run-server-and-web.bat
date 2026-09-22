@echo off
REM ==============================================================================
REM SwarmForge - Lanceur Combiné Serveur Java & Client Web
REM Démarre SwarmForge Server (gRPC 50051 + WebSocket 8081) et ouvre le Client Web
REM ==============================================================================

echo ==============================================================================
echo        SwarmForge - Demarrage Serveur Reel (Java) ^& Client Web
echo ==============================================================================
echo.

cd /d "%~dp0.."

echo [1/3] Verification de Maven et Java...
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    echo [ATTENTION] Maven n'a pas ete trouve dans le PATH.
    echo Tentative d'utilisation du serveur autonome integre...
    goto run_python_fallback
)

echo [2/3] Lancement du Serveur Java SwarmForge en arriere-plan...
start "SwarmForge Server (gRPC 50051 / WS 8081)" cmd /k "cd /d "%~dp0.." && mvn exec:java -pl swarmforge-server -Dexec.args="--local""

echo [3/3] Lancement du Client Web SwarmForge...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-web.ps1"
goto end

:run_python_fallback
echo [INFO] Demarrage via le serveur Python integre...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0run-web.ps1"

:end
