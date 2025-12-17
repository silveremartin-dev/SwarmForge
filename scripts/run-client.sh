#!/bin/bash
# SwarmForge Editor Fast Launcher (Linux/Mac)

echo "========================================"
echo "  SwarmForge - Editor Launcher"
echo "========================================"

cd "$(dirname "$0")/.."

echo "[1/2] Ensuring Infrastructure is UP..."
docker-compose up -d postgres redis 2>/dev/null || echo "WARNING: Docker might not be running"

# Check if JAR exists
if [ ! -f "swarmforge-editor/target/swarmforge-editor-2.0.0-SNAPSHOT.jar" ]; then
    echo "JAR not found. Building first..."
    mvn install -DskipTests -pl swarmforge-editor -am -q
fi

echo ""
echo "[2/2] Launching Editor..."
cd swarmforge-editor/target
java -jar swarmforge-editor-2.0.0-SNAPSHOT.jar
