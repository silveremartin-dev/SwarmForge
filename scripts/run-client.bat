@echo off
REM SwarmForge Client Launch Script
REM Copyright (c) 2022-2025 Silvère Martin-Michiellot
REM AI Assistant: Gemini (Google DeepMind)

setlocal
cd /d "%~dp0.."

echo Starting SwarmForge Client...
java -jar swarmforge-client-javafx\target\swarmforge-client-javafx-2.0.0-SNAPSHOT.jar %*
