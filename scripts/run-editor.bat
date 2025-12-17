@echo off
REM SwarmForge Editor Launcher
cd /d "%~dp0.."

echo Starting SwarmForge Editor...
mvn compile exec:java -pl swarmforge-editor -q

if errorlevel 1 (
    echo Failed to start editor
    pause
)
