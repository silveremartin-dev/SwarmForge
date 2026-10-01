@echo off
REM ==============================================================================
REM SwarmForge Client + Server Launcher - Client Lourd avec Serveur (Windows)
REM Demarre le serveur SwarmForge gRPC en arriere-plan puis lance le client lourd.
REM ==============================================================================

echo ==============================================================================
echo        SwarmForge - Client Lourd + Serveur (Viewer 3D + Serveur)
echo ==============================================================================
echo.

cd /d "%~dp0..\.."

set "DEBUG_OPT="
set "HEADLESS_OPT="
set "PASSED_ARGS="
if "%MAVEN_OPTS%"=="" set "MAVEN_OPTS=-Xmx4g -XX:+UseG1GC"

:parse_args
if "%~1"=="" goto run_stack
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

:run_stack
echo [1/3] Compilation du Serveur et du Client...
call mvn compile -pl swarmforge-server,swarmforge-client -am -q
if errorlevel 1 (
    echo [ERREUR] Echec de la compilation.
    pause
    exit /b 1
)

echo [2/3] Demarrage du Serveur SwarmForge (Port 9090 / gRPC)...
start "SwarmForge Server" cmd /k "cd /d "%CD%" && mvn exec:java -pl swarmforge-server -Dexec.args="--local""

echo [INFO] Attente de 3 secondes pour l'initialisation du serveur...
timeout /t 3 /nobreak >nul

echo [3/3] Lancement du Client Lourd SwarmForge...
call mvn exec:java -pl swarmforge-client -q %DEBUG_OPT% %HEADLESS_OPT% -Dexec.args="%PASSED_ARGS%"

if errorlevel 1 (
    echo [ERREUR] Echec de l'execution du Client Lourd.
    pause
    exit /b 1
)
