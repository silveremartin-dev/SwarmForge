@echo off
REM ==============================================================================
REM SwarmForge Universal 5-Language I18n, Tooltip & Hardcoded String Audit Launcher
REM ==============================================================================

echo ==============================================================================
echo       SwarmForge - Universal 5-Language I18n ^& Tooltip Audit Suite
echo ==============================================================================
echo.

cd /d "%~dp0..\.."

REM Check if Python is available
where py >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    py scripts\i18n\i18n_validator_and_translator.py %*
    goto end
)

where python >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    python scripts\i18n\i18n_validator_and_translator.py %*
    goto end
)

where python3 >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    python3 scripts\i18n\i18n_validator_and_translator.py %*
    goto end
)

echo [ERREUR] Python 3 est introuvable dans le PATH système.
pause
exit /b 1

:end
