@echo off
REM SwarmForge Client (Viewer) Launcher (Windows)
REM Automatically starts infrastructure (if needed) and launches Client.

echo ========================================
echo   SwarmForge - Client (Viewer) Launcher
echo ========================================
echo.

cd /d "%~dp0.."

echo [1/2] Ensuring Infrastructure is UP...
docker-compose up -d postgres redis 2>nul
if errorlevel 1 (
    echo WARNING: Docker infrastructure may not be running.
)

echo.
echo [2/2] Launching SwarmForge Client...
call mvn compile exec:java -pl swarmforge-client -q %*
if errorlevel 1 (
    echo.
    echo ERROR: Failed to start SwarmForge Client.
    pause
    exit /b 1
)
