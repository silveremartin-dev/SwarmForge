@echo off
echo Starting SwarmForge Infrastructure (Postgres, Redis)...

REM Check if Docker is available
docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker is not running or not installed. Please start Docker Desktop/Daemon.
    pause
    exit /b 1
)

cd ..
echo Running docker-compose up -d postgres redis...
docker-compose up -d postgres redis
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to start infrastructure containers.
    pause
    exit /b 1
)

echo.
echo Verifying containers...
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}" | findstr /i "postgres redis"
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Containers might not be running. Please check 'docker ps'.
) else (
    echo.
    echo [SUCCESS] Infrastructure started successfully. You can now run the server.
)
pause
