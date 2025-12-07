#!/bin/bash
# SwarmForge Client Launch Script
# Copyright (c) 2022-2025 Silvère Martin-Michiellot
# AI Assistant: Gemini (Google DeepMind)

cd "$(dirname "$0")/.."
java --enable-preview -jar swarmforge-client-javafx/target/swarmforge-client-javafx-2.0.0-SNAPSHOT.jar "$@"
