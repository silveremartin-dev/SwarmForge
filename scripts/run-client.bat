@echo off
REM SwarmForge Client Launcher (Windows)

echo ========================================
echo   SwarmForge - Client Launcher
echo ========================================
echo.

cd /d "%~dp0.."

set DEBUG_OPT=
set HEADLESS_OPT=
set PASSED_ARGS=

:parse_args
if "%~1"=="" goto run_client
if "%~1"=="--debug" (
    echo [INFO] Debug mode active (JDWP agent on port 5007)
    set "DEBUG_OPT=-Dexec.jvmArgs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5007"
    shift
    goto parse_args
)
if "%~1"=="--nogui" (
    echo [INFO] Running client in No-GUI/Headless mode
    set "HEADLESS_OPT=-Djava.awt.headless=true"
    set "PASSED_ARGS=%PASSED_ARGS% --nogui"
    shift
    goto parse_args
)
set "PASSED_ARGS=%PASSED_ARGS% %~1"
shift
goto parse_args

:run_client
echo Launching SwarmForge Client...
call mvn compile exec:java -pl swarmforge-client -q %DEBUG_OPT% %HEADLESS_OPT% -Dexec.args="%PASSED_ARGS%"

if errorlevel 1 (
    echo ERROR: Failed to start SwarmForge Client.
    pause
    exit /b 1
)
