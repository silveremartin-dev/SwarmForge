@echo off
REM SwarmForge Test Execution Script (Windows)
REM Performs a build followed by unit and integration test execution.

echo ========================================
echo   SwarmForge - Test Runner (Windows)
echo ========================================
echo.

cd /d "%~dp0.."

echo [1/2] Executing build-all...
call scripts\build-all.bat
if errorlevel 1 (
    echo ERROR: Build stage failed! Skipping test execution.
    exit /b 1
)

echo.
echo [2/2] Running all project unit and integration tests with Maven...
call mvn test %*
if errorlevel 1 (
    echo.
    echo ERROR: One or more unit tests failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo All Tests Passed Successfully!
echo ========================================
