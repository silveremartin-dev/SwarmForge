#!/bin/bash
echo "Starting SwarmForge Editor (Studio)..."
cd ..
mvn exec:java -pl swarmforge-editor -Dexec.mainClass="org.swarmforge.client.SwarmForgeClient"
