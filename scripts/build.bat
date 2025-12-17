@echo off
echo ==========================================
echo      SwarmForge Build Script (Windows)
echo ==========================================

echo Building project with Maven...
call mvn clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo Build Failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Build Successful!
pause
