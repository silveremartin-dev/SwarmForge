@echo off
REM ==============================================================================
REM SwarmForge Release & Standalone Packaging Entry Script (Windows Batch)
REM ==============================================================================

cd /d "%~dp0"
call "%~dp0deploy-release-all.bat" %*
