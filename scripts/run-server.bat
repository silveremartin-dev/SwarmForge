@echo off
REM SwarmForge Server Launch Script
REM Copyright (c) 2022-2025 Silvère Martin-Michiellot
REM AI Assistant: Gemini (Google DeepMind)

setlocal
cd /d "%~dp0.."

echo Starting SwarmForge Server...
java -jar swarmforge-server\target\swarmforge-server-2.0.0-SNAPSHOT.jar %*
