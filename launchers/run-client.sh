#!/bin/bash
echo "Starting SwarmForge Client (Viewer)..."
cd ..
mvn exec:java -pl swarmforge-client -Dexec.mainClass="org.swarmforge.client.ClientApp"
