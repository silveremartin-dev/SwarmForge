@echo off
REM SwarmForge Server Launcher (Windows)
REM Automatically starts infrastructure and launches Server.

echo ========================================
echo   SwarmForge - Server Launcher
echo ========================================
echo.

cd /d "%~dp0.."

echo [1/2] Ensuring Infrastructure is UP...
docker-compose up -d postgres redis 2>nul
if errorlevel 1 (
    echo WARNING: Docker infrastructure may not be running.
)

echo.
echo [2/2] Launching SwarmForge Server...
call mvn compile exec:java -pl swarmforge-server -q %*
if errorlevel 1 (
    echo.
    echo ERROR: Failed to start SwarmForge Server.
    pause
    exit /b 1
)
