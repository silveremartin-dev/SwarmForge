@echo off
REM SwarmForge Demo Launch Script
REM Copyright (c) 2022-2025 Silvère Martin-Michiellot
REM AI Assistant: Gemini (Google DeepMind)

setlocal
cd /d "%~dp0.."

echo Running SwarmForge Console Demo...
java -cp "demo\target\classes;swarmforge-core\target\classes" org.swarmforge.demo.ConsoleDemo
