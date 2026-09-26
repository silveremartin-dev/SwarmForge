#!/usr/bin/env bash
set -euo pipefail

cd ~/swarmforge
sed -i '/<module>swarmforge-editor<\/module>/d' pom.xml
sed -i '/<module>swarmforge-client<\/module>/d' pom.xml

echo "=== Packaging SwarmForge Benchmarks Shaded Fat-JAR ==="
mvn package -pl swarmforge-benchmarks -am -DskipTests --no-transfer-progress

echo "=== Executing SwarmForge Performance Benchmark Suite ==="
JAR_FILE=$(ls target/swarmforge-benchmarks-*.jar swarmforge-benchmarks/target/swarmforge-benchmarks-*-shaded.jar 2>/dev/null | grep -E 'shaded\.jar$' | head -n1 || ls swarmforge-benchmarks/target/swarmforge-benchmarks-*.jar | head -n1)
java -Xms2g -Xmx6g -jar "$JAR_FILE"
