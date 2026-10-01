@echo off
REM ==============================================================================
REM SwarmForge Server Launcher (Windows Batch)
REM Par defaut : Demarre avec la console d'administration graphique (GUI).
REM Options :
REM   --nogui / --headless : Mode console sans interface graphique
REM   --scenario <1-16>    : Charge un scenario academique precis au demarrage
REM   --postgres           : Demarre l'infrastructure Docker Postgres / Redis
REM   --debug              : Active l'agent de debogage JDWP sur le port 5005
REM ==============================================================================

echo ==============================================================================
echo                      SwarmForge - Server Launcher
echo ==============================================================================
echo.

cd /d "%~dp0..\.."

set "START_DOCKER=false"
set "DEBUG_OPT="
set "USE_GUI=true"
set "PASSED_ARGS="
if "%MAVEN_OPTS%"=="" set "MAVEN_OPTS=-Xms2g -Xmx8g -XX:+UseG1GC --add-modules jdk.incubator.vector --enable-native-access=ALL-UNNAMED -XX:+AlwaysPreTouch"

:parse_args
if "%~1"=="" goto run_server
if "%~1"=="--postgres" (
    set "START_DOCKER=true"
    set "PASSED_ARGS=%PASSED_ARGS% --postgres"
    shift
    goto parse_args
)
if "%~1"=="--nogui" (
    set "USE_GUI=false"
    set "PASSED_ARGS=%PASSED_ARGS% --nogui"
    shift
    goto parse_args
)
if "%~1"=="--headless" (
    set "USE_GUI=false"
    set "PASSED_ARGS=%PASSED_ARGS% --nogui"
    shift
    goto parse_args
)
if "%~1"=="--debug" (
    echo [INFO] Debug mode active (agent JDWP sur le port 5005)
    set "DEBUG_OPT=-Dexec.jvmArgs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
    shift
    goto parse_args
)
set "PASSED_ARGS=%PASSED_ARGS% %~1"
shift
goto parse_args

:run_server
if "%START_DOCKER%"=="true" (
    echo [1/3] Demarrage de l'infrastructure Docker (Postgres ^& Redis)...
    docker compose up -d postgres redis 2>nul || docker-compose up -d postgres redis 2>nul || echo [AVERTISSEMENT] Echec de tentative de demarrage Docker.
) else (
    echo [1/3] Mode Autonome Local (H2 Database en memoire actif)...
)

echo [2/3] Compilation du Serveur SwarmForge...
call mvn compile -pl swarmforge-server -am -q
if errorlevel 1 (
    echo [ERREUR] Echec de la compilation du Serveur.
    pause
    exit /b 1
)

if "%USE_GUI%"=="true" (
    echo [3/3] Lancement du Serveur SwarmForge avec Console Graphique (GUI)...
    call mvn exec:java -pl swarmforge-server -q %DEBUG_OPT% "-Dexec.mainClass=org.swarmforge.server.ServerGuiLauncher" -Dexec.args="%PASSED_ARGS%"
) else (
    echo [3/3] Lancement du Serveur SwarmForge en mode Console (Headless / No-GUI)...
    call mvn exec:java -pl swarmforge-server -q %DEBUG_OPT% "-Dexec.mainClass=org.swarmforge.server.SwarmForgeServer" -Dexec.args="%PASSED_ARGS%"
)

if errorlevel 1 (
    echo [ERREUR] Echec de l'execution du Serveur SwarmForge.
    pause
    exit /b 1
)
