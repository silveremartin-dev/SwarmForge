@echo off
REM ==============================================================================
REM SwarmForge Web Client + Server Launcher - Client Leger avec Serveur (Windows)
REM Demarre le serveur SwarmForge gRPC/WS en arriere-plan et lance le client Web.
REM ==============================================================================

echo ==============================================================================
echo        SwarmForge - Client Web Leger + Serveur de Simulation
echo ==============================================================================
echo.

cd /d "%~dp0..\.."

REM Detection de Python
set "PYTHON_CMD="
where py >nul 2>&1 && set "PYTHON_CMD=py"
if "%PYTHON_CMD%"=="" (
    where python >nul 2>&1 && set "PYTHON_CMD=python"
)
if "%PYTHON_CMD%"=="" (
    if exist "%LOCALAPPDATA%\Programs\Python\Python312\python.exe" set "PYTHON_CMD=%LOCALAPPDATA%\Programs\Python\Python312\python.exe"
    if exist "%LOCALAPPDATA%\Programs\Python\Python311\python.exe" set "PYTHON_CMD=%LOCALAPPDATA%\Programs\Python\Python311\python.exe"
    if exist "%LOCALAPPDATA%\Programs\Python\Python314\python.exe" set "PYTHON_CMD=%LOCALAPPDATA%\Programs\Python\Python314\python.exe"
    if exist "%ProgramFiles%\Python312\python.exe" set "PYTHON_CMD=%ProgramFiles%\Python312\python.exe"
    if exist "%ProgramFiles%\Python311\python.exe" set "PYTHON_CMD=%ProgramFiles%\Python311\python.exe"
)

if "%PYTHON_CMD%"=="" (
    echo [ERREUR] Python introuvable. Veuillez installer Python 3.
    pause
    exit /b 1
)

echo [1/3] Compilation du Serveur SwarmForge...
call mvn compile -pl swarmforge-server -am -q
if errorlevel 1 (
    echo [ERREUR] Echec de la compilation du Serveur.
    pause
    exit /b 1
)

echo [2/3] Demarrage du Serveur SwarmForge (Port 9090 gRPC / Port 8081 WebSocket)...
start "SwarmForge Server" cmd /k "cd /d "%CD%" && mvn exec:java -pl swarmforge-server -Dexec.args="--local""

echo [INFO] Attente de 3 secondes pour l'initialisation du serveur...
timeout /t 3 /nobreak >nul

echo [3/3] Demarrage du client Web SwarmForge sur http://localhost:5173...
%PYTHON_CMD% scripts\run\web_server.py %*
