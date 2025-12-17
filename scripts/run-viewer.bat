@echo off
REM SwarmForge Client (Viewer) Launcher
cd /d "%~dp0.."

echo Starting SwarmForge Client (Viewer)...
mvn compile exec:java -pl swarmforge-client -Dexec.mainClass="org.swarmforge.client.ClientApp" -q

if errorlevel 1 (
    echo Failed to start client
    pause
)
