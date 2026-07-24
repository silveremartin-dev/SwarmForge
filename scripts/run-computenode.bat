@echo off
REM SwarmForge Compute Node Launcher (Windows)

echo ========================================
echo   SwarmForge - Compute Node Launcher
echo ========================================
echo.

cd /d "%~dp0.."

echo Starting SwarmForge Compute Node...
call mvn compile exec:java -pl swarmforge-compute -q %*
if errorlevel 1 (
    echo.
    echo ERROR: Failed to start Compute Node.
    pause
    exit /b 1
)
