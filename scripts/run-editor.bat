@echo off
REM SwarmForge Editor (Studio) Launcher (Windows)
REM Automatically starts infrastructure (if needed) and launches Editor.

echo ========================================
echo   SwarmForge - Editor (Studio) Launcher
echo ========================================
echo.

cd /d "%~dp0.."

echo [1/2] Ensuring Infrastructure is UP...
docker-compose up -d postgres redis 2>nul
if errorlevel 1 (
    echo WARNING: Docker infrastructure may not be running.
)

echo.
echo [2/2] Launching SwarmForge Editor...
call mvn compile exec:java -pl swarmforge-editor -q %*
if errorlevel 1 (
    echo.
    echo ERROR: Failed to start SwarmForge Editor.
    pause
    exit /b 1
)
