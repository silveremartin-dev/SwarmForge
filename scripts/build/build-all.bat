@echo off
REM ==============================================================================
REM SwarmForge - Full Project Build (Windows Batch)
REM ==============================================================================

cd /d "%~dp0..\.."

echo ========================================
echo   SwarmForge - Full Maven Build
echo ========================================
echo.

call mvn clean install -DskipTests

if errorlevel 1 (
    echo.
    echo [ERREUR] Echec du build SwarmForge.
    pause
    exit /b 1
)

echo.
echo [SUCCES] Tous les modules SwarmForge ont ete compiles avec succes !
