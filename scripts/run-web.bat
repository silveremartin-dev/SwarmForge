@echo off
REM SwarmForge Web Client & Simulation Launcher (Windows)
REM Starts the 3D Web Simulation Client + Integrated Live Simulation Server.

echo ======================================================
echo       SwarmForge - Web Client & Serveur Mock         
echo ======================================================
echo.

cd /d "%~dp0.."

where py >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Lancement du serveur complet (Web + WebSocket 8081) avec Python Launcher...
    py scripts\mock_server.py
    exit /b 0
)

where python >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Lancement du serveur complet (Web + WebSocket 8081) avec Python...
    python scripts\mock_server.py
    exit /b 0
)

if exist "%LOCALAPPDATA%\Programs\Python\Python314\python.exe" (
    echo [INFO] Python 3.14 detecte dans AppData...
    "%LOCALAPPDATA%\Programs\Python\Python314\python.exe" scripts\mock_server.py
    exit /b 0
)

where node >nul 2>&1
if %errorlevel% equ 0 (
    cd swarmforge-web
    echo [INFO] Lancement via Vite Dev Server...
    call npm run dev -- --host --port 5173 --open
    exit /b 0
)

echo [ERREUR] Ni Python ni Node.js n'ont ete trouves.
echo Veuillez verifier votre installation de Python.
pause
exit /b 1
