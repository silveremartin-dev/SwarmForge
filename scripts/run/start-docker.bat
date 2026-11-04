@echo off
REM SwarmForge Infrastructure Starter (PostgreSQL + Redis)

cd /d "%~dp0..\.."

echo ========================================
echo   Starting SwarmForge Infrastructure
echo ========================================

where docker >nul 2>&1
if errorlevel 1 (
    where docker-compose >nul 2>&1
    if errorlevel 1 (
        echo [INFO] Docker / Docker Compose non detecte sur le systeme.
        echo [INFO] SwarmForge fonctionne de maniere autonome avec base H2 en memoire.
        echo [INFO] Aucune action requise.
        exit /b 0
    )
)

docker compose up -d 2>nul
if errorlevel 1 (
    echo Docker compose failed, trying legacy docker-compose...
    docker-compose up -d 2>nul
    if errorlevel 1 (
        echo [AVERTISSEMENT] Echec du demarrage des conteneurs Docker.
        echo SwarmForge utilisera la base locale H2 en memoire.
        exit /b 0
    )
)

echo Infrastructure started.
echo PostgreSQL: localhost:5432 (swarmforge/swarmforge)
echo Redis:      localhost:6379
