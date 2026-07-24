@echo off
REM SwarmForge Server Launcher (Windows)

echo ========================================
echo   SwarmForge - Server Launcher
echo ========================================
echo.

cd /d "%~dp0.."

set START_DOCKER=false
set DEBUG_OPT=
set PASSED_ARGS=

:parse_args
if "%~1"=="" goto run_server
if "%~1"=="--postgres" (
    set START_DOCKER=true
    set "PASSED_ARGS=%PASSED_ARGS% --postgres"
    shift
    goto parse_args
)
if "%~1"=="--debug" (
    echo [INFO] Debug mode active (JDWP agent on port 5005)
    set "DEBUG_OPT=-Dexec.jvmArgs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
    shift
    goto parse_args
)
set "PASSED_ARGS=%PASSED_ARGS% %~1"
shift
goto parse_args

:run_server
if "%START_DOCKER%"=="true" (
    echo [1/2] Launching Docker Infrastructure [Postgres and Redis]...
    docker-compose up -d postgres redis 2>nul || docker compose up -d postgres redis 2>nul || echo WARNING: Docker infrastructure startup attempted
) else (
    echo [1/2] Running in Standalone/Local mode - H2 Database fallback active...
)

echo.
echo [2/2] Launching SwarmForge Server...
call mvn compile exec:java -pl swarmforge-server -q %DEBUG_OPT% -Dexec.args="%PASSED_ARGS%"

if errorlevel 1 (
    echo ERROR: Failed to start SwarmForge Server.
    pause
    exit /b 1
)
