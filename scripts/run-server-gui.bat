@echo off
REM SwarmForge Server GUI Launcher (Windows)

echo ========================================
echo   SwarmForge - Server GUI Launcher
echo ========================================
echo.

cd /d "%~dp0.."

echo [1/2] Ensuring Infrastructure is UP...
docker-compose up -d postgres redis 2>nul
if errorlevel 1 (
    echo WARNING: Docker infrastructure may not be running.
)

echo.
echo [2/2] Launching Server GUI...
call mvn compile exec:java -pl swarmforge-server "-Dexec.mainClass=org.swarmforge.server.ServerGuiLauncher" %*
if errorlevel 1 (
    echo.
    echo ERROR: Failed to start Server GUI.
    pause
    exit /b 1
)
