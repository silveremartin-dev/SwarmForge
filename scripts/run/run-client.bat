@echo off
REM ==============================================================================
REM SwarmForge Client (Viewer) Launcher - Client Lourd Seul (Windows)
REM Demarre le client lourd SwarmForge Client (viewer 3D) en mode autonome.
REM ==============================================================================

echo ==============================================================================
echo           SwarmForge - Client Lourd (Viewer 3D - Client Seul)
echo ==============================================================================
echo.

cd /d "%~dp0..\.."

set "DEBUG_OPT="
set "HEADLESS_OPT="
set "PASSED_ARGS="
if "%MAVEN_OPTS%"=="" set "MAVEN_OPTS=-Xmx4g -XX:+UseG1GC"

:parse_args
if "%~1"=="" goto run_client
if "%~1"=="--debug" (
    echo [INFO] Mode Debug actif (agent JDWP sur port 5007)
    set "DEBUG_OPT=-Dexec.jvmArgs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5007"
    shift
    goto parse_args
)
if "%~1"=="--nogui" (
    echo [INFO] Execution en mode sans interface (Headless)
    set "HEADLESS_OPT=-Djava.awt.headless=true"
    set "PASSED_ARGS=%PASSED_ARGS% --nogui"
    shift
    goto parse_args
)
set "PASSED_ARGS=%PASSED_ARGS% %~1"
shift
goto parse_args

:run_client
echo [1/2] Compilation des modules Core ^& Client...
call mvn compile -pl swarmforge-client -am -q
if errorlevel 1 (
    echo [ERREUR] Echec de la compilation du Client.
    pause
    exit /b 1
)

echo [2/2] Lancement du Client Lourd SwarmForge...
call mvn exec:java -pl swarmforge-client -q %DEBUG_OPT% %HEADLESS_OPT% -Dexec.args="%PASSED_ARGS%"

if errorlevel 1 (
    echo [ERREUR] Echec de l'execution du Client Lourd.
    pause
    exit /b 1
)
