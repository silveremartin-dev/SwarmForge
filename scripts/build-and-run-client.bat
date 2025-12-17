@echo off
REM SwarmForge Build & Run Editor (Windows)
REM Builds, starts infrastructure, and launches.

echo ========================================
echo   SwarmForge - Build & Run Editor
echo ========================================
echo.

cd /d "%~dp0.."

echo [Build Phase] Compiling project...
call mvn install -DskipTests -pl swarmforge-editor -am -q
if errorlevel 1 (
    echo.
    echo ERROR: Build failed.
    pause
    exit /b 1
)

echo.
REM Delegate to the run script which handles Docker and launch
call scripts\run-client.bat
