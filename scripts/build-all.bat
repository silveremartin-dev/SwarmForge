@echo off
REM SwarmForge Full Build Script (Windows)
REM Compiles and packages all modules across the repository.

echo ========================================
echo   SwarmForge - Full Build All (Windows)
echo ========================================
echo.

cd /d "%~dp0.."

echo Building ALL project modules with Maven...
call mvn package -DskipTests %*
if errorlevel 1 (
    echo.
    echo ERROR: Build Failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build Successful for ALL modules!
echo ========================================
