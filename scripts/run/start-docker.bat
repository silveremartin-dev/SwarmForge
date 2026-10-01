@echo off
REM SwarmForge Infrastructure Starter (PostgreSQL + Redis)

cd /d "%~dp0..\.."

echo ========================================
echo   Starting SwarmForge Infrastructure
echo ========================================

docker compose up -d
if errorlevel 1 (
    echo Docker compose failed, trying legacy docker-compose...
    docker-compose up -d
)

echo Infrastructure started.
echo PostgreSQL: localhost:5432 (swarmforge/swarmforge)
echo Redis:      localhost:6379
