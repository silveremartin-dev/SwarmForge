@echo off
REM ==============================================================================
REM SwarmForge - Javadoc Generation Script (Windows Batch)
REM Generates aggregate and per-module Javadoc in /javadoc
REM ==============================================================================

setlocal enabledelayedexpansion

cd /d "%~dp0..\.."
set "ROOT_DIR=%CD%"

echo ==========================================
echo  Building SwarmForge Javadoc Documentation
echo ==========================================
echo.

echo [1/4] Installing modules in local repository...
call mvn install -DskipTests -q
if errorlevel 1 (
    echo [ERREUR] Maven install failed.
    exit /b 1
)

echo [2/4] Running Maven javadoc:javadoc...
call mvn javadoc:javadoc -Dquiet=true "-Ddoclint=none" "-DfailOnError=false"
if errorlevel 1 (
    echo [ERREUR] javadoc:javadoc failed.
    exit /b 1
)

echo [3/4] Running Maven javadoc:aggregate...
call mvn javadoc:aggregate -Dquiet=true "-Ddoclint=none" "-DfailOnError=false"
if errorlevel 1 (
    echo [ERREUR] javadoc:aggregate failed.
    exit /b 1
)

echo [4/4] Structuring output in javadoc directory...
set "TEMP_MODS=%ROOT_DIR%\target\temp_module_javadocs"
set "TEMP_AGG=%ROOT_DIR%\target\temp_aggregate_javadoc"
set "FINAL_DIR=%ROOT_DIR%\javadoc"

if exist "%TEMP_MODS%" rd /s /q "%TEMP_MODS%"
if exist "%TEMP_AGG%" rd /s /q "%TEMP_AGG%"
md "%TEMP_MODS%"
md "%TEMP_AGG%"

for %%m in (swarmforge-core swarmforge-server swarmforge-editor swarmforge-client swarmforge-compute swarmforge-benchmarks) do (
    set "MOD_APIDOCS="
    if exist "%ROOT_DIR%\%%m\javadoc\apidocs" set "MOD_APIDOCS=%ROOT_DIR%\%%m\javadoc\apidocs"
    if not defined MOD_APIDOCS (
        if exist "%ROOT_DIR%\%%m\target\site\apidocs" set "MOD_APIDOCS=%ROOT_DIR%\%%m\target\site\apidocs"
    )
    if not defined MOD_APIDOCS (
        if exist "%ROOT_DIR%\%%m\javadoc\index.html" set "MOD_APIDOCS=%ROOT_DIR%\%%m\javadoc"
    )
    if defined MOD_APIDOCS (
        echo Collecting Javadoc for module '%%m'...
        md "%TEMP_MODS%\%%m" 2>nul
        xcopy /s /e /q /y "!MOD_APIDOCS!\*" "%TEMP_MODS%\%%m\" >nul
    )
)

set "AGG_APIDOCS="
if exist "%ROOT_DIR%\javadoc\apidocs" set "AGG_APIDOCS=%ROOT_DIR%\javadoc\apidocs"
if not defined AGG_APIDOCS (
    if exist "%ROOT_DIR%\target\site\apidocs" set "AGG_APIDOCS=%ROOT_DIR%\target\site\apidocs"
)
if not defined AGG_APIDOCS (
    if exist "%ROOT_DIR%\javadoc\index.html" set "AGG_APIDOCS=%ROOT_DIR%\javadoc"
)
if defined AGG_APIDOCS (
    echo Collecting Aggregate Javadoc...
    xcopy /s /e /q /y "%AGG_APIDOCS%\*" "%TEMP_AGG%\" >nul
)

if exist "%FINAL_DIR%" rd /s /q "%FINAL_DIR%"
md "%FINAL_DIR%"

for %%m in (swarmforge-core swarmforge-server swarmforge-editor swarmforge-client swarmforge-compute swarmforge-benchmarks) do (
    if exist "%ROOT_DIR%\%%m\javadoc" rd /s /q "%ROOT_DIR%\%%m\javadoc"
)

if exist "%TEMP_AGG%\index.html" (
    xcopy /s /e /q /y "%TEMP_AGG%\*" "%FINAL_DIR%\" >nul
)

for %%m in (swarmforge-core swarmforge-server swarmforge-editor swarmforge-client swarmforge-compute swarmforge-benchmarks) do (
    if exist "%TEMP_MODS%\%%m\index.html" (
        md "%FINAL_DIR%\%%m" 2>nul
        xcopy /s /e /q /y "%TEMP_MODS%\%%m\*" "%FINAL_DIR%\%%m\" >nul
    )
)

if exist "%TEMP_MODS%" rd /s /q "%TEMP_MODS%"
if exist "%TEMP_AGG%" rd /s /q "%TEMP_AGG%"

echo ==========================================
echo  Javadoc successfully generated!
echo  Aggregate: %FINAL_DIR%\index.html
echo ==========================================
