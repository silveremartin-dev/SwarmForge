@echo off
REM SwarmForge Compute Node Launcher
cd /d "%~dp0.."

echo Starting SwarmForge Compute Node...
mvn compile exec:java -pl swarmforge-compute -q %*

if errorlevel 1 (
    echo Failed to start compute node
    pause
)
