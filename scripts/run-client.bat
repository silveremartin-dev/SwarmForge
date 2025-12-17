@echo off
REM SwarmForge Editor Fast Launcher (Windows)
REM Automatically starts infrastructure and launches pre-built application.

echo ========================================
echo   SwarmForge - Editor Launcher
echo ========================================
echo.

cd /d "%~dp0.."

echo [1/2] Ensuring Infrastructure is UP...
docker-compose up -d postgres redis 2>nul
if errorlevel 1 (
    echo WARNING: Docker might not be running.
)

REM Check if JAR exists
if not exist "swarmforge-editor\target\swarmforge-editor-2.0.0-SNAPSHOT.jar" (
    echo.
    echo JAR not found. Building first...
    call mvn install -DskipTests -pl swarmforge-editor -am -q
)

echo.
echo [2/2] Launching Editor...
cd swarmforge-editor\target
java -jar swarmforge-editor-2.0.0-SNAPSHOT.jar
if errorlevel 1 (
    echo.
    echo ERROR: Application exited with an error.
)
pause
