#!/bin/bash
# SwarmForge Demo Launch Script
# Copyright (c) 2022-2025 Silvère Martin-Michiellot
# AI Assistant: Gemini (Google DeepMind)

cd "$(dirname "$0")/.."

echo "Running SwarmForge Console Demo..."
java -cp "demo/target/classes:swarmforge-core/target/classes" org.swarmforge.demo.ConsoleDemo
