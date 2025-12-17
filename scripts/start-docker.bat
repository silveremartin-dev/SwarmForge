@echo off
REM SwarmForge Infrastructure Startup

echo Starting Docker containers (Postgres, Redis)...
cd /d "%~dp0.."
docker-compose up -d
echo.
echo Infrastructure started.
pause
