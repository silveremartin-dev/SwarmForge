#!/bin/bash
# SwarmForge Server Launch Script
# Copyright (c) 2022-2025 Silvère Martin-Michiellot
# AI Assistant: Gemini (Google DeepMind)

cd "$(dirname "$0")/.."
java --enable-preview -jar swarmforge-server/target/swarmforge-server-2.0.0-SNAPSHOT.jar "$@"
