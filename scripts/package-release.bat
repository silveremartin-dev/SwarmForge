@echo off
REM SwarmForge Release Packaging Script Wrapper
title SwarmForge Release Packager

echo ========================================================
echo   SwarmForge - Automated Release Packager
echo ========================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0package-release.ps1" %*

if errorlevel 1 (
    echo.
    echo [ERROR] Packaging failed.
    pause
    exit /b 1
)

echo.
echo [SUCCESS] Packaging complete! Check dist\release\
pause
