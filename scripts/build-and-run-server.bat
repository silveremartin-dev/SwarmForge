@echo off
REM SwarmForge Server Build & Run (Windows)
REM Builds the server and then calls the launcher.

echo ========================================
echo   SwarmForge Server - Build & Run
echo ========================================
echo.

cd /d "%~dp0.."

echo [Build Phase] Compiling project...
call mvn install -pl swarmforge-server -am -DskipTests -q
if errorlevel 1 (
    echo.
    echo ERROR: Build failed.
    pause
    exit /b 1
)

echo.
REM Delegate to the run script which handles Docker startup
call scripts\run-server.bat
