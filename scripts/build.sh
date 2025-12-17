#!/bin/bash

echo "=========================================="
echo "      SwarmForge Build Script (Linux/Mac)"
echo "=========================================="

echo "Building project with Maven..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "Build Failed!"
    exit 1
fi

echo ""
echo "Build Successful!"
