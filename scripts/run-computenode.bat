@echo off
REM SwarmForge Compute Node Launcher (Windows)

echo ========================================
echo   SwarmForge - Compute Node Launcher
echo ========================================
echo.

cd /d "%~dp0.."

set DEBUG_OPT=
set HEADLESS_OPT=
set PASSED_ARGS=

:parse_args
if "%~1"=="" goto run_compute
if "%~1"=="--debug" (
    echo [INFO] Debug mode active (JDWP agent on port 5008)
    set "DEBUG_OPT=-Dexec.jvmArgs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008"
    shift
    goto parse_args
)
if "%~1"=="--nogui" (
    echo [INFO] Running compute node in No-GUI mode
    set "HEADLESS_OPT=-Djava.awt.headless=true"
    set "PASSED_ARGS=%PASSED_ARGS% --nogui"
    shift
    goto parse_args
)
set "PASSED_ARGS=%PASSED_ARGS% %~1"
shift
goto parse_args

:run_compute
echo Starting SwarmForge Compute Node...
call mvn compile exec:java -pl swarmforge-compute -q %DEBUG_OPT% %HEADLESS_OPT% -Dexec.args="%PASSED_ARGS%"

if errorlevel 1 (
    echo ERROR: Failed to start Compute Node.
    pause
    exit /b 1
)
