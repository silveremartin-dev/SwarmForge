#!/bin/bash
echo "Starting SwarmForge Compute Node..."
cd ..
mvn exec:java -pl swarmforge-compute -Dexec.mainClass="org.swarmforge.compute.ComputeNodeApp" -Dexec.args="--host localhost --port 50051"
