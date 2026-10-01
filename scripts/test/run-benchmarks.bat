@echo off
REM SwarmForge Benchmarks Launcher (Windows)

echo ========================================
echo   SwarmForge - Benchmarks Launcher
echo ========================================
echo.

cd /d "%~dp0..\.."

echo Building and Running SwarmForge Benchmarks...
call mvn clean package -pl swarmforge-benchmarks -DskipTests
if errorlevel 1 (
    echo ERROR: Failed to build SwarmForge Benchmarks.
    pause
    exit /b 1
)

echo Executing Benchmarks...
for %%f in (swarmforge-benchmarks\target\swarmforge-benchmarks-*.jar) do (
    java -jar "%%f" %*
)
