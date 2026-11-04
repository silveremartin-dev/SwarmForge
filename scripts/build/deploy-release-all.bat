@echo off
REM ==============================================================================
REM SwarmForge - Multi-Platform Release Deployment Script (Linux, macOS, Windows)
REM Builds and packages releases for all 3 target platforms simultaneously.
REM ==============================================================================

setlocal

set "SCRIPT_DIR=%~dp0"
set "PASSED_ARGS="

:loop
if "%~1"=="" goto run
if /i "%~1"=="--skip-build" (
    set "PASSED_ARGS=%PASSED_ARGS% -SkipBuild"
) else if /i "%~1"=="--version" (
    set "PASSED_ARGS=%PASSED_ARGS% -Version %~2"
    shift
) else if /i "%~1"=="-v" (
    set "PASSED_ARGS=%PASSED_ARGS% -Version %~2"
    shift
) else (
    set "PASSED_ARGS=%PASSED_ARGS% %~1"
)
shift
goto loop

:run
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%..\package-release.ps1" -Platform all %PASSED_ARGS%

if errorlevel 1 (
    echo.
    echo [ERREUR] Le deploiement multi-plateformes a echoue.
    exit /b 1
)
