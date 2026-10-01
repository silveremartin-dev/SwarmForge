@echo off
REM ==============================================================================
REM SwarmForge Web Client Launcher - Client Leger Seul (Windows)
REM Lance le serveur HTTP local pour distribuer le client Web (port 5173).
REM ==============================================================================

echo ==============================================================================
echo           SwarmForge - Client Web Leger (3D - Client Seul)
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

echo [INFO] Demarrage du client Web SwarmForge sur http://localhost:5173...
%PYTHON_CMD% scripts\run\web_server.py %*
