@echo off
REM ==============================================================================
REM SwarmForge Editor (Studio) Launcher - Editeur Seul (Windows)
REM Demarre l'interface Studio JavaFX / jMonkeyEngine 3D en mode autonome.
REM ==============================================================================

echo ==============================================================================
echo            SwarmForge - Studio Editeur JavaFX (Editeur Seul)
echo ==============================================================================
echo.

cd /d "%~dp0..\.."

set "DEBUG_OPT="
set "HEADLESS_OPT="
set "PASSED_ARGS="
if "%MAVEN_OPTS%"=="" set "MAVEN_OPTS=-Xmx4g -XX:+UseG1GC"

:parse_args
if "%~1"=="" goto run_editor
if "%~1"=="--debug" (
    echo [INFO] Mode Debug actif (agent JDWP sur port 5006)
    set "DEBUG_OPT=-Dexec.jvmArgs=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006"
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

:run_editor
echo [1/2] Compilation des modules Core ^& Editor...
call mvn compile -pl swarmforge-editor -am -q
if errorlevel 1 (
    echo [ERREUR] Echec de la compilation de l'Editeur.
    pause
    exit /b 1
)

echo [2/2] Lancement du Studio SwarmForge...
call mvn exec:java -pl swarmforge-editor -q %DEBUG_OPT% %HEADLESS_OPT% -Dexec.args="%PASSED_ARGS%"

if errorlevel 1 (
    echo [ERREUR] Echec de l'execution du Studio SwarmForge.
    pause
    exit /b 1
)
