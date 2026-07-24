@echo off
REM SwarmForge Build Script (Windows)

echo ========================================
echo   SwarmForge - Build Script (Windows)
echo ========================================
echo.

cd /d "%~dp0.."

echo Building project with Maven...
call mvn clean install -DskipTests %*
if errorlevel 1 (
    echo.
    echo ERROR: Build Failed!
    pause
    exit /b 1
)

echo.
echo Build Successful!
