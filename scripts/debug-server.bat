@echo off
REM SwarmForge Server Debug Launcher (Windows)
REM Launches Server in Debug mode (Port 5006)

echo ========================================
echo   SwarmForge Server - DEBUG MODE
echo ========================================
echo Connecting on port 5006...

cd /d "%~dp0.."

call mvn exec:java -pl swarmforge-server -Dexec.args="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5006"
pause
