@echo off
rem SwarmForge - Javadoc Generation Script (Windows Batch)
rem Generates aggregate and per-module Javadoc directly in /javadoc (without apidocs sub-folder)

set SCRIPT_DIR=%~dp0
set ROOT_DIR=%SCRIPT_DIR%..
cd /d "%ROOT_DIR%"

echo ==========================================
echo  Building SwarmForge Javadoc Documentation
echo ==========================================

powershell -ExecutionPolicy Bypass -File "%SCRIPT_DIR%generate-javadoc.ps1"
