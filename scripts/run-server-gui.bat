@echo off
REM SwarmForge Server GUI Launcher
cd /d "%~dp0.."

echo Starting SwarmForge Server GUI...
call docker-compose up -d postgres redis 2>nul
mvn compile exec:java -pl swarmforge-server -Dexec.mainClass="org.swarmforge.server.ServerGuiApp" -q

if errorlevel 1 (
    echo Failed to start server GUI
    pause
)
