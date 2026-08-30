@echo off
REM SwarmForge Benchmarks Launcher (Windows)

echo ========================================
echo   SwarmForge - Benchmarks Launcher
echo ========================================
echo.

cd /d "%~dp0.."

echo Building and Running SwarmForge Benchmarks...
call mvn clean package -pl swarmforge-benchmarks -DskipTests
if errorlevel 1 (
    echo ERROR: Failed to build SwarmForge Benchmarks.
    pause
    exit /b 1
)

echo Executing Benchmarks...
java -jar swarmforge-benchmarks/target/swarmforge-benchmarks-2.0.0-SNAPSHOT.jar %*
