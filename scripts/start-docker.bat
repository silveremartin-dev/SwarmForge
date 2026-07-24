@echo off
REM SwarmForge Infrastructure Startup (Windows)

echo ========================================
echo   SwarmForge - Infrastructure Startup
echo ========================================
echo.

cd /d "%~dp0.."

echo Starting Docker containers (Postgres, Redis)...
docker-compose up -d postgres redis
if errorlevel 1 (
    echo.
    echo ERROR: Failed to start Docker infrastructure. Ensure Docker Desktop is running.
    pause
    exit /b 1
)

echo.
echo Infrastructure started successfully.
