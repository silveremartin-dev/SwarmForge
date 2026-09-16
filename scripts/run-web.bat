@echo off
REM SwarmForge Web Client & Simulation Launcher (Windows)
REM Starts the 3D Web Simulation Client + Integrated Live Simulation Server.

echo ======================================================
echo       SwarmForge - Web Client ^& Serveur Simulation  
echo ======================================================
echo.

cd /d "%~dp0.."

where py >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Lancement du serveur complet [Web sur :5173 + WebSocket sur :8081] avec Python Launcher...
    py scripts\mock_server.py
    exit /b 0
)

where python >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] Lancement du serveur complet [Web sur :5173 + WebSocket sur :8081] avec Python...
    python scripts\mock_server.py
    exit /b 0
)

for %%P in (
    "%LOCALAPPDATA%\Programs\Python\Python314\python.exe"
    "%LOCALAPPDATA%\Programs\Python\Python313\python.exe"
    "%LOCALAPPDATA%\Programs\Python\Python312\python.exe"
    "%LOCALAPPDATA%\Programs\Python\Python311\python.exe"
    "%LOCALAPPDATA%\Programs\Python\Python310\python.exe"
    "%ProgramFiles%\Python313\python.exe"
    "%ProgramFiles%\Python312\python.exe"
    "%ProgramFiles%\Python311\python.exe"
    "C:\Python312\python.exe"
    "C:\Python311\python.exe"
) do (
    if exist "%%~P" (
        echo [INFO] Python detecte : %%~P
        "%%~P" scripts\mock_server.py
        exit /b 0
    )
)

where node >nul 2>&1
if %errorlevel% equ 0 (
    cd swarmforge-web
    if not exist "node_modules" (
        echo [INFO] Installation des dependances NPM...
        call npm install
    )
    echo [INFO] Lancement via Vite Dev Server...
    call npm run dev -- --host --port 5173 --open
    exit /b 0
)

echo [ERREUR] Ni Python ni Node.js n'ont ete trouves.
echo Veuillez verifier votre installation de Python ou Node.js.
pause
exit /b 1
