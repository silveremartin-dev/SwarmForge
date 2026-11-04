@echo off
REM ==============================================================================
REM SwarmForge - Immediate Windows Release Deployment & Packaging Script
REM Builds and packages the standalone Windows release bundle (.zip)
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
powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%..\package-release.ps1" -Platform windows %PASSED_ARGS%

if errorlevel 1 (
    echo.
    echo [ERREUR] Le deploiement de la release Windows a echoue.
    exit /b 1
)
