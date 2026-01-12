#!/bin/bash
echo "Running SwarmForge Benchmarks..."
cd ..
echo "Building Benchmarks Jar..."
mvn clean package -pl swarmforge-benchmarks -DskipTests
echo "Running Benchmarks..."
java -jar swarmforge-benchmarks/target/swarmforge-benchmarks-2.0.0-SNAPSHOT.jar
