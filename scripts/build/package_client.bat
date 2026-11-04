@echo off
REM ==============================================================================
REM SwarmForge - Package Client Artifacts (Windows Batch)
REM ==============================================================================

cd /d "%~dp0..\.."

echo ==========================================
echo   Packaging SwarmForge Client Artifacts
echo ==========================================
echo.

call mvn package -pl swarmforge-client -am -DskipTests -q

if errorlevel 1 (
    echo.
    echo [ERREUR] Echec du packaging du Client.
    pause
    exit /b 1
)

echo.
echo [SUCCES] Artefacts du Client packages avec succes dans swarmforge-client\target\
