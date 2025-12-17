@echo off
REM SwarmForge Client Debug Launcher (Windows)
REM Launches Client in Debug mode (Port 5005)

echo ========================================
echo   SwarmForge Client - DEBUG MODE
echo ========================================
echo Connecting on port 5005...

cd /d "%~dp0.."

call mvn javafx:run -pl swarmforge-client-javafx -Djavafx.options="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
pause
